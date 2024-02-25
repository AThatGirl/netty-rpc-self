package com.cj.jerry.rpc;

import com.cj.jerry.rpc.exception.ZookeeperException;
import com.cj.jerry.rpc.utils.zookeeper.ZookeeperNode;
import com.cj.jerry.rpc.utils.zookeeper.ZookeeperUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ManagerApplication {

    public static void main(String[] args) {
        //帮我们创建基础目录


        //MyWatcher是默认Watcher
        ZooKeeper zooKeeper = ZookeeperUtil.createZooKeeper();
        String basePath = "/jrpc-metadata";
        String providersPath = basePath + "/providers";
        String consumersPath = basePath + "/consumers";
        ZookeeperNode baseNode = new ZookeeperNode("/jrpc-metadata", null);
        ZookeeperNode providersNode = new ZookeeperNode(providersPath, null);
        ZookeeperNode consumersNode = new ZookeeperNode(consumersPath, null);
        List.of(baseNode, providersNode, consumersNode).forEach(node -> {
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
        });
        ZookeeperUtil.close(zooKeeper);
    }

}
