package com.cj.jerry.rpc.loadbalancer.impl;

import com.cj.jerry.rpc.JerryRpcBootstrap;
import com.cj.jerry.rpc.exception.LoadBalancerException;
import com.cj.jerry.rpc.loadbalancer.AbstractLoadBalancer;
import com.cj.jerry.rpc.loadbalancer.Selector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class MinimumResponseTimeBalancer extends AbstractLoadBalancer {


    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new MinimumResponseTimeSelector(serviceList);
    }

    private static class MinimumResponseTimeSelector implements Selector {

        public MinimumResponseTimeSelector(List<InetSocketAddress> serviceList) {

        }

        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = JerryRpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.firstEntry();
            log.info("最小响应时间选择器：{}", entry);
            if (entry != null) {
                return (InetSocketAddress) entry.getValue().remoteAddress();
            }
            //直接从缓存中获取一个
            Channel channel = (Channel) JerryRpcBootstrap.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress) channel.remoteAddress();
        }
    }
}
