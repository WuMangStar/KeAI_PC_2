package aei.lang.keai.Function.Group;

import aei.lang.keai.Function.Api.ChatAIAPI;
import aei.lang.keai.Function.FunctionI;
import aei.lang.keai.MySQL.ContextAI;
import aei.lang.keai.MySQL.SettingAI;
import aei.lang.keai.Utils.HttpUtil;
import aei.lang.keai.Utils.GroupMsgUtils;
import aei.lang.msg.Messenger;
import aei.lang.msg.Msg;
import aei.lang.plugin.SecPlugin;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import kotlin.text.Charsets;
import okhttp3.*;
import okhttp3.internal.sse.RealEventSource;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static aei.lang.keai.StartBot.Sp;

public class ChatAI extends GroupMsgUtils implements FunctionI {

    @Override
    public String getName() {
        return "AI聊天";
    }



    @Override
    public void init(SecPlugin api, Messenger messenger, Connection conn) throws Exception {
        QQBotInit(api, messenger);
        final int CHAT_MOD_FLAG = 1;
        final String[] CHAT_MOD = {"gpt-4o", "gpt-4o-mini", "claude", "claude-3-5-sonnet"};
        final String[] CHAT_MOD_EN = {"ChatGPT 4o", "ChatGPT 4o mini", "Claude 3 haiku", "Claude 3.5 sonnet"};
        final int ART_MOD_FLAG = 2;
        final String[] ART_MOD = {"photographic", "anime", "digital-art", "comic-book", "fantasy-art", "analog-film", "neon-punk", "isometric", "low-poly", "origami", "line-art", "craft-clay", "cinematic", "3d-model", "pixel-art"};
        final String[] ART_MOD_ZH = {"摄影", "动漫", "数码", "漫画", "奇幻", "胶片", "霓虹", "等轴", "低多边形", "折纸", "素描", "粘土", "电影", "模型", "像素"};
        final int ART_SIZE_FLAG = 3;
        final String[] ART_SIZE = {"1:1", "9:16", "16:9", "4:3"};

        if (uin.equals("2168044167")) return;
        switch (textmsg) {
            case "记忆":
                ContextAI mess = new ContextAI(conn, msgid, groupid);
                mess.delContext();
                send("本群的聊天记录已经清理完成");
                return;
            case "设置":

                SettingAI sett = new SettingAI(conn, groupid);
                send("智能体:" + sett.getModel() +
                        "\n绘画风格:" + sett.getArt() +
                        "\n绘图尺寸:" + sett.getSize() +
                        "\n提示词:" + sett.getTips()
                );
                return;
            case "模型":
                Sp.put("Setting", CHAT_MOD_FLAG);
                StringBuilder msg1 = new StringBuilder();
                msg1.append("智能体模型列表:").append("\n");
                for (int i = 0; i < CHAT_MOD_EN.length; i++) {
                    msg1.append(i + 1).append(". ").append(CHAT_MOD_EN[i]).append("\n");
                }
                msg1.append("\n").append("输入序号选择");
                send(msg1.toString());
                return;
            case "风格":
                Sp.put("Setting", ART_MOD_FLAG);
                StringBuilder msg2 = new StringBuilder();
                msg2.append("绘画风格列表:").append("\n");
                for (int i = 0; i < ART_MOD_ZH.length; i++) {
                    msg2.append(i + 1).append(". ").append(ART_MOD_ZH[i]).append("\n");
                }
                msg2.append("\n").append("输入序号选择");
                send(msg2.toString());
                return;
            case "尺寸":
                Sp.put("Setting", ART_SIZE_FLAG);
                StringBuilder msg3 = new StringBuilder();
                msg3.append("绘图格式列表:").append("\n");
                for (int i = 0; i < ART_SIZE.length; i++) {
                    msg3.append(i + 1).append(". ").append(ART_SIZE[i]).append("\n");
                }
                msg3.append("\n").append("输入序号选择");
                send(msg3.toString());
                return;
        }
        if (textmsg.startsWith("提示")) {
            String text = textmsg.substring(2);
            SettingAI sett = new SettingAI(conn, groupid);
            if (textmsg.trim().equals("提示")) {
                sett.setDefaultTips();
                send("AI提示词已恢复默认");
            } else {
                sett.setTips(text);
                send("AI提示词修改成功");
            }
            return;
        }
        if (textmsg.startsWith(".") || coverAt(botUin)) {
            String text;
            if (coverAt(botUin)) {
                text = textmsg.replaceAll("@" + atName, "").trim();
            } else {
                text = textmsg.substring(1);
            }
            System.out.println("User：" + text);
            ChatAIAPI ai=new ChatAIAPI();
            String aimsg = ai.RequestAI(conn, groupid,msgid, text,imgList);
            System.out.println("AI：" + aimsg);
            String[] lines = aimsg.split("\n");

            Messenger msgGroup = api.sendMessenger(msg -> {
                msg.addMsg(Msg.Account, botUin);
                msg.addMsg(Msg.Group);
                msg.addMsg(Msg.GroupId, groupid);
                msg.addMsg(Msg.Reply, msgid);
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    if (line.matches(".*\\(https://spc\\.unk/.+\\).*")) {
                        String cutstr = line.substring(line.indexOf("https://spc.unk/"), line.indexOf(")"));
                        JSONObject jsonimg = JSON.parseObject(HttpUtil.doGet("https://api.chaton.ai/storage/" + cutstr.replaceFirst("https://spc.unk/", "")));
                        msg.addMsg(Msg.Text, line.substring(0, line.indexOf("![")));
                        msg.addMsg(Msg.Img, jsonimg.getString("getUrl"));
                        msg.addMsg(Msg.Text, line.substring(line.indexOf(")") + 1));
                        continue;
                    }
                    msg.addMsg(Msg.Text, line);
                    if (i != lines.length - 1) {
                        msg.addMsg(Msg.Text, "\n");
                    }
                }
                if (aimsg.length() > 250) {
                    msg.addMsg(Msg.MultiMsgPut);
                    msg.addMsg(Msg.Uin, uin);
                    msg.addMsg(Msg.UinName, uinName);
                }
            });

            if (aimsg.length() > 250) {
                final Messenger rsp = api.sendMessenger(rsp1 -> {
                    rsp1.addMsg(Msg.Account, botUin);
                    rsp1.addMsg(Msg.Group);//声明 聊天记录 来源是 群聊
                    rsp1.addMsg(Msg.GroupId, groupid);
                    rsp1.addMsg(Msg.Time, System.currentTimeMillis() / 1000);//时间戳秒
                    rsp1.addMsg(Msg.MultiMsg, msgGroup.getString(Msg.MsgId));//添加消息元素
                });//构建 聊天记录
                api.sendMessenger(msg -> {
                    msg.addMsg(Msg.Account, botUin);
                    msg.addMsg(Msg.Group);
                    msg.addMsg(Msg.GroupId, groupid);
                    if (rsp.hasMsg(Msg.Id)) {
                        msg.addMsg(Msg.Xml, "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>");
                        msg.addMsg(Msg.Xml, "<msg serviceID=\"35\" templateID=\"1\" action=\"viewMultiMsg\" brief=\"KeAI 折叠消息\" m_resid=\"", rsp.getString(Msg.Id), "\" m_fileName=\"", rsp.getString(Msg.Name), "\" tSum=\"2\" sourceMsgId=\"0\" url=\"\" flag=\"3\" adverSign=\"0\" multiMsgFlag=\"0\">");
                        msg.addMsg(Msg.Xml, "<item layout=\"1\" advertiser_id=\"0\" aid=\"0\">");
                        msg.addMsg(Msg.Xml, "<title size=\"34\" maxLines=\"2\" lineSpace=\"12\">@" + uinName + " 请点进查看</title>");
                        msg.addMsg(Msg.Xml, "<title size=\"26\" color=\"#FF6151\" maxLines=\"2\" lineSpace=\"12\">消息长度:" + aimsg.length() + "</title>");
                        msg.addMsg(Msg.Xml, "<hr hidden=\"false\" style=\"0\" />");
                        msg.addMsg(Msg.Xml, "<summary size=\"26\" color=\"#1F9389\">KeAI 折叠消息</summary>");
                        msg.addMsg(Msg.Xml, "</item>");
                        msg.addMsg(Msg.Xml, "<source name=\"聊天记录\" icon=\"\" action=\"\" appid=\"-1\" />");
                        msg.addMsg(Msg.Xml, "</msg>");
                    } else {
                        msg.addMsg(Msg.Text, "合成失败");
                    }
                });
            }
            return;
        }
        if (textmsg.startsWith("。")) {
            String text = textmsg.substring(1);
            System.out.println("User：" + text);
            ChatAIAPI ai=new ChatAIAPI();
            String aimsg = ai.RequestAI(conn, groupid,msgid, text,imgList);
            System.out.println("AI：" + aimsg);
            String[] lines = aimsg.split("\n");
            StringBuilder texts = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                StringBuilder line = new StringBuilder(lines[i]);
                if (line.toString().matches(".*\\(https://spc\\.unk/.+\\).*")) {
                    String cutstr = line.substring(line.indexOf("https://spc.unk/"), line.indexOf(")"));
                    String imgurl = HttpUtil.doGet("https://api.chaton.ai/storage/" + cutstr.replaceFirst("https://spc.unk/", ""));
                    JSONObject jsonimg = JSON.parseObject(imgurl);
                    texts.append(line, 0, line.indexOf("](") - 1).append(" #1080px #1080px]");
                    texts.append("(").append(jsonimg.getString("getUrl")).append(")");
                    texts.append(line.substring(line.indexOf(")") + 1));
                } else {
                    texts.append(line);
                }
                if (i != lines.length - 1) {
                    texts.append("\n");
                }
            }
            JSONArray mdJson = new JSONArray();
            mdJson.add(texts);
            sendMD(mdJson);
            return;
        }
        if (textmsg.matches("\\d{1,2}")) {
            if (!Sp.containsKey("Setting")) return;
            switch (Sp.get("Setting")) {
                case CHAT_MOD_FLAG:
                    int i = Integer.parseInt(textmsg);
                    if (i > CHAT_MOD.length) return;
                    Sp.remove("Setting");
                    String model = CHAT_MOD[i - 1];
                    try {
                        SettingAI sett = new SettingAI(conn, groupid);
                        sett.setModel(model);
                        send("智能体模型已切换为：" + model);
                    } catch (
                            SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                case ART_MOD_FLAG:
                    int i2 = Integer.parseInt(textmsg);
                    if (i2 > ART_MOD.length) return;
                    Sp.remove("Setting");

                    String artStyle = ART_MOD[i2 - 1];
                    try {
                        SettingAI sett = new SettingAI(conn, groupid);
                        sett.setArt(artStyle);

                        send("绘画风格已切换为：" + ART_MOD_ZH[i2 - 1]);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                case ART_SIZE_FLAG:
                    int i3 = Integer.parseInt(textmsg);
                    if (i3 > ART_SIZE.length) return;
                    System.out.println(textmsg);
                    Sp.remove("Setting");

                    String artSize = ART_SIZE[i3 - 1];
                    try {
                        SettingAI sett = new SettingAI(conn, groupid);
                        sett.setSize(artSize);

                        send("绘画尺寸已切换为：" + artSize);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return;
            }
        }

    }



}
