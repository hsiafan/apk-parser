package net.dongliu.apk.parser.utils;

/**
 * Unsigned utils, for compatible with java6/java7.
 */
public class Unsigned {
    public static long toLong(final int value) {
        return value & 0xffffffffL;
    }

    public static int toUInt(final long value) {
        return (int) value;
    }

    public static int toInt(final short value) {
        return value & 0xffff;
    }

    public static short toUShort(final int value) {
        return (short) value;
    }

    public static int ensureUInt(final long value) {
        if (value < 0 || value > Integer.MAX_VALUE) {
            throw new ArithmeticException("unsigned integer overflow");
        }
        return (int) value;
    }


    public static long ensureULong(final long value) {
        if (value < 0) {
            throw new ArithmeticException("unsigned long overflow");
        }
        return value;
    }

    public static short toShort(final byte value) {
        return (short) (value & 0xff);
    }

    public static byte toUByte(final short value) {
        return (byte) value;
    }
}
