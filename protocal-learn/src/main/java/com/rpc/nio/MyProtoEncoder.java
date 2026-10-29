package com.rpc.nio;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

// 编码器（出栈：Java 对象 → 按照协议拼成 ByteBuf）
public class MyProtoEncoder extends MessageToByteEncoder<MyMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MyMessage msg, ByteBuf out) throws Exception {
        byte[] bodyBytes = msg.getBody().getBytes();
        // 按协议顺序写入
        out.writeShort(MyMessage.MAGIC);
        out.writeByte(MyMessage.VERSION);
        out.writeByte(msg.getMsgType());
        out.writeInt(bodyBytes.length);
        out.writeBytes(bodyBytes);
    }
}
