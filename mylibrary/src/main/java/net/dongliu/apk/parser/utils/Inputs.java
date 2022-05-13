package net.dongliu.apk.parser.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Inputs {

    public static byte[] readAll(final InputStream in) throws IOException {
        final byte[] buf = new byte[1024 * 8];
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            int len;
            while ((len = in.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            return bos.toByteArray();
        }
    }

    public static byte[] readAllAndClose(final InputStream in) throws IOException {
        try (in) {
            return Inputs.readAll(in);
        }
    }
}
