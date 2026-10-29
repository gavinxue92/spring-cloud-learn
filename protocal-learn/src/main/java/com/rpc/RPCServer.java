package com.rpc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

// RPC服务端
public class RPCServer {

    private final int port;
    private boolean running = true;

    public RPCServer (int port) {
        this.port = port;
    }

    public void start() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("[服务端] 启动成功，正在监听端口 " + port + "...");
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    // 每一个客户端连接开一个线程处理
                    new Thread(() -> handleClient(clientSocket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleClient(Socket socket) {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            while (true) {
                // 【网络层协议第一步】：读取前 4 个字节，获取接下来要接收的数据长度
                int bodyLength;
                try {
                    bodyLength = dis.readInt();
                } catch (EOFException e) {
                    break; // 客户端断开连接
                }

                // 【网络层协议第二步】：根据长度精准读取对应大小的字节并转成字符串
                byte[] bodyBytes = new byte[bodyLength];
                dis.readFully(bodyBytes);
                String requestStr = new String(bodyBytes, StandardCharsets.UTF_8);

                System.out.println("[服务端] 收到原始请求报文: " + requestStr);

                // 解析我们简易的协议格式: method:args (例如 "add:10,20")
                String[] parts = requestStr.split(":");
                String method = parts[0];
                String[] args = parts[1].split(",");

                int result = 0;
                if ("add".equals(method)) {
                    result = Integer.parseInt(args[0]) + Integer.parseInt(args[1]);
                } else if ("multiply".equals(method)) {
                    result = Integer.parseInt(args[0]) * Integer.parseInt(args[1]);
                }

                // 构造响应报文
                String responseStr = String.valueOf(result);
                byte[] respBytes = responseStr.getBytes(StandardCharsets.UTF_8);

                // 写回客户端：[4字节长度] + [响应正文]
                dos.writeInt(respBytes.length);
                dos.write(respBytes);
                dos.flush();
            }
        } catch (IOException e) {
            // 异常断开
        }
    }

    public static void main(String[] args) {
        int port = 8888;
        RPCServer server = new RPCServer(port);
        server.start();
    }
}
