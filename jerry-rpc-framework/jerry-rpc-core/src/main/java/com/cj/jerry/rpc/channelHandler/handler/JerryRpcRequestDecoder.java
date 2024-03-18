package com.cj.jerry.rpc.channelHandler.handler;

import com.cj.jerry.rpc.compress.Compressor;
import com.cj.jerry.rpc.compress.CompressorFactory;
import com.cj.jerry.rpc.enumeration.RequestType;
import com.cj.jerry.rpc.serialize.Serializer;
import com.cj.jerry.rpc.serialize.SerializerFactory;
import com.cj.jerry.rpc.transport.message.JerryRpcRequest;
import com.cj.jerry.rpc.transport.message.MessageFormatConstant;
import com.cj.jerry.rpc.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

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
public class JerryRpcRequestDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * maxFrameLength:最大帧长度
     * lengthFieldOffset:长度字段的偏移量
     * lengthFieldLength:长度字段的长度
     * lengthAdjustment:负数适配长度
     * initialBytesToStrip:跳过的字节数
     */
    public JerryRpcRequestDecoder() {
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
        log.info("版本{}匹配成功" , version);
        //解析头部长度
        short headerLength = byteBuf.readShort();
        log.info("头部长度为{}" , headerLength);
        //解析总长度
        int fullLength = byteBuf.readInt();
        log.info("总长度为{}" , fullLength);
        //解析请求类型
        byte requestType = byteBuf.readByte();
        log.info("请求类型为{}" , requestType);
        //解析序列化
        byte serializeType = byteBuf.readByte();
        log.info("序列化为{}" , serializeType);
        //解析压缩
        byte compress = byteBuf.readByte();
        log.info("压缩为{}" , compress);
        //解析请求id
        long requestId = byteBuf.readLong();
        log.info("请求id为{}" , requestId);
        JerryRpcRequest jerryRpcRequest = new JerryRpcRequest();
        jerryRpcRequest.setRequestType(requestType);
        jerryRpcRequest.setSerializeType(serializeType);
        jerryRpcRequest.setCompressType(compress);
        jerryRpcRequest.setRequestId(requestId);

        //心跳请求没有负载，此处可以判断直接返回
        if (requestType == RequestType.HEART_BEAT.getId()) {
            return jerryRpcRequest;
        }
        //解析body
        int payloadLength = fullLength - headerLength;
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);
        //解压缩
        Compressor compressor = CompressorFactory.getCompressor(compress).getCompressor();
        payload = compressor.decompress(payload);
        log.info("解压后的数据为:{}", payload);

        //反序列化
        Serializer serializer = SerializerFactory.getSerializer(serializeType).getSerializer();
        RequestPayload requestPayload = serializer.deserialize(payload, RequestPayload.class);
        jerryRpcRequest.setRequestPayload(requestPayload);
        return jerryRpcRequest;
    }
}
