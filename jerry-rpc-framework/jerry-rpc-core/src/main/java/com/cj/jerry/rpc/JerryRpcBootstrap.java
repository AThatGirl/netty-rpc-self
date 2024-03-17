package com.cj.jerry.rpc;

import com.cj.jerry.rpc.discovery.Registry;
import com.cj.jerry.rpc.discovery.RegistryConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class JerryRpcBootstrap {
    //JerryRpcBootstrap是一个单例，饿汉式
    private static final JerryRpcBootstrap jerryRpcBootstrap = new JerryRpcBootstrap();

    //定义相关的一些基础配置
    private String applicationName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private int port = 8088;
    //维护一个zookeeper实例
    //private ZooKeeper zooKeeper;

    //注册中心
    private Registry registry;
    //key是interface全限定名，value是ServiceConfig
    //连接的缓存
    public static final Map<InetSocketAddress, Channel> CHANNEL_MAP = new ConcurrentHashMap<>(16); // <ip:port, channel>
    public static final Map<String, ServiceConfig<?>> SERVICE_LIST = new HashMap<>(16);


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
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(new SimpleChannelInboundHandler<Object>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
                                    ByteBuf byteBuf = (ByteBuf) msg;
                                    log.info("收到客户端消息：{}",byteBuf.toString(Charset.defaultCharset()));
                                    //channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer("jerry-rpc hello服务端收到".getBytes()));
                                }
                            });
                        }
                    });
            //绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
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
}
