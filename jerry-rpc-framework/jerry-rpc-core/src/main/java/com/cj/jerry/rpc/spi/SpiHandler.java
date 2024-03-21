package com.cj.jerry.rpc.spi;

import com.cj.jerry.rpc.config.Configuration;
import com.cj.jerry.rpc.loadbalancer.LoadBalancer;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现简易版的spi
 */
public class SpiHandler {


    private static final String BASE_PATH = "META-INF/service";
    //定义一个缓存保存spi相关的原始内容
    private static final Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>();
    static {
        //加载spi
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL fileUrl = classLoader.getResource(BASE_PATH);
        File file = new File(fileUrl.getFile());
        File[] children = file.listFiles();

    }

    public static <T>  T get(Class<T> clazz) {
        String name = clazz.getName();


    }

}
