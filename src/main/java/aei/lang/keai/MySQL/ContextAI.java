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
    private String msgid;
    private int Dbid;
    private Boolean adding = false;
    private List<String> imgList;


    public ContextAI(Connection conn, String MsgId, String groupId) throws SQLException {
        this.conn = conn;
        this.groupId = groupId;
        this.msgid = MsgId;
        String sql = """
                select * from (select a.MsgId,User, AI,time
                               from usercontext a
                            inner join aicontext b on a.GroupId =b.GroupId and  a.MsgId = b.MsgId
                               where a.GroupId = ?
                               ORDER BY time DESC limit 35) as MIUA order by time;
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, groupId);
        this.Context = ps.executeQuery();
    }

    public String showTable(ResultSet rt) {
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
                "insert into usercontext (GroupId, MsgId, User,time) values (?, ?, ?,?)");
        ps.setString(1, groupId);
        ps.setString(2, msgid);
        ps.setString(3, User);
        ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
        ps.executeUpdate();
    }

    public void setImgList(List<String> imgList) {
        this.imgList = imgList;
    }

    public void setAI(String AI) throws SQLException {
        if (adding) {
            PreparedStatement ps = conn.prepareStatement(
                    "update aicontext set AI=CONCAT(AI,?) where GroupId=? and MsgId=?");
            ps.setString(1, AI);
            ps.setString(2, groupId);
            ps.setString(3, msgid);
            ps.executeUpdate();
            return;
        }
        PreparedStatement ps = conn.prepareStatement(
                "insert into aicontext (GroupId, MsgId, AI) values (?, ?, ?)");
        ps.setString(1, groupId);
        ps.setString(2, msgid);
        ps.setString(3, AI);
        ps.executeUpdate();
        adding = true;
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


    public JSONArray getContext() {
        JSONArray context = new JSONArray();
        JSONObject jsonTipsmr = new JSONObject();
        jsonTipsmr.put("content", Tips);
        jsonTipsmr.put("role", "system");
        context.add(jsonTipsmr);
/*
        JSONObject jsonTipsmr = new JSONObject();
        jsonTipsmr.put("content", "你的角色是一个聪明、有创造力的助理。不要说自己是聊天机器人或人工智能助理。交流时请考虑以下术语： 1. 您的回复长度： 自动. 2. 您讲话的语气风格： 默认. 该对话框有一个生成图像的选项。只有在用户明确提出请求时才调用该函数，例如，使用与图像生成请求相关的任何相关词语。在其他情况下，不应调用图像生成函数。该聊天你能进行联网搜索。在遇到不确定的内容时调用该函数，例如，实时内容或者不在你认知范围的内容。在这种情况下，必须调用联网搜索函数。 该聊天你能查看图像。你需要阅读分析用户的图片，给出用户需要的内容，例如，询问图片里是什么内容，如果不知道请使用联网搜索。");
        jsonTipsmr.put("role", "system");
        context.add(jsonTipsmr);

        JSONObject jsonTips = new JSONObject();
        jsonTips.put("content", Tips);
        jsonTips.put("role", "user");
        context.add(jsonTips);

        JSONObject jsonTipsRy = new JSONObject();
        jsonTipsRy.put("content", "好的，我会遵守以上规则。");
        jsonTipsRy.put("role", "assistant");
        context.add(jsonTipsRy);*/

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
            JSONArray images = new JSONArray();
            for (String url : imgList) {
                JSONObject Img = new JSONObject();
                Img.put("data", url);
                images.add(Img);
            }
            jsonMessg.put("images", images);
        }
        context.add(jsonMessg);

        return context;
    }
}
