package net.dongliu.apk.parser.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {

    public static byte[] toByteArray(InputStream in) throws IOException {
        try {
            byte[] buf = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                int len;
                while ((len = in.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }
                return bos.toByteArray();
            } finally {
                bos.close();
            }
        } finally {
            in.close();
        }
    }

    public static String toString(InputStream in) throws IOException {
        return new String(toByteArray(in), "UTF-8");
    }

    public static String toString(InputStream in, String encoding) throws IOException {
        return new String(toByteArray(in), encoding);
    }

    public static long toLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            return 0;
        }
    }

    public static int toInt(String value) {
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String between(String text, String begin, String end) {
        if (text == null) {
            return null;
        }
        int idx = text.indexOf(begin);
        if (idx < 0) {
            idx = 0;
        } else {
            idx = idx + begin.length();
        }
        int eidx = text.indexOf(end, idx);
        if (eidx < 0) {
            eidx = 0;
        }
        return text.substring(idx, eidx);
    }
}
