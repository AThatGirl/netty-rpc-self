package com.cj.jerry.rpc.proxy.handler;

import com.cj.jerry.rpc.JerryRpcBootstrap;
import com.cj.jerry.rpc.NettyBootstrapInitializer;
import com.cj.jerry.rpc.compress.CompressorFactory;
import com.cj.jerry.rpc.discovery.Registry;
import com.cj.jerry.rpc.enumeration.RequestType;
import com.cj.jerry.rpc.exception.NetworkException;
import com.cj.jerry.rpc.serialize.SerializerFactory;
import com.cj.jerry.rpc.transport.message.JerryRpcRequest;
import com.cj.jerry.rpc.transport.message.RequestPayload;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {

    private final Registry registry;
    private final Class<?> interfaceRef;

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("进入invoke方法");
        //封装报文
        RequestPayload requestPayload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .parameterValues(args)
                .returnType(method.getReturnType())
                .build();
        JerryRpcRequest jerryRpcRequest = JerryRpcRequest.builder()
                .requestId(JerryRpcBootstrap.idGenerator.getId())
                .compressType(CompressorFactory.getCompressor(JerryRpcBootstrap.COMPRESS_TYPE).getCode())
                .requestType(RequestType.REQUEST_TYPE.getId())
                .serializeType(SerializerFactory.getSerializer(JerryRpcBootstrap.SERIALIZE_TYPE).getCode())
                .requestPayload(requestPayload)
                .build();
        //将请求存入本地线程，在合适的时候调用remove
        JerryRpcBootstrap.REQUEST_THREAD_LOCAL.set(jerryRpcRequest);
        log.info("封装的请求完毕：{}",jerryRpcRequest);
        //1.从注册中心获取服务
        //传入服务的名字
        InetSocketAddress address = JerryRpcBootstrap.LOAD_BALANCER.selectServiceAddress(interfaceRef.getName());
        if (log.isDebugEnabled()) {
            log.debug("获取了和【{}】建立的通道准备发送数据", address);
        }
        Channel channel = getAvailableChannel(address);
        /*
                //同步策略
                ChannelFuture channelFuture = channel.writeAndFlush(Unpooled.copiedBuffer(interfaceRef.getName().getBytes(StandardCharsets.UTF_8))).await();
                if (channelFuture.isDone()) {
                    Object obj = channelFuture.getNow();
                } else if (!channelFuture.isSuccess()) {
                    Throwable cause = channelFuture.cause();
                    throw new RuntimeException(cause);
                }*/
        //异步策略
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        JerryRpcBootstrap.PENDING_QUESTIONS.put(1L, completableFuture);
        channel.writeAndFlush(jerryRpcRequest).addListener((ChannelFutureListener) promise -> {
            if (!promise.isSuccess()) {
                Throwable cause = promise.cause();
                throw new RuntimeException(cause);
            }
        });
        //清理ThreadLocal
        JerryRpcBootstrap.REQUEST_THREAD_LOCAL.remove();
        log.info("consumer发送消息成功:{}", jerryRpcRequest);
//                completableFuture.get(3, TimeUnit.SECONDS)
        //如果这里没有处理completableFuture，那么就会阻塞在这里
        return completableFuture.get(10, TimeUnit.SECONDS);
    }

    /**
     * 根据地址获取可用的channel
     *
     * @param address
     * @return
     */
    private Channel getAvailableChannel(InetSocketAddress address) {
        Channel channel = JerryRpcBootstrap.CHANNEL_CACHE.get(address);

        //如果还是没有创建好channel
        if (channel == null) {
            //await方法会等待连接成功再返回，netty还提供了异步处理逻辑
            //sync和await，都是阻塞当前线程，获取返回值（连接过程是异步的，发送数据的过程是异步的）
            //sync会在主线程抛出异常，await不会，异常在子线程中处理需要使用future处理
            //channel = NettyBootstrapInitializer.getBootstrap().connect(address).await().channel();
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap().connect(address).addListener((ChannelFutureListener) promise -> {
                if (promise.isDone()) {
                    //异步
                    channelFuture.complete(promise.channel());
                }
            });
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取通道发生异常", e);
                throw new RuntimeException(e);
            }
            //缓存
            JerryRpcBootstrap.CHANNEL_CACHE.put(address, channel);
        }
        if (channel == null) {
            throw new NetworkException("获取通道发生异常");
        }
        return channel;
    }
}