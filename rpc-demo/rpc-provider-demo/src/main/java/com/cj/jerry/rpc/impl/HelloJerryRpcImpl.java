package com.cj.jerry.rpc.impl;


import com.cj.jerry.rpc.HelloJerryRpc;
import com.cj.jerry.rpc.annotation.JerryRpcApi;

@JerryRpcApi
public class HelloJerryRpcImpl implements HelloJerryRpc {
    @Override
    public String sayHi(String msg) {
        return "hello " + msg;
    }
}
