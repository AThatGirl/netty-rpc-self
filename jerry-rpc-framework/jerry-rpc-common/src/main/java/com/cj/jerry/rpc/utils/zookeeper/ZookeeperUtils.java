package com.cj.jerry.rpc.utils.zookeeper;

import com.cj.jerry.rpc.Constant;

import java.io.IOException;

import com.cj.jerry.rpc.exception.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZookeeperUtils {

    public static ZooKeeper createZooKeeper() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        //定义连接参数
        String connectString = Constant.DEFAULT_ZK_CONNECT;
        //定义会话超时时间
        int sessionTimeout = Constant.DEFAULT_ZK_SESSION_TIMEOUT;
        return createZooKeeper(connectString, sessionTimeout);
    }

    public static ZooKeeper createZooKeeper(String connectString, int sessionTimeout) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            //MyWatcher是默认Watcher
            final ZooKeeper zooKeeper = new ZooKeeper(connectString, sessionTimeout, event -> {

                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("zookeeper连接成功");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            return zooKeeper;
        } catch (InterruptedException | IOException e) {
            log.error("创建zookeeper发生异常", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建一个节点
     *
     * @param zooKeeper
     * @param node
     * @param createMode
     */
    public static boolean createNode(ZooKeeper zooKeeper, ZookeeperNode node, Watcher watcher, CreateMode createMode) {
        try {
            if (zooKeeper.exists(node.getNodePath(), watcher) == null) {
                zooKeeper.create(node.getNodePath(), node.getData(), ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.info("节点{}创建成功", node.getNodePath());
                return true;
            } else {
                if (log.isDebugEnabled()) {
                    log.info("节点{}已经存在", node.getNodePath());
                }
                return false;
            }
        } catch (KeeperException | InterruptedException e) {
            throw new ZookeeperException();
        }
    }

    public static void close(ZooKeeper zooKeeper) {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("关闭zookeeper发生异常", e);
            throw new ZookeeperException();
        }
    }

    /**
     * 判断节点是否存在
     *
     * @param path
     * @param zooKeeper
     * @param watcher
     * @return
     */
    public static boolean exists(ZooKeeper zooKeeper, String path, Watcher watcher) {
        try {
            return zooKeeper.exists(path, watcher) != null;
        } catch (KeeperException | InterruptedException e) {
            log.error("判断节点{}是否存在发生异常", path, e);
            throw new ZookeeperException();
        }
    }

    /**
     * 查询一个节点的子元素
     * @param zooKeeper
     * @param serviceNode
     */
    public static List<String> getChildren(ZooKeeper zooKeeper, String serviceNode, Watcher watcher) {

        try {
            return zooKeeper.getChildren(serviceNode, watcher);
        } catch (KeeperException | InterruptedException e) {
            log.error("查询节点{}的子节点发生异常", serviceNode, e);
            throw new ZookeeperException();
        }

    }
}
