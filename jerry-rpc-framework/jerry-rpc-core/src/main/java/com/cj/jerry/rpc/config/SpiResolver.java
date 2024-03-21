package com.cj.jerry.rpc.config;

import com.cj.jerry.rpc.compress.Compressor;
import com.cj.jerry.rpc.compress.CompressorFactory;
import com.cj.jerry.rpc.loadbalancer.LoadBalancer;
import com.cj.jerry.rpc.serialize.Serializer;
import com.cj.jerry.rpc.serialize.SerializerFactory;
import com.cj.jerry.rpc.spi.SpiHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class SpiResolver {


    public void loadFormSpi(Configuration configuration) {

        //加载spi
        List<ObjectWrapper<LoadBalancer>> loadBalancerWrapper = SpiHandler.getList(LoadBalancer.class);
        //将其放入工厂
        if (loadBalancerWrapper != null && loadBalancerWrapper.size() > 0) {
            configuration.setLoadBalancer(loadBalancerWrapper.get(0).getImpl());
            log.info("spi加载loadbalancer完成:{}", configuration.getLoadBalancer());
        }
        List<ObjectWrapper<Compressor>> compressorWrapper = SpiHandler.getList(Compressor.class);
        if (compressorWrapper != null && compressorWrapper.size() > 0) {
            compressorWrapper.forEach(CompressorFactory::addCompressor);
            log.info("spi加载compressor完成:{}", configuration.getCompressType());
        }
        List<ObjectWrapper<Serializer>> serializerWrapper = SpiHandler.getList(Serializer.class);
        if (serializerWrapper != null && serializerWrapper.size() > 0) {
            serializerWrapper.forEach(SerializerFactory::addSerializer);
            log.info("spi加载serializer完成:{}", configuration.getSerializeType());
        }


    }
}
