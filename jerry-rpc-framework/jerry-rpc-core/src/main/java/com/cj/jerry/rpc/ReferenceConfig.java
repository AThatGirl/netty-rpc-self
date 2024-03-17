package com.cj.jerry.rpc;

import com.cj.jerry.rpc.discovery.Registry;
import com.cj.jerry.rpc.exception.NetworkException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ReferenceConfig<T> {

    private Class<T> interfaceRef;

    private Registry registry;


    public Class<T> getInterfaceRef() {
        return interfaceRef;
    }

    public void setInterfaceRef(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        //使用动态代理完成一些工作，生成代理对象
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //
                log.info("进入invoke方法");
                //1.从注册中心获取服务
                //传入服务的名字
                InetSocketAddress address = registry.lookup(interfaceRef.getName());
                if (log.isDebugEnabled()) {
                    log.debug("服务调用方，发现了服务【{}】的可用主机【{}】", interfaceRef.getName(), address);
                }
                Channel channel = JerryRpcBootstrap.CHANNEL_MAP.get(address);
                if (channel == null) {

                    NioEventLoopGroup group = new NioEventLoopGroup();
                    Bootstrap bootstrap = new Bootstrap();
                    try {
                        bootstrap.group(group)
                                .channel(NioSocketChannel.class)
                                .handler(new ChannelInitializer<Channel>() {
                                    @Override
                                    protected void initChannel(Channel channel) throws Exception {

                                    }
                                });
                        channel = bootstrap.connect(address).sync().channel();
                        //缓存
                        JerryRpcBootstrap.CHANNEL_MAP.put(address, channel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

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
                    channel = channelFuture.get(3, TimeUnit.SECONDS);
                    //缓存
                    JerryRpcBootstrap.CHANNEL_MAP.put(address, channel);
                }
                if (channel == null) {
                    throw new NetworkException("获取通道发生异常");
                }
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
                CompletableFuture<Object> completableFuture = new CompletableFuture();
                channel.writeAndFlush(Unpooled.copiedBuffer("jerry-rpc hello".getBytes())).addListener((ChannelFutureListener) promise -> {
                    if (!promise.isSuccess()) {
                        Throwable cause = promise.cause();
                        throw new RuntimeException(cause);
                    }
                    log.info("consumer发送消息成功");
                });
//                completableFuture.get(3, TimeUnit.SECONDS)
                return null;
            }
        });
        return (T) helloProxy;
    }

    public Registry getRegistryConfig() {
        return registry;
    }

    public void setRegistryConfig(Registry registry) {
        this.registry = registry;
    }
}
