package com.rpc.nio;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import java.util.List;
// 解码器（入栈：ByteBuf 字节流 → 按协议解析成 MyMessage）
public class MyProtoDecoder extends ReplayingDecoder<MyMessage> {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 严格按协议顺序读取
        short magic = in.readShort();
        if (magic != MyMessage.MAGIC) {
            throw new RuntimeException("非法协议，魔数不对");
        }
        byte version = in.readByte();
        if (version != MyMessage.VERSION) {
            throw new RuntimeException("版本不匹配");
        }
        byte msgType = in.readByte();
        int bodyLen = in.readInt();
        byte[] body = new byte[bodyLen];
        in.readBytes(body);
        String content = new String(body);
        out.add(new MyMessage(msgType, content));
    }
}
