package com.cj.jerry.rpc;

import com.cj.jerry.rpc.channelHandler.handler.JerryRpcRequestDecoder;
import com.cj.jerry.rpc.channelHandler.handler.JerryRpcResponseEncoder;
import com.cj.jerry.rpc.channelHandler.handler.MethodCallHandler;
import com.cj.jerry.rpc.core.HearbeatDetector;
import com.cj.jerry.rpc.discovery.Registry;
import com.cj.jerry.rpc.discovery.RegistryConfig;
import com.cj.jerry.rpc.loadbalancer.LoadBalancer;
import com.cj.jerry.rpc.loadbalancer.impl.ConsistentHashBalancer;
import com.cj.jerry.rpc.loadbalancer.impl.RoundRobinLoadBalancer;
import com.cj.jerry.rpc.transport.message.JerryRpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class JerryRpcBootstrap {
    public static final int PORT = 8092;
    //JerryRpcBootstrap是一个单例，饿汉式
    private static final JerryRpcBootstrap jerryRpcBootstrap = new JerryRpcBootstrap();

    //定义相关的一些基础配置
    private String applicationName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    public final static IdGenerator idGenerator = new IdGenerator(1,2);
    public static String SERIALIZE_TYPE = "jdk";
    public static String COMPRESS_TYPE = "gzip";
    public static LoadBalancer LOAD_BALANCER;
    public static final ThreadLocal<JerryRpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();
    //维护一个zookeeper实例
    //private ZooKeeper zooKeeper;

    //注册中心
    private Registry registry;
    //key是interface全限定名，value是ServiceConfig
    //连接的缓存
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16); // <ip:port, channel>
    public static final TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>(); // <ip:port, channel>
    public static final Map<String, ServiceConfig<?>> SERVICE_LIST = new HashMap<>(16);
    //定义全局对外挂起的completableFuture
    public static final Map<Long, CompletableFuture<Object>> PENDING_QUESTIONS = new HashMap<>(16);

    public JerryRpcBootstrap() {

    }

    //定义当前应用的名字
    public JerryRpcBootstrap application(String appName) {
        this.applicationName = appName;
        return this;
    }

    //配置注册中心
    public JerryRpcBootstrap registry(RegistryConfig registryConfig) {
        //尝试使用工厂方法模式，registryConfig获取一个注册中心
        this.registry = registryConfig.getRegistry();
        //todo 修改
        JerryRpcBootstrap.LOAD_BALANCER = new ConsistentHashBalancer();
        return this;
    }

    //协议
    public JerryRpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        System.out.println(111);
        if (log.isDebugEnabled()) {
            log.debug("当前工程使用了:{}协议序列化", protocolConfig);
        }
        return this;
    }

    //发布服务
    public JerryRpcBootstrap publish(ServiceConfig<?> serviceConfig) {
        registry.register(serviceConfig);
        SERVICE_LIST.put(serviceConfig.getInterfaceProvider().getName(), serviceConfig);
        return this;
    }

    public JerryRpcBootstrap publish(List<?> services) {
        return this;
    }

    public void start() {
        //创建eventLoopGroup
        EventLoopGroup boss = new NioEventLoopGroup(2);
        EventLoopGroup worker = new NioEventLoopGroup(10);

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new LoggingHandler())
                                    .addLast(new JerryRpcRequestDecoder())
                                    //根据请求进行方法调用
                                    .addLast(new MethodCallHandler())
                                    .addLast(new JerryRpcResponseEncoder());
                        }
                    });
            //绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(PORT).sync();
            //等待服务端口关闭
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                worker.shutdownGracefully().sync();
                boss.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public JerryRpcBootstrap reference(ReferenceConfig<?> referenceConfig) {
        referenceConfig.setRegistryConfig(registry);
        return jerryRpcBootstrap;
    }

    public static JerryRpcBootstrap getInstance() {
        return jerryRpcBootstrap;
    }

    //序列化方式配置
    public JerryRpcBootstrap serialize(String serializeType) {
        SERIALIZE_TYPE = serializeType;
        log.info("当前工程使用了:{}序列化", SERIALIZE_TYPE);
        return this;
    }
    //压缩方式配置
    public JerryRpcBootstrap compress(String compressType) {
        COMPRESS_TYPE = compressType;
        log.info("当前工程使用了:{}压缩", COMPRESS_TYPE);
        return this;
    }

    public Registry getRegistry() {
        return registry;
    }
}
