package net.dongliu.apk.parser.utils;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

public class Utils {

    public static byte[] toByteArray(InputStream in) throws IOException {
        try {
            byte[] buf = new byte[1024];
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                int len;
                while ((len = in.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }
                return bos.toByteArray();
            }
        } finally {
            in.close();
        }
    }

    public static ZipArchiveEntry getEntry(ZipFile zf, String path) {
        Enumeration<ZipArchiveEntry> enu = zf.getEntries();
        while (enu.hasMoreElements()) {
            ZipArchiveEntry entry = enu.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            if (entry.getName().equals(path)) {
                return entry;
            }

        }
        return null;
    }
}
