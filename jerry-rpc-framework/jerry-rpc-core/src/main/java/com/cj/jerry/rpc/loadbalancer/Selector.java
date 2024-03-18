package com.cj.jerry.rpc.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 *
 */
public interface Selector {

    /**
     * 根据服务列表执行一种算法获取一个服务节点
     * @return
     */
    InetSocketAddress getNext();

    //todo 服务动态上下线
    void reBalance();
}
