package com.cj.jerry.rpc.loadbalancer.impl;

import com.cj.jerry.rpc.JerryRpcBootstrap;
import com.cj.jerry.rpc.discovery.Registry;
import com.cj.jerry.rpc.exception.LoadBalancerException;
import com.cj.jerry.rpc.loadbalancer.AbstractLoadBalancer;
import com.cj.jerry.rpc.loadbalancer.LoadBalancer;
import com.cj.jerry.rpc.loadbalancer.Selector;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer extends AbstractLoadBalancer {


    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new RoundRobinSelector(serviceList);
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
            } else {
                //游标后移一个
                index.incrementAndGet();
            }
            return address;
        }

    }
}
