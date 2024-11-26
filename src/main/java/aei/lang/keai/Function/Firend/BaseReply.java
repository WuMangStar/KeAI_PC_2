package aei.lang.keai.Function.Firend;

import aei.lang.keai.Function.FunctionI;
import aei.lang.keai.Utils.FirendMsgUtils;
import aei.lang.keai.Utils.GroupMsgUtils;
import aei.lang.msg.Messenger;
import aei.lang.plugin.SecPlugin;

import java.sql.Connection;

public class BaseReply extends FirendMsgUtils implements FunctionI {

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
        }

    }
}
