package com.cj.jerry.rpc.channelHandler.handler;

import com.cj.jerry.rpc.transport.message.JerryRpcResponse;
import com.cj.jerry.rpc.transport.message.MessageFormatConstant;
import com.cj.jerry.rpc.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

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
public class JerryRpcResponseEncoder extends MessageToByteEncoder<JerryRpcResponse> {


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, JerryRpcResponse jerryRpcResponse, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        //头部长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
//        byteBuf.writeInt(MessageFormatConstant.FULL_LENGTH);
        byteBuf.writerIndex(byteBuf.writerIndex() + 4);
        byteBuf.writeByte(jerryRpcResponse.getCode());
        byteBuf.writeByte(jerryRpcResponse.getSerializeType());
        byteBuf.writeByte(jerryRpcResponse.getCompressType());
        //8B requestId
        byteBuf.writeLong(jerryRpcResponse.getRequestId());

        byte[] bodyBytes = getBodyBytes(jerryRpcResponse.getBody());
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
    }

    private byte[] getBodyBytes(Object body) {
        //对象变成字节数组
        if (body == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(baos);
            objectOutputStream.writeObject(body);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("对象序列化异常", e);
            throw new RuntimeException(e);
        }

    }
}
