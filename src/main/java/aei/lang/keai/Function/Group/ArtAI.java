package aei.lang.keai.Function.Group;

import aei.lang.keai.Function.Api.ArtAIAPI;
import aei.lang.keai.Function.Api.ChatAIAPI;
import aei.lang.keai.Function.FunctionI;
import aei.lang.keai.Utils.FileUtils;
import aei.lang.keai.Utils.GroupMsgUtils;
import aei.lang.msg.Messenger;
import aei.lang.plugin.SecPlugin;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;

import static aei.lang.keai.StartBot.Sp;

public class ArtAI extends GroupMsgUtils implements FunctionI {
    final int ART_MOD_FLAG = 2;
    @Override
    public String getName() {
        return "AI画图";
    }

    @Override
    public void init(SecPlugin api, Messenger messenger, Connection conn) throws Exception {
        QQBotInit(api, messenger);
        switch (textmsg) {
            case "绘画模型":
                send("使用画开头或draw开始绘画\n\n" +
                        "解除  解除NSFW过滤器\n" +
                        "封锁  启用NSFW过滤器");
                return;
            case "风格":
                Sp.put(uin, ART_MOD_FLAG);
                File file = new File("src/main/resources/dream_art_imageStyle_all.json");
                InputStream in = new FileInputStream(file);
                JSONArray jsondata = JSON.parseArray(in);
                StringBuilder outMsg = new StringBuilder();
                AtomicInteger i = new AtomicInteger(1);
                jsondata.forEach(json -> {
                    JSONObject data = (JSONObject) json;
                    outMsg.append(i).append(":").append(data.getJSONObject("prompt").getString("zh-Hans")).append("\n");
                    i.getAndIncrement();
                });
                outMsg.append("\n").append("输入序号选择");
                send(outMsg.toString());
                return;
            case "解除":
                if (uin.equals("2915372048")) {
                    Sp.put(groupid, 1);
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
            String trimText = textmsg.trim();
            if (trimText.equals("画") || trimText.equals("draw") || trimText.equals("画画")) return;
            String text = textmsg.startsWith("画") ? textmsg.substring(1) : textmsg.substring(4);
            ChatAIAPI AI = new ChatAIAPI();
            if (!groupid.equals("465267302") || Sp.containsKey(groupid)) {
                if (!AI.easyGPT("作为提示词检测师，你的任务是审核提示词内容。请判断提示词中的内容是否包含不当内容（NSFW），例如：屁股，容易露出人物肉体的词汇，例如保鲜膜，以及不合理的内容 如四条腿。如果包含，请只返回“违规”；如果不包含，请只返回“合法”。提示词如下：" + text, "gpt-4o").startsWith("合法")) {
                    send("内容违规:NSFW");
                    return;
                }
            }
            char chr = text.trim().charAt(0);
            if (chr >= 0x4e00 && chr <= 0x9fa5) {
                text = AI.translate(text);
            }
            File file = new File("ArtStyle/"+uin+".json");
            JSONObject data;
            if (file.exists()) {
                InputStream out = new FileInputStream(file);
               data = JSON.parseObject(out);
            }else{
                File styall = new File("src/main/resources/dream_art_imageStyle_all.json");
                InputStream in = new FileInputStream(styall);
                JSONArray jsondata = JSON.parseArray(in);
               data= jsondata.getJSONObject(33);
            }
            if (!configTask("{\n" +
                    "  \"adetailer\": \"{\\\"ad_confidence\\\":0.1,\\\"ad_denoising_strength\\\":0.45,\\\"ad_inpaint_height\\\":768,\\\"ad_inpaint_width\\\":768,\\\"ad_mask_max_ratio\\\":0.08,\\\"ad_model\\\":\\\"face_yolov8s.pt\\\",\\\"ad_negative_prompt\\\":\\\"((poorly drawn face)), paintings,  (worst quality:2), (low quality:2), lowres, ((extra limbs)), cloned face, (((disfigured))), ugly\\\",\\\"ad_prompt\\\":\\\"${userInputPrompt}\\\",\\\"ad_steps\\\":20,\\\"ad_use_steps\\\":true,\\\"enable\\\":true}\",\n" +
                    "  \"adetailerUse\": 1,\n" +
                    "  \"blockedWords\": [],\n" +
                    "  \"cfgScale\": "+data.getFloat("cfgScale")+",\n" +
                    "  \"classificationInfo\": null,\n" +
                    "  \"clipSkip\": "+data.getString("clipSkip")+",\n" +
                    "  \"consumeCount\": 1,\n" +
                    "  \"controlGuessMode\": \"Balanced\",\n" +
                    "  \"controlNetParams\": \"{\\\"params\\\":[{\\\"control_mode\\\":\\\"Balanced\\\",\\\"guidance_end\\\":1.0,\\\"guidance_start\\\":0.0,\\\"image\\\":\\\"\\\",\\\"model\\\":\\\"xinsir-controlnet-union-sdxl-1.0_promax [9460e4db]\\\",\\\"module\\\":\\\"none\\\",\\\"union_control_type\\\":\\\"Unknown\\\",\\\"weight\\\":0.6}],\\\"version\\\":\\\"1\\\"}\",\n" +
                    "  \"ct\": 1,\n" +
                    "  \"denoising\": 0.6,\n" +
                    "  \"extraArgs\": \"{\\\"canvas_resolution\\\":[864,1152],\\\"enable_hr\\\":true,\\\"hr_denoising\\\":0.3,\\\"hr_scale\\\":1.5,\\\"hr_upscaler\\\":\\\"R-ESRGAN 4x+\\\",\\\"sampler_name\\\":\\\"Euler a\\\",\\\"subseed\\\":-1,\\\"version\\\":1}\",\n" +
                    "  \"faceEditor\": false,\n" +
                    "  \"faceEditorConfidence\": 0.97,\n" +
                    "  \"faceEditorMargin\": 1.6,\n" +
                    "  \"faceEditorPrompt\": null,\n" +
                    "  \"faceEditorStrength\": 0.15,\n" +
                    "  \"gpd\": [],\n" +
                    "  \"imgTagUrl\": null,\n" +
                    "  \"iter\": "+data.getInteger("samplingStep")+",\n" +
                    "  \"locale\": \"zh-CN_CN\",\n" +
                    "  \"mode\": "+data.getInteger("mode")+ ",\n" +
                    "  \"np\": \""+data.getString("negativePrompt")+"\",\n" +
                    "  \"pattern\": 0,\n" +
                    "  \"pf\": 2,\n" +
                    "  \"proCardType\": 1,\n" +
                    "  \"prompt\": \"" + Base64.getEncoder().encodeToString((data.getString("preText")+","+text + ","+data.getString("text")+",<lora:gzy_detailer_xl:0.000000>").getBytes(StandardCharsets.UTF_8)) + "\",\n" +
                    "  \"promptImg\": null,\n" +
                    "  \"promptStyle\": \""+data.getString("text")+"\",\n" +
                    "  \"ratio\": -1,\n" +
                    "  \"restoreFaces\": -1,\n" +
                    "  \"sceneIdList\": [\n" +
                    "    11\n" +
                    "  ],\n" +
                    "  \"seed\": -1,\n" +
                    "  \"style\": \""+data.getString("stylesId")+"\",\n" +
                    "  \"subSeedStrength\": 0.0,\n" +
                    "  \"taggerThreshold\": 0.5,\n" +
                    "  \"tileMode\": \"\",\n" +
                    "  \"uuid\": \"qwb1026@gmail.com\",\n" +
                    "  \"vt\": 1,\n" +
                    "  \"controlMode\": -1,\n" +
                    "  \"controlWeight\": 1.0,\n" +
                    "  \"ef\": 0,\n" +
                    "  \"nbt\": 0.4,\n" +
                    "  \"sampler\": -1,\n" +
                    "  \"try\": 2\n" +
                    "}")){
                send("绘画超时，请重试");
            };
            return;
        }

        if (textmsg.startsWith("自定义")) {
            if (!groupid.equals("465267302") || Sp.containsKey(groupid)) return;
            String text = textmsg.substring(3);
            configTask(text);
        }
        if (textmsg.matches("鉴权.*~.*")) {
            String text = textmsg.substring(2);
            ChatAIAPI ai = new ChatAIAPI();
            String time = text.substring(0, text.indexOf("~"));
            String date = text.substring(text.indexOf("~") + 1);
            send("Bearer " + ai.be() + "." + ai.af("POST", "/chats/stream", time, date));
        }
        if (textmsg.matches("\\d{1,2}")) {
            int i = Integer.parseInt(textmsg);
            if (i==0) return;
            if (!Sp.containsKey(uin)) return;
            File file = new File("src/main/resources/dream_art_imageStyle_all.json");
            InputStream in = new FileInputStream(file);
            JSONArray jsondata = JSON.parseArray(in);
           JSONObject json= jsondata.getJSONObject(i-1);
           send("风格介绍" +
                   "\n风格："+json.getJSONObject("prompt").getString("zh-Hans")+
                   "\n说明："+(json.containsKey("descMessage")?json.getJSONObject("descMessage").getString("zh-Hans"):"暂无"));

            FileUtils.createCacheDirectory("ArtStyle");
           OutputStream out = new FileOutputStream("ArtStyle/"+uin+".json");
           out.write(json.toString().getBytes(StandardCharsets.UTF_8));
           out.close();
           Sp.remove(uin);
        }
    }

    private boolean configTask(String config) throws GeneralSecurityException, InterruptedException {
        ArtAIAPI ai = new ArtAIAPI();
        JSONObject jsondata = JSON.parseObject(ai.artRequset("und-textart/ainfer", config));
        String taskKey = jsondata.getString("key");
        System.out.println("任务开始：" + taskKey);
        Thread.sleep(3000);
        for (int i = 0; i < 27; i++) {
            Thread.sleep(777);
            JSONObject taskState = JSON.parseObject(ai.artRequset("und-textart/multiResult", "{\"key\":\"" + taskKey + "\",\"pf\":2,\"uuid\":\"qwb1026@gmail.com\",\"ver\":1,\"vt\":1}"));
            JSONObject stateJson = taskState.getJSONObject("results").getJSONObject(taskKey);
            if (stateJson.getInteger("resultCode") == 100) {
                String resultUrl = stateJson.getJSONObject("data").getString("resultUrl");
                sendImg(ai.downloadArt(resultUrl, taskKey));
                return true;
            }
        }
        return false;
    }


}
