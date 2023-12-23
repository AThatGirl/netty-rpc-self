package com.cj.jerry.rpc;


public interface HelloJerryRpc {

    /**
     * 通用接口
     * server和client都需要
     * @param msg
     */
    String sayHi(String msg);
}
