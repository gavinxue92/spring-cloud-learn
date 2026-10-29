package com.rpc.nio;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
// 客户端（Netty Client）
public class MyProtoClient {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new MyProtoDecoder());
                            p.addLast(new MyProtoEncoder());
                            p.addLast(new SimpleChannelInboundHandler<MyMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, MyMessage msg) {
                                    System.out.println("客户端收到响应：" + msg.getBody());
                                }
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    // 连接成功，发请求 msgType=0
                                    MyMessage req = new MyMessage((byte)0, "hello 我的私有协议");
                                    ctx.writeAndFlush(req);
                                }
                            });
                        }
                    });
            ChannelFuture f = b.connect("127.0.0.1",8888).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
