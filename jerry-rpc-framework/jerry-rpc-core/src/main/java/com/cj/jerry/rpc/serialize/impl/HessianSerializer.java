package com.cj.jerry.rpc.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.cj.jerry.rpc.exception.SerializeException;
import com.cj.jerry.rpc.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return null;
        }
        //对象变成字节数组


        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ) {
            Hessian2Output hessian2Output = new Hessian2Output(baos);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            log.info("调用序hessian列化");
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("hessian序列化时异常", e);
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
        ) {
            Hessian2Input hessian2Input = new Hessian2Input(bais);
            log.info("调用hessian反序列化");
            return (T) hessian2Input.readObject();
        } catch (IOException e) {
            log.error("hessian反序列化时异常", e);
            throw new SerializeException(e);
        }
    }
}
