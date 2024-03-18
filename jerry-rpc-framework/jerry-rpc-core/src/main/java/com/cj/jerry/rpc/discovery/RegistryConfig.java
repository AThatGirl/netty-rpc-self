package com.cj.jerry.rpc.discovery;

import com.cj.jerry.rpc.Constant;
import com.cj.jerry.rpc.discovery.impl.ZookeeperRegistry;
import com.cj.jerry.rpc.exception.DiscoveryException;

public class RegistryConfig {

    //定义连接的url
    private String connectSting;

    public RegistryConfig(String connectSting) {
        this.connectSting = connectSting;
    }

    /**
     * 可以使用简单工厂完成
     * @return
     */
    public Registry getRegistry() {
        //获取注册中心的类型
        String registryType = getRegistryType(connectSting, true).toLowerCase().trim();
        if (registryType.equals("zookeeper")) {
            String host = getRegistryType(connectSting, false);
            return new ZookeeperRegistry(host, Constant.DEFAULT_ZK_SESSION_TIMEOUT);
        }
        throw new DiscoveryException("未发现合适的注册中心");
    }

    private String getRegistryType(String connectSting, boolean ifType) {
        String[] typeAndHost = connectSting.split("://");
        if (typeAndHost.length != 2) {
            throw new RuntimeException("给定的注册中心连接url不合法");
        }
        return ifType ? typeAndHost[0] : typeAndHost[1];
    }
}
