package com.cj.jerry.rpc.channelHandler.handler;

import com.cj.jerry.rpc.JerryRpcBootstrap;
import com.cj.jerry.rpc.transport.message.JerryRpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MySimpleChannelInvocationHandler extends SimpleChannelInboundHandler<JerryRpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, JerryRpcResponse jerryRpcResponse) throws Exception {
        //从全局挂起的请求中寻找与之匹配待处理的cf
        Object result = jerryRpcResponse.getBody();
        log.info("客户端收到消息：{}", result);
        CompletableFuture<Object> completableFuture = JerryRpcBootstrap.PENDING_QUESTIONS.get(jerryRpcResponse.getRequestId());
        completableFuture.complete(result);
        log.info("已经寻找到编号为{}的completableFuture处理响应", jerryRpcResponse.getRequestId());
    }

}
