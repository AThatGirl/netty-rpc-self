package com.cj.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

public class ZookeeperTest {

    ZooKeeper zooKeeper;

    @Before
    public void createZk() {
        //定义连接参数
        String connectString = "127.0.0.1:2181";
        //定义会话超时时间
        int sessionTimeout = 10000;
        try {
            //MyWatcher是默认Watcher
            zooKeeper = new ZooKeeper(connectString, sessionTimeout, new MyWatcher());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreate() {
        try {
            zooKeeper.create("/jerry", "jerry".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void deleteNode() {
        try {
            zooKeeper.delete("/jerry", -1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //检查版本号
    @Test
    public void testCheckVersion() {

        try {
            Stat stat = zooKeeper.exists("/jerry", true);
            //数据版本
            int version = stat.getVersion();
            System.out.println(version);
            //ACL版本
            int aversion = stat.getAversion();
            System.out.println(aversion);
            //子节点数据版本
            int cversion = stat.getCversion();
            System.out.println(cversion);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testWatcher() {

        try {
            zooKeeper.exists("/jerry", true);
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
