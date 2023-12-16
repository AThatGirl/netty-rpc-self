package com.cj;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;



public class NettyTest {

    @Test
    public void testByteBuf() {
        ByteBuf header = Unpooled.buffer();
        ByteBuf body = Unpooled.buffer();
        //逻辑组装byteBuf
        CompositeByteBuf byteBuf = Unpooled.compositeBuffer();
        byteBuf.addComponents(header, body);
    }

}
