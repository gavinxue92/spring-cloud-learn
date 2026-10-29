package com.rpc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

// RPC客户端
public class RPCClient {

    private final String host;
    private final int port;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public RPCClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        // 建立 TCP 连接（传输层三次握手）
        this.socket = new Socket(host, port);
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
    }

    public int call(String method, int a, int b) throws IOException {
        // 1. 将函数调用打包成自定义的应用层协议字符串，例如 "add:10,20"
        String requestStr = method + ":" + a + "," + b;
        byte[] reqBytes = requestStr.getBytes(StandardCharsets.UTF_8);

        // 2. 先写 4 字节的长度头，再写正文内容（解决 TCP 粘包/半包）
        dos.writeInt(reqBytes.length);
        dos.write(reqBytes);
        dos.flush();

        // 3. 阻塞读取服务端的响应长度
        int respLength = dis.readInt();
        byte[] respBytes = new byte[respLength];
        dis.readFully(respBytes);

        // 4. 解析结果
        return Integer.parseInt(new String(respBytes, StandardCharsets.UTF_8));
    }

    public void close() throws IOException {
        socket.close();
    }

    public static void main(String[] args) throws Exception {
        int port = 8888;

        // 1. 创建客户端连接
        RPCClient client = new RPCClient("127.0.0.1", port);

        // 2. 像调用本地方法一样发起远程调用！
        int res1 = client.call("add", 100, 200);
        System.out.println("[客户端] 远程调用 add(100, 200) 结果 = " + res1);

        int res2 = client.call("multiply", 6, 7);
        System.out.println("[客户端] 远程调用 multiply(6, 7) 结果 = " + res2);

        client.close();
    }
}
