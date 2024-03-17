package com.cj.jerry.rpc.channelHandler.handler;

import com.cj.jerry.rpc.JerryRpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MySimpleChannelInvocationHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        //从全局挂起的请求中寻找与之匹配待处理的cf
        String result = msg.toString(Charset.defaultCharset());
        log.info("客户端收到消息：{}", result);
        CompletableFuture<Object> completableFuture = JerryRpcBootstrap.PENDING_QUESTIONS.get(1L);
        completableFuture.complete(result);
    }

}
