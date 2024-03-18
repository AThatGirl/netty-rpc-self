package com.cj.jerry.rpc.serialize.impl;

import com.alibaba.fastjson2.JSON;
import com.cj.jerry.rpc.serialize.Serializer;

import java.nio.charset.StandardCharsets;

public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null) {
            return null;
        }
        return JSON.parseObject(bytes, clazz);
    }
}
