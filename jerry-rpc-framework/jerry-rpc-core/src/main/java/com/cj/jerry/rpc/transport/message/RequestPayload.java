package com.cj.jerry.rpc.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用来描述请求调用方所请求的接口的描述
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestPayload implements Serializable {

    //接口的名字
    private String interfaceName;
    //方法的名字
    private String methodName;
    //参数的类型
    private Class<?>[] parameterTypes;
    //方法的参数
    private Object[] parameterValues;
    //方法的返回值
    private Class<?> returnType;
}
