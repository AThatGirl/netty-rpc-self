package com.cj.jerry.rpc.discovery;

import com.cj.jerry.rpc.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

public interface Registry {


    /**
     * 注册服务
     *
     * @param serviceConfig
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉取一个可用的服务
     *
     * @param serviceName
     * @return
     */
    List<InetSocketAddress> lookup(String serviceName);
}
