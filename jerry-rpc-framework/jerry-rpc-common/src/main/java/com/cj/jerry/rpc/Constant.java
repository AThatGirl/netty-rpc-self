package com.cj.jerry.rpc;

public class Constant {

    public static final String DEFAULT_ZK_CONNECT = "127.0.0.1:2181";

    public static final int DEFAULT_ZK_SESSION_TIMEOUT = 10000;

    //服务提供方和调用方在注册中心的基础路径
    public static final String BASE_PROVIDERS_PATH = "/jrpc-metadata/providers";
    public static final String BASE_CONSUMERS_PATH = "/jrpc-metadata/consumers";
}
