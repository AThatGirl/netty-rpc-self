package com.cj.jerry.rpc;

import com.cj.jerry.rpc.annotation.JerryRpcApi;
import com.cj.jerry.rpc.channelHandler.handler.JerryRpcRequestDecoder;
import com.cj.jerry.rpc.channelHandler.handler.JerryRpcResponseEncoder;
import com.cj.jerry.rpc.channelHandler.handler.MethodCallHandler;
import com.cj.jerry.rpc.core.HeartbeatDetector;
import com.cj.jerry.rpc.discovery.Registry;
import com.cj.jerry.rpc.discovery.RegistryConfig;
import com.cj.jerry.rpc.loadbalancer.LoadBalancer;
import com.cj.jerry.rpc.transport.message.JerryRpcRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
public class JerryRpcBootstrap {

    private Configuration configuration;
    //JerryRpcBootstrap是一个单例，饿汉式
    private static final JerryRpcBootstrap jerryRpcBootstrap = new JerryRpcBootstrap();
    public static final ThreadLocal<JerryRpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    //连接的缓存
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16); // <ip:port, channel>
    public static final TreeMap<Long, Channel> ANSWER_TIME_CHANNEL_CACHE = new TreeMap<>(); // <ip:port, channel>
    public static final Map<String, ServiceConfig<?>> SERVICE_LIST = new HashMap<>(16);
    //定义全局对外挂起的completableFuture
    public static final Map<Long, CompletableFuture<Object>> PENDING_QUESTIONS = new HashMap<>(16);

    public JerryRpcBootstrap() {
        configuration = new Configuration();
    }

    //定义当前应用的名字
    public JerryRpcBootstrap application(String appName) {
        configuration.setApplicationName(appName);
        return this;
    }

    //配置注册中心
    public JerryRpcBootstrap registry(RegistryConfig registryConfig) {
        configuration.setRegistryConfig(registryConfig);
        log.debug("当前工程使用了:{}注册中心", registryConfig);
        return this;
    }

    public JerryRpcBootstrap loadBalancer(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
        log.debug("当前工程使用了:{}负载均衡策略", loadBalancer);
        return this;
    }

    //协议
    public JerryRpcBootstrap protocol(ProtocolConfig protocolConfig) {
        configuration.setProtocolConfig(protocolConfig);
        if (log.isDebugEnabled()) {
            log.debug("当前工程使用了:{}协议序列化", protocolConfig);
        }
        return this;
    }

    //发布服务
    public JerryRpcBootstrap publish(ServiceConfig<?> serviceConfig) {
        configuration.getRegistryConfig().getRegistry().register(serviceConfig);
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
            ChannelFuture channelFuture = serverBootstrap.bind(configuration.getPort()).sync();
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
        //开启服务心跳检测
        HeartbeatDetector.detectHeartbeat(referenceConfig.getInterfaceRef().getName());
        referenceConfig.setRegistryConfig(configuration.getRegistryConfig().getRegistry());
        return jerryRpcBootstrap;
    }

    public static JerryRpcBootstrap getInstance() {
        return jerryRpcBootstrap;
    }

    //序列化方式配置
    public JerryRpcBootstrap serialize(String serializeType) {
        configuration.setSerializeType(serializeType);
        log.info("当前工程使用了:{}序列化", serializeType);
        return this;
    }

    //压缩方式配置
    public JerryRpcBootstrap compress(String compressType) {
        configuration.setCompressType(compressType);
        log.info("当前工程使用了:{}压缩", compressType);
        return this;
    }

    public Registry getRegistry() {
        return configuration.getRegistryConfig().getRegistry();
    }

    public JerryRpcBootstrap scan(String packageName) {
        //需要通过packageName获取其下所有的类的全限定名称
        List<String> classNames = getAllClassNames(packageName);
        //通过反射获取他的接口构建具体实现
        List<Class<?>> classes = classNames.stream().map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(clazz -> clazz.getAnnotation(JerryRpcApi.class) != null)
                .collect(Collectors.toList());
        log.info("扫描到{}个接口：{}", classes.size(), classes);
        for (Class<?> aClass : classes) {
            //获取接口
            Class<?>[] interfaces = aClass.getInterfaces();
            Object instance = null;
            try {
                instance = aClass.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterfaceProvider(anInterface);
                serviceConfig.setRef(instance);
                publish(serviceConfig);
                log.info("发布接口：{}", anInterface.getName());
            }
        }
        //发布
        return this;
    }

    private List<String> getAllClassNames(String packageName) {
        //通过package获取绝对路径
        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
            if (url == null) {
            throw new RuntimeException("包扫描时路径不存在");
        }
        String absolutePath = url.getPath();
        List<String> classNames = new ArrayList<>();
        List<String> classNamesResult = recursionFile(absolutePath, classNames, basePath);
        log.info("扫描到{}个类：{}", classNamesResult.size(), classNamesResult);
        return classNamesResult;
    }

    private List<String> recursionFile(String absolutePath, List<String> classNames, String basePath) {
        //获取文件
        File file = new File(absolutePath);
        //判断是否为文件夹
        if (file.isDirectory()) {
            log.info("扫描到文件夹：{}", file.getAbsolutePath());
            //找到文件夹，继续递归
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getName().endsWith(".class"));
            if (children == null || children.length == 0) {
                return classNames;
            }
            for (File child : children) {
                if (child.isDirectory()) {
                    recursionFile(child.getAbsolutePath(), classNames, basePath);
                } else {
                    String className = getClassNameByAbsolutePath(child.getAbsolutePath(), basePath);
                    classNames.add(className);
                }
            }

        } else {
            String className = getClassNameByAbsolutePath(absolutePath, basePath);
            classNames.add(className);
        }
        return classNames;
    }

    private String getClassNameByAbsolutePath(String absolutePath, String basePath) {
        String fileName = absolutePath
                .substring(absolutePath.indexOf(basePath.replaceAll("/", "\\\\")))
                .replaceAll("\\\\", ".");
        return fileName.substring(0, fileName.indexOf(".class"));
    }

    public Configuration getConfiguration() {
        return configuration;
    }

}
