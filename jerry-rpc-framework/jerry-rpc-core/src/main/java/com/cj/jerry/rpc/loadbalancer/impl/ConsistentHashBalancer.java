package com.cj.jerry.rpc.loadbalancer.impl;

import com.cj.jerry.rpc.JerryRpcBootstrap;
import com.cj.jerry.rpc.exception.LoadBalancerException;
import com.cj.jerry.rpc.loadbalancer.AbstractLoadBalancer;
import com.cj.jerry.rpc.loadbalancer.Selector;
import com.cj.jerry.rpc.transport.message.JerryRpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConsistentHashBalancer extends AbstractLoadBalancer {


    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new ConsistentHashSelector(serviceList, 256);
    }

    private static class ConsistentHashSelector implements Selector {
        //Hash环用来存储服务器节点
        private SortedMap<Integer, InetSocketAddress> cricle = new TreeMap<>();
        //虚拟节点的个数
        private int virtualNodes;

        @Override
        public InetSocketAddress getNext() {
            //1.hash环已经建立好了，接下来要对请求的要素进行处理
            JerryRpcRequest jerryRpcRequest = JerryRpcBootstrap.REQUEST_THREAD_LOCAL.get();
            //根据请求特征选择服务器 id
            String requestId = Long.toString(jerryRpcRequest.getRequestId());
            //请求id做hash计算
            int hash = hash(requestId);
            //判断该hash值是否能直接落在一个服务器上，和服务器一样
            if (!cricle.containsKey(hash)) {
                //寻找离最近的节点
                SortedMap<Integer, InetSocketAddress> tailMap = cricle.tailMap(hash);
                hash = tailMap.isEmpty() ? cricle.firstKey() : tailMap.firstKey();
            }
            return cricle.get(hash);
        }


        public ConsistentHashSelector(List<InetSocketAddress> serviceList, int virtualNodes) {
            //将节点转换为虚拟节点，进行挂载
            this.virtualNodes = virtualNodes;
            for (InetSocketAddress address : serviceList) {
                //把每个节点加入到hash环中
                addNodeToCircle(address);
            }
        }

        /**
         * 将每个节点挂载到hash环上
         *
         * @param address
         */
        private void addNodeToCircle(InetSocketAddress address) {
            //为每个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(address.toString() + "-" + i);
                log.info("hash为【{}】已经挂载到了hash环", hash);
                cricle.put(hash, address);
            }
        }

        private void removeNodeFromCircle(InetSocketAddress address) {
            //为每个节点生成匹配的虚拟节点进行挂载
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(address.toString() + "-" + i);
                cricle.remove(hash);
            }
        }

        /**
         * 具体的hash算法
         *
         * @param s
         * @return
         */
        private int hash(String s) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (Exception e) {
                throw new RuntimeException("MD5加密出现异常", e);
            }
            byte[] digest = md.digest(s.getBytes());
            //MD5得到字节数组，只需要int 4个字节
            int res = 0;
            for (int i = 0; i < 4; i++) {
                int middle = digest[i] << ((3 - i)*8);
                res = res | middle;
            }
            return res;
        }
    }

}
