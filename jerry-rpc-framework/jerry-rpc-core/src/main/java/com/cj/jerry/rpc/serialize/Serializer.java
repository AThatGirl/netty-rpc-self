package com.cj.jerry.rpc.serialize;

/**
 * 序列化器
 */
public interface Serializer {

    /**
     * 序列化
     */
    byte[] serialize(Object object);

    /**
     * 反序列化
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
