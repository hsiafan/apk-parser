package net.dongliu.apk.parser;

import net.dongliu.apk.parser.bean.ApkSignStatus;
import net.dongliu.apk.parser.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Parse apk file from byte array.
 * This class is not thread-safe
 *
 * @author Liu Dong
 */
public class ByteArrayApkParser extends AbstractApkParser implements Closeable {

    private byte[] apkData;

    public ByteArrayApkParser(byte[] apkData) {
        this.apkData = apkData;
    }

    @Override
    protected byte[] getCertificateData() throws IOException {
        try (InputStream in = new ByteArrayInputStream(apkData);
             ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().toUpperCase().endsWith(".RSA") || entry.getName().toUpperCase().endsWith(".DSA")) {
                    return Utils.toByteArray(zis);
                }
            }
        }
        return null;
    }

    @Override
    public byte[] getFileData(String path) throws IOException {
        try (InputStream in = new ByteArrayInputStream(apkData);
             ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (path.equals(entry.getName())) {
                    return Utils.toByteArray(zis);
                }
            }
        }
        return null;
    }

    @Override
    public ApkSignStatus verifyApk() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.apkData = null;
    }
}
