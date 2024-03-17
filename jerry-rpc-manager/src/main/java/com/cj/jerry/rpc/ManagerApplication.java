package com.cj.jerry.rpc;

import com.cj.jerry.rpc.utils.zookeeper.ZookeeperNode;
import com.cj.jerry.rpc.utils.zookeeper.ZookeeperUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.util.List;

@Slf4j
public class ManagerApplication {

    public static void main(String[] args) {
        //帮我们创建基础目录


        //MyWatcher是默认Watcher
        ZooKeeper zooKeeper = ZookeeperUtils.createZooKeeper();
        String basePath = "/jrpc-metadata";
        String providersPath = basePath + "/providers";
        String consumersPath = basePath + "/consumers";
        ZookeeperNode baseNode = new ZookeeperNode("/jrpc-metadata", null);
        ZookeeperNode providersNode = new ZookeeperNode(providersPath, null);
        ZookeeperNode consumersNode = new ZookeeperNode(consumersPath, null);
        List.of(baseNode, providersNode, consumersNode).forEach(node -> {
            ZookeeperUtils.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
        });
        ZookeeperUtils.close(zooKeeper);
    }

}
