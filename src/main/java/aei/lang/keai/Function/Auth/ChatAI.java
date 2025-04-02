package aei.lang.keai.Function.Auth;

import aei.lang.keai.Function.Api.ChatAIAPI;
import aei.lang.keai.Function.AuthMsg;
import aei.lang.keai.Function.FunctionI;
import aei.lang.keai.MySQL.ContextAI;
import aei.lang.keai.MySQL.SettingAI;
import aei.lang.keai.Utils.AuthMsgUtils;
import aei.lang.keai.Utils.FirendMsgUtils;
import aei.lang.keai.Utils.HttpUtil;
import aei.lang.msg.Messenger;
import aei.lang.msg.Msg;
import aei.lang.plugin.SecPlugin;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.sql.Connection;

import static aei.lang.keai.StartBot.Sp;


public class ChatAI extends AuthMsgUtils implements FunctionI {
    @Override
    public String getName() {
        return "官机AI";
    }

    @Override
    public void init(SecPlugin api, Messenger messenger, Connection conn) throws Exception {
        QQBotInit(api, messenger);
        final int CHAT_MOD_FLAG = 1;
        final String[] CHAT_MOD = {"gpt-4o", "gpt-4o-mini", "claude", "claude-3-5-sonnet","deepseek-r1"};
        final String[] CHAT_MOD_EN = {"ChatGPT 4o", "ChatGPT 4o mini", "Claude 3 haiku", "Claude 3.5 sonnet","DeepSeek R1"};
        final String keyId = Sp.containsKey(uin) ? uin : groupid;
        switch (textmsg) {
            case "记忆":
                ContextAI mess = new ContextAI(conn, msgid,keyId);
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
        if (textmsg.matches("\\d{1,2}")) {
            int i = Integer.parseInt(textmsg);
            if (i!=0) {
                if (Sp.containsKey(keyId)) {
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
        System.out.println("User：" + textmsg);
        ChatAIAPI ai=new ChatAIAPI();
        String aimsg = ai.RequestAI(conn, groupid,msgid, textmsg,imgList).replace("成人","承仁");
        String[] lines = aimsg.split("\n");
        api.sendMessenger(msg -> {
            msg.addMsg(Msg.Account, botUin);
            msg.addMsg(Msg.Group);
            msg.addMsg(Msg.GroupId, groupid);
            msg.addMsg(Msg.MsgId, msgid);
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
        });
        return;
    }
}
