package aei.lang.keai.MySQL;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.sql.*;
import java.util.List;

public class ContextAI {
    private final Connection conn;
    private final ResultSet Context;
    private final String groupId;
    private String Tips;
    private String User;
    private int msgid;
    private int Dbid;
    private Boolean adding=false;
    private List<String> imgList;


    public ContextAI(Connection conn, String MsgId, String groupId) throws SQLException {
        this.conn = conn;
        this.groupId = groupId;
        this.msgid = Integer.parseInt(MsgId);
        String sql = """
                select * from (select a.MsgId,User, AI
                from usercontext a
                         inner join aicontext b on a.GroupId =b.GroupId and  a.MsgId = b.MsgId
                where a.GroupId = ?
                ORDER BY a.MsgId DESC limit 35) as MIUA order by MsgId;
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, groupId);
        this.Context = ps.executeQuery();
    }

    public String showTable(ResultSet rt) throws SQLException {
        StringBuilder sb = new StringBuilder();
        try {
            if (rt != null) {
                ResultSetMetaData metaData = rt.getMetaData();
                int columnCount = metaData.getColumnCount();

                // 打印列名
                for (int i = 1; i <= columnCount; i++) {
                    sb.append(metaData.getColumnName(i));
                    if (i < columnCount) {
                        sb.append(", ");
                    }
                }
                sb.append("\n");

                // 打印行数据
                while (rt.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        sb.append(rt.getString(i));
                        if (i < columnCount) {
                            sb.append(", ");
                        }
                    }
                    sb.append("\n");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return sb.toString();
    }

    public void setTips(String tips) {
        this.Tips = tips;
    }


    public void setUser(String User) throws SQLException {
        this.User = User;
        PreparedStatement ps = conn.prepareStatement(
                "insert into usercontext (GroupId, MsgId, User) values (?, ?, ?)");
        ps.setString(1, groupId);
        ps.setInt(2,msgid);
        ps.setString(3, User);
        ps.executeUpdate();
    }

    public void setImgList(List<String> imgList) throws SQLException {
        this.imgList = imgList;
    }

    public void setAI(String AI) throws SQLException {
        if (adding) {
            PreparedStatement ps = conn.prepareStatement(
                    "update aicontext set AI=CONCAT(AI,?) where GroupId=? and MsgId=?");
            ps.setString(1, AI);
            ps.setString(2, groupId);
            ps.setInt(3, msgid);
            ps.executeUpdate();
            return;
        }
        PreparedStatement ps = conn.prepareStatement(
                    "insert into aicontext (GroupId, MsgId, AI) values (?, ?, ?)");
        ps.setString(1, groupId);
        ps.setInt(2, msgid);
        ps.setString(3, AI);
        ps.executeUpdate();
        adding=true;
    }

    public void delContext() throws SQLException {
        String sql = "delete from aicontext where groupid= ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, groupId);
        ps.executeUpdate();
        String sql2 = "delete from usercontext where groupid= ?";
        PreparedStatement ps2 = conn.prepareStatement(sql2);
        ps2.setString(1, groupId);
        ps2.executeUpdate();

    }


    public JSONArray getContext() throws SQLException {
        JSONArray context = new JSONArray();
        JSONObject jsonTips = new JSONObject();
        jsonTips.put("content", Tips);
        jsonTips.put("role", "system");
        context.add(jsonTips);

        while (true) {
            try {
                if (!Context.next()) break;

                JSONObject jsonUser = new JSONObject();
                jsonUser.put("content", Context.getString("User"));
                jsonUser.put("role", "user");
                context.add(jsonUser);

                JSONObject jsonAI = new JSONObject();
                jsonAI.put("content", Context.getString("AI"));
                jsonAI.put("role", "assistant");
                context.add(jsonAI);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        JSONObject jsonMessg = new JSONObject();
        jsonMessg.put("content", User);
        jsonMessg.put("role", "user");
        if (imgList != null && !imgList.isEmpty()) {
            JSONArray images=new JSONArray();
            for (String url : imgList) {
                JSONObject Img = new JSONObject();
                Img.put("data", url);
                images.add(Img);
            }
            jsonMessg.put("images",images);
        }
        context.add(jsonMessg);

        return context;
    }
}
