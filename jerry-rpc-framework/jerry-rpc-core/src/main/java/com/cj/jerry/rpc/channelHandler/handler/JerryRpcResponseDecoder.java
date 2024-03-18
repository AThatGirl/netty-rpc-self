package com.cj.jerry.rpc.channelHandler.handler;

import com.cj.jerry.rpc.enumeration.RequestType;
import com.cj.jerry.rpc.serialize.Serializer;
import com.cj.jerry.rpc.serialize.SerializerFactory;
import com.cj.jerry.rpc.transport.message.JerryRpcRequest;
import com.cj.jerry.rpc.transport.message.JerryRpcResponse;
import com.cj.jerry.rpc.transport.message.MessageFormatConstant;
import com.cj.jerry.rpc.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;

/**
 *
 * 0---1---2---3---4---5---6---7---8---9---10---11---12---13---14---15---16---17---18
 * | magic         |ver|header |   fullLength   |  qt |ser|comp|        requestId
 * -------|---------|--------|------------|-----------|----------|-------------|-----------
 * |                                    body                                              |
 *
 * 基于长度字段的帧解码器
 */
@Slf4j
public class JerryRpcResponseDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * maxFrameLength:最大帧长度
     * lengthFieldOffset:长度字段的偏移量
     * lengthFieldLength:长度字段的长度
     * lengthAdjustment:负数适配长度
     * initialBytesToStrip:跳过的字节数
     */
    public JerryRpcResponseDecoder() {
        super(MessageFormatConstant.MAX_FRAME_LENGTH,
                MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH,
                MessageFormatConstant.FULL_FIELD_LENGTH,
                -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FIELD_LENGTH),
                0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) decode;
            log.info("调用decodeFrame方法");
            return decodeFrame(byteBuf);
        }
        log.error("decode不是ByteBuf类型");
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        //解析魔数值
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        //检测魔数是否匹配
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MessageFormatConstant.MAGIC[i]) {
                throw new RuntimeException("获取的请求不合法");
            }
        }
        log.info("魔数{}匹配成功:" , new String(magic, StandardCharsets.UTF_8));
        //解析版本
        byte version = byteBuf.readByte();
        if (version > MessageFormatConstant.VERSION) {
            throw new RuntimeException("获取的请求版本不支持");
        }
        //解析头部长度
        short headerLength = byteBuf.readShort();
        //解析总长度
        int fullLength = byteBuf.readInt();
        //解析请求类型
        byte responseCode = byteBuf.readByte();
        //解析序列化
        byte serialize = byteBuf.readByte();
        //解析压缩
        byte compress = byteBuf.readByte();
        //解析请求id
        long requestId = byteBuf.readLong();
        JerryRpcResponse jerryRpcResponse = new JerryRpcResponse();
        jerryRpcResponse.setCode(responseCode);
        jerryRpcResponse.setSerializeType(serialize);
        jerryRpcResponse.setCompressType(compress);
        jerryRpcResponse.setRequestId(requestId);

        //解析body
        int bodyLength = fullLength - headerLength;
        byte[] payload = new byte[bodyLength];
        byteBuf.readBytes(payload);
        //TODO 解压缩

        //反序列化
        Serializer serializer = SerializerFactory.getSerializer(serialize).getSerializer();
        Object body = serializer.deserialize(payload, Object.class);
        log.info("获取到请求:{}", jerryRpcResponse);
        jerryRpcResponse.setBody(body);
        return jerryRpcResponse;
    }
}
