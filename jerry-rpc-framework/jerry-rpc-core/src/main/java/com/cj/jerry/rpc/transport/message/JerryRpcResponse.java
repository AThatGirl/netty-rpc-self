package com.cj.jerry.rpc.transport.message;

import lombok.*;

import java.io.Serializable;

/**
 * 服务调用方发起的请求内容
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class JerryRpcResponse implements Serializable {

    //请求的id
    private long requestId;

    //请求的类型，压缩的类型，序列化方式
    //private Byte requestType;
    private byte compressType;
    private byte serializeType;

    //响应码：1：成功，2：异常
    private byte code;


    //具体的消息体
    private Object body;

}
