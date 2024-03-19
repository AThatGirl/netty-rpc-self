package com.cj.jerry.rpc.channelHandler.handler;

import com.cj.jerry.rpc.JerryRpcBootstrap;
import com.cj.jerry.rpc.compress.Compressor;
import com.cj.jerry.rpc.compress.CompressorFactory;
import com.cj.jerry.rpc.serialize.Serializer;
import com.cj.jerry.rpc.serialize.SerializerFactory;
import com.cj.jerry.rpc.transport.message.JerryRpcRequest;
import com.cj.jerry.rpc.transport.message.MessageFormatConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 4B magic(魔数值)
 * 1B version(版本)
 * 2B header(头部长度)
 * 4B fullLength(报文总长度)
 * 1B serialize
 * 1B compress
 * 1B requestType
 * 8B requestId
 * <p>
 * body
 * 出栈时第一个经过的处理器
 */
@Slf4j
public class JerryRpcRequestEncoder extends MessageToByteEncoder<JerryRpcRequest> {


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, JerryRpcRequest jerryRpcRequest, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        //头部长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
//        byteBuf.writeInt(MessageFormatConstant.FULL_LENGTH);
        byteBuf.writerIndex(byteBuf.writerIndex() + 4);
        byteBuf.writeByte(jerryRpcRequest.getRequestType());
        byteBuf.writeByte(jerryRpcRequest.getSerializeType());
        byteBuf.writeByte(jerryRpcRequest.getCompressType());
        //8B requestId
        byteBuf.writeLong(jerryRpcRequest.getRequestId());
        byteBuf.writeLong(jerryRpcRequest.getTimeStamp());
        //判断心跳请求
        byte[] bodyBytes = null;
        if (jerryRpcRequest.getRequestPayload() != null) {
            Serializer serializer = SerializerFactory.getSerializer(jerryRpcRequest.getSerializeType()).getSerializer();
            bodyBytes = serializer.serialize(jerryRpcRequest.getRequestPayload());
            //压缩方式
            Compressor compressor = CompressorFactory.getCompressor(jerryRpcRequest.getCompressType()).getCompressor();
            bodyBytes = compressor.compress(bodyBytes);

        }

        if (bodyBytes != null) {
            byteBuf.writeBytes(bodyBytes);
        }
        int byteLength = bodyBytes == null ? 0 : bodyBytes.length;
        //重新处理报文长度
        //保存当前写指针的位置
        int writeIndex = byteBuf.writerIndex();
        //将写指针的位置移动到总长度的位置上
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + byteLength);
        //将写指针归位
        byteBuf.writerIndex(writeIndex);
        log.info("请求【{}】已经完成报文的编码", jerryRpcRequest.getRequestId());
    }

}
