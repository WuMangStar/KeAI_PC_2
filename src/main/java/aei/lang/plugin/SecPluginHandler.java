package aei.lang.plugin;


import aei.lang.msg.Messenger;

/**
 * @Author MCSQNXA
 * @CreateTime 2024-08-04 下午11:10
 * @Description
 */
public interface SecPluginHandler {




    /**
     * @CreateTime 2024-08-04 23:28:46
     * @Description 错误消息
     */
    void onMsgError(SecPlugin api, Exception e);

    /**
     * @CreateTime 2024-08-04 23:21:16
     * @Description 收到消息
     */
    void onMsgHandler(SecPlugin api, Messenger messenger);

}
