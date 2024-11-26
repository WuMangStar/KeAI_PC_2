package aei.lang.keai.Utils;

import aei.lang.msg.Messenger;
import aei.lang.msg.Msg;
import aei.lang.plugin.SecPlugin;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class FirendMsgUtils {

    private SecPlugin api;

    protected String textmsg;
    protected String uinName;
    protected String botUin;
    protected String uin;
    protected String msgid;

    protected List<String> imgList;

    protected void QQBotInit(SecPlugin api, Messenger messenger) {
        this.api = api;
        textmsg = messenger.getString(Msg.Text);
        uinName = messenger.getString(Msg.UinName);
        botUin = messenger.getString(Msg.Account);
        uin = messenger.getString(Msg.Uin);
        msgid = messenger.getString(Msg.MsgId);
        imgList = messenger.getList(Msg.Url);
    }

    protected void send(String text) {
        api.sendMessenger(msg -> {
            msg.addMsg(Msg.Account, botUin);
            msg.addMsg(Msg.Friend);
            msg.addMsg(Msg.Reply,msgid);
            msg.addMsg(Msg.Uin,uin);
            msg.addMsg(Msg.Text, text);
        });
    }
    public void sendImg( String url) {
        api.sendMessenger(msg -> {
            msg.addMsg(Msg.Account, botUin);
            msg.addMsg(Msg.Friend);
            msg.addMsg(Msg.Reply,msgid);
            msg.addMsg(Msg.Uin,uin);
            msg.addMsg(Msg.Img, url);
        });
    }

    public void sendMD(JSONArray jsonArr) {
        String str = HttpUtil.doPostJson("https://api.s01s.cn/API/lt_zf/", "{\"zf\":" + jsonArr.toString() + ",\"ms\":\"你好啊\",\"yh\":[\"" + uinName + "\",\"" + uin + "\"],\"type\":\"md\"}");
        JSONObject mdjson = JSON.parseObject(str);
        String rsp = mdjson.getJSONObject("meta").getJSONObject("detail").getString("resid");
        api.sendMessenger(msg -> {
            msg.addMsg(Msg.Account, botUin);
            msg.addMsg(Msg.Friend);
            msg.addMsg(Msg.Uin,uin);
            msg.addMsg(Msg.Reply,msgid);
            if (!rsp.isEmpty()) {
                msg.addMsg(Msg.Xml, "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>");
                msg.addMsg(Msg.Xml, "<msg serviceID=\"35\" templateID=\"1\" action=\"viewMultiMsg\" brief=\"KeAI 折叠消息\" m_resid=\"", rsp, "\" m_fileName=\"2910\" tSum=\"2\" sourceMsgId=\"0\" url=\"\" flag=\"3\" adverSign=\"0\" multiMsgFlag=\"0\">");
                msg.addMsg(Msg.Xml, "<item layout=\"1\" advertiser_id=\"0\" aid=\"0\">");
                msg.addMsg(Msg.Xml, "<title size=\"34\" maxLines=\"2\" lineSpace=\"12\">@" + uinName + " 请点进查看</title>");
                msg.addMsg(Msg.Xml, "<title size=\"26\" color=\"#FF6151\" maxLines=\"2\" lineSpace=\"12\">消息长度:" + jsonArr.getString(0).length() + "</title>");
                msg.addMsg(Msg.Xml, "<hr hidden=\"false\" style=\"0\" />");
                msg.addMsg(Msg.Xml, "<summary size=\"26\" color=\"#1F9389\">KeAI 折叠消息</summary>");
                msg.addMsg(Msg.Xml, "</item>");
                msg.addMsg(Msg.Xml, "<source name=\"聊天记录\" icon=\"\" action=\"\" appid=\"-1\" />");
                msg.addMsg(Msg.Xml, "</msg>");
            } else {
                msg.addMsg(Msg.Text, "合成失败");
            }
        });
    }

}
