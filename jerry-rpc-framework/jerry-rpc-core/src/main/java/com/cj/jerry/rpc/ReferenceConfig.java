package com.cj.jerry.rpc;

import com.cj.jerry.rpc.discovery.Registry;
import com.cj.jerry.rpc.proxy.handler.RpcConsumerInvocationHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

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
        RpcConsumerInvocationHandler handler = new RpcConsumerInvocationHandler(registry, interfaceRef);
        //使用动态代理完成一些工作，生成代理对象

        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, handler);
        return (T) helloProxy;
    }

    public Registry getRegistryConfig() {
        return registry;
    }

    public void setRegistryConfig(Registry registry) {
        this.registry = registry;
    }
}
