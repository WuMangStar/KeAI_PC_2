package aei.lang.keai.Utils;

import aei.lang.msg.Messenger;
import aei.lang.msg.Msg;
import aei.lang.plugin.SecPlugin;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.List;

public class GroupMsgUtils {

    private SecPlugin api;

    protected String textmsg;
    protected String groupid;
    protected String uinName;
    protected String atName;
    protected String botUin;
    protected String uin;
    protected String msgid;

    protected List<String> imgList;
    protected List<String> atUinList;
    protected List<String> atNameList;
    protected List<String> textList;

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
        atUinList = messenger.getList(Msg.AtUin);
        atNameList = messenger.getList(Msg.AtName);
        textList=messenger.getList(Msg.Text);
    }

    protected void send(String text) {
       Messenger msgGroup=api.sendMessenger(msg -> {
            msg.addMsg(Msg.Account, botUin);
            msg.addMsg(Msg.Group);
            msg.addMsg(Msg.GroupId, groupid);
            msg.addMsg(Msg.Reply, msgid);
            msg.addMsg(Msg.Text, text);
            if (text.length() > 500) {
                msg.addMsg(Msg.MultiMsgPut);
                msg.addMsg(Msg.Uin, uin);
                msg.addMsg(Msg.UinName, uinName);
            }
        });
        if (text.length() > 500) {
            final Messenger rsp = api.sendMessenger(rsp1 -> {
                rsp1.addMsg(Msg.Account, botUin);
                rsp1.addMsg(Msg.Group);//声明 聊天记录 来源是 群聊
                rsp1.addMsg(Msg.GroupId, groupid);
                rsp1.addMsg(Msg.Time, System.currentTimeMillis() / 1000);//时间戳秒
                rsp1.addMsg(Msg.MultiMsg, msgGroup.getString(Msg.MsgId));//添加消息元素
            });//构建 聊天记录
            api.sendMessenger(msg -> {
                msg.addMsg(Msg.Account, botUin);
                msg.addMsg(Msg.Group);
                msg.addMsg(Msg.GroupId, groupid);
                if (rsp.hasMsg(Msg.Id)) {
                    msg.addMsg(Msg.Xml, "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>");
                    msg.addMsg(Msg.Xml, "<msg serviceID=\"35\" templateID=\"1\" action=\"viewMultiMsg\" brief=\"KeAI 折叠消息\" m_resid=\"", rsp.getString(Msg.Id), "\" m_fileName=\"", rsp.getString(Msg.Name), "\" tSum=\"2\" sourceMsgId=\"0\" url=\"\" flag=\"3\" adverSign=\"0\" multiMsgFlag=\"0\">");
                    msg.addMsg(Msg.Xml, "<item layout=\"1\" advertiser_id=\"0\" aid=\"0\">");
                    msg.addMsg(Msg.Xml, "<title size=\"34\" maxLines=\"2\" lineSpace=\"12\">@" + uinName + " 请点进查看</title>");
                    msg.addMsg(Msg.Xml, "<title size=\"26\" color=\"#FF6151\" maxLines=\"2\" lineSpace=\"12\">"+text.split("\n")[0]+"</title>");
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

    protected boolean coverAt(String atUin) {
        for (String at : atUinList) {
            if (at.equals(atUin)) return true;
        }
        return false;
    }
    protected String textNoAt() {
        StringBuilder text = new StringBuilder();
        for (String txt : textList) {
            boolean flag = true;
            for (String atN : atNameList) {
                if (txt.equals("@"+atN)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                text.append(txt);
            }
        }
        return text.toString();
    }


    public void sendImg( String url) {
        api.sendMessenger(msg -> {
            msg.addMsg(Msg.Account, botUin);
            msg.addMsg(Msg.Group);
            msg.addMsg(Msg.GroupId, groupid);
            msg.addMsg(Msg.Reply, msgid);
            msg.addMsg(Msg.Img, url);
        });
    }

    public void sendMD(JSONArray jsonArr) {
        String str = HttpUtil.doPostJson("https://api.s01s.cn/API/lt_zf/", "{\"zf\":" + jsonArr.toString() + ",\"ms\":\"你好啊\",\"yh\":[\"" + uinName + "\",\"" + uin + "\"],\"type\":\"md\"}");
        JSONObject mdjson = JSON.parseObject(str);
        String rsp = mdjson.getJSONObject("meta").getJSONObject("detail").getString("resid");
        api.sendMessenger(msg -> {
            msg.addMsg(Msg.Account, botUin);
            msg.addMsg(Msg.Group);
            msg.addMsg(Msg.GroupId, groupid);
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
