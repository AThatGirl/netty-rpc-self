package com.cj.jerry.rpc.compress;

import com.cj.jerry.rpc.compress.impl.GzipCompressor;
import com.cj.jerry.rpc.config.ObjectWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 压缩类工厂
 */
@Slf4j
public class CompressorFactory {

    private final static ConcurrentHashMap<String, ObjectWrapper<Compressor>> COMPRESSOR_CACHE = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Byte, ObjectWrapper<Compressor>> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>();

    static {
        ObjectWrapper<Compressor> gzipCompressor = new ObjectWrapper<>((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip", gzipCompressor);
        COMPRESSOR_CACHE_CODE.put((byte) 1, gzipCompressor);
    }

    public static ObjectWrapper<Compressor> getCompressor(String serializeType) {
        if (COMPRESSOR_CACHE.get(serializeType) == null) {
            log.info("未找到您配置的压缩策略：{}，默认使用gzip", serializeType);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return COMPRESSOR_CACHE.get(serializeType);
    }

    public static ObjectWrapper<Compressor> getCompressor(byte serializeCode) {
        if (COMPRESSOR_CACHE_CODE.get(serializeCode) == null) {
            log.info("未找到您配置的压缩策略：{}，默认使用gzip", serializeCode);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return COMPRESSOR_CACHE_CODE.get(serializeCode);
    }

    public static void addCompressor(ObjectWrapper<Compressor> objectWrapper) {
        COMPRESSOR_CACHE.put(objectWrapper.getName(), objectWrapper);
        COMPRESSOR_CACHE_CODE.put(objectWrapper.getCode(), objectWrapper);
    }


}
