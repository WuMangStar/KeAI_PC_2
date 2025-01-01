package aei.lang.keai.Function.Api;

import aei.lang.keai.MySQL.ContextAI;
import aei.lang.keai.MySQL.SettingAI;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ChatAIAPI {
    private final String version = "ChatOn_Android/1.58.504";
    //
    public String be() {
        return "dzlqVkZVWkxuQlFRMWpYUA==";
    }

    public String af(String Act, String url, String time, String json) throws InvalidKeyException, NoSuchAlgorithmException {
       // byte[] key = {14, 94, 79, 102, 38, -11, 11, 65, 100, 43, 115, 94, 15, -15, 14, 16, 66, -127, -8, -30, 98, 109, -21, 60, 62, 41, 78, 29, 72, -75, 47, 8};
        byte[] key = {118, 57, 109, 118, 121, 73, 83, 76, 115, 105, 74, 51, 113, 84, 81, 85, 119, 121, 48, 121, 102, 101, 79, 69, 65, 100, 49, 69, 83, 72, 84, 77};
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
        ArtAIAPI serverEncrypt = new ArtAIAPI();
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

    public String RequestAI(Connection conn, String keyId, String msgid, String text, List<String> imgList) throws Exception {
        ContextAI mess = new ContextAI(conn, msgid, keyId);
        SettingAI sett = new SettingAI(conn, keyId);

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

        StringBuilder ReplyMessages = new StringBuilder();
        StringBuilder WebMessages = new StringBuilder();
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
                        System.out.println(dataJSON.getString("model")+":"+ ReplyMessages + WebMessages);
                        eventLatch.countDown();
                    }
                    JSONObject delta = choices.getJSONObject("delta");
                    if (delta.containsKey("content")) {
                        ReplyMessages.append(delta.getString("content"));
                    }
                } else if (dataJSON.containsKey("data")) {
                    JSONObject dataObj = dataJSON.getJSONObject("data");
                    if (dataObj.containsKey("web")) {
                        JSONObject web = dataObj.getJSONObject("web");
                        if (web.containsKey("sources")) {
                            JSONArray sources = web.getJSONArray("sources");
                            WebMessages.append("\n\n").append("参考：");
                            for (Object urlSources : sources) {
                                String title = ((JSONObject) urlSources).getString("title");
                                String url = ((JSONObject) urlSources).getString("url");
                                WebMessages.append("\n").append(title).append(":").append(url);
                            }
                        }
                    }
                }
            }
        });
        realEventSource.connect(okHttpClient);
        eventLatch.await();
        mess.setAI(ReplyMessages.toString());
        return ReplyMessages.toString()+WebMessages;
    }

    public String easyGPT(String text, String model) throws InterruptedException, NoSuchAlgorithmException, InvalidKeyException {
        ZonedDateTime UtcTime = ZonedDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String time = UtcTime.format(formatter);

        MediaType mediaType = MediaType.parse("application/json; charset=UTF-8");
        String jsonStr = "{\n" +
                "  \"max_tokens\": 8000,\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"content\": \"" + text + "\",\n" +
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
