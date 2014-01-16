package net.dongliu.apk.parser;

import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.CertificateMeta;
import net.dongliu.apk.parser.bean.Locale;
import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.struct.AndroidFiles;
import net.dongliu.apk.parser.struct.dex.DexClass;
import net.dongliu.apk.parser.struct.resource.ResourceTable;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * convenient methods for apk parser
 *
 * @author dongliu
 */
public class ApkParserUtils {

    /**
     * Decode binary XML files
     */
    public static String getManifestXml(String apkFile, Locale locale) throws IOException {
        ZipFile zf = null;
        try {
            zf = new ZipFile(apkFile);
            ZipArchiveEntry manifestEntry = null;
            ZipArchiveEntry resourceEntry = null;
            Enumeration<ZipArchiveEntry> enu = zf.getEntries();
            while (enu.hasMoreElements()) {
                ZipArchiveEntry entry = enu.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                if (entry.getName().equals(AndroidFiles.RESOURCE)) {
                    resourceEntry = entry;
                }
                if (entry.getName().equals(AndroidFiles.MANIFEST)) {
                    manifestEntry = entry;
                }

            }
            ResourceTableParser resourceTableParser = new ResourceTableParser(
                    zf.getInputStream(resourceEntry));
            resourceTableParser.parse();
            ResourceTable resourceTable = resourceTableParser.getResourceTable();

            BinaryXmlParser binaryXmlParser = new BinaryXmlParser(zf.getInputStream(manifestEntry),
                    resourceTable);
            binaryXmlParser.setLocale(locale);
            binaryXmlParser.parse();
            return binaryXmlParser.getXml();
        } finally {
            ZipFile.closeQuietly(zf);
        }

    }


    /**
     * Decode binary XML files
     */
    public static ApkMeta getApkMeta(String apkFile, Locale locale) throws IOException {
        ZipFile zf = null;
        try {
            zf = new ZipFile(apkFile);
            ZipArchiveEntry manifestEntry = null;
            ZipArchiveEntry resourceEntry = null;
            Enumeration<ZipArchiveEntry> enu = zf.getEntries();
            while (enu.hasMoreElements()) {
                ZipArchiveEntry entry = enu.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                if (entry.getName().equals(AndroidFiles.RESOURCE)) {
                    resourceEntry = entry;
                }
                if (entry.getName().equals(AndroidFiles.MANIFEST)) {
                    manifestEntry = entry;
                }

            }
            ResourceTableParser resourceTableParser = new ResourceTableParser(
                    zf.getInputStream(resourceEntry));
            resourceTableParser.parse();
            ResourceTable resourceTable = resourceTableParser.getResourceTable();

            BinaryXmlParser binaryXmlParser = new BinaryXmlParser(zf.getInputStream(manifestEntry),
                    resourceTable);
            binaryXmlParser.setLocale(locale);
            ApkMetaParserReader apkMetaParserReader = new ApkMetaParserReader();
            binaryXmlParser.setXmlStreamReader(apkMetaParserReader);
            binaryXmlParser.parse();
            return apkMetaParserReader.getApkMeta();
        } finally {
            ZipFile.closeQuietly(zf);
        }

    }


    /**
     * get apk cetificate info.
     *
     * @param apkPath
     * @return null or empty if no cetificate in apk, otherwise the cetificate list.
     * @throws IOException
     */
    public static List<CertificateMeta> getCertificates(String apkPath) throws IOException {

        ZipArchiveEntry entry;
        ZipFile zf = null;
        try {
            zf = new ZipFile(apkPath);
            Enumeration<ZipArchiveEntry> enu = zf.getEntries();
            while (enu.hasMoreElements()) {
                entry = enu.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                if (!entry.getName().toUpperCase().endsWith(".RSA")
                        && !entry.getName().toUpperCase().endsWith(".DSA")) {
                    continue;
                }
                CetificateParser parser = new CetificateParser(zf.getInputStream(entry));
                parser.parse();
                return parser.getCertificateMetas();
            }
            return null;
        } catch (CertificateEncodingException e) {
            throw new ParserException(e.getMessage());
        } finally {
            ZipFile.closeQuietly(zf);
        }

    }

    /**
     * get all package name in dex file.
     *
     * @param apkPath
     * @return null or empty if no cetificate in apk, otherwise the cetificate list.
     * @throws IOException
     */
    public static Set<String> listPackages(String apkPath) throws IOException {

        ZipArchiveEntry entry;
        ZipFile zf = null;
        try {
            zf = new ZipFile(apkPath);
            Enumeration<ZipArchiveEntry> enu = zf.getEntries();
            while (enu.hasMoreElements()) {
                entry = enu.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                if (entry.getName().equals(AndroidFiles.DEX)) {
                    DexParser parser = new DexParser(zf.getInputStream(entry));
                    parser.parse();
                    DexClass[] dexClasses = parser.getDexClasses();
                    Set<String> packageNameSet = new HashSet<String>();
                    for (DexClass dexClass : dexClasses) {
                        //remove first 'L' and chars after the last '/'
                        String type = dexClass.classType;
                        if (type != null && !type.isEmpty()) {
                            int idx = type.lastIndexOf('/');
                            if (idx == -1) {
                                idx = type.length();
                            }
                            type = dexClass.classType.substring(1, idx).replace('/', '.');
                            packageNameSet.add(type);
                        }
                    }
                    return packageNameSet;
                }
            }
            return null;
        } finally {
            ZipFile.closeQuietly(zf);
        }

    }

    public static void main(String args[]) throws IOException {
        String file = args[0];

        // Parse Binary XML
        System.out.println(getApkMeta(file, Locale.zh_CN));
    }
}