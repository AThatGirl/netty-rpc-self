package com.cj.jerry.rpc;

import java.lang.ref.Reference;

public class ConsumerApplication {

    public static void main(String[] args) {
        //获取代理对象,使用ReferenceConfig封装
        //reference中一定有生成代理的模板方法
        ReferenceConfig<HelloJerryRpc> reference = new ReferenceConfig<>();
        reference.setInterfaceRef(HelloJerryRpc.class);

        //代理做了些什么，
        // 1.选择一个服务
        // 2.拉取服务列表
        // 3.选择一个服务并且建立连接
        // 4.发送请求，携带一些信息，获得结果
        JerryRpcBootstrap.getInstance()
                .application("com.cj.jerry.rpc.provider.demo")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .reference(reference);

        //获取一个代理对象
        HelloJerryRpc helloJerryRpc = reference.get();
        helloJerryRpc.sayHi("Jerry hi");
    }
}
