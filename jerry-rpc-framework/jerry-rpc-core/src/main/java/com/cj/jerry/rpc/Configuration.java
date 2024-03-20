package com.cj.jerry.rpc;


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
        //读取xml获取上面的信息
        loadFormXml(this);
    }

    private void loadFormXml(Configuration configuration) {

        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("jrpc.xml");
            Document document = builder.parse(inputStream);

            //获取xpath解析器
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();
            configuration.setPort(resolvePort(document, xpath));
            configuration.setApplicationName(resolveApplicationName(document, xpath));
            configuration.setIdGenerator(resolveIdGenerator(document, xpath));
            configuration.setLoadBalancer(resolveLoadBalancer(document, xpath));
            configuration.setRegistryConfig(resolveRegistryConfig(document, xpath));
            configuration.setSerializeType(resolveSerializeType(document, xpath));
            configuration.setSerializer(resolveSerializer(document, xpath));
            configuration.setCompressType(resolveCompressType(document, xpath));
            configuration.setCompressor(resolveCompressor(document, xpath));
            System.out.println(this);
        } catch (Exception e) {
            log.error("加载xml配置失败", e);
        }
    }

    private int resolvePort(Document document, XPath xpath) {
        String port = parseString(document, xpath, "/configuration/port");
        if (port == null) {
            throw new RuntimeException("未配置端口");
        }
        return Integer.parseInt(port);
    }


    private String resolveApplicationName(Document document, XPath xpath) {
        return parseString(document, xpath, "/configuration/applicationName");
    }

    private IdGenerator resolveIdGenerator(Document document, XPath xpath) {

        String aClass = parseString(document, xpath, "/configuration/idGenerator", "class");
        String dataCenterId = parseString(document, xpath, "/configuration/idGenerator", "dataCenterId");
        String machineId = parseString(document, xpath, "/configuration/idGenerator", "machineId");
        Object instance = null;
        try {
            Class<?> clazz = Class.forName(aClass);
            instance = clazz.getConstructor(new Class[]{long.class, long.class})
                    .newInstance(Long.parseLong(dataCenterId), Long.parseLong(machineId));
        } catch (InstantiationException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return (IdGenerator) instance;
    }

    private RegistryConfig resolveRegistryConfig(Document document, XPath xpath) {
        String url = parseString(document, xpath, "/configuration/registry", "url");
        return new RegistryConfig(url);
    }
    private LoadBalancer resolveLoadBalancer(Document document, XPath xpath) {
        return parseObject(document, xpath, "/configuration/loadBalancer", null);
    }
    private String resolveCompressType(Document document, XPath xpath) {
        return parseString(document, xpath, "/configuration/compressType", "type");
    }

    private Compressor resolveCompressor(Document document, XPath xpath) {
        return parseObject(document, xpath, "/configuration/compressor", null);
    }

    private String resolveSerializeType(Document document, XPath xpath) {
        return parseString(document, xpath, "/configuration/serializeType", "type");
    }

    private Serializer resolveSerializer(Document document, XPath xpath) {
        return parseObject(document, xpath, "/configuration/serializer", null);
    }

    private String resolveProtocolType(Document document, XPath xpath) {
        return parseString(document, xpath, "/configuration/protocol", "type");
    }


    //获得一个节点属性的值返回字符串
    private String parseString(Document document, XPath xpath, String expression) {
        //解析一个表达式
        try {
            XPathExpression expr = xpath.compile(expression);
            Node targetNode = (Node) expr.evaluate(document, XPathConstants.NODE);
            return targetNode.getTextContent();
        } catch (XPathExpressionException e) {
            log.error("获取节点属性异常", e);
        }
        return null;
    }

    //获得一个节点属性的值返回字符串
    private String parseString(Document document, XPath xpath, String expression, String attributeName) {
        //解析一个表达式
        try {
            XPathExpression expr = xpath.compile(expression);
            Node targetNode = (Node) expr.evaluate(document, XPathConstants.NODE);
            Node node = targetNode.getAttributes().getNamedItem(attributeName);
            String className = node.getNodeValue();
            log.info("className:{}", className);
            return className;
        } catch (XPathExpressionException e) {
            log.error("获取节点属性异常", e);
        }
        return null;
    }

    //解析一个节点返回一个实例
    private <T> T parseObject(Document document, XPath xpath, String expression, Class<?>[] paramType, Object... param) {
        //解析一个表达式
        try {
            XPathExpression expr = xpath.compile(expression);
            Node targetNode = (Node) expr.evaluate(document, XPathConstants.NODE);
            Node node = targetNode.getAttributes().getNamedItem("class");
            String className = node.getNodeValue();
            log.info("className:{}", className);
            Class<?> aClass = Class.forName(className);
            Object instance = null;
            if (paramType == null) {
                instance = aClass.getConstructor().newInstance();
            } else {
                instance = aClass.getConstructor(paramType).newInstance(param);
            }
            return (T) instance;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException | XPathExpressionException e) {
            log.error("解析表达式发生异常", e);
        }
        return null;
    }


}
