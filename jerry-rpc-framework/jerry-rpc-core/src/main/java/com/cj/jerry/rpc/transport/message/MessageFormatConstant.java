package com.cj.jerry.rpc.transport.message;

public class MessageFormatConstant {

    //魔数
    public static final byte[] MAGIC = "jerry-rpc".getBytes();
    //版本
    public static final byte VERSION = 1;
    //头部长度
    public static final short HEADER_LENGTH = 21;

}
