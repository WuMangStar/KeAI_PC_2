package aei.lang.keai.Function.Group;

import aei.lang.keai.Function.FunctionI;
import aei.lang.keai.Utils.GroupMsgUtils;
import aei.lang.msg.Messenger;
import aei.lang.plugin.SecPlugin;

import java.sql.Connection;

public class BaseReply extends GroupMsgUtils implements FunctionI {

    @Override
    public String getName() {
        return "基础回复";
    }

    @Override
    public void init(SecPlugin api, Messenger messenger, Connection conn) {
        QQBotInit(api, messenger);
        switch (textmsg) {
            case "死了没":
                send("我在");
                break;
            case "菜单":
                send("聊天模型\n" +
                        "绘画模型");
                break;
        }
        if (textNoAt().trim().startsWith("测试")){
            send(textNoAt());
        }

    }
}
