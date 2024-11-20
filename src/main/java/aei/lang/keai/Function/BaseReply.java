package aei.lang.keai.Function;

import aei.lang.keai.FunctionI;
import aei.lang.keai.Utils.QQBot;
import aei.lang.msg.Messenger;
import aei.lang.plugin.SecPlugin;

import java.sql.Connection;

public class BaseReply extends QQBot implements FunctionI {

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
