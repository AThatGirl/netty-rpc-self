package com.cj.jerry.rpc;

import com.cj.jerry.rpc.channelHandler.ConsumerChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * 提供一个bootstrap单例
 * TODO 想对bootstrap扩展，做一些事情
 */
@Slf4j
public class NettyBootstrapInitializer {

    private static Bootstrap bootstrap = new Bootstrap();

    static {
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ConsumerChannelInitializer());
    }
    private NettyBootstrapInitializer() {

    }

    public static Bootstrap getBootstrap() {
        return bootstrap;
    }

}
