package com.cj.jerry.rpc.discovery;

import com.cj.jerry.rpc.Constant;
import com.cj.jerry.rpc.ServiceConfig;
import com.cj.jerry.rpc.utils.NetUtils;
import com.cj.jerry.rpc.utils.zookeeper.ZookeeperNode;
import com.cj.jerry.rpc.utils.zookeeper.ZookeeperUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

/**
 * 提炼共享内容，所有注册中心都能做的事情
 * 还可以做模板方法
 */
public abstract class AbstractRegistry implements Registry {

    private ZooKeeper zooKeeper = ZookeeperUtils.createZooKeeper();


    @Override
    public void register(ServiceConfig<?> serviceConfig) {

    }
}
