package net.dongliu.apk.parser;

import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.ApkSigner;
import net.dongliu.apk.parser.bean.CertificateMeta;
import org.junit.Test;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Locale;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApkFileTest {

    @Test
    public void testParserMeta() throws IOException {
        String path = getClass().getClassLoader().getResource("apks/Twitter_v7.93.2.apk").getPath();
        try (ApkFile apkFile = new ApkFile(path)) {
            apkFile.setPreferredLocale(Locale.ENGLISH);
            ApkMeta apkMeta = apkFile.getApkMeta();
            assertEquals("Twitter", apkMeta.getLabel());
        }
    }

    @Test
    public void testParserMeta_Type_0204() throws IOException {
        String path = getClass().getClassLoader().getResource("apks/NetworkStack_210000000.apk").getPath();
        try (ApkFile apkFile = new ApkFile(path)) {
            apkFile.setPreferredLocale(Locale.ENGLISH);
            ApkMeta apkMeta = apkFile.getApkMeta();
            assertEquals("NetworkStack", apkMeta.getLabel());
        }
    }

    @Test
    public void testGetSignature() throws IOException, CertificateException {
        String path = getClass().getClassLoader().getResource("apks/Twitter_v7.93.2.apk").getPath();
        try (ApkFile apkFile = new ApkFile(path)) {
            List<ApkSigner> apkSingers = apkFile.getApkSingers();
            assertEquals(1, apkSingers.size());
            ApkSigner apkSigner = apkSingers.get(0);
            assertEquals("META-INF/CERT.RSA", apkSigner.getPath());
            List<CertificateMeta> certificateMetas = apkSigner.getCertificateMetas();
            assertEquals(1, certificateMetas.size());
            CertificateMeta certificateMeta = certificateMetas.get(0);
            assertEquals("69ee076cc84f4d94802d61907b07525f", certificateMeta.getCertMd5());
        }
    }

    @Test
    public void testAppIsNotDebuggable() throws IOException {
        String path = getClass().getClassLoader().getResource("apks/app-release.apk").getPath();
        try (ApkFile apkFile = new ApkFile(path)) {
            ApkMeta apkMeta = apkFile.getApkMeta();
            assertFalse(apkMeta.isDebuggable());
        }
    }

    @Test
    public void testAppIsDebuggable() throws IOException {
        String path = getClass().getClassLoader().getResource("apks/app-debug.apk").getPath();
        try (ApkFile apkFile = new ApkFile(path)) {
            ApkMeta apkMeta = apkFile.getApkMeta();
            assertTrue(apkMeta.isDebuggable());
        }
    }
}
