package com.rpc.nio;

public class MyMessage {
    // 包头
    public static final short MAGIC = (short) 0xCAFE;
    public static final byte VERSION = 0x01;

    private byte msgType; // 0 请求 1响应
    private String body;

    public MyMessage(byte msgType, String body) {
        this.msgType = msgType;
        this.body = body;
    }

    // getter setter
    public byte getMsgType() { return msgType; }
    public String getBody() { return body; }
}
