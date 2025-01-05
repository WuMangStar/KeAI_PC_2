package aei.lang.keai.Function.Group;

import aei.lang.keai.Function.Api.ChatAIAPI;
import aei.lang.keai.Function.Api.MCRank;
import aei.lang.keai.Function.Api.MinecraftServerPing;
import aei.lang.keai.Function.FunctionI;
import aei.lang.keai.Utils.GroupMsgUtils;
import aei.lang.msg.Messenger;
import aei.lang.plugin.SecPlugin;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

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
            case "我的世界":
                send("ping  获取在线人数\n" +
                        "rank  玩家在线榜单\n" +
                        "save  保存在线时间");
                return;
            case "ping":
                send(mcr.ping());
                return;

            case "rank":
                send(mcr.getRanking());
                return;

            case "save":
                mcr.save();
                send("手动记录完成");
                return;
        }
        if (textmsg.matches("ping ([a-zA-Z0-9]+\\.)+[a-zA-Z0-9]{1,5}")) {
            String text = textmsg.substring(4).trim();
            if (!text.isEmpty()){
                MinecraftServerPing ping = new MinecraftServerPing(text, 25565);
                String status = ping.getServerStatus();
                JSONObject statusJson = JSON.parseObject(status.substring(status.indexOf("{")));
                System.out.println(statusJson);
                JSONObject players = statusJson.getJSONObject("players");
                int onlineMax = players.getIntValue("max");
                int online = players.getIntValue("online");
                JSONArray sampleNew = players.getJSONArray("sample");
                StringBuilder outMsg = new StringBuilder();
                outMsg.append(online).append("/").append(onlineMax);
                if (online>0) {
                    sampleNew.forEach(sample -> {
                        JSONObject playerL = (JSONObject) sample;
                        outMsg.append("\n").append(playerL.getString("name"));
                    });
                }
                send(outMsg.toString());
            }

        }
        if (textmsg.matches("添加.*~.*")){
            if (!uin.equals("2915372048"))return;
            String text = textmsg.substring(2);
            String playName=text.substring(0,text.indexOf("~"));
            long date=Long.parseLong(text.substring(text.indexOf("~")+1));
            mcr.addTime(playName,date);
            send("Player："+playName+"  +"+MCRank.msToTime(date));
        }
    }
}
