package aei.lang.keai.Function;

import aei.lang.keai.Function.Auth.BaseReply;
import aei.lang.keai.Function.Auth.ChatAI;
import aei.lang.msg.Messenger;
import aei.lang.msg.Msg;
import aei.lang.plugin.SecPlugin;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class AuthMsg {
    public AuthMsg(SecPlugin api, Messenger messenger, Connection conn) {
        List<FunctionI> init = new ArrayList<>();
        init.add(new BaseReply());
        init.add(new ChatAI());

        for (FunctionI f : init) {
            try {
                f.init(api, messenger,conn);
            } catch (Exception e) {
                api.sendMessenger(msg -> {
                    msg.addMsg(Msg.Account, messenger.getString(Msg.Account));
                    msg.addMsg(Msg.Friend);
                    msg.addMsg(Msg.Uin,messenger.getString(Msg.Uin));
                    msg.addMsg(Msg.MsgId, messenger.getString(Msg.MsgId));
                    msg.addMsg(Msg.Text, f.getName() + "错误：" + e.getMessage());
                });
                api.printE(f.getName() + " 错误：" + e.getMessage());
            }

        }
    }
}
