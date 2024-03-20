package com.cj.jerry.rpc.loadbalancer;

import com.cj.jerry.rpc.JerryRpcBootstrap;
import com.cj.jerry.rpc.discovery.Registry;
import com.cj.jerry.rpc.exception.LoadBalancerException;
import com.cj.jerry.rpc.loadbalancer.impl.RoundRobinLoadBalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractLoadBalancer implements LoadBalancer {

    //一个服务匹配一个selecor
    private Map<String, Selector> cache = new ConcurrentHashMap<>(8);
    @Override
    public InetSocketAddress selectServiceAddress(String serviceName) {
        //内部维护一个服务列表，内部应该维护服务列表作为缓存
        Selector selector = cache.get(serviceName);
        //如果没有就需要为这个service创建一个selector
        if (selector == null) {
            List<InetSocketAddress> serviceList = JerryRpcBootstrap.getInstance().getRegistry().lookup(serviceName);
            selector = getSelector(serviceList);
            //放入缓存
            cache.put(serviceName, selector);

        }

        return selector.getNext();
    }

    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);

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
            } else {
                //游标后移一个
                index.incrementAndGet();
            }
            return address;
        }

    }

    @Override
    public synchronized void reLoadBalance(String serviceName, List<InetSocketAddress> serviceList) {
        cache.put(serviceName, getSelector(serviceList));
    }
}
