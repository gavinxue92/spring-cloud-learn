package com.rpc.nio;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
// 服务端（Netty Server）
public class MyProtoServer {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            // 先解码、后编码
                            p.addLast(new MyProtoDecoder());
                            p.addLast(new MyProtoEncoder());
                            p.addLast(new SimpleChannelInboundHandler<MyMessage>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, MyMessage msg) {
                                    System.out.println("服务端收到消息：" + msg.getBody());
                                    // 构造响应，msgType=1
                                    MyMessage resp = new MyMessage((byte)1, "服务已收到：" + msg.getBody());
                                    ctx.writeAndFlush(resp);
                                }
                            });
                        }
                    });
            ChannelFuture f = b.bind(8888).sync();
            System.out.println("服务端启动，端口8888");
            f.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
