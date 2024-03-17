package com.cj.jerry.rpc.exception;

public class NetworkException extends RuntimeException{

    public NetworkException() {

    }
    public NetworkException(Throwable e) {
        super(e);
    }

    public NetworkException(String message) {
        super(message);
    }
}
