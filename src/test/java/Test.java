import aei.lang.keai.Function.Api.ArtAIAPI;
import aei.lang.keai.Function.Api.ChatAIAPI;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Test {
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        ChatAIAPI ai = new ChatAIAPI();
        String time = "2025-03-04T12:11:10Z";
        String date = "{\"function_image_gen\":true,\"function_web_search\":false,\"max_tokens\":8000,\"messages\":[{\"content\":\"你的角色是一个聪明、有创造力的助理。不要说自己是聊天机器人或人工智能助理。交流时请考虑以下术语： 1. 您的回复长度： 自动. 2. 您讲话的语气风格： 默认. 该对话框有一个生成图像的选项。只有在用户明确提出请求时才调用该函数，例如，使用与图像生成请求相关的任何相关词语。在其他情况下，不应调用图像生成函数。\",\"role\":\"system\"},{\"content\":\"你好\",\"role\":\"user\"}],\"model\":\"gpt-4o-mini\",\"source\":\"chat/free\"}";
        System.out.println("Bearer V0xHREtEZDMzZGFCUGx3cg==.PiZE+8qK0/CyIZIhT5KmnQHJU4ztsvbrvnWHjORz/3s=");
        System.out.println("Bearer " + ai.be() + "." + ai.af("POST", "/chats/stream", time, date));

        byte[] key1 = {14, 94, 79, 102, 38, -11, 11, 65, 100, 43, 115, 94, 15, -15, 14, 16, 66, -127, -8, -30, 98, 109, -21, 60, 62, 41, 78, 29, 72, -75, 47, 8};
        byte[] key2 = {118, 57, 109, 118, 121, 73, 83, 76, 115, 105, 74, 51, 113, 84, 81, 85, 119, 121, 48, 121, 102, 101, 79, 69, 65, 100, 49, 69, 83, 72, 84, 77};
        System.out.println(new String(key1));
        System.out.println(new String(Base64.getDecoder().decode(key2)));
        System.out.println(new String(Base64.getDecoder().decode("sEvP75KGUUlCnR6i5hbashrZr5lowzTBWLGDKDd")));
    }
}
