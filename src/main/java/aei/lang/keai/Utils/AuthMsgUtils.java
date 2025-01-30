package aei.lang.keai.Utils;


import aei.lang.msg.Messenger;
import aei.lang.msg.Msg;
import aei.lang.plugin.SecPlugin;

import java.util.List;

public class AuthMsgUtils {

    private SecPlugin api;

    protected String textmsg;
    protected String groupid;
    protected String uinName;
    protected String atName;
    protected String botUin;
    protected String uin;
    protected String msgid;

    protected List<String> imgList;

    protected void QQBotInit(SecPlugin api, Messenger messenger) {
        this.api = api;

        textmsg =messenger.getString(Msg.Text);
        groupid = messenger.getString(Msg.GroupId);
        uinName = messenger.getString(Msg.UinName);
        atName = messenger.getString(Msg.AtName);
        botUin = messenger.getString(Msg.Account);
        uin = messenger.getString(Msg.Uin);
        msgid = messenger.getString(Msg.MsgId);

        imgList = messenger.getList(Msg.Url);
    }

    protected void send(String text) {
      api.sendMessenger(msg -> {
            msg.addMsg(Msg.Account, botUin);
            msg.addMsg(Msg.Group);
            msg.addMsg(Msg.GroupId, groupid);
            msg.addMsg(Msg.MsgId, msgid);
            msg.addMsg(Msg.Text, text);
        });
    }
    public void sendImg( String url) {
        api.sendMessenger(msg -> {
            msg.addMsg(Msg.Account, botUin);
            msg.addMsg(Msg.Group);
            msg.addMsg(Msg.GroupId, groupid);
            msg.addMsg(Msg.MsgId, msgid);
            msg.addMsg(Msg.Img, url);
        });
    }

}
