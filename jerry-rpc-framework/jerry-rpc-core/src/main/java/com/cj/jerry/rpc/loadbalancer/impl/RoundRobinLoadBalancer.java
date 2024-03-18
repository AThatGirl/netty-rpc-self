package com.cj.jerry.rpc.loadbalancer.impl;

import com.cj.jerry.rpc.JerryRpcBootstrap;
import com.cj.jerry.rpc.discovery.Registry;
import com.cj.jerry.rpc.exception.LoadBalancerException;
import com.cj.jerry.rpc.loadbalancer.LoadBalancer;
import com.cj.jerry.rpc.loadbalancer.Selector;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer {

    private Registry registry;
    //一个服务匹配一个selecor
    private Map<String, Selector> cache = new ConcurrentHashMap<>(8);

    public RoundRobinLoadBalancer() {
        this.registry = JerryRpcBootstrap.getInstance().getRegistry();
    }
    @Override
    public InetSocketAddress selectServiceAddress(String serviceName) {
        //内部维护一个服务列表，内部应该维护服务列表作为缓存
        Selector selector = cache.get(serviceName);
        //如果没有就需要为这个service创建一个selector
        if (selector == null) {
            List<InetSocketAddress> serviceList = this.registry.lookup(serviceName);
            selector = new RoundRobinSelector(serviceList);
            //放入缓存
            cache.put(serviceName, selector);

        }

        return selector.getNext();
    }

    private static class RoundRobinSelector implements Selector {
        private List<InetSocketAddress> serviceList;
        private AtomicInteger index;


        public RoundRobinSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {
            if (serviceList == null || serviceList.size() == 0) {
                throw new LoadBalancerException("没有可用的服务");
            }
            InetSocketAddress address = serviceList.get(index.get());
            //如果到了最后一个位置
            if (index.get() == serviceList.size() - 1) {
                index.set(0);
            }
            //游标后移一个
            index.incrementAndGet();
            return address;
        }

        @Override
        public void reBalance() {

        }
    }
}
