package com.cj.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class MyWatcher implements Watcher {

    @Override
    public void process(WatchedEvent event) {
        //判断事件类型，连接类型的事件
        if (event.getType().equals(Event.EventType.None)) {
            if (event.getState().equals(Event.KeeperState.SyncConnected)) {
                System.out.println("zookeeper连接成功");
            } else if (event.getState().equals(Event.KeeperState.AuthFailed)) {
                System.out.println("zookeeper认证失败");
            } else if (event.getState().equals(Event.KeeperState.Disconnected)) {
                System.out.println("zookeeper连接断开");
            } else if (event.getState().equals(Event.KeeperState.Expired)) {
                System.out.println("zookeeper会话过期");
            }

        } else if (event.getType() == Event.EventType.NodeChildrenChanged) {
            System.out.println(event.getPath() + "子节点变更");
        } else if (event.getType() == Event.EventType.NodeDataChanged) {
            System.out.println(event.getPath() + "数据变更");
        } else if (event.getType() == Event.EventType.NodeDeleted) {
            System.out.println(event.getPath() + "节点删除");
        } else if (event.getType() == Event.EventType.NodeCreated) {
            System.out.println(event.getPath() + "节点创建");
        }
    }

}
