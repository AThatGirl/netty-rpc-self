package com.cj.jerry.rpc;

import com.cj.jerry.rpc.discovery.RegistryConfig;
import com.cj.jerry.rpc.impl.HelloJerryRpcImpl;

public class ProviderApplication {
    public static void main(String[] args) {
        ServiceConfig<HelloJerryRpc> service = new ServiceConfig<>();
        service.setInterfaceProvider(HelloJerryRpc.class);
        service.setRef(new HelloJerryRpcImpl());
        //服务提供方需要注册启动服务
        JerryRpcBootstrap.getInstance()
                .application("com.cj.jerry.rpc.provider.demo")
                //配置注册中心
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .protocol(new ProtocolConfig("jdk"))
                //发布服务
//                .publish(service)
                .scan("com.cj")
                //启动服务
                .start();
    }
}
