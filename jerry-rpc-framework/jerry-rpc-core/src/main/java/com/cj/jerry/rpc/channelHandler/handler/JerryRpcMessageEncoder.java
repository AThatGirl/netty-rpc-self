package com.cj.jerry.rpc.channelHandler.handler;

import com.cj.jerry.rpc.transport.message.JerryRpcRequest;
import com.cj.jerry.rpc.transport.message.MessageFormatConstant;
import com.cj.jerry.rpc.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

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
public class JerryRpcMessageEncoder extends MessageToByteEncoder<JerryRpcRequest> {


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
        byte[] bodyBytes = getBodyBytes(jerryRpcRequest.getRequestPayload());
        byteBuf.writeBytes(bodyBytes);
        //重新处理报文长度
        //保存当前写指针的位置
        int writeIndex = byteBuf.writerIndex();
        //将写指针的位置移动到总长度的位置上
        byteBuf.writerIndex(7);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyBytes.length);
        //将写指针归位
        byteBuf.writerIndex(writeIndex);
    }

    private byte[] getBodyBytes(RequestPayload requestPayload) {
        //对象变成字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(baos);
            objectOutputStream.writeObject(requestPayload);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("对象序列化异常", e);
            throw new RuntimeException(e);
        }

    }
}
