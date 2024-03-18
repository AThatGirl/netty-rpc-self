package com.cj.jerry.rpc.compress.impl;

import com.cj.jerry.rpc.compress.Compressor;
import io.netty.handler.codec.compression.GzipOptions;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class GzipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {


        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos);) {
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            byte[] result = baos.toByteArray();
            log.info("调用gzip压缩，原:{}, 压缩后:{}", bytes.length, result.length);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public byte[] decompress(byte[] bytes) {
        try (ByteArrayInputStream baos = new ByteArrayInputStream(bytes);
             GZIPInputStream gzipOutputStream = new GZIPInputStream(baos);) {
            byte[] result = gzipOutputStream.readAllBytes();
            log.info("调用gzip解压缩，原:{}, 解压缩后:{}", bytes.length, result.length);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
