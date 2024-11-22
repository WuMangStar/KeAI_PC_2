package aei.lang.keai.Utils;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpUtil {
    public static String doPostForm(String url, FormBody formBody) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String doPostJson(String url, String json) {
        OkHttpClient okHttpClient = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, json);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String doGet(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request;
        request = new Request.Builder()
                .url(url)
                .get()
                .build();//创造请求
        try (Response response = okHttpClient.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isImageUrl(String url) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .head() // 使用 HEAD 请求
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String contentType = response.header("Content-Type");
                if (contentType != null && contentType.startsWith("image")) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

}
