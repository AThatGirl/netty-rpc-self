package com.cj.jerry.rpc.utils;

import com.cj.jerry.rpc.exception.NetworkException;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.util.Enumeration;

@Slf4j
public class NetUtils {

    public static String getIp() {
        try {
            //获取所有的网卡信息
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                //过滤掉所有的回环接口和虚拟接口
                if (iface.isLoopback() || iface.isVirtual() || !iface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    //过滤掉
                    if (address instanceof Inet6Address && !address.isLoopbackAddress()) {
                        continue;
                    }
                    String ipAddress = address.getHostAddress();
                    System.out.println("本机的IP地址是:" + ipAddress);
                    return ipAddress;
                }
            }
            throw new NetworkException();
        } catch (SocketException e) {
            log.error("获取本机IP地址发生异常", e);
            throw new NetworkException(e);
        }
    }


}
