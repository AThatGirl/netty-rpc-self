package com.cj.jerry.rpc.discovery.impl;

import com.cj.jerry.rpc.Constant;
import com.cj.jerry.rpc.JerryRpcBootstrap;
import com.cj.jerry.rpc.ServiceConfig;
import com.cj.jerry.rpc.discovery.AbstractRegistry;
import com.cj.jerry.rpc.utils.NetUtils;
import com.cj.jerry.rpc.utils.zookeeper.ZookeeperNode;
import com.cj.jerry.rpc.utils.zookeeper.ZookeeperUtils;
import com.cj.jerry.rpc.watcher.UpAndDownWatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {
    //维护一个zookeeper实例
    private ZooKeeper zooKeeper;

    public ZookeeperRegistry() {
        this.zooKeeper = ZookeeperUtils.createZooKeeper();;
    }
    public ZookeeperRegistry(String connectSting, int timeout) {
        this.zooKeeper = ZookeeperUtils.createZooKeeper();;
    }

    @Override
    public void register(ServiceConfig<?> serviceConfig) {
        String parentNode = Constant.BASE_PROVIDERS_PATH + "/" + serviceConfig.getInterfaceProvider().getName();
        //创建持久节点
        if (!ZookeeperUtils.exists(zooKeeper, parentNode, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.PERSISTENT);
        }
        //创建临时节点，ip:port
        //TODO 端口后续处理
        String node = parentNode + "/" + NetUtils.getIp() + ":" + JerryRpcBootstrap.PORT;
        if (!ZookeeperUtils.exists(zooKeeper, node, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(node, null);
            ZookeeperUtils.createNode(zooKeeper, zookeeperNode, null, CreateMode.EPHEMERAL);
        }
        if (log.isDebugEnabled()) {
            log.debug("服务:{}已经被注册", serviceConfig.getInterfaceProvider().getName());
        }

    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        //找到服务对应的节点
        String serviceNode = Constant.BASE_PROVIDERS_PATH + "/" + serviceName;

        //从zk中获取对应的子节点:192.168.111.111:2125
        List<String> children = ZookeeperUtils.getChildren(zooKeeper, serviceNode, new UpAndDownWatcher());
        List<InetSocketAddress> inetSocketAddresses = children.stream().map(ipString -> {
            String ip = ipString.split(":")[0];
            String port = ipString.split(":")[1];
            return new InetSocketAddress(ip, Integer.parseInt(port));
        }).collect(Collectors.toList());
        if (inetSocketAddresses.isEmpty()) {
            throw new RuntimeException("没有找到对应的服务");
        }
        return inetSocketAddresses;
    }
}
