package com.cj.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

//收到的，就是入栈处理器
public class MyChannelHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("服务端已经收到了消息：" + byteBuf.toString(StandardCharsets.UTF_8));
        //给客户端回一下
        ctx.channel().writeAndFlush(Unpooled.copiedBuffer("hello client".getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //出现异常后执行的动作
        cause.printStackTrace();
        ctx.close();
    }
}
