package aei.lang.keai;

import aei.lang.keai.MySQL.SQLConn;
import aei.lang.msg.Messenger;
import aei.lang.msg.Msg;
import aei.lang.plugin.SecPlugin;
import aei.lang.plugin.SecPluginHandler;

import java.sql.Connection;
import java.util.HashMap;

public class StartBot implements SecPluginHandler {
    private final Connection sqlConnection;

    public static HashMap<String, Integer> Sp;

    StartBot(){
        sqlConnection=new SQLConn().getConnection();
    }

    public static void main(String[] args) {
        SecPlugin plugin = new SecPlugin("ws://127.0.0.1:24804");
        plugin.setPid("aei.lang.keai");//插件别名 唯一 id
        plugin.setName("KeAI");//插件名字
        plugin.setToken("SecretToken");//连接口令
        plugin.setHandler(new StartBot());//设置消息处理对象
        plugin.setDebug(false);//设置调试模式
        plugin.start();//启动插件 新建一个线程

    }

    @Override
    public void onMsgError(SecPlugin api, Exception e) {

    }

    @Override
    public void onMsgHandler(SecPlugin api, Messenger messenger) {
        if (messenger.hasMsg(Msg.Group)&&messenger.hasMsg(Msg.Text)) {//群聊消息
            new GroupMsg(api, messenger,sqlConnection);



        } else if (messenger.hasMsg(Msg.System)) {//系统消息
            if (messenger.hasMsg(Msg.GroupNotify)) {//群通知
                if (messenger.getLong(Msg.Account) == messenger.getLong(Msg.Uin)) {//别人邀请我进群
                    api.printI(api.sendMessenger(messenger.addMsg(Msg.Agree)).toString());////Msg.Agree同意 Msg.Refuse拒绝 Msg.Ignore忽略
                }
            }
        }
    }
    public Connection getSqlConnection() {
        return sqlConnection;
    }
}
