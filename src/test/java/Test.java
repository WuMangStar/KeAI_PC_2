import aei.lang.keai.Function.Api.ArtAIAPI;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Test {
    public static void main(String[] args) throws IOException, GeneralSecurityException {
     /*   ArtAIAPI artAIAPI = new ArtAIAPI();
        OkHttpClient client = new OkHttpClient();
       InputStream inputStream= ZipInputStream();
        InputStream inputStream = new FileInputStream("artCache/gzy_textart_Unidream_1735992911522_w27n_cross.zip");
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), artAIAPI.invertBytes(inputStream.readAllBytes()));
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file","gzy_textart_Unidream_1735992911522_w27n_cross.png", fileBody)  // 添加第一个文本字段
                .addFormDataPart("data", artAIAPI.nEncryptServerRequest_img("{\"subDir\":\"cn#andr\"}"))
                .build();
        Request request = new Request.Builder()
                .url("https://appinference-upload4.guangzhuiyuan.com/api/aigc-oss/upload/tmp/zip/textart")  // 替换为你的请求URL
                .addHeader("CLIENTID", "UNIDREAM.a.45307e0ba846bb0ae4bf06329f7c7669")
                .addHeader("DEVICEID", artAIAPI.getDeviceId())
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            JSONObject bodyJson = JSON.parseObject(body);
            int resultCode = bodyJson.getInteger("resultCode");
            if (resultCode != 100) throw new RuntimeException(bodyJson.toJSONString());
            System.out.printf(artAIAPI.nDecryptResponse_img(bodyJson.getString("data")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }
}
