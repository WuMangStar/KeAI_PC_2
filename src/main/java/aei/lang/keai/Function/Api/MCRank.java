package aei.lang.keai.Function.Api;

import aei.lang.msg.Msg;
import aei.lang.plugin.SecPlugin;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

 class MinecraftServerPing {
    private static final int TIMEOUT = 5000; // 设置超时时间为5000毫秒
    private final String serverAddress;
    private final int port;

    public MinecraftServerPing(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
    }

    public String getServerStatus() throws IOException {
        Socket socket = new Socket();
        // 设置连接超时
        socket.connect(new InetSocketAddress(serverAddress, port), TIMEOUT);

        // 创建输出流
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());

        // 创建握手包
        ByteArrayOutputStream handshake = new ByteArrayOutputStream();
        DataOutputStream handshakeData = new DataOutputStream(handshake);
        handshakeData.writeByte(0x00); // 握手包ID
        writeVarInt(handshakeData, 763); // 协议版本（763用于MC 1.20.1）
        writeString(handshakeData, serverAddress, StandardCharsets.UTF_8); // 服务器地址
        handshakeData.writeShort(port); // 服务器端口
        writeVarInt(handshakeData, 1); // 下一个状态（1表示状态）

        // 发送握手包
        byte[] handshakePacket = createPacket(handshake.toByteArray());
        out.write(handshakePacket);

        // 发送状态请求包
        ByteArrayOutputStream request = new ByteArrayOutputStream();
        DataOutputStream requestData = new DataOutputStream(request);
        requestData.writeByte(0x00); // 状态请求包ID
        byte[] requestPacket = createPacket(request.toByteArray());
        out.write(requestPacket);

        // 读取响应
        byte[] responseData = readPacket(in);
        String jsonResponse = new String(responseData, StandardCharsets.UTF_8);

        // 发送Ping包
        ByteArrayOutputStream ping = new ByteArrayOutputStream();
        DataOutputStream pingData = new DataOutputStream(ping);
        pingData.writeByte(0x01); // Ping包ID
        pingData.writeLong(System.currentTimeMillis());
        byte[] pingPacket = createPacket(ping.toByteArray());
        out.write(pingPacket);

        // 读取Pong响应
        readPacket(in);
        socket.close();
        return jsonResponse;

    }

    private byte[] createPacket(byte[] data) throws IOException {
        ByteArrayOutputStream packet = new ByteArrayOutputStream();
        DataOutputStream packetData = new DataOutputStream(packet);
        writeVarInt(packetData, data.length);
        packetData.write(data);
        return packet.toByteArray();
    }

    private void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & 0xFFFFFF80) != 0L) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value & 0x7F);
    }

    private void writeString(DataOutputStream out, String value, java.nio.charset.Charset charset) throws IOException {
        byte[] bytes = value.getBytes(charset);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private byte[] readPacket(DataInputStream in) throws IOException {
        int length = readVarInt(in);
        byte[] data = new byte[length];
        in.readFully(data);
        return data;
    }

    private int readVarInt(DataInputStream in) throws IOException {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = in.readByte();
            int value = (read & 0x7F);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt太大了");
            }
        } while ((read & 0x80) != 0);

        return result;
    }
}


public class MCRank {
    private final Connection conn;
    private Map<String, Long> onlineTime = new HashMap<>();
    private int onlineMax;
    private int online;

    public MCRank(Connection conn, SecPlugin plugin) {
        this.conn = conn;
        ScheduledExecutorService service = Executors
                .newSingleThreadScheduledExecutor();
        Set<String> setB = new HashSet<>();
        service.scheduleAtFixedRate(() -> {
            try {
                MinecraftServerPing ping = new MinecraftServerPing("s6.yzrilyzr.top", 25565);
                String status = ping.getServerStatus();
                JSONObject statusJson = JSON.parseObject(status.substring(3));
                JSONObject players = statusJson.getJSONObject("players");
                onlineMax = players.getIntValue("max");
                online = players.getIntValue("online");
                JSONArray sampleNew = players.getJSONArray("sample");
                Set<String> setA = new HashSet<>();
                if (sampleNew != null) {
                    setA.addAll(sampleNew.toJavaList(String.class));
                }
                StringBuilder outMsg = new StringBuilder();
                Set<String> playersNew = new HashSet<>(setA);
                playersNew.removeAll(setB);
                if (!playersNew.isEmpty()) {
                    for (String player : playersNew) {
                        String playerName = JSON.parseObject(player).getString("name");
                        outMsg.append("\n").append(playerName).append(" 加入服务器");
                        onlineTime.put(playerName, System.currentTimeMillis());
                        setData(playerName, 0);
                    }
                }
                Set<String> playersOld = new HashSet<>(setB);
                playersOld.removeAll(setA);
                if (!playersOld.isEmpty()) {
                    for (String player : playersOld) {
                        String playerName = JSON.parseObject(player).getString("name");
                        long onlineEnd = (System.currentTimeMillis() - onlineTime.get(playerName));
                        outMsg.append("\n").append(playerName).append(" 退出服务器 在线：").append(msToTime(onlineEnd));
                        setData(playerName, onlineEnd);
                        onlineTime.remove(playerName);
                    }
                }
                if (!outMsg.isEmpty()) {
                    plugin.sendMessenger(msg -> {
                        msg.addMsg(Msg.Account, "3153208536");
                        msg.addMsg(Msg.Group);
                        msg.addMsg(Msg.GroupId, "465267302");
                        msg.addMsg(Msg.Text, online + "/" + onlineMax);
                        msg.addMsg(Msg.Text, outMsg);
                        //懒得拓展，写死了
                    });
                }
                setB.clear();
                setB.addAll(setA);
            } catch (Exception e) {
                try {
                    System.out.println("无法链接到服务器，30s后重试 Time:" + LocalDateTime.now());
                    Thread.sleep(30000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);
    }

    public String ping() {
        if (onlineMax==0){
            return "无法链接到服务器";
        }
        StringBuilder outMsg = new StringBuilder();
        outMsg.append(online).append("/").append(onlineMax);
        for (String key : onlineTime.keySet()) {
            outMsg.append("\n").append(key);
        }
        return outMsg.toString();
    }

    public String getRanking() throws SQLException, IOException {
        String sql = "select Player,OnlineTime,EndTime  from onlinetime order by OnlineTime desc limit 10";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rt = ps.executeQuery();
        StringBuilder builder = new StringBuilder();
        builder.append("Online Time Rankings\n");
        int i = 1;
        while (rt.next()) {
            long EndTime = rt.getTimestamp("EndTime").getTime();
            if (EndTime + 259200000 > System.currentTimeMillis()) {
                String playerName = rt.getString("Player");
                builder.append("\n").append("No.").append(i);
                builder.append("\n").append("玩家：").append(playerName);
                builder.append("\n").append("在线时长：").append(msToTime(rt.getLong("OnlineTime")));
                builder.append("\n").append("最后上线：");
                if (onlineTime.containsKey(playerName)) {
                    builder.append("当前在线");
                } else {
                    builder.append(rt.getTimestamp("EndTime"));
                }
                builder.append("\n");
                i++;
            }
        }
        if (i == 1) {
            builder.append("\n").append("暂无");
        }
        return builder.toString();
    }

    public void setData(String Player, long OnlineTime) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("select * from onlinetime where Player = ?");
        ps.setString(1, Player);
        ResultSet rt = ps.executeQuery();
        if (!rt.next()) {
            String sql = "insert into onlinetime values(?,?,?)";
            PreparedStatement ps1 = conn.prepareStatement(sql);
            ps1.setString(1, Player);
            ps1.setLong(2, OnlineTime);
            ps1.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps1.executeUpdate();
            return;
        }
        PreparedStatement ps2 = conn.prepareStatement("update onlinetime set OnlineTime = OnlineTime+?,EndTime=?  where Player = ?");
        ps2.setLong(1, OnlineTime);
        ps2.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        ps2.setString(3, Player);
        ps2.executeUpdate();
    }

    public static String msToTime(long time) {
        long day = time / 1000 / 60 / 60 / 24;
        long hour = time / 1000 / 60 / 60 % 24;
        long minute = time / 1000 / 60 % 60;
        long second = time / 1000 % 60;
        long secondMilli = time % 1000;
        StringBuilder sb = new StringBuilder();
        if (day > 0) {
            sb.append(day).append("day ");
        }
        if (hour > 0) {
            sb.append(hour).append("h ");
        }
        if (minute > 0) {
            sb.append(minute).append("min ");
        }
        if (second > 0) {
            sb.append(second).append("s ");
        }
        if (secondMilli > 0) {
            sb.append(secondMilli).append("ms");
        }
        return sb.toString();
    }

}
