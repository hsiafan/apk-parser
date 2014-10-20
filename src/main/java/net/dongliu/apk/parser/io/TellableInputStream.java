package net.dongliu.apk.parser.io;

import net.dongliu.apk.parser.struct.ByteOrder;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream which can tell it's current pos.
 *
 * @author dongliu
 */
public class TellableInputStream {
    private InputStream in;
    private ByteOrder byteOrder;
    private long pos;

    public TellableInputStream(InputStream in, ByteOrder byteOrder) {
        if (in instanceof BufferedInputStream) {
            this.in = in;
        } else {
            this.in = new BufferedInputStream(in);
        }
        this.byteOrder = byteOrder;
    }

    public int read() throws IOException {
        int ret = in.read();
        if (ret != -1) {
            pos++;
        }
        return ret;
    }

    private int read(byte b[]) throws IOException {
        int ret = in.read(b);
        if (ret != -1) {
            pos += ret;
        }
        return ret;
    }

    private int read(byte b[], int off, int len) throws IOException {
        int ret = in.read(b, off, len);
        if (ret != -1) {
            pos += ret;
        }
        return ret;
    }

    private long _skip(long n) throws IOException {
        long ret = this.in.skip(n);
        if (ret != -1) {
            pos += ret;
        }
        return ret;
    }

    public void close() throws IOException {
        in.close();
    }

    /**
     * the bytes have been readed
     *
     * @return
     */
    public long tell() {
        return this.pos;
    }

    /**
     * seek to pos.
     *
     * @param pos
     */
    public void advanceToPos(long pos) throws IOException {
        if (this.pos < pos) {
            skip((int) (pos - this.pos));
        } else if (this.pos > pos) {
            throw new IOException("target pos less the current");
        }
    }

    /**
     * read bytes from input stream, the bytes size equals with len param. if get EOF before
     * read enough data, throw IOException.
     */
    public byte[] readBytes(int len) throws IOException {
        byte[] bytes = new byte[len];
        int readed = 0;
        while (readed < len) {
            int read = read(bytes, readed, len - readed);
            if (read == -1) {
                throw new EOFException("UnExpected EOF");
            }
            readed += read;
        }
        return bytes;
    }

    /**
     * read one unsigned byte.
     *
     * @return
     * @throws IOException
     */
    public short readUByte() throws IOException {
        int ret = read();
        if (ret == -1) {
            throw new EOFException("UnExpected EOF");
        }
        return (short) ret;
    }

    /**
     * skip n bytes.
     *
     * @param len
     * @throws IOException
     */
    public void skip(int len) throws IOException {
        readBytes(len);
    }

    /**
     * read int from input stream. if get EOF before read enough data, throw IOException.
     */
    public int readInt() throws IOException {
        byte[] bytes = readBytes(4);
        if (byteOrder == ByteOrder.BIG) {
            return makeInt(bytes[0], bytes[1], bytes[2], bytes[3]);
        } else {
            return makeInt(bytes[3], bytes[2], bytes[1], bytes[0]);
        }
    }

    /**
     * read unsigned int from input stream. if get EOF before read enough data, throw IOException.
     */
    public long readUInt() throws IOException {
        byte[] bytes = readBytes(4);
        if (byteOrder == ByteOrder.BIG) {
            return makeUInt(bytes[0], bytes[1], bytes[2], bytes[3]);
        } else {
            return makeUInt(bytes[3], bytes[2], bytes[1], bytes[0]);
        }
    }

    /**
     * read unsigned short from input stream. if get EOF before read enough data, throw IOException.
     */
    public int readUShort() throws IOException {
        byte[] bytes = readBytes(2);
        if (byteOrder == ByteOrder.BIG) {
            return makeUShort(bytes[0], bytes[1]);
        } else {
            return makeUShort(bytes[1], bytes[0]);
        }
    }

    /**
     * read utf16 strings, use strLen, not ending 0 char.
     *
     * @param strLen
     * @return
     * @throws IOException
     */
    public String readStringUTF16(int strLen) throws IOException {
        byte[] bytes = readBytes(strLen * 2);
        if (byteOrder == ByteOrder.LITTLE) {
            for (int i = 0; i < strLen; i++) {
                swap(bytes, 2 * i, 2 * i + 1);
            }
        }
        return new String(bytes, "UTF-16");
    }

    private void swap(byte[] bytes, int i, int j) {
        byte temp = bytes[i];
        bytes[i] = bytes[j];
        bytes[j] = temp;
    }

    /**
     * read bytes as ascii chars.
     *
     * @param len
     * @return
     */
    public String readChars(int len) throws IOException {
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = (char) readUByte();
        }
        return new String(chars);
    }

    /**
     * bytes to unsigned int.
     */
    private long makeUInt(byte b3, byte b2, byte b1, byte b0) {
        long l = 0;
        l |= (long) (b3 & 0xff) << 24;
        l |= (b2 & 0xff) << 16;
        l |= (b1 & 0xff) << 8;
        l |= (b0 & 0xff);
        return l;
    }

    /**
     * bytes to unsigned int.
     */
    private int makeInt(byte b3, byte b2, byte b1, byte b0) {
        int i = 0;
        i |= (b3 & 0xff) << 24;
        i |= (b2 & 0xff) << 16;
        i |= (b1 & 0xff) << 8;
        i |= (b0 & 0xff);
        return i;
    }

    /**
     * bytes to unsigned short.
     */
    private int makeUShort(byte b1, byte b0) {
        int i = 0;
        i |= (b1 & 0xff) << 8;
        i |= (b0 & 0xff);
        return i;
    }
}
