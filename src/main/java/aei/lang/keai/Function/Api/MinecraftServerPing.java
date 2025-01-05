package aei.lang.keai.Function.Api;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MinecraftServerPing {
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
        writeVarInt(handshakeData, 767); // 协议版本（763用于MC 1.20.1）
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
