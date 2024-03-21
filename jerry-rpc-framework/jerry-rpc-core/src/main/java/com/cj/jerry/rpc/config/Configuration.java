package com.cj.jerry.rpc.config;


import com.cj.jerry.rpc.IdGenerator;
import com.cj.jerry.rpc.ProtocolConfig;
import com.cj.jerry.rpc.compress.Compressor;
import com.cj.jerry.rpc.compress.impl.GzipCompressor;
import com.cj.jerry.rpc.discovery.RegistryConfig;
import com.cj.jerry.rpc.loadbalancer.LoadBalancer;
import com.cj.jerry.rpc.loadbalancer.impl.RoundRobinLoadBalancer;
import com.cj.jerry.rpc.serialize.Serializer;
import com.cj.jerry.rpc.serialize.impl.JdkSerializer;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * 代码配置->xml配置->默认配置
 */
@Slf4j
@Data
@ToString
public class Configuration {

    public int port = 8090;


    //定义相关的一些基础配置
    private String applicationName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private IdGenerator idGenerator = new IdGenerator(1, 2);
    private String serializeType = "jdk";
    private Serializer serializer = new JdkSerializer();
    private String compressType = "gzip";
    private Compressor compressor = new GzipCompressor();
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();


    public Configuration() {
        SpiResolver spiResolver = new SpiResolver();
        spiResolver.loadFormSpi(this);

        XmlResolver xmlResolver = new XmlResolver();
        //读取xml获取上面的信息
        xmlResolver.loadFormXml(this);

    }



}
