package com.cj.jerry.rpc.channelHandler;

import com.cj.jerry.rpc.channelHandler.handler.MySimpleChannelInvocationHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline().addLast(new MySimpleChannelInvocationHandler());
    }
}
