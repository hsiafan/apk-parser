package net.dongliu.apk.parser;

import net.dongliu.apk.parser.bean.ApkSignStatus;
import net.dongliu.apk.parser.utils.Inputs;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.ByteBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Parse apk file from byte array.
 * This class is not thread-safe
 *
 * @author Liu Dong
 */
public class ByteArrayApkFile extends AbstractApkFile implements Closeable {

    private byte[] apkData;

    public ByteArrayApkFile(byte[] apkData) {
        this.apkData = apkData;
    }

    @Override
    protected Map<String, byte[]> getAllCertificateData() throws IOException {
        Map<String, byte[]> map = new LinkedHashMap<>();
        try (InputStream in = new ByteArrayInputStream(apkData);
             ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.toUpperCase().endsWith(".RSA") || name.toUpperCase().endsWith(".DSA")) {
                    map.put(name, Inputs.toByteArray(zis));
                }
            }
        }
        return map;
    }

    @Override
    public byte[] getFileData(String path) throws IOException {
        try (InputStream in = new ByteArrayInputStream(apkData);
             ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (path.equals(entry.getName())) {
                    return Inputs.toByteArray(zis);
                }
            }
        }
        return null;
    }

    @Override
    protected ByteBuffer fileData() throws IOException {
        return ByteBuffer.wrap(apkData).asReadOnlyBuffer();
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
