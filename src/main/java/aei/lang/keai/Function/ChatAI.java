package aei.lang.keai.Function;

import aei.lang.keai.FunctionI;
import aei.lang.keai.MySQL.ContextAI;
import aei.lang.keai.MySQL.SettingAI;
import aei.lang.keai.Utils.HttpUtil;
import aei.lang.keai.Utils.QQBot;
import aei.lang.msg.Messenger;
import aei.lang.msg.Msg;
import aei.lang.plugin.SecPlugin;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import kotlin.text.Charsets;
import okhttp3.*;
import okhttp3.internal.sse.RealEventSource;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static aei.lang.keai.StartBot.Sp;

public class ChatAI extends QQBot implements FunctionI {

    @Override
    public String getName() {
        return "AI聊天";
    }

    private final String version = "ChatOn_Android/1.99.430";

    @Override
    public void init(SecPlugin api, Messenger messenger, Connection conn) throws SQLException, NoSuchAlgorithmException, InvalidKeyException {
        QQBotInit(api, messenger);
        final int CHAT_MOD_FLAG = 1;
        final String[] CHAT_MOD = {"gpt-4o", "gpt-4o-mini", "claude", "claude-3-5-sonnet"};
        final String[] CHAT_MOD_EN = {"ChatGPT 4o", "ChatGPT 4o mini", "Claude 3 haiku", "Claude 3.5 sonnet"};
        final int ART_MOD_FLAG = 2;
        final String[] ART_MOD = {"photographic", "anime", "digital-art", "comic-book", "fantasy-art", "analog-film", "neon-punk", "isometric", "low-poly", "origami", "line-art", "craft-clay", "cinematic", "3d-model", "pixel-art"};
        final String[] ART_MOD_ZH = {"摄影", "动漫", "数码", "漫画", "奇幻", "胶片", "霓虹", "等轴", "低多边形", "折纸", "素描", "粘土", "电影", "模型", "像素"};
        final int ART_SIZE_FLAG = 3;
        final String[] ART_SIZE = {"1:1", "9:16", "16:9", "4:3"};

        if (uin.equals("2168044167")) return;
        switch (textmsg) {
            case "记忆":
                ContextAI mess = new ContextAI(conn, msgid, groupid);
                mess.delContext();
                send("本群的聊天记录已经清理完成");
                return;
            case "设置":

                SettingAI sett = new SettingAI(conn, groupid);
                send("智能体:" + sett.getModel() +
                        "\n绘画风格:" + sett.getArt() +
                        "\n绘图尺寸:" + sett.getSize() +
                        "\n提示词:" + sett.getTips()
                );
                return;
            case "模型":
                Sp.put("Setting",CHAT_MOD_FLAG);
                StringBuilder msg1 = new StringBuilder();
                msg1.append("智能体模型列表:").append("\n");
                for (int i = 0; i < CHAT_MOD_EN.length; i++) {
                    msg1.append(i + 1).append(". ").append(CHAT_MOD_EN[i]).append("\n");
                }
                msg1.append("\n").append("输入序号选择");
                send(msg1.toString());
                return;
            case "风格":
                Sp.put("Setting",ART_MOD_FLAG);
                StringBuilder msg2 = new StringBuilder();
                msg2.append("绘画风格列表:").append("\n");
                for (int i = 0; i < ART_MOD_ZH.length; i++) {
                    msg2.append(i + 1).append(". ").append(ART_MOD_ZH[i]).append("\n");
                }
                msg2.append("\n").append("输入序号选择");
                send(msg2.toString());
                return;
            case "尺寸":
                Sp.put("Setting",ART_SIZE_FLAG);
                StringBuilder msg3 = new StringBuilder();
                msg3.append("绘图格式列表:").append("\n");
                for (int i = 0; i < ART_SIZE.length; i++) {
                    msg3.append(i + 1).append(". ").append(ART_SIZE[i]).append("\n");
                }
                msg3.append("\n").append("输入序号选择");
                send(msg3.toString());
                return;
        }
        if (textmsg.startsWith("提示")) {
            String text = textmsg.substring(2);
            SettingAI sett = new SettingAI(conn, groupid);
            if (textmsg.trim().equals("提示")) {
                sett.setDefaultTips();
                send("AI提示词已恢复默认");
            } else {
                sett.setTips(text);
                send("AI提示词修改成功");
            }
            return;
        }
        if (textmsg.startsWith(".") || coverAt(botUin)) {
            String text;
            if (coverAt(botUin)) {
                text = textmsg.replaceAll("@" + atName, "").trim();
            } else {
                text = textmsg.substring(1);
            }
            System.out.println("User：" + text);
            ContextAI mess = new ContextAI(conn, msgid, groupid);
            SettingAI sett = new SettingAI(conn, groupid);

            mess.setUser(text);
            mess.setImgList(imgList);
            mess.setTips(sett.getTips());

            JSONObject json = new JSONObject();
            json.put("function_image_gen", true);
            json.put("function_web_search", true);
            json.put("image_aspect_ratio", sett.getSize());
            json.put("image_style", sett.getArt());
            json.put("max_tokens", 8000);
            json.put("messages", mess.getContext());
            json.put("model", sett.getModel());
            json.put("source", "chat/ask_web");
            String jsonStr = json.toString();

            ZonedDateTime UtcTime = ZonedDateTime.now(ZoneId.of("UTC"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String time = UtcTime.format(formatter);

            MediaType mediaType = MediaType.parse("application/json; charset=UTF-8");
            RequestBody requestBody = RequestBody.create(mediaType, jsonStr);
            Request request = new Request.Builder()
                    .url("https://api.chaton.ai/chats/stream")
                    .addHeader("Host", "api.chaton.ai")
                    .addHeader("date", time)
                    .addHeader("client-time-zone", "+08:00")
                    .addHeader("authorization", "Bearer " + be() + "." + af("POST", "/chats/stream", time, jsonStr))
                    .addHeader("user-agent", version)
                    .addHeader("accept-language", "zh-CN")
                    .addHeader("x-cl-options", "hb")
                    .addHeader("content-type", "application/json; charset=UTF-8")
                    .addHeader("accept-encoding", "gzip")
                    .post(requestBody)
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.MINUTES)
                    .build();

            StringBuilder allMessages = new StringBuilder();
            StringBuilder msgSource = new StringBuilder();
            List<String> aiImage = new ArrayList<>();
            ContextAI finalMess = mess;
            RealEventSource realEventSource = new RealEventSource(request, new EventSourceListener() {
                @Override
                public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                    super.onEvent(eventSource, id, type, data);
                    JSONObject dataJSON = JSON.parseObject(data);
                    if (dataJSON.containsKey("data")) {
                        JSONObject dataObj = dataJSON.getJSONObject("data");
                        if (dataObj.containsKey("web")) {
                            JSONObject web = dataObj.getJSONObject("web");
                            if (web.containsKey("sources")) {
                                JSONArray sources = web.getJSONArray("sources");
                                msgSource.append("参考：");
                                for (Object urlSources : sources) {
                                    String title = ((JSONObject) urlSources).getString("title");
                                    String url = ((JSONObject) urlSources).getString("url");
                                    msgSource.append("\n").append(title).append(":").append(url);
                                }
                            }
                        }
                    }
                    if (dataJSON.containsKey("choices")) {
                        JSONObject choices = dataJSON.getJSONArray("choices").getJSONObject(0);
                        String finish_reason = choices.getString("finish_reason");
                        JSONObject delta = choices.getJSONObject("delta");

                        if (delta.containsKey("tool_calls")) {
                            JSONObject tool_calls = delta.getJSONArray("tool_calls").getJSONObject(0);
                            int index = tool_calls.getIntValue("index");
                            if (tool_calls.containsKey("id")) {
                                aiImage.add("");
                                return;
                            }
                            if (tool_calls.containsKey("function")) {
                                String msgText = tool_calls.getJSONObject("function").getString("arguments");
                                aiImage.set(index, aiImage.get(index) + msgText);
                                return;
                            }
                            return;
                        }
                        if ("stop".equals(finish_reason) || "length".equals(finish_reason)) {
                            String aimsg = allMessages.toString();
                            System.out.println("AI：" + aimsg);
                            String[] lines = aimsg.split("\n");
                            Messenger msgGroup1 = api.sendMessenger(msg -> {
                                msg.addMsg(Msg.Account, botUin);
                                msg.addMsg(Msg.Group);
                                msg.addMsg(Msg.GroupId, groupid);
                                msg.addMsg(Msg.Reply, msgid);
                                StringBuilder texts = new StringBuilder();
                                for (int i = 0; i < lines.length; i++) {
                                    String line = lines[i];
                                    if (line.matches(".*\\(https://spc\\.unk/.+\\).*")) {
                                        msg.addMsg(Msg.Text, texts);
                                        texts.delete(0, texts.length());
                                        String cutstr = line.substring(line.indexOf("https://spc.unk/"), line.indexOf(")"));
                                        String imgurl = HttpUtil.doGet("https://api.chaton.ai/storage/" + cutstr.replaceFirst("https://spc.unk/", ""));
                                        JSONObject jsonimg = JSON.parseObject(imgurl);
                                        msg.addMsg(Msg.Text, line.substring(0, line.indexOf("![")));
                                        msg.addMsg(Msg.Img, jsonimg.getString("getUrl"));
                                        //msg.addMsg(Msg.Text, "(" + jsonimg.getString("getUrl") + ")");
                                        msg.addMsg(Msg.Text, line.substring(line.indexOf(")") + 1));
                                        continue;
                                    }
                                    texts.append(line);
                                    if (i != lines.length - 1) {
                                        texts.append("\n");
                                    }

                                }
                                msg.addMsg(Msg.Text, texts);
                                if (allMessages.length() > 250) {
                                    msg.addMsg(Msg.MultiMsgPut);
                                    msg.addMsg(Msg.Uin, uin);
                                    msg.addMsg(Msg.UinName, uinName);
                                } else {
                                    if (!msgSource.isEmpty()) {
                                        msg.addMsg(Msg.Text, "\n\n" + msgSource);
                                    }
                                }
                            });
                            if (allMessages.length() > 250) {
                                final Messenger rsp = api.sendMessenger(msg -> {
                                    msg.addMsg(Msg.Account, botUin);
                                    msg.addMsg(Msg.Group);//声明 聊天记录 来源是 群聊
                                    msg.addMsg(Msg.GroupId, groupid);
                                    msg.addMsg(Msg.Time, System.currentTimeMillis() / 1000);//时间戳秒
                                    msg.addMsg(Msg.MultiMsg, msgGroup1.getString(Msg.MsgId));//添加消息元素
                                    if (!msgSource.isEmpty()) {
                                        Messenger msgGroup2 = api.sendMessenger(msg2 -> {
                                            msg2.addMsg(Msg.Account, botUin);
                                            msg2.addMsg(Msg.Group);
                                            msg2.addMsg(Msg.GroupId, groupid);
                                            msg2.addMsg(Msg.MultiMsgPut);
                                            msg2.addMsg(Msg.Uin, uin);
                                            msg2.addMsg(Msg.UinName, uinName);
                                            msg2.addMsg(Msg.Text, msgSource.toString());
                                        });
                                        msg.addMsg(Msg.MultiMsg, msgGroup2.getString(Msg.MsgId));//添加消息元素
                                    }
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
                                        msg.addMsg(Msg.Xml, "<title size=\"26\" color=\"#FF6151\" maxLines=\"2\" lineSpace=\"12\">消息长度:" + allMessages.length() + "</title>");
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
                            try {
                                finalMess.setAI(aimsg);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }
                        if (delta.containsKey("content")) {
                            String msgText = delta.getString("content");
                            allMessages.append(msgText);
                            return;
                        }
                    }
                }
            });
            realEventSource.connect(okHttpClient);
            return;
        }
        if (textmsg.startsWith("。")) {
            String text = textmsg.substring(1);
            System.out.println("User：" + text);
            ContextAI mess = new ContextAI(conn, msgid, groupid);
            SettingAI sett = new SettingAI(conn, groupid);
            mess.setUser(text);
            mess.setImgList(imgList);
            mess.setTips(sett.getTips());

            JSONObject json = new JSONObject();
            json.put("function_image_gen", true);
            json.put("function_web_search", true);
            json.put("image_aspect_ratio", sett.getSize());
            json.put("image_style", sett.getArt());
            json.put("max_tokens", 8000);
            json.put("messages", mess.getContext());
            json.put("model", sett.getModel());
            json.put("source", "chat/ask_web");
            String jsonStr = json.toString();

            ZonedDateTime UtcTime = ZonedDateTime.now(ZoneId.of("UTC"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String time = UtcTime.format(formatter);

            MediaType mediaType = MediaType.parse("application/json; charset=UTF-8");
            RequestBody requestBody = RequestBody.create(mediaType, jsonStr);
            Request request = new Request.Builder()
                    .url("https://api.chaton.ai/chats/stream")
                    .addHeader("Host", "api.chaton.ai")
                    .addHeader("date", time)
                    .addHeader("client-time-zone", "+08:00")
                    .addHeader("authorization", "Bearer " + be() + "." + af("POST", "/chats/stream", time, jsonStr))
                    .addHeader("user-agent", version)
                    .addHeader("accept-language", "zh-CN")
                    .addHeader("x-cl-options", "hb")
                    .addHeader("content-type", "application/json; charset=UTF-8")
                    .addHeader("accept-encoding", "gzip")
                    .post(requestBody)
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.MINUTES)
                    .build();

            StringBuilder allMessages = new StringBuilder();
            StringBuilder msgSource = new StringBuilder();
            ContextAI finalMess = mess;
            RealEventSource realEventSource = new RealEventSource(request, new EventSourceListener() {
                @Override
                public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                    super.onEvent(eventSource, id, type, data);
                    JSONObject dataJSON = JSON.parseObject(data);
                    if (dataJSON.containsKey("data")) {
                        JSONObject dataObj = dataJSON.getJSONObject("data");
                        if (dataObj.containsKey("web")) {
                            JSONObject web = dataObj.getJSONObject("web");
                            if (web.containsKey("sources")) {
                                JSONArray sources = web.getJSONArray("sources");
                                msgSource.append("参考：");
                                for (Object urlSources : sources) {
                                    String title = ((JSONObject) urlSources).getString("title");
                                    String url = ((JSONObject) urlSources).getString("url");
                                    msgSource.append("\n").append(title).append(":").append(url);
                                }
                            }
                        }
                    }
                    if (dataJSON.containsKey("choices")) {
                        JSONObject choices = dataJSON.getJSONArray("choices").getJSONObject(0);
                        String finish_reason = choices.getString("finish_reason");
                        if ("stop".equals(finish_reason) || "length".equals(finish_reason)) {
                            String aimsg = allMessages.toString();
                            System.out.println("AI：" + aimsg);
                            String[] lines = aimsg.split("\n");
                            StringBuilder texts = new StringBuilder();
                            for (int i = 0; i < lines.length; i++) {
                                StringBuilder line = new StringBuilder(lines[i]);
                                if (line.toString().matches(".*\\(https://spc\\.unk/.+\\).*")) {
                                    String cutstr = line.substring(line.indexOf("https://spc.unk/"), line.indexOf(")"));
                                    String imgurl = HttpUtil.doGet("https://api.chaton.ai/storage/" + cutstr.replaceFirst("https://spc.unk/", ""));
                                    JSONObject jsonimg = JSON.parseObject(imgurl);
                                    texts.append(line.substring(0, line.indexOf("](") - 1)).append(" #1080px #1080px]");
                                    texts.append("(").append(jsonimg.getString("getUrl")).append(")");
                                    texts.append(line.substring(line.indexOf(")") + 1));
                                } else {
                                    texts.append(line);
                                }
                                if (i != lines.length - 1) {
                                    texts.append("\n");
                                }
                            }
                            JSONArray jsonArr = new JSONArray();
                            jsonArr.add(texts);
                            jsonArr.add(msgSource);
                            sendMD(jsonArr);
                            try {
                                finalMess.setAI(aimsg);
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                            return;
                        }
                        JSONObject delta = choices.getJSONObject("delta");
                        if (delta.containsKey("content")) {
                            String msgText = delta.getString("content");
                            allMessages.append(msgText);
                            return;
                        }
                    }
                }
            });
            realEventSource.connect(okHttpClient);
            return;
        }
        if (textmsg.matches("\\d{1,2}")) {
            if (!Sp.containsKey("Setting")) return;
            switch (Sp.get("Setting")) {
                case CHAT_MOD_FLAG:
                    int i = Integer.parseInt(textmsg);
                    if (i > CHAT_MOD.length) return;
                    Sp.remove("Setting");
                    String model = CHAT_MOD[i - 1];
                    try {
                        SettingAI sett = new SettingAI(conn, groupid);
                        sett.setModel(model);
                        send("智能体模型已切换为：" + model);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                case ART_MOD_FLAG:
                    int i2 = Integer.parseInt(textmsg);
                    if (i2 > ART_MOD.length) return;
                    Sp.remove("Setting");
                    String artStyle = ART_MOD[i2 - 1];
                    try {
                        SettingAI sett = new SettingAI(conn, groupid);
                        sett.setArt(artStyle);
                        send("绘画风格已切换为：" + ART_MOD_ZH[i2 - 1]);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                case ART_SIZE_FLAG:
                    int i3 = Integer.parseInt(textmsg);
                    if (i3 > ART_SIZE.length) return;
                    System.out.println(textmsg);
                    Sp.remove("Setting");
                    String artSize = ART_SIZE[i3 - 1];
                    try {
                        SettingAI sett = new SettingAI(conn, groupid);
                        sett.setSize(artSize);
                        send("绘画尺寸已切换为：" + artSize);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return;
            }
        }

    }
    private String be() {
        return "/Im5m39ealFF8r24GuSu7w==";
    }

    private String af(String Act, String url, String time, String json) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] key = {14, 94, 79, 102, 38, -11, 11, 65, 100, 43, 115, 94, 15, -15, 14, 16, 66, -127, -8, -30, 98, 109, -21, 60, 62, 41, 78, 29, 72, -75, 47, 8};
        byte[] value = (Act + ":" + url + ":" + time + "\n" + json).getBytes(Charsets.UTF_8);
        Mac mac;
        mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, mac.getAlgorithm()));
        byte[] doFinal = mac.doFinal(value);
        return Base64.getEncoder().encodeToString(doFinal);
    }

    public String getHtml(String url) throws NoSuchAlgorithmException, InvalidKeyException {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String formattedDateTime = now.format(formatter);
        Base64.Encoder encoder = Base64.getEncoder();
        String link = encoder.encodeToString(url.getBytes());
        Request request = new Request.Builder()
                .url("https://api.chaton.ai/urls/" + link)
                .addHeader("user-agent", version)
                .addHeader("date", formattedDateTime)
                .addHeader("authorization", "Bearer " + be() + "." + af("GET", "/urls/" + link, formattedDateTime, ""))
                .get()
                .build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String translate(String data) throws Exception {
        OkHttpClient client = new OkHttpClient();
        ArtAI.ServerEncrypt serverEncrypt = new ArtAI.ServerEncrypt();
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data", serverEncrypt.outerEn("{\"appId\":\"com.hugelettuce.unidream.ai.drawing\",\"platform\":2,\"text\":\"" + data + "\"}", "f@uck#oai", "\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008"))  // 添加第一个文本字段
                .build();
        Request request = new Request.Builder()
                .url("https://aichat.hugelettuce.com/service/oai/translate")  // 替换为你的请求URL
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            JSONObject jsonObject = JSON.parseObject(response.body().string());
            String re = jsonObject.getJSONObject("data").getString("translateResult");
            System.out.println(re);
            return re;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String easyGPT(String tips, String model) throws InterruptedException, NoSuchAlgorithmException, InvalidKeyException {
        ZonedDateTime UtcTime = ZonedDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String time = UtcTime.format(formatter);

        MediaType mediaType = MediaType.parse("application/json; charset=UTF-8");
        String jsonStr = "{\n" +
                "  \"max_tokens\": 8000,\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"content\": \"" + tips + "\",\n" +
                "      \"role\": \"user\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"model\": \"" + model + "\",\n" +
                "  \"source\": \"chat/free\"\n" +
                "}";
        RequestBody requestBody = RequestBody.create(mediaType, jsonStr);

        Request request = new Request.Builder()
                .url("https://api.chaton.ai/chats/stream")
                .addHeader("Host", "api.chaton.ai")
                .addHeader("date", time)
                .addHeader("client-time-zone", "+08:00")
                .addHeader("authorization", "Bearer " + be() + "." + af("POST", "/chats/stream", time, jsonStr))
                .addHeader("user-agent", version)
                .addHeader("accept-language", "zh-CN")
                .addHeader("x-cl-options", "hb")
                .addHeader("content-type", "application/json; charset=UTF-8")
                .addHeader("accept-encoding", "gzip")
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();

        StringBuilder allMessages = new StringBuilder();
        CountDownLatch eventLatch = new CountDownLatch(1);
        RealEventSource realEventSource = new RealEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                super.onEvent(eventSource, id, type, data);
                JSONObject dataJSON = JSON.parseObject(data);
                if (dataJSON.containsKey("choices")) {
                    JSONObject choices = dataJSON.getJSONArray("choices").getJSONObject(0);
                    String finish_reason = choices.getString("finish_reason");
                    if ("stop".equals(finish_reason) || "length".equals(finish_reason)) {
                        eventLatch.countDown();
                    }
                    JSONObject delta = choices.getJSONObject("delta");
                    if (delta.containsKey("content")) {
                        allMessages.append(delta.getString("content"));
                    }
                }
            }
        });
        realEventSource.connect(okHttpClient);
        eventLatch.await();
        return allMessages.toString();
    }

}
