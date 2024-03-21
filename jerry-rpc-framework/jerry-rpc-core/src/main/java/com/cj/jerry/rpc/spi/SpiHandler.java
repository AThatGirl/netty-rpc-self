package com.cj.jerry.rpc.spi;

import com.cj.jerry.rpc.config.Configuration;
import com.cj.jerry.rpc.config.ObjectWrapper;
import com.cj.jerry.rpc.exception.SpiException;
import com.cj.jerry.rpc.loadbalancer.LoadBalancer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 实现简易版的spi
 */
@Slf4j
public class SpiHandler {


    private static final String BASE_PATH = "META-INF/service";
    //定义一个缓存保存spi相关的原始内容
    private static final Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>();
    //缓存每个接口所对应实现的实例
    private static final Map<Class<?>, List<ObjectWrapper<?>>> SPI_IMPLEMENT = new ConcurrentHashMap<>();

    static {
        //加载spi
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL fileUrl = classLoader.getResource(BASE_PATH);
        if (fileUrl != null) {
            File file = new File(fileUrl.getFile());
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    String key = child.getName();
                    List<String> values = getImplNames(child);
                    SPI_CONTENT.put(key, values);
                }
            }
        }
        log.info("加载spi完成：{}", SPI_CONTENT);
    }

    private static List<String> getImplNames(File child) {
        try (
                FileReader fileReader = new FileReader(child);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
        ) {
            List<String> implNames = new ArrayList<>();
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null || "".equals(line)) {
                    break;
                }
                implNames.add(line);
            }
            return implNames;
        } catch (Exception e) {
            log.error("读取spi文件发生异常", e);
        }
        return new ArrayList<>();
    }

    public synchronized static <T> ObjectWrapper<T> get(Class<T> clazz) {
        //优先走缓存
        List<ObjectWrapper<?>> impls = SPI_IMPLEMENT.get(clazz);

        if (impls != null && impls.size() > 0) {
            return (ObjectWrapper<T>) impls.get(0);
        }
        buildCache(clazz);
        SPI_IMPLEMENT.put(clazz, impls);
        List<ObjectWrapper<?>> result = SPI_IMPLEMENT.get(clazz);
        return result == null || result.size() == 0 ? null : (ObjectWrapper<T>) result.get(0);
    }

    public synchronized static <T> List<ObjectWrapper<T>> getList(Class<T> clazz) {
        //优先走缓存
        List<ObjectWrapper<?>> impls = SPI_IMPLEMENT.get(clazz);
        if (impls != null && impls.size() > 0) {
            return impls.stream().map(wrapper -> (ObjectWrapper<T>) wrapper).collect(Collectors.toList());
        }
        buildCache(clazz);
        List<ObjectWrapper<?>> objectWrappers = SPI_IMPLEMENT.get(clazz);
        if (objectWrappers != null && objectWrappers.size() > 0) {
            return objectWrappers.stream().map(wrapper -> (ObjectWrapper<T>) wrapper).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private static void buildCache(Class<?> clazz) {
        String name = clazz.getName();
        List<String> implNames = SPI_CONTENT.get(name);
        if (implNames == null || implNames.size() == 0) {
            log.error("没有找到【{}】的实现类", name);
            return;
        }
        List<ObjectWrapper<?>> implement = new ArrayList<>();
        //实例化所有的实现
        for (String implName : implNames) {
            try {
                String[] codeAndTypeAndName = implName.split("-");
                if (codeAndTypeAndName.length != 3) {
                    throw new SpiException("配置的spi格式不正确");
                }
                Byte code = Byte.valueOf(codeAndTypeAndName[0]);
                String type = codeAndTypeAndName[1];
                String implementName = codeAndTypeAndName[2];
                Class<?> implClass = Class.forName(implementName);
                Object impl = implClass.getConstructor().newInstance();
                ObjectWrapper<?> objectWrapper = new ObjectWrapper<>(code, type, impl);
                implement.add(objectWrapper);
            } catch (Exception e) {
                log.error("实例化【{}】实现类发生异常", implName, e);
            }
        }
        SPI_IMPLEMENT.put(clazz, implement);
    }


}
