package aei.lang.keai.Function.Firend;

import aei.lang.keai.Function.Api.ChatAIAPI;
import aei.lang.keai.Function.FunctionI;
import aei.lang.keai.Utils.FirendMsgUtils;
import aei.lang.keai.Utils.HttpUtil;
import aei.lang.msg.Messenger;
import aei.lang.msg.Msg;
import aei.lang.plugin.SecPlugin;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import kotlin.Function;

import java.sql.Connection;


public class ChatAI extends FirendMsgUtils implements FunctionI {
    @Override
    public String getName() {
        return "私聊AI";
    }

    @Override
    public void init(SecPlugin api, Messenger messenger, Connection conn) throws Exception {
        QQBotInit(api, messenger);
        System.out.println("User：" + textmsg);
        ChatAIAPI ai=new ChatAIAPI();
        String aimsg = ai.RequestAI(conn, uin,msgid, textmsg,imgList);
        System.out.println("AI：" + aimsg);
        String[] lines = aimsg.split("\n");
        api.sendMessenger(msg -> {
            msg.addMsg(Msg.Account, botUin);
            msg.addMsg(Msg.Friend);
            msg.addMsg(Msg.Uin);
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
        });
        return;
    }
}
