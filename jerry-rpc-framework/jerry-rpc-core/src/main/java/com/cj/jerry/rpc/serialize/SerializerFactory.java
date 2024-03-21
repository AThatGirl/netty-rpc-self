package com.cj.jerry.rpc.serialize;

import com.cj.jerry.rpc.config.ObjectWrapper;
import com.cj.jerry.rpc.serialize.impl.HessianSerializer;
import com.cj.jerry.rpc.serialize.impl.JdkSerializer;
import com.cj.jerry.rpc.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SerializerFactory {

    private final static ConcurrentHashMap<String, ObjectWrapper<Serializer>> SERIALIZER_CACHE = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<Byte, ObjectWrapper<Serializer>> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>();
    static {
        ObjectWrapper<Serializer> jdkSerializer = new ObjectWrapper<Serializer>((byte) 1, "jdk", new JdkSerializer());
        ObjectWrapper<Serializer> jsonSerializer = new ObjectWrapper<Serializer>((byte) 2, "json", new JsonSerializer());
        ObjectWrapper<Serializer> hessianSerializer = new ObjectWrapper<Serializer>((byte) 3, "hessian", new HessianSerializer());
        SERIALIZER_CACHE.put("jdk", jdkSerializer);
        SERIALIZER_CACHE.put("json", jsonSerializer);
        SERIALIZER_CACHE.put("hessian", hessianSerializer);
        SERIALIZER_CACHE_CODE.put((byte)1, jdkSerializer);
        SERIALIZER_CACHE_CODE.put((byte)2, jsonSerializer);
        SERIALIZER_CACHE_CODE.put((byte)3, hessianSerializer);
    }
    public static ObjectWrapper<Serializer> getSerializer(String serializeType) {
        if (SERIALIZER_CACHE.get(serializeType) == null) {
            log.info("未找到您配置的序列化策略：{}，默认使用jdk", serializeType);
            return SERIALIZER_CACHE.get("jdk");
        }
        return SERIALIZER_CACHE.get(serializeType);
    }
    public static ObjectWrapper<Serializer> getSerializer(byte serializeCode) {
        if (SERIALIZER_CACHE_CODE.get(serializeCode) == null) {
            log.info("未找到您配置的序列化策略：{}，默认使用jdk", serializeCode);
            return SERIALIZER_CACHE.get("jdk");
        }
        return SERIALIZER_CACHE_CODE.get(serializeCode);
    }

    public static void addSerializer(ObjectWrapper<Serializer> serializerObjectWrapper) {
        SERIALIZER_CACHE.put(serializerObjectWrapper.getName(), serializerObjectWrapper);
        SERIALIZER_CACHE_CODE.put(serializerObjectWrapper.getCode(), serializerObjectWrapper);
    }

}
