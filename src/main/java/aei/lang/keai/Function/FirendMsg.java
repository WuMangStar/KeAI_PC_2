package aei.lang.keai.Function;

import aei.lang.keai.Function.Firend.BaseReply;
import aei.lang.keai.Function.Firend.ChatAI;
import aei.lang.msg.Messenger;
import aei.lang.msg.Msg;
import aei.lang.plugin.SecPlugin;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class FirendMsg {
    public FirendMsg(SecPlugin api, Messenger messenger, Connection conn) {
        List<FunctionI> init = new ArrayList<>();
        init.add(new ChatAI());
        init.add(new BaseReply());

        for (FunctionI f : init) {
            try {
                f.init(api, messenger,conn);
            } catch (Exception e) {
                api.sendMessenger(msg -> {
                    msg.addMsg(Msg.Account, messenger.getString(Msg.Account));
                    msg.addMsg(Msg.Friend);
                    msg.addMsg(Msg.Uin,messenger.getString(Msg.Uin));
                  //  msg.addMsg(Msg.Reply, messenger.getString(Msg.MsgId));
                    msg.addMsg(Msg.Text, f.getName() + "错误：" + e.getMessage());
                });
                api.printE(f.getName() + " 错误：" + e.getMessage());
            }

        }
    }
}
