package aei.lang.keai.Function.Group;

import aei.lang.keai.Function.Api.MCRank;
import aei.lang.keai.Function.FunctionI;
import aei.lang.keai.Utils.GroupMsgUtils;
import aei.lang.msg.Messenger;
import aei.lang.plugin.SecPlugin;

import java.sql.Connection;

import static aei.lang.keai.StartBot.mcr;

public class MineCraft extends GroupMsgUtils implements FunctionI {
    @Override
    public String getName() {
        return "我的世界相关功能";
    }

    @Override
    public void init(SecPlugin api, Messenger messenger, Connection conn) throws Exception {
        QQBotInit(api, messenger);
        switch (textmsg) {
            case "ping":
                send(mcr.ping());
                return;

            case "rank":
                send(mcr.getRanking());
                return;
        }
    }
}
