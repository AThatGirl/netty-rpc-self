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
    private Long requestId;

    //请求的类型，压缩的类型，序列化方式
    private Byte requestType;
    private Byte compressType;
    private Byte serializeType;


    //具体的消息体
    private RequestPayload requestPayload;

}
