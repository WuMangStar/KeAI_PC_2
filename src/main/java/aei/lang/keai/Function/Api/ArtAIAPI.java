package aei.lang.keai.Function.Api;

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
import java.security.Security;
import java.security.spec.KeySpec;
import java.util.Base64;

import aei.lang.keai.Utils.FileUtils;

public class ArtAIAPI {

    private final String apiKey = "o0B/jYI8VPKcvI77gvZNPQlCggd7/EwFN+oLArHsFKDvAwswCBBLAg1Y1QcK39yTpj25igwcc72Z4Rcmp9U2NcBjKHpJdHndMizApiZ/WPzz/8CclAt9yO3FuvGg1IlDmA6pPvrSkMoCkk0c6Ym67jUAG2DnkCfiVtHD5AbP75M=";

    private final String imgapikey="xWS1b0tnnLqIYQncH4LuTQ5RQ+vUOHCSTr38ZT5XE382VsW3n/IUezX3pWvLwowXcu32CKD5I5NB6oNVol6I+1uuj5zxkFpnOui5RrK91HcSJ9GTjvfABs8Lf9OLtPzf5trobdx8AxnBLvnpi/X64Ldnt1iBsnxVwtotjbVI9CA=";
    private final String deviceId = "e8f086af-7c85-4c69-9a23-2e548cd8ce34";
    private final int iter = 4096;
    private final int keyLength = 32;
    private final byte[] passwordDecryptionKey;

    public ArtAIAPI() throws GeneralSecurityException {
        String salt = "this secret is use by android key decrypt.";
        String password = "2423//dGhpcyBzZWNyZXQgaXMgdXNlIGJ5IGFuZHJvaWQga2V5IGRlY3J5cHQuNzVhQzJNNWZlNXk0FC//.NWY4MTQ0NDJjODI2NDJhMWJiYTVlOGZhMjAyYjlmM2YxNjg4NzI0MDEzNzA5FC//.C2N5M5U7761";
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
    public String nDecryptResponse_img(String in) throws GeneralSecurityException {
        byte[] decryptedPass = aesDecrypt(Base64.getDecoder().decode(imgapikey), passwordDecryptionKey);
        byte[] key = pbkdf2(decryptedPass, deviceId.getBytes(StandardCharsets.UTF_8), iter, keyLength);
        return new String(aesDecrypt(Base64.getDecoder().decode(in), key));
    }

    public String nEncryptServerRequest(String in) throws GeneralSecurityException {
        byte[] decryptedPass = aesDecrypt(Base64.getDecoder().decode(apiKey), passwordDecryptionKey);
        byte[] key = pbkdf2(decryptedPass, deviceId.getBytes(StandardCharsets.UTF_8), iter, keyLength);
        return Base64.getEncoder().encodeToString(aesEncrypt(in.getBytes(StandardCharsets.UTF_8), key));
    }
    public String nEncryptServerRequest_img(String in) throws GeneralSecurityException {
        byte[] decryptedPass = aesDecrypt(Base64.getDecoder().decode(imgapikey), passwordDecryptionKey);
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

    public String artRequset(String task, String data) throws GeneralSecurityException {
        OkHttpClient client = new OkHttpClient();
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("data", nEncryptServerRequest(data))  // 添加第一个文本字段
                .build();
        String HOST = "appinference-distribute2.thirteenleafclover.com";
        Request request = new Request.Builder()
                .url("https://" + HOST + "/api/aigc/2.0/" + task)  // 替换为你的请求URL
                .addHeader("CLIENTID", "UNIDREAM.a.45307e0ba846bb0ae4bf06329f7c7669")
                .addHeader("DEVICEID", deviceId)
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            JSONObject bodyJson = JSON.parseObject(body);
            if (!(bodyJson.getInteger("resultCode") == -220)) {
                return nDecryptResponse(bodyJson.getString("data"));
            }
            throw new RuntimeException(bodyJson.getString("msg"));
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
        System.out.println("URT：" + url);
        String CACHE_DIR = "artCache";
        FileUtils.createCacheDirectory(CACHE_DIR);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        try (Response response = okHttpClient.newCall(request).execute()) {
            File saveFile = new File(CACHE_DIR, taskKey + ".png");
            FileUtils.saveByte(invertBytes(response.body().bytes()), saveFile);
            System.out.println("完成：" + saveFile.getAbsolutePath());
            return saveFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("下载或保存图片时发生错误: " + e.getMessage(), e);
        }
    }
}
