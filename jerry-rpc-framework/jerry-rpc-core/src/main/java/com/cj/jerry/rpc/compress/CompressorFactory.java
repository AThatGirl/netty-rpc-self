package com.cj.jerry.rpc.compress;

import com.cj.jerry.rpc.compress.impl.GzipCompressor;
import com.cj.jerry.rpc.serialize.SerializerWrapper;
import com.cj.jerry.rpc.serialize.impl.HessianSerializer;
import com.cj.jerry.rpc.serialize.impl.JdkSerializer;
import com.cj.jerry.rpc.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 压缩类工厂
 */
@Slf4j
public class CompressorFactory {

    private final static ConcurrentHashMap<String, CompressorWrapper> COMPRESSOR_CACHE = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Byte, CompressorWrapper> COMPRESSOR_CACHE_CODE = new ConcurrentHashMap<>();
    static {
        CompressorWrapper gzipCompressor = new CompressorWrapper((byte) 1, "gzip", new GzipCompressor());
        COMPRESSOR_CACHE.put("gzip", gzipCompressor);
        COMPRESSOR_CACHE_CODE.put((byte)1, gzipCompressor);
    }
    public static CompressorWrapper getCompressor(String serializeType) {
        if (COMPRESSOR_CACHE.get(serializeType) == null) {
            log.info("未找到您配置的压缩策略：{}，默认使用gzip", serializeType);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return COMPRESSOR_CACHE.get(serializeType);
    }
    public static CompressorWrapper getCompressor(byte serializeCode) {
        if (COMPRESSOR_CACHE_CODE.get(serializeCode) == null) {
            log.info("未找到您配置的压缩策略：{}，默认使用gzip", serializeCode);
            return COMPRESSOR_CACHE.get("gzip");
        }
        return COMPRESSOR_CACHE_CODE.get(serializeCode);
    }

}
