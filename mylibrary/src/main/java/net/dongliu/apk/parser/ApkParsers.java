package net.dongliu.apk.parser;

import net.dongliu.apk.parser.bean.ApkMeta;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Convenient utils method for parse apk file
 *
 * @author Liu Dong
 */
public class ApkParsers {

    private static boolean useBouncyCastle;

    public static boolean useBouncyCastle() {
        return ApkParsers.useBouncyCastle;
    }

    /**
     * Use BouncyCastle instead of JSSE to parse X509 certificate.
     * If want to use BouncyCastle, you will also need to add bcprov and bcpkix lib to your project.
     */
    public static void useBouncyCastle(final boolean useBouncyCastle) {
        ApkParsers.useBouncyCastle = useBouncyCastle;
    }

    /**
     * Get apk meta info for apk file
     *
     * @throws IOException
     */
    public static ApkMeta getMetaInfo(final String apkFilePath) throws IOException {
        try (final ApkFile apkFile = new ApkFile(apkFilePath)) {
            return apkFile.getApkMeta();
        }
    }

    /**
     * Get apk meta info for apk file
     *
     * @throws IOException
     */
    public static ApkMeta getMetaInfo(final File file) throws IOException {
        try (final ApkFile apkFile = new ApkFile(file)) {
            return apkFile.getApkMeta();
        }
    }

    /**
     * Get apk meta info for apk file
     *
     * @throws IOException
     */
    public static ApkMeta getMetaInfo(final byte[] apkData) throws IOException {
        try (final ByteArrayApkFile apkFile = new ByteArrayApkFile(apkData)) {
            return apkFile.getApkMeta();
        }
    }

    /**
     * Get apk meta info for apk file, with locale
     *
     * @throws IOException
     */
    public static ApkMeta getMetaInfo(final String apkFilePath, final Locale locale) throws IOException {
        try (final ApkFile apkFile = new ApkFile(apkFilePath)) {
            apkFile.setPreferredLocale(locale);
            return apkFile.getApkMeta();
        }
    }

    /**
     * Get apk meta info for apk file
     *
     * @throws IOException
     */
    public static ApkMeta getMetaInfo(final File file, final Locale locale) throws IOException {
        try (final ApkFile apkFile = new ApkFile(file)) {
            apkFile.setPreferredLocale(locale);
            return apkFile.getApkMeta();
        }
    }

    /**
     * Get apk meta info for apk file
     *
     * @throws IOException
     */
    public static ApkMeta getMetaInfo(final byte[] apkData, final Locale locale) throws IOException {
        try (final ByteArrayApkFile apkFile = new ByteArrayApkFile(apkData)) {
            apkFile.setPreferredLocale(locale);
            return apkFile.getApkMeta();
        }
    }

    /**
     * Get apk manifest xml file as text
     *
     * @throws IOException
     */
    public static String getManifestXml(final String apkFilePath) throws IOException {
        try (final ApkFile apkFile = new ApkFile(apkFilePath)) {
            return apkFile.getManifestXml();
        }
    }

    /**
     * Get apk manifest xml file as text
     *
     * @throws IOException
     */
    public static String getManifestXml(final File file) throws IOException {
        try (final ApkFile apkFile = new ApkFile(file)) {
            return apkFile.getManifestXml();
        }
    }

    /**
     * Get apk manifest xml file as text
     *
     * @throws IOException
     */
    public static String getManifestXml(final byte[] apkData) throws IOException {
        try (final ByteArrayApkFile apkFile = new ByteArrayApkFile(apkData)) {
            return apkFile.getManifestXml();
        }
    }

    /**
     * Get apk manifest xml file as text
     *
     * @throws IOException
     */
    public static String getManifestXml(final String apkFilePath, final Locale locale) throws IOException {
        try (final ApkFile apkFile = new ApkFile(apkFilePath)) {
            apkFile.setPreferredLocale(locale);
            return apkFile.getManifestXml();
        }
    }

    /**
     * Get apk manifest xml file as text
     *
     * @throws IOException
     */
    public static String getManifestXml(final File file, final Locale locale) throws IOException {
        try (final ApkFile apkFile = new ApkFile(file)) {
            apkFile.setPreferredLocale(locale);
            return apkFile.getManifestXml();
        }
    }

    /**
     * Get apk manifest xml file as text
     *
     * @throws IOException
     */
    public static String getManifestXml(final byte[] apkData, final Locale locale) throws IOException {
        try (final ByteArrayApkFile apkFile = new ByteArrayApkFile(apkData)) {
            apkFile.setPreferredLocale(locale);
            return apkFile.getManifestXml();
        }
    }
}
