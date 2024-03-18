package com.cj.jerry.rpc.channelHandler;

import com.cj.jerry.rpc.channelHandler.handler.JerryRpcRequestEncoder;
import com.cj.jerry.rpc.channelHandler.handler.JerryRpcResponseDecoder;
import com.cj.jerry.rpc.channelHandler.handler.MySimpleChannelInvocationHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        //netty自带处理器
        channel.pipeline()
                //日志处理
                .addLast(new LoggingHandler())
                //消息编码
                .addLast(new JerryRpcRequestEncoder())
                //入栈解码器
                .addLast(new JerryRpcResponseDecoder())
                .addLast(new MySimpleChannelInvocationHandler());
    }
}
