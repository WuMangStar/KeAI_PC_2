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
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;

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
        final String keyId = Sp.containsKey(uin) ? uin : groupid;
        switch (textmsg) {
            case "记忆":
                ContextAI mess = new ContextAI(conn, msgid, keyId);
                mess.delContext();
                send((uin.equals(keyId) ? "个人私有" : "本群公有") + "聊天记录已经清理完成");
                return;
            case "聊天模型":
                send("使用.开头或@开始聊天\n\n" +
                        "设置  查询当前模型设置\n" +
                        "模型  聊天模型进行切换\n" +
                        "记忆  清除模型的上下文\n" +
                        "私有  切换上下文为独占\n" +
                        "公有  切换上下文群公共\n" +
                        "提示[提示词] 更改模型提示词");
                return;
            case "设置":
                SettingAI sett = new SettingAI(conn, keyId);
                send((uin.equals(keyId) ? "个人私有" : "本群公有") + "设置" +
                        "\n智能体:" + sett.getModel() +
                        "\n提示词:" + sett.getTips()
                );
                return;
            case "模型":
                Sp.put(keyId, CHAT_MOD_FLAG);
                StringBuilder msg1 = new StringBuilder();
                msg1.append("智能体模型列表:").append("\n");
                for (int i = 0; i < CHAT_MOD_EN.length; i++) {
                    msg1.append(i + 1).append(". ").append(CHAT_MOD_EN[i]).append("\n");
                }
                msg1.append("\n").append("输入序号选择");
                send(msg1.toString());
                return;
            case "私有":
                if (Sp.containsKey(uin)) {
                    send("当前聊天模式为私有，无需切换");
                } else {
                    Sp.put(uin, 1);
                    send("当前模式以切换为私有模式");
                }
                return;
            case "公有":
                if (!Sp.containsKey(uin)) {
                    send("当前聊天模式为公有，无需切换");
                } else {
                    Sp.remove(uin);
                    send("当前模式以切换为公有模式");
                }
                return;
        }
        if (textmsg.startsWith("提示")) {
            String text = textmsg.substring(2);
            SettingAI sett = new SettingAI(conn, keyId);
            if (textmsg.trim().equals("提示")) {
                sett.setDefaultTips();
                send("AI提示词已恢复默认");
            } else {
                sett.setTips(text);
                send("AI提示词修改成功");
            }
            return;
        }
        if (coverAt(botUin) || textNoAt().trim().startsWith(".")) {
            String trimText = textNoAt().trim();
            if (trimText.equals(".") || trimText.equals("...")) return;
            String text = "";
            if (messenger.hasMsg(Msg.Reply)) {
                Messenger CacheMsg = api.sendMessenger(msg -> {
                    msg.addMsg(Msg.Account, botUin);
                    msg.addMsg(Msg.Group);
                    msg.addMsg(Msg.GroupId, groupid);
                    msg.addMsg(Msg.GroupMsgCacheGet, messenger.getString(Msg.Reply));
                });
                text += CacheMsg.hasMsg(Msg.Text) ? CacheMsg.getString(Msg.Text) + "\n\n" : "";
                imgList.addAll(CacheMsg.getList(Msg.Url));
            }
            text += coverAt(botUin) ? trimText : trimText.substring(1);
            System.out.println("User：" + text);
            ChatAIAPI ai = new ChatAIAPI();
            String aimsg = ai.RequestAI(conn, keyId, msgid, text, imgList);
            String[] lines = aimsg.split("\n");

            Messenger msgGroup = api.sendMessenger(msg -> {
                msg.addMsg(Msg.Account, botUin);
                msg.addMsg(Msg.Group);
                msg.addMsg(Msg.GroupId, groupid);
                msg.addMsg(Msg.Reply, msgid);
                StringBuilder BuffStr = new StringBuilder();
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    if (line.matches(".*\\(https://spc\\.unk/.+\\).*")) {
                        msg.addMsg(Msg.Text, BuffStr);
                        BuffStr.delete(0, BuffStr.length());
                        String cutstr = line.substring(line.indexOf("https://spc.unk/"), line.indexOf(")"));
                        JSONObject jsonimg = JSON.parseObject(HttpUtil.doGet("https://api.chaton.ai/storage/" + cutstr.replaceFirst("https://spc.unk/", "")));
                        msg.addMsg(Msg.Text, line.substring(0, line.indexOf("![")));
                        msg.addMsg(Msg.Img, jsonimg.getString("getUrl"));
                        msg.addMsg(Msg.Text, line.substring(line.indexOf(")") + 1));
                        continue;
                    }
                    BuffStr.append(line);
                    if (i != lines.length - 1) {
                        BuffStr.append("\n");
                    }
                }
                msg.addMsg(Msg.Text, BuffStr);
                if (aimsg.length() > 350) {
                    msg.addMsg(Msg.MultiMsgPut);
                    msg.addMsg(Msg.Uin, uin);
                    msg.addMsg(Msg.UinName, uinName);
                }
            });

            if (aimsg.length() > 350) {
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
                        msg.addMsg(Msg.Text, aimsg);
                    }
                });
            }
            return;
        }
        if (textmsg.startsWith("。")) {
            String trimText = textmsg.trim();
            if (trimText.equals("。") || trimText.equals("。。。")) return;
            String text = textmsg.substring(1);
            System.out.println("User：" + text);
            ChatAIAPI ai = new ChatAIAPI();
            String aimsg = ai.RequestAI(conn, keyId, msgid, text, imgList);
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
            int i = Integer.parseInt(textmsg);
            if (i==0) return;
            if (!Sp.containsKey(keyId)) return;
            SettingAI sett = new SettingAI(conn, keyId);
            switch (Sp.get(keyId)) {
                case CHAT_MOD_FLAG:
                    if (i > CHAT_MOD.length) return;
                    Sp.remove(keyId);
                    String model = CHAT_MOD[i - 1];
                    sett.setModel(model);
                    send("智能体模型已切换为：" + model);
                    return;
            }
        }

    }


}
