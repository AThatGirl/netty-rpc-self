package com.cj.jerry.rpc.channelHandler.handler;

import com.cj.jerry.rpc.JerryRpcBootstrap;
import com.cj.jerry.rpc.ServiceConfig;
import com.cj.jerry.rpc.enumeration.RespCode;
import com.cj.jerry.rpc.transport.message.JerryRpcRequest;
import com.cj.jerry.rpc.transport.message.JerryRpcResponse;
import com.cj.jerry.rpc.transport.message.RequestPayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<JerryRpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, JerryRpcRequest jerryRpcRequest) throws Exception {
        //获取负载内容
        RequestPayload requestPayload = jerryRpcRequest.getRequestPayload();
        //根据负载内容进行方法调用
        Object object = callTargetMethod(requestPayload);
        //封装响应
        JerryRpcResponse response = new JerryRpcResponse();
        response.setCode(RespCode.SUCCESS.getCode());
        response.setRequestId(jerryRpcRequest.getRequestId());
        response.setSerializeType(jerryRpcRequest.getSerializeType());
        response.setCompressType(jerryRpcRequest.getCompressType());
        response.setBody(object);

        //写出响应
        channelHandlerContext.channel().writeAndFlush(response);
    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parameterTypes = requestPayload.getParameterTypes();
        Object[] parameterValues = requestPayload.getParameterValues();

        //寻找合适的类完成方法调用
        ServiceConfig<?> serviceConfig = JerryRpcBootstrap.SERVICE_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();
        //反射调用
        Object returnValue = null;
        try {
            Class<?> aClass = refImpl.getClass();
            Method method = aClass.getMethod(methodName, parameterTypes);
            returnValue =method.invoke(refImpl, parameterValues);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("调用服务【{}】的【{}】方法时发生异常", interfaceName, methodName, e);
            throw new RuntimeException(e);
        }

        return returnValue;
    }
}
