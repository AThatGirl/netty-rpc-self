package com.cj.jerry.rpc.transport.message;

import lombok.*;

/**
 * 服务调用方发起的请求内容
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class JerryRpcRequest {

    //请求的id
    private long requestId;

    //请求的类型，压缩的类型，序列化方式
    private byte requestType;
    private byte compressType;
    private byte serializeType;

    //时间戳
    private long timeStamp;

    //具体的消息体
    private RequestPayload requestPayload;

}
