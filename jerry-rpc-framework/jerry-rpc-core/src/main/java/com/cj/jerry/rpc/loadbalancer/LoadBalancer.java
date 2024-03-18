package com.cj.jerry.rpc.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡的接口
 */
public interface LoadBalancer {

    //具备的能力：根据服务列表找到一个可用的服务

    /**
     * 根据服务名获取一个可用的服务
     * @param serviceName
     * @return
     */
    InetSocketAddress selectServiceAddress(String serviceName);


}
