package net.dongliu.apk.parser.utils;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class BuffersTest {

    @Test
    public void testGetUnsignedByte() throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[]{2, -10});
        assertEquals(2, Buffers.readUByte(byteBuffer));
        assertEquals(246, Buffers.readUByte(byteBuffer));
    }
}