package com.cj.jerry.rpc;

public class ServiceConfig<T> {
    private Class<?> interfaceProvider;
    private Object ref;

    public Class<?> getInterfaceProvider() {
        return interfaceProvider;
    }

    public void setInterfaceProvider(Class<?> interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }

}
