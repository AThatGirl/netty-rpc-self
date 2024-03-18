package com.cj.jerry.rpc.serialize.impl;

import com.cj.jerry.rpc.exception.SerializeException;
import com.cj.jerry.rpc.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        //对象变成字节数组

        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
        ) {
            objectOutputStream.writeObject(object);
            log.info("调用jdk序列化");
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("对象序列化时异常", e);
            throw new SerializeException(e);
        }

    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {

        if (bytes == null || clazz == null) {
            return null;
        }
        //对象变成字节数组

        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream objectInputStream = new ObjectInputStream(bais);
        ) {
            log.info("调用jdk反序列化");
            return (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("对象反序列化时异常", e);
            throw new SerializeException(e);
        }
    }
}
