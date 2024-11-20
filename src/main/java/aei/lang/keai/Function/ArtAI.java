package aei.lang.keai.Function;

import aei.lang.keai.FunctionI;
import aei.lang.keai.Utils.QQBot;
import aei.lang.msg.Messenger;
import aei.lang.plugin.SecPlugin;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;

import static aei.lang.keai.StartBot.Sp;

public class ArtAI extends QQBot implements FunctionI {
    @Override
    public String getName() {
        return "AI画图";
    }

    @Override
    public void init(SecPlugin api, Messenger messenger, Connection conn) throws SQLException, NoSuchAlgorithmException, InvalidKeyException {
        QQBotInit(api, messenger);
        switch (textmsg) {
            case "解除":
                if (uin.equals("2915372048")) {
                    Sp.put(groupid,1);
                    send("已关闭NSFW过滤器");
                }
                return;
            case "封锁":
                if (uin.equals("2915372048")) {
                    Sp.remove(groupid);
                    send("启用NSFW过滤器");
                }
                return;
        }
        if (textmsg.startsWith("画") || textmsg.startsWith("draw")) {
            String text;
            if (textmsg.startsWith("画")) {
                text = textmsg.substring(1).trim();
            } else {
                text = textmsg.substring(4).trim();
            }
            try {
                ChatAI AI = new ChatAI();
                if (!Sp.containsKey(groupid)) {
                    if (AI.easyGPT("作为提示词检测师，你的任务是审核提示词内容。请判断提示词中的内容是否包含不当内容（NSFW）。如果包含，请返回“违规”；如果不包含，请返回“合法”。提示词如下：" + text , "gpt-4o").startsWith("违规")) {
                        send("内容违规:NSFW");
                        return;
                    }
                }
                char chr = text.charAt(1);
                if (chr >= 0x4e00 && chr <= 0x9fa5) {
                    text = AI.translate(text);
                }
                JSONObject jsondata = JSON.parseObject(artRequset("und-textart/ainfer", "{\n" +
                        "  \"adetailer\": \"{\\\"ad_confidence\\\":0.1,\\\"ad_denoising_strength\\\":0.45,\\\"ad_inpaint_height\\\":768,\\\"ad_inpaint_width\\\":768,\\\"ad_mask_max_ratio\\\":0.08,\\\"ad_model\\\":\\\"face_yolov8s.pt\\\",\\\"ad_negative_prompt\\\":\\\"((poorly drawn face)), paintings,  (worst quality:2), (low quality:2), lowres, ((extra limbs)), cloned face, (((disfigured))), ugly\\\",\\\"ad_prompt\\\":\\\"${userInputPrompt}\\\",\\\"ad_steps\\\":20,\\\"ad_use_steps\\\":true,\\\"enable\\\":true}\",\n" +
                        "  \"adetailerUse\": 1,\n" +
                        "  \"blockedWords\": [],\n" +
                        "  \"cfgScale\": 1,\n" +
                        "  \"classificationInfo\": null,\n" +
                        "  \"clipSkip\": 1,\n" +
                        "  \"consumeCount\": 1,\n" +
                        "  \"controlGuessMode\": \"Balanced\",\n" +
                        "  \"controlNetParams\": \"{\\\"params\\\":[{\\\"control_mode\\\":\\\"Balanced\\\",\\\"guidance_end\\\":1.0,\\\"guidance_start\\\":0.0,\\\"image\\\":\\\"\\\",\\\"model\\\":\\\"xinsir-controlnet-union-sdxl-1.0_promax [9460e4db]\\\",\\\"module\\\":\\\"none\\\",\\\"union_control_type\\\":\\\"Unknown\\\",\\\"weight\\\":0.6}],\\\"version\\\":\\\"1\\\"}\",\n" +
                        "  \"ct\": 1,\n" +
                        "  \"denoising\": 0.6,\n" +
                        "  \"extraArgs\": \"{\\\"canvas_resolution\\\":[864,1152],\\\"enable_hr\\\":true,\\\"hr_denoising\\\":0.3,\\\"hr_scale\\\":1.5,\\\"hr_upscaler\\\":\\\"R-ESRGAN 4x+ Anime6B\\\",\\\"sampler_name\\\":\\\"Euler a\\\",\\\"subseed\\\":-1,\\\"version\\\":1}\",\n" +
                        "  \"faceEditor\": true,\n" +
                        "  \"faceEditorConfidence\": 0.97,\n" +
                        "  \"faceEditorMargin\": 1.6,\n" +
                        "  \"faceEditorPrompt\": null,\n" +
                        "  \"faceEditorStrength\": 0.15,\n" +
                        "  \"gpd\": [],\n" +
                        "  \"imgTagUrl\": null,\n" +
                        "  \"iter\": 8,\n" +
                        "  \"locale\": \"zh-CN_CN\",\n" +
                        "  \"mode\": 6,\n" +
                        "  \"np\": \"nsfw,lowres,(bad),text,bad hand,bad face,error,fewer,extra,missing,worst quality,jpeg artifacts,low quality,watermark,unfinished,displeasing,oldest,early,chromatic aberration,signature,extra digits,artistic error,username,scan,[abstract]\",\n" +
                        "  \"pattern\": 0,\n" +
                        "  \"pf\": 2,\n" +
                        "  \"proCardType\": 1,\n" +
                        "  \"prompt\": \"" + Base64.getEncoder().encodeToString((text + ",(safe:0.5),masterpiece,best quality,<lora:Hyper-SDXL-8steps-lora:1>, <lora:gzy_detailer_xl:3.000000>").getBytes(StandardCharsets.UTF_8)) + "\",\n" +
                        "  \"promptImg\": null,\n" +
                        "  \"promptStyle\": \"(safe:0.5),masterpiece,best quality,<lora:Hyper-SDXL-8steps-lora:1>\",\n" +
                        "  \"ratio\": -1,\n" +
                        "  \"restoreFaces\": -1,\n" +
                        "  \"sceneIdList\": [\n" +
                        "    11\n" +
                        "  ],\n" +
                        "  \"seed\": -1,\n" +
                        "  \"style\": \"sdxl_anime\",\n" +
                        "  \"subSeedStrength\": 0,\n" +
                        "  \"taggerThreshold\": 0.5,\n" +
                        "  \"tileMode\": \"\",\n" +
                        "  \"uuid\": \"qwb1026@gmail.com\",\n" +
                        "  \"vt\": 1,\n" +
                        "  \"controlMode\": -1,\n" +
                        "  \"controlWeight\": 1,\n" +
                        "  \"ef\": 0,\n" +
                        "  \"nbt\": 0.4,\n" +
                        "  \"sampler\": -1,\n" +
                        "  \"try\": 2\n" +
                        "}"));
                String taskKey = jsondata.getString("key");
                System.out.println("任务开始：" + taskKey);
                for (int i=0;i<10;i++){
                    Thread.sleep(1111);
                    JSONObject taskState = JSON.parseObject(artRequset("und-textart/multiResult", "{\"key\":\"" + taskKey + "\",\"pf\":2,\"uuid\":\"qwb1026@gmail.com\",\"ver\":1,\"vt\":1}"));
                    JSONObject stateJson = taskState.getJSONObject("results").getJSONObject(taskKey);
                    if (stateJson.getInteger("resultCode") == 100) {
                        String resultUrl = stateJson.getJSONObject("data").getString("resultUrl");
                        sendImg(downloadArt(resultUrl, taskKey));
                        return;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    private final String HOST = "appinference-distribute2.thirteenleafclover.com";

    public String artRequset(String task, String data) throws GeneralSecurityException {
        OkHttpClient client = new OkHttpClient();
        ServerEncrypt serverEncrypt = new ServerEncrypt();
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data", serverEncrypt.nEncryptServerRequest(data))  // 添加第一个文本字段
                .build();
        Request request = new Request.Builder()
                .url("https://" + HOST + "/api/aigc/2.0/" + task)  // 替换为你的请求URL
                .addHeader("CLIENTID", "UNIDREAM.a.45307e0ba846bb0ae4bf06329f7c7669")
                //.addHeader("CLIENTVERSION","3.8.3")
                .addHeader("DEVICEID", serverEncrypt.getDeviceId())
                // .addHeader("ACCESSTOKEN","Ltaror4rLla6ZlrtmooraEZ4rAlrLR6BZLLBW6rt4bBLuo6arvrrBZmaL6mWtmERoZLokEbBo6mkobZ6L6LLRb4rLkZ6uoBvkklltEBbAmmLLLuEBrm6BLtAkrmorbLbFC//.4Hezk1HnJmg9a84Np03YD1M16FC//.oFC//.p")
                // .addHeader("LOCALE","CN")
                // .addHeader("TIMESTAMP","1728656212685")
                // .addHeader("DEVICEMODEL","PHB110")
                // .addHeader("PLATFORM","2")
                // .addHeader("SIGNATURE","4e2b1e8293a4ffea06b86f6d0dcdf036")
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            String DeData = serverEncrypt.nDecryptResponse(JSON.parseObject(body).getString("data") );
            return DeData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean censor(String text) {
        OkHttpClient client = new OkHttpClient();
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("text", text)  // 添加第一个文本字段
                .build();
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/rest/2.0/solution/v1/text_censor/v2/user_defined?access_token=" + censorToken())  // 替换为你的请求URL
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            JSONObject jsonObject = JSON.parseObject(response.body().string());
            return jsonObject.getInteger("conclusionType") == 1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String censorToken() {
        OkHttpClient client = new OkHttpClient();
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("grant_type", "client_credentials")  // 添加第一个文本字段
                .addFormDataPart("client_id", "RzCYFe2ZhAAwSYlWXaaf6I7Y")
                .addFormDataPart("client_secret", "vHZMywxCVWCg6l203LwAzW9EUYatAqS9")
                .build();
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")  // 替换为你的请求URL
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            JSONObject jsonObject = JSON.parseObject(response.body().string());
            return jsonObject.getString("access_token");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] invertBytes(byte[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ 0xFF);
        }
        return result;
    }

    public String downloadArt(String url, String taskKey) {
        System.out.println("URT："+url);
        String CACHE_DIR = "artCache";
        createCacheDirectory(CACHE_DIR);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        try (Response response = okHttpClient.newCall(request).execute()) {
            File saveFile = new File(CACHE_DIR, taskKey + ".png");
            saveByte(invertBytes(response.body().bytes()), saveFile);
            System.out.println("完成：" + saveFile.getAbsolutePath());
            return saveFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("下载或保存图片时发生错误: " + e.getMessage(), e);
        }
    }

    static class ServerEncrypt {
        private final String apiKey = "o0B/jYI8VPKcvI77gvZNPQlCggd7/EwFN+oLArHsFKDvAwswCBBLAg1Y1QcK39yTpj25igwcc72Z4Rcmp9U2NcBjKHpJdHndMizApiZ/WPzz/8CclAt9yO3FuvGg1IlDmA6pPvrSkMoCkk0c6Ym67jUAG2DnkCfiVtHD5AbP75M=";
        private final String deviceId = "8517b8c1-e8ec-4030-a04c-21d2f4e13602";
        private final String password = "2423//dGhpcyBzZWNyZXQgaXMgdXNlIGJ5IGFuZHJvaWQga2V5IGRlY3J5cHQuNzVhQzJNNWZlNXk0FC//.NWY4MTQ0NDJjODI2NDJhMWJiYTVlOGZhMjAyYjlmM2YxNjg4NzI0MDEzNzA5FC//.C2N5M5U7761";
        private final String salt = "this secret is use by android key decrypt.";
        private final int iter = 4096;
        private final int keyLength = 32;
        private final byte[] passwordDecryptionKey;

        public ServerEncrypt() throws GeneralSecurityException {
            passwordDecryptionKey = pbkdf2(password.getBytes(StandardCharsets.UTF_8), salt.getBytes(StandardCharsets.UTF_8), iter, keyLength);
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }

        public String getDeviceId() {
            return deviceId;
        }

        public static byte[] pbkdf2(byte[] password, byte[] salt, int iter, int keyLength) throws GeneralSecurityException {
            char[] chars = new char[password.length];
            for (int i = 0; i < password.length; i++) {
                chars[i] = (char) password[i];
            }
            KeySpec spec = new PBEKeySpec(chars, salt, iter, keyLength * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            return factory.generateSecret(spec).getEncoded();
        }

        public byte[] aesEncrypt(byte[] in, byte[] key) throws GeneralSecurityException {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
            return cipher.doFinal(in);
        }

        public byte[] aesDecrypt(byte[] in, byte[] key) throws GeneralSecurityException {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
            return cipher.doFinal(in);
        }

        public String nDecryptResponse(String in) throws GeneralSecurityException {
            byte[] decryptedPass = aesDecrypt(Base64.getDecoder().decode(apiKey), passwordDecryptionKey);
            byte[] key = pbkdf2(decryptedPass, deviceId.getBytes(StandardCharsets.UTF_8), iter, keyLength);
            return new String(aesDecrypt(Base64.getDecoder().decode(in), key));
        }

        public String nEncryptServerRequest(String in) throws GeneralSecurityException {
            byte[] decryptedPass = aesDecrypt(Base64.getDecoder().decode(apiKey), passwordDecryptionKey);
            byte[] key = pbkdf2(decryptedPass, deviceId.getBytes(StandardCharsets.UTF_8), iter, keyLength);
            return Base64.getEncoder().encodeToString(aesEncrypt(in.getBytes(StandardCharsets.UTF_8), key));
        }

        public String outerEn(String data, String passwd, String Key) throws Exception {
            SecretKey generateSecret = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(passwd.getBytes(StandardCharsets.UTF_8)));
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS7Padding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, generateSecret, new IvParameterSpec(Key.getBytes()));
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        }

        public String outerDe(String data, String passwd, String Key) throws Exception {
            SecretKey generateSecret = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(passwd.getBytes(StandardCharsets.UTF_8)));
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS7Padding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, generateSecret, new IvParameterSpec(Key.getBytes()));
            return new String(cipher.doFinal(Base64.getDecoder().decode(data)));
        }
    }

}
