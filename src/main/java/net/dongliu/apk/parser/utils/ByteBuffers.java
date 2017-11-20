package net.dongliu.apk.parser.utils;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * utils method for byte buffer
 *
 * @author Liu Dong dongliu@live.cn
 */
public class ByteBuffers {

    /**
     * get one unsigned byte as short type
     */
    public static short readUByte(ByteBuffer buffer) {
        byte b = buffer.get();
        return (short) (b & 0xff);
    }

    /**
     * get one unsigned short as int type
     */
    public static int readUShort(ByteBuffer buffer) {
        short s = buffer.getShort();
        return s & 0xffff;
    }

    /**
     * get one unsigned int as long type
     */
    public static long readUInt(ByteBuffer buffer) {
        int i = buffer.getInt();
        return i & 0xffffffffL;
    }

    /**
     * get bytes
     */
    public static byte[] readBytes(ByteBuffer buffer, int size) {
        byte[] bytes = new byte[size];
        buffer.get(bytes);
        return bytes;
    }


    /**
     * read utf16 strings, use strLen, not ending 0 char.
     */
    public static String readString(ByteBuffer buffer, int strLen) {
        StringBuilder sb = new StringBuilder(strLen);
        for (int i = 0; i < strLen; i++) {
            sb.append(buffer.getChar());
        }
        return sb.toString();
    }

    /**
     * read utf16 strings, ending with 0 char.
     */
    public static String readZeroTerminatedString(ByteBuffer buffer, int strLen) {
        StringBuilder sb = new StringBuilder(strLen);
        for (int i = 0; i < strLen; i++) {
            char c = buffer.getChar();
            if (c == '\0') {
                skip(buffer, (strLen - i - 1) * 2);
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * skip count bytes
     */
    public static void skip(ByteBuffer buffer, int count) {
        position(buffer, buffer.position() + count);
    }

    // Cast java.nio.ByteBuffer instances where necessary to java.nio.Buffer to avoid NoSuchMethodError
    // when running on Java 6 to Java 8.
    // The Java 9 ByteBuffer classes introduces overloaded methods with covariant return types the following methods:
    // position, limit, flip, clear, mark, reset, rewind, etc.


    /**
     * set position
     */
    public static void position(ByteBuffer buffer, int position) {
        ((Buffer) buffer).position(position);
    }

    /**
     * set position
     */
    public static void position(ByteBuffer buffer, long position) {
        if (position > Integer.MAX_VALUE) {
            throw new ArithmeticException("position overflow int: " + position);
        }
        ((Buffer) buffer).position((int) position);
    }
}
