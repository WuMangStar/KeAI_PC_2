package aei.lang.keai.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingAI {
    private final String groupId;
    private final Connection conn;

    private String Model="gpt-4";
    private String Tips;
    public final String Tips_default= "你的角色是一个聪明、有创造力的助理。不要说自己是聊天机器人或人工智能助理。交流时请考虑以下术语： 1. 您的回复长度： 自动. 2. 您讲话的语气风格： 默认. 该对话框有一个生成图像的选项。只有在用户明确提出请求时才调用该函数，例如，使用与图像生成请求相关的任何相关词语。在其他情况下，不应调用图像生成函数。 此对话框包含对网络搜索功能的调用。只有在需要获取最新数据或训练数据库中没有的数据时才使用。";

    public SettingAI(Connection conn, String groupId) throws SQLException {
        this.conn = conn;
        this.groupId = groupId;
        String sql = "select * from aisetting where GroupId = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, groupId);
        ResultSet Setting = ps.executeQuery();
        if (!Setting.next()) {
            String sqlinst = "insert into aisetting values(?,?,?)";
            PreparedStatement psinst = conn.prepareStatement(sqlinst);
            psinst.setString(1, groupId);
            psinst.setString(2, this.Model);
            psinst.setString(3, Tips_default);
            psinst.executeUpdate();
            Tips=Tips_default;
            return;
        }
            Model=Setting.getString("Model");
            Tips=Setting.getString("Tips");
    }

    public String getModel() {
        return this.Model;
    }

    public String getTips() {
        return Tips;
    }

    public void setModel(String model) throws SQLException {
        String sql = "update aisetting set Model = ? where GroupId = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, model);
        ps.setString(2, groupId);
        ps.executeUpdate();
    }

    public void setTips(String tips) throws SQLException{
        String sql = "update aisetting set Tips = ? where GroupId = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, tips);
        ps.setString(2, groupId);
        ps.executeUpdate();
    }
    public void setDefaultTips() throws SQLException{
        String sql = "update aisetting set Tips = ? where GroupId = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, Tips_default);
        ps.setString(2, groupId);
        ps.executeUpdate();
    }
}
