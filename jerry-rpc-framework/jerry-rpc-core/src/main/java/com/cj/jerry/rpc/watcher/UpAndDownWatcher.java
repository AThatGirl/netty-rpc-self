package com.cj.jerry.rpc.watcher;

import com.cj.jerry.rpc.JerryRpcBootstrap;
import com.cj.jerry.rpc.NettyBootstrapInitializer;
import com.cj.jerry.rpc.discovery.Registry;
import com.cj.jerry.rpc.loadbalancer.LoadBalancer;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

@Slf4j
public class UpAndDownWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeChildrenChanged) {
            log.info("服务节点发生上下线：{}，将重新拉取服务列表", event.getPath());
        }
        String serviceName = getServiceName(event.getPath());
        Registry registry = JerryRpcBootstrap.getInstance().getRegistry();

        List<InetSocketAddress> addresses = registry.lookup(serviceName);
        for (InetSocketAddress address : addresses) {
            //新增的节点会在address中，不在CHANNEL_CACHE中
            if (!JerryRpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                Channel channel = null;
                try {
                    channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                JerryRpcBootstrap.CHANNEL_CACHE.put(address, channel);
            }
        }
        //下线的节点会在CHANNEL_CACHE中，不在address中
        for (Map.Entry<InetSocketAddress, Channel> entry : JerryRpcBootstrap.CHANNEL_CACHE.entrySet()) {
            if (!addresses.contains(entry.getKey())) {
                JerryRpcBootstrap.CHANNEL_CACHE.remove(entry.getKey());
            }
        }
        log.info("服务列表数量为：{}", JerryRpcBootstrap.CHANNEL_CACHE.size());

        //获得负载均衡器，进行重新的loadBalance
        LoadBalancer loadBalancer = JerryRpcBootstrap.LOAD_BALANCER;
        loadBalancer.reLoadBalance(serviceName, addresses);

    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}
