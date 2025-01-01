package aei.lang.keai.Function;

import aei.lang.keai.Function.Group.ArtAI;
import aei.lang.keai.Function.Group.BaseReply;
import aei.lang.keai.Function.Group.ChatAI;
import aei.lang.keai.Function.Group.MineCraft;
import aei.lang.msg.Messenger;
import aei.lang.msg.Msg;
import aei.lang.plugin.SecPlugin;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;


public class GroupMsg {
    public GroupMsg(SecPlugin api, Messenger messenger, Connection conn) {
        List<FunctionI> init = new ArrayList<>();
        init.add(new BaseReply());
        init.add(new ChatAI());
        init.add(new ArtAI());
        init.add(new MineCraft());
        for (FunctionI f : init) {
            try {
                f.init(api, messenger, conn);
            } catch (Exception e) {
                api.sendMessenger(msg -> {
                    msg.addMsg(Msg.Account, messenger.getString(Msg.Account));
                    msg.addMsg(Msg.Group);
                    msg.addMsg(Msg.GroupId, messenger.getString(Msg.GroupId));
                    msg.addMsg(Msg.Reply, messenger.getString(Msg.MsgId));
                    msg.addMsg(Msg.Text, "错误：" + e.getMessage());
                });
                api.printE(f.getName() + " 错误：" + e.getMessage());
            }

        }
    }
}
