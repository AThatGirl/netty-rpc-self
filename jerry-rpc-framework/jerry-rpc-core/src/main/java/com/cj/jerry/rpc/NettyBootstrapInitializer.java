package com.cj.jerry.rpc;

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
                .handler(new ChannelInitializer<>() {

                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg) throws Exception {
                                //从全局挂起的请求中寻找与之匹配待处理的cf
                                String result = msg.toString(Charset.defaultCharset());
                                log.info("客户端收到消息：{}", result);
                                CompletableFuture<Object> completableFuture = JerryRpcBootstrap.PENDING_QUESTIONS.get(1L);
                                completableFuture.complete(result);
                            }
                        });
                    }

                });
    }
    private NettyBootstrapInitializer() {

    }

    public static Bootstrap getBootstrap() {
        return bootstrap;
    }

}
