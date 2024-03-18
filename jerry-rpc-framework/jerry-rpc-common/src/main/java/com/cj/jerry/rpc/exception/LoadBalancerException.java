package com.cj.jerry.rpc.exception;

public class LoadBalancerException extends RuntimeException {

    public LoadBalancerException() {

    }

    public LoadBalancerException(Throwable e) {
        super(e);
    }

    public LoadBalancerException(String message) {

    }
}
