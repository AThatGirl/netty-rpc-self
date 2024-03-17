package com.cj.jerry.rpc.discovery;

import com.cj.jerry.rpc.ServiceConfig;

import java.net.InetSocketAddress;

public interface Registry {


    /**
     * 注册服务
     *
     * @param serviceConfig
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉取一个可用的服务
     * @param serviceName
     * @return
     */
    InetSocketAddress lookup(String serviceName);
}
