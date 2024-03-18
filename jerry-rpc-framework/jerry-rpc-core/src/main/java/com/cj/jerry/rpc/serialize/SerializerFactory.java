package com.cj.jerry.rpc.serialize;

import com.cj.jerry.rpc.serialize.impl.HessianSerializer;
import com.cj.jerry.rpc.serialize.impl.JdkSerializer;
import com.cj.jerry.rpc.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SerializerFactory {

    private final static ConcurrentHashMap<String, SerializerWrapper> SERIALIZER_CACHE = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Byte, SerializerWrapper> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>();
    static {
        SerializerWrapper jdkSerializer = new SerializerWrapper((byte) 1, "jdk", new JdkSerializer());
        SerializerWrapper jsonSerializer = new SerializerWrapper((byte) 2, "json", new JsonSerializer());
        SerializerWrapper hessianSerializer = new SerializerWrapper((byte) 3, "hessian", new HessianSerializer());
        SERIALIZER_CACHE.put("jdk", jdkSerializer);
        SERIALIZER_CACHE.put("json", jsonSerializer);
        SERIALIZER_CACHE.put("hessian", hessianSerializer);
        SERIALIZER_CACHE_CODE.put((byte)1, jdkSerializer);
        SERIALIZER_CACHE_CODE.put((byte)2, jsonSerializer);
        SERIALIZER_CACHE_CODE.put((byte)3, hessianSerializer);
    }
    public static SerializerWrapper getSerializer(String serializeType) {
        if (SERIALIZER_CACHE.get(serializeType) == null) {
            log.info("未找到您配置的序列化策略：{}，默认使用jdk", serializeType);
            return SERIALIZER_CACHE.get("jdk");
        }
        return SERIALIZER_CACHE.get(serializeType);
    }
    public static SerializerWrapper getSerializer(byte serializeCode) {
        if (SERIALIZER_CACHE_CODE.get(serializeCode) == null) {
            log.info("未找到您配置的序列化策略：{}，默认使用jdk", serializeCode);
            return SERIALIZER_CACHE.get("jdk");
        }
        return SERIALIZER_CACHE_CODE.get(serializeCode);
    }

}
