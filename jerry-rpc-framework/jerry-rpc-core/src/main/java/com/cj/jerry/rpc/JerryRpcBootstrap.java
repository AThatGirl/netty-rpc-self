package com.cj.jerry.rpc;

import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
public class JerryRpcBootstrap {
    //JerryRpcBootstrap是一个单例，饿汉式
    private static final JerryRpcBootstrap jerryRpcBootstrap = new JerryRpcBootstrap();

    //定义相关的一些基础配置
    private String applicationName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    public JerryRpcBootstrap() {

    }

    //定义当前应用的名字
    public JerryRpcBootstrap application(String appName) {
        this.applicationName = appName;
        return this;
    }

    //配置注册中心
    public JerryRpcBootstrap registry(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
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
    public JerryRpcBootstrap publish(ServiceConfig<?> service) {
        String parentNode = service.getInterfaceProvider().getName();
        if (log.isDebugEnabled()) {
            log.debug("服务:{}已经被注册", service.getInterfaceProvider().getName());
        }
        return this;
    }

    public JerryRpcBootstrap publish(List<?> services) {
        return this;
    }

    public JerryRpcBootstrap start() {
        return jerryRpcBootstrap;
    }

    public JerryRpcBootstrap reference(ReferenceConfig<?> referenceConfig) {
        return jerryRpcBootstrap;
    }

    public static JerryRpcBootstrap getInstance() {
        return jerryRpcBootstrap;
    }
}
