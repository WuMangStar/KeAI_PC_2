package aei.lang.keai.Function.Group;

import aei.lang.keai.Function.Api.ArtAIAPI;
import aei.lang.keai.Function.Api.ChatAIAPI;
import aei.lang.keai.Function.FunctionI;
import aei.lang.keai.Utils.GroupMsgUtils;
import aei.lang.msg.Messenger;
import aei.lang.plugin.SecPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class BaseReply extends GroupMsgUtils implements FunctionI {

    @Override
    public String getName() {
        return "基础回复";
    }

    @Override
    public void init(SecPlugin api, Messenger messenger, Connection conn) throws IOException, GeneralSecurityException {
        QQBotInit(api, messenger);
        switch (textmsg) {
            case "死了没":
                send("我在");
                return;
            case "菜单":
            case "功能":
                send("聊天模型\n" +
                        "绘画模型\n" +
                        "我的世界");
                return;
        }

    }

}
