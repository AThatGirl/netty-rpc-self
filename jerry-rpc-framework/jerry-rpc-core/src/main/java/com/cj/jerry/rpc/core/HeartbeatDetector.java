package com.cj.jerry.rpc.core;

import com.cj.jerry.rpc.JerryRpcBootstrap;
import com.cj.jerry.rpc.NettyBootstrapInitializer;
import com.cj.jerry.rpc.compress.CompressorFactory;
import com.cj.jerry.rpc.discovery.Registry;
import com.cj.jerry.rpc.enumeration.RequestType;
import com.cj.jerry.rpc.serialize.SerializerFactory;
import com.cj.jerry.rpc.transport.message.JerryRpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class HeartbeatDetector {

    public static void detectHeartbeat(String serviceName) {
        //拉取服务
        Registry registry = JerryRpcBootstrap.getInstance().getRegistry();
        log.info("心跳检测服务：{}", serviceName);
        List<InetSocketAddress> addresses = registry.lookup(serviceName);
        for (InetSocketAddress address : addresses) {

            try {
                if (!JerryRpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    Channel channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    JerryRpcBootstrap.CHANNEL_CACHE.put(address, channel);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        //定时发送任务
        Thread thread = new Thread(() -> {
            new Timer().scheduleAtFixedRate(new MyTimerTask(), 0, 2000);
        }, "jerry-rpc-heartbeat-detector");
        thread.setDaemon(true);
        thread.start();

    }

    private static class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            //将响应时长的map清空
            JerryRpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.clear();
            //将缓存中所有的channel遍历
            Map<InetSocketAddress, Channel> channelCache = JerryRpcBootstrap.CHANNEL_CACHE;
            for (Map.Entry<InetSocketAddress, Channel> entry : channelCache.entrySet()) {


                int tryTimes = 3;
                while (tryTimes > 0) {

                    Channel channel = entry.getValue();

                    long start = System.currentTimeMillis();

                    JerryRpcRequest jerryRpcRequest = JerryRpcRequest.builder()
                            .requestId(JerryRpcBootstrap.idGenerator.getId())
                            .compressType(CompressorFactory.getCompressor(JerryRpcBootstrap.COMPRESS_TYPE).getCode())
                            .requestType(RequestType.HEART_BEAT.getId())
                            .serializeType(SerializerFactory.getSerializer(JerryRpcBootstrap.SERIALIZE_TYPE).getCode())
                            .timeStamp(start)
                            .build();
                    //写出报文
                    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                    JerryRpcBootstrap.PENDING_QUESTIONS.put(jerryRpcRequest.getRequestId(), completableFuture);
                    channel.writeAndFlush(jerryRpcRequest).addListener((ChannelFutureListener) promis -> {
                        if (promis.isSuccess()) {
                            completableFuture.complete(null);
                        } else {
                            completableFuture.completeExceptionally(promis.cause());
                        }
                    });

                    Long endTime = 0L;
                    try {
                        //get()会阻塞，方法如果回不来，会一直阻塞
                        completableFuture.get(1, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        log.error("和地址为【{}】的主机连接异常, 正在进行第【{}】次尝试", channel.remoteAddress(), 4 - tryTimes);
                        //重试次数-1
                        tryTimes--;
                        if (tryTimes == 0) {
                            //将失效的地址移除
                            JerryRpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                        }
                        try {
                            Thread.sleep(10 * (new Random().nextInt(5)));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        continue;
                    }

                    Long time = endTime - start;
                    //使用TreeMap进行缓存
                    JerryRpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(time, channel);
                    log.info("给【{}】发送心跳包，耗时：{}ms", entry.getKey(), time);
                    break;
                }
            }
        }
    }

}
