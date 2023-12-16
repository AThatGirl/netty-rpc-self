package com.cj.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class AppServer {

    private int port;

    public AppServer(int port) {
        this.port = port;
    }

    public void start() {

        //创建EventLoopGroup
        //boss只负责处理请求，之后会将请求分发给worker，建议1:5
        EventLoopGroup boos = new NioEventLoopGroup(2);
        EventLoopGroup worker = new NioEventLoopGroup(10);
        try {
            //需要一个服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //配置服务器
            serverBootstrap.group(boos, worker)
                    //配置channel类型
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //服务端channel中的pipeline中加上我的处理器，客户端只要发送消息服务端就能收到
                            socketChannel.pipeline().addLast(new MyChannelHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                boos.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        new AppServer(8080).start();
    }

}
