package aei.lang.plugin;

import aei.lang.msg.Messenger;
import aei.lang.msg.Msg;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author MCSQNXA
 * @CreateTime 2024-08-04 下午11:10
 * @Description 插件
 */
@SuppressWarnings("unused")
public class SecPlugin extends WebSocketClient {


    /**
     * @CreateTime 2024-08-06 13:38:43
     * @Description 发送消息
     */
    public Messenger sendMessenger(Messenger.Builder builder) {
        return this.sendMessenger(null, builder);
    }

    /**
     * @CreateTime 2024-08-04 23:54:45
     * @Description 发送消息
     */
    public Messenger sendMessenger(Messenger patent, Messenger.Builder builder) {
        return this.sendMessenger(patent, builder, true);
    }

    /**
     * @CreateTime 2024-08-04 23:54:45
     * @Description 发送消息
     */
    public Messenger sendMessenger(Messenger patent, Messenger.Builder builder, boolean needRsp) {
        Messenger messenger = new Messenger();

        if (patent != null) {
            messenger.addMsg(Msg.Account, patent.getString(Msg.Account));

            if (patent.hasMsg(Msg.Group)) {//群聊消息
                messenger.addMsg(Msg.Group);
                messenger.addMsg(Msg.GroupId, patent.getString(Msg.GroupId));
            } else if (patent.hasMsg(Msg.Friend)) {//好友消息
                messenger.addMsg(Msg.Friend);
                messenger.addMsg(Msg.Uin, patent.getString(Msg.Uin));
            } else if (patent.hasMsg(Msg.Temp)) {//群聊 临时消息
                messenger.addMsg(Msg.Temp);
                messenger.addMsg(Msg.GroupId, patent.getString(Msg.GroupId));
                messenger.addMsg(Msg.Uin, patent.getString(Msg.Uin));
            } else if (patent.hasMsg(Msg.Guild)) {//频道消息
                messenger.addMsg(Msg.Guild);
                messenger.addMsg(Msg.GuildId, patent.getString(Msg.GuildId));
                messenger.addMsg(Msg.ChannelId, patent.getString(Msg.ChannelId));
            }

            if (patent.getLong(Msg.UserGolineMode) == Msg.GM_Official) {//官方人机 发送消息需要携带 MsgId
                messenger.addMsg(Msg.MsgId, patent.getString(Msg.MsgId));
            }
        }

        builder.build(messenger);

        if (!messenger.hasMsg(Msg.Account)) {
            throw new RuntimeException("缺少发送参数 " + Msg.Account);
        }

        return this.sendMessenger(messenger, needRsp);
    }

    /**
     * @CreateTime 2024-08-06 14:01:17
     * @Description 发送消息
     */
    public Messenger sendMessenger(Messenger messenger) {
        return this.sendMessenger(messenger, true);
    }

    /**
     * @CreateTime 2024-08-04 23:41:48
     * @Description 发送消息
     */
    public Messenger sendMessenger(Messenger messenger, boolean needRsp) {
        try {
            JSONObject object = this.sendWss("SendOicqMsg", this.toArray(messenger), needRsp);

            if (needRsp) {
                return this.toMsg(object.getJSONArray("data"));
            }
        } catch (Exception e) {
            this.handler.onMsgError(this, e);
        }

        return new Messenger();
    }

    /**
     * @CreateTime 2024-08-05 17:51:58
     * @Description 发送 WebSocket
     */
    private JSONObject sendWss(String cmd, Object data, boolean needRsp) {
        try {
            int seq = this.nextSeq();

            JSONObject json = new JSONObject();
            json.put("seq", seq);
            json.put("cmd", cmd);
            json.put("rsp", needRsp);

            if (data != null) {
                json.put("data", data);
            }

            if (this.isDebug()) {
                this.printV("[sendWss] " + json);
            }

            super.send(json.toString());//发送 Websocket 消息

            if (needRsp) {
                for (int i = 0; i < 3000; i++) {//等待应答包
                    SecPlugin.threadSleep(10);

                    synchronized (this.rsp) {
                        if (this.rsp.containsKey(seq)) {
                            JSONObject rsp = this.rsp.get(seq);
                            this.rsp.remove(seq);
                            return rsp;
                        }
                    }
                }
            }
        } catch (Exception e) {
            this.handler.onMsgError(this, e);
        }

        return new JSONObject();
    }

    /**
     * @CreateTime 2024-08-05 17:49:44
     * @Description 应答包
     */
    private final HashMap<Integer, JSONObject> rsp = new HashMap<>();

    /**
     * @CreateTime 2024-08-05 17:54:28
     * @Description 线程休眠
     */
    public static void threadSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @CreateTime 2024-08-06 08:57:11
     * @Description 当 WebSocket 连接建立时 需要进行上线 之后才会收到框架推送过来的消息
     */
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        SecPlugin.thread(() -> {
            try {
                JSONObject j = new JSONObject();
                j.put("pid", this.pid);
                j.put("name", this.name);
                j.put("token", this.token);

                JSONObject rsp = this.sendWss("SyncOicq", j, true);

                if (rsp.isEmpty()) {
                    this.handler.onMsgError(this, new Exception("SyncOicq failed."));
                } else if (rsp.getJSONObject("data").getBoolean("status")) {
                    this.handler.onMsgHandler(this, new Messenger().addMsg("SyncOicq"));
                } else {
                    this.handler.onMsgError(this, new Exception("口令错误"));
                    this.stop();
                }
            } catch (Exception e) {
                this.handler.onMsgError(this, e);
            }
        });
    }

    @Override
    public void onMessage(String s) {
        SecPlugin.thread(() -> {
            try {
                if (this.isDebug()) {
                    this.printV("[recvWss] " + s);
                }

                JSONObject json = JSONObject.parseObject(s);
                String cmd = json.getString("cmd");

                if (cmd.equals("PushOicqMsg")) {//框架 推送过来的消息
                    this.parsePushOicqMsg(json.getJSONArray("data"));
                } else if (cmd.equals("Response")) {//收到发送消息的应答包
                    this.parseResponse(json);
                }


            } catch (Exception e) {
                this.handler.onMsgError(this, e);
            }
        });
    }

    /**
     * @CreateTime 2024-08-06 12:33:11
     * @Description 连接丢失
     */
    @Override
    public void onClose(int i, String s, boolean b) {
        if (this.reconnecting) {
            return;
        }

        SecPlugin.thread(() -> {
            this.reconnecting = true;

            while (!super.isOpen()) {
                try {
                    SecPlugin.threadSleep(3000);

                    if (super.getReadyState().equals(ReadyState.NOT_YET_CONNECTED)) {
                        super.connect();
                    } else if (super.getReadyState().equals(ReadyState.CLOSED)) {
                        super.reconnect();
                    }
                } catch (Exception e) {
                    this.handler.onMsgError(this, e);
                }
            }

            this.reconnecting = false;
        });
    }

    /**
     * @CreateTime 2024-08-06 12:54:03
     * @Description 重连中
     */
    private volatile boolean reconnecting = false;


    @Override
    public void onError(Exception e) {
        if (e instanceof ConnectException) {
            this.printW("正在连接框架 " + e.getMessage());
        } else {
            this.handler.onMsgError(this, e);
        }
    }

    /**
     * @CreateTime 2024-08-06 00:11:49
     * @Description 解析
     */
    private void parsePushOicqMsg(JSONArray a) {
        this.handler.onMsgHandler(this, this.toMsg(a));
    }

    /**
     * @CreateTime 2024-08-06 09:31:18
     * @Description 转成数组
     */
    private JSONArray toArray(Messenger messenger) {
        JSONArray a = new JSONArray();

        for (int i = 0; i < messenger.getListSize(); i++) {
            JSONObject j = new JSONObject();
            Map<String, String> map = messenger.getList().get(i);

            j.putAll(map);

            a.add(j);
        }

        return a;
    }

    /**
     * @CreateTime 2024-08-06 09:29:29
     * @Description 转成消息
     */
    private Messenger toMsg(JSONArray a) {
        Messenger messenger = new Messenger();

        for (int i = 0; i < a.size(); i++) {
            JSONObject o = a.getJSONObject(i);

            for (String k : o.keySet()) {
                messenger.addMsg(k, o.getString(k));
            }
        }

        return messenger;
    }

    /**
     * @CreateTime 2024-08-06 00:12:51
     * @Description 解析
     */
    private void parseResponse(JSONObject json) {
        synchronized (this.rsp) {//线程加锁
            long time = System.currentTimeMillis();

            if (this.rsp.size() > 10) {//触发过期应答包回收机制
                ArrayList<Integer> list = new ArrayList<>();

                for (Map.Entry<Integer, JSONObject> m : this.rsp.entrySet()) {
                    if (m.getValue().getLong("expire") < time) {
                        list.add(m.getKey());
                    }
                }

                for (Integer i : list) {//释放
                    this.rsp.remove(i);
                }
            }

            //缓存新的数据包
            json.put("expire", time + 30 * 1000);//设置数据包过期时间

            this.rsp.put(json.getInteger("seq"), json);//缓存
        }
    }

    /**
     * @CreateTime 2024-08-05 17:46:16
     * @Description 推送日志
     */
    public void pushE(String log) {
        this.sendWss("PrintE", log, false);
    }

    /**
     * @CreateTime 2024-08-05 17:46:16
     * @Description 推送日志
     */
    public void pushI(String log) {
        this.sendWss("PrintI", log, false);
    }

    /**
     * @CreateTime 2024-08-05 17:46:16
     * @Description 推送日志
     */
    public void pushV(String log) {
        this.sendWss("PrintV", log, false);
    }

    /**
     * @CreateTime 2024-08-05 17:46:16
     * @Description 推送日志
     */
    public void pushW(String log) {
        this.sendWss("PrintW", log, false);
    }

    /**
     * @CreateTime 2024-08-06 14:09:30
     * @Description 上次 Ping
     */
    private volatile long last_ping = 0;

    /**
     * @CreateTime 2024-08-06 14:07:21
     * @Description 连接可用
     */
    public boolean ping() {
        if (this.last_ping > System.currentTimeMillis() - 3000) {
            return true;//3s内 不用进行 ping 测试
        }

        this.last_ping = System.currentTimeMillis();

        return !this.sendWss("Ping", null, true).isEmpty();
    }

    /**
     * @CreateTime 2024-08-04 23:55:56
     * @Description 序号
     */
    private int seq = 0;

    /**
     * @CreateTime 2024-08-06 00:10:31
     * @Description 锁
     */
    private final Object seq_lock = new Object();

    /**
     * @CreateTime 2024-08-04 23:56:09
     * @Description 序号
     */
    public int nextSeq() {
        synchronized (this.seq_lock) {
            return ++this.seq;
        }
    }

    /**
     * @CreateTime 2024-08-04 23:19:47
     * @Description 停止插件
     */
    public synchronized void stop() {
        if (!this.running) {
            return;
        }

        try {
            super.closeBlocking();
        } catch (InterruptedException e) {
            this.handler.onMsgError(this, e);
        }

        this.running = false;
    }

    /**
     * @CreateTime 2024-08-04 23:22:45
     * @Description 运行中
     */
    private volatile boolean running = false;

    /**
     * @CreateTime 2024-08-04 23:18:07
     * @Description 启动插件
     */
    public synchronized void start() {
        if (this.running) {
            return;
        }

        this.running = true;

        try {
            super.connectBlocking();
        } catch (InterruptedException e) {
            this.handler.onMsgError(this, e);
        }
    }

    /**
     * @CreateTime 2024-08-04 23:16:55
     * @Description 处理 对象
     */
    private SecPluginHandler handler;

    /**
     * @CreateTime 2024-08-04 23:17:16
     * @Description 设置 对象
     */
    public void setHandler(SecPluginHandler handler) {
        this.handler = handler;
    }

    /**
     * @CreateTime 2024-08-06 14:15:37
     * @Description 调试模式
     */
    private volatile boolean debug = false;

    /**
     * @CreateTime 2024-08-06 14:15:51
     * @Description 调试模式
     */
    public boolean isDebug() {
        return this.debug;
    }

    /**
     * @CreateTime 2024-08-06 14:16:10
     * @Description 设置
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @CreateTime 2024-08-04 23:15:12
     * @Description 口令
     */
    private String token;

    /**
     * @CreateTime 2024-08-04 23:15:25
     * @Description 设置 口令
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @CreateTime 2024-08-04 23:14:13
     * @Description 插件 别名
     */
    private String pid = "";

    /**
     * @CreateTime 2024-08-04 23:14:30
     * @Description 获取 别名
     */
    public String getPid() {
        return this.pid;
    }

    /**
     * @CreateTime 2024-08-04 23:14:43
     * @Description 设置 别名
     */
    public void setPid(String id) {
        this.pid = id;
    }

    /**
     * @CreateTime 2024-08-06 00:18:40
     * @Description 插件 名字
     */
    private String name = "";

    /**
     * @CreateTime 2024-08-06 00:19:01
     * @Description 获取名字
     */
    public String getName() {
        return this.name;
    }

    /**
     * @CreateTime 2024-08-06 00:19:15
     * @Description 设置 名字
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @CreateTime 2024-08-04 23:13:34
     * @Description 构造函数
     */
    public SecPlugin(String uri) {
        super(URI.create(uri), new Draft_6455());
        super.setDaemon(true);
    }

    /**
     * @CreateTime 2022-05-28 12:37:58
     * @Description 线程池
     */
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    /**
     * @CreateTime 2022-05-28 19:52:23
     * @Description 新建子线程
     */
    public static void thread(Runnable runnable) {
        SecPlugin.threadPool.submit(runnable);
    }

    /**
     * @CreateTime 2022-04-10 08:16:46
     * @Description 时间格式
     */
    private static final SimpleDateFormat format = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss:SSS] ", Locale.CHINA);

    /**
     * @CreateTime 2024-07-01 17:34:20
     * @Description 打印错误
     */
    public void printE(String log0) {
        System.out.println("\033[1;31m" + SecPlugin.format.format(System.currentTimeMillis()) + log0 + "\033[0m");
    }

    /**
     * @CreateTime 2024-07-01 17:46:30
     * @Description 打印信息
     */
    public void printI(String log0) {
        System.out.println("\033[1;38m" + SecPlugin.format.format(System.currentTimeMillis()) + log0 + "\033[0m");
    }

    /**
     * @CreateTime 2024-07-01 17:45:07
     * @Description 打印信息
     */
    public void printV(String log0) {
        System.out.println("\033[1;32m" + SecPlugin.format.format(System.currentTimeMillis()) + log0 + "\033[0m");
    }

    /**
     * @CreateTime 2024-07-01 17:40:59
     * @Description 打印警告
     */
    public void printW(String log0) {
        System.out.println("\033[1;33m" + SecPlugin.format.format(System.currentTimeMillis()) + log0 + "\033[0m");
    }


}
