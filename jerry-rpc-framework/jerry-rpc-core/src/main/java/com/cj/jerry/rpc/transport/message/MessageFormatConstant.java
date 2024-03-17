package com.cj.jerry.rpc.transport.message;

public class MessageFormatConstant {

    //魔数
    public static final byte[] MAGIC = "jrpc".getBytes();
    //版本
    public static final byte VERSION = 1;
    //头部长度
    public static final short HEADER_LENGTH = (short) (MAGIC.length + 18);

    //最大的帧长度
    public static final int MAX_FRAME_LENGTH = 1024 * 1024;

    public static final int VERSION_LENGTH = 1;
    //头部长度的长度
    public static final int HEADER_FIELD_LENGTH = 2;
    public static final int FULL_FIELD_LENGTH = 4;
}
