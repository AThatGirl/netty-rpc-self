package com.cj.jerry.rpc.serialize;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@Slf4j
public class SerializeUtils {


    public static byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        //对象变成字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(baos);
            objectOutputStream.writeObject(object);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("对象序列化异常", e);
            throw new RuntimeException(e);
        }

    }

}
