package net.dongliu.apk.parser;

import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.ApkSignStatus;
import net.dongliu.apk.parser.bean.CertificateMeta;
import net.dongliu.apk.parser.bean.DexClass;
import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.parser.*;
import net.dongliu.apk.parser.struct.AndroidConstants;
import net.dongliu.apk.parser.struct.resource.ResourceTable;
import net.dongliu.apk.parser.utils.Utils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.cert.CertificateEncodingException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * ApkParser and result holder.
 * This class is not thread-safe.
 *
 * @author dongliu
 */
public class ApkParser implements Closeable {

    private DexClass[] dexClasses;
    private ResourceTable resourceTable;

    private String manifestXml;
    private ApkMeta apkMeta;
    private Set<Locale> locales;
    private List<CertificateMeta> certificateMetaList;
    private final ZipFile zf;
    private File apkFile;

    /**
     * default use system locale
     */
    private Locale preferredLocale = Locale.getDefault();

    public ApkParser(File apkFile) throws IOException {
        this.apkFile = apkFile;
        this.zf = new ZipFile(apkFile);
    }

    public ApkParser(String filePath) throws IOException {
        this(new File(filePath));
    }

    /**
     * return decoded AndroidManifest.xml
     *
     * @return decoded AndroidManifest.xml
     */
    public String getManifestXml() throws IOException {
        if (this.manifestXml == null) {
            parseManifestXml();
        }
        return this.manifestXml;
    }

    /**
     * return decoded AndroidManifest.xml
     *
     * @return decoded AndroidManifest.xml
     */
    public ApkMeta getApkMeta() throws IOException {
        if (this.apkMeta == null) {
            parseApkMeta();
        }
        return this.apkMeta;
    }

    /**
     * get locales supported from resource file
     *
     * @return decoded AndroidManifest.xml
     * @throws IOException
     */
    public Set<Locale> getLocales() throws IOException {
        if (this.locales == null) {
            parseResourceTable();
        }
        return this.locales;
    }

    /**
     * get the apk's certificates.
     */
    public List<CertificateMeta> getCertificateMetaList() throws IOException,
            CertificateEncodingException {
        if (this.certificateMetaList == null) {
            parseCertificate();
        }
        return this.certificateMetaList;
    }

    private void parseCertificate() throws IOException, CertificateEncodingException {
        ZipArchiveEntry entry = null;
        Enumeration<ZipArchiveEntry> enu = zf.getEntries();
        while (enu.hasMoreElements()) {
            entry = enu.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            if (entry.getName().toUpperCase().endsWith(".RSA")
                    || entry.getName().toUpperCase().endsWith(".DSA")) {
                break;
            }
        }
        if (entry == null) {
            throw new ParserException("ApkParser certificate not found");
        }
        try (InputStream in = zf.getInputStream(entry)) {
            CertificateParser parser = new CertificateParser(in);
            parser.parse();
            this.certificateMetaList = parser.getCertificateMetas();
        }
    }

    /**
     * parse manifest.xml, get apkMeta.
     *
     * @throws IOException
     */
    private void parseApkMeta() throws IOException {
        if (this.manifestXml == null) {
            parseManifestXml();
        }
    }

    /**
     * parse manifest.xml, get manifestXml as xml text.
     *
     * @throws IOException
     */
    private void parseManifestXml() throws IOException {
        XmlTranslator xmlTranslator = new XmlTranslator();
        ApkMetaTranslator translator = new ApkMetaTranslator();
        XmlStreamer xmlStreamer = new CompositeXmlStreamer(xmlTranslator, translator);
        transBinaryXml(AndroidConstants.MANIFEST_FILE, xmlStreamer);
        this.manifestXml = xmlTranslator.getXml();
        if (this.manifestXml == null) {
            throw new ParserException("manifest xml not exists");
        }
        this.apkMeta = translator.getApkMeta();
    }

    /**
     * trans binary xml file to text xml file.
     *
     * @param path the xml file path in apk file
     * @return the text. null if file not exists
     * @throws IOException
     */
    public String transBinaryXml(String path) throws IOException {
        ZipArchiveEntry entry = Utils.getEntry(zf, path);
        if (entry == null) {
            return null;
        }
        if (this.resourceTable == null) {
            parseResourceTable();
        }

        XmlTranslator xmlTranslator = new XmlTranslator();
        transBinaryXml(path, xmlTranslator);
        return xmlTranslator.getXml();
    }


    private void transBinaryXml(String path, XmlStreamer xmlStreamer) throws IOException {
        ZipArchiveEntry entry = Utils.getEntry(zf, path);
        if (entry == null) {
            return;
        }
        if (this.resourceTable == null) {
            parseResourceTable();
        }

        long begin = System.currentTimeMillis();
        InputStream in = zf.getInputStream(entry);
        ByteBuffer buffer = ByteBuffer.wrap(IOUtils.toByteArray(in));
        BinaryXmlParser binaryXmlParser = new BinaryXmlParser(buffer, resourceTable);
        binaryXmlParser.setLocale(preferredLocale);
        binaryXmlParser.setXmlStreamer(xmlStreamer);
        binaryXmlParser.parse();
        System.out.print("parser binary xml: ");
        System.out.println(System.currentTimeMillis() - begin);
    }


    /**
     * get class infos form dex file. currently only class name
     */
    public DexClass[] getDexClasses() throws IOException {
        if (this.dexClasses == null) {
            parseDexFile();
        }
        return this.dexClasses;
    }

    private void parseDexFile() throws IOException {
        ZipArchiveEntry resourceEntry = Utils.getEntry(zf, AndroidConstants.DEX_FILE);
        if (resourceEntry == null) {
            throw new ParserException("Resource table not found");
        }
        InputStream in = zf.getInputStream(resourceEntry);
        ByteBuffer buffer = ByteBuffer.wrap(IOUtils.toByteArray(in));
        DexParser dexParser = new DexParser(buffer);
        dexParser.parse();
        this.dexClasses = dexParser.getDexClasses();
    }

    /**
     * read file in apk into bytes
     *
     * @return
     */
    private byte[] getData(String path) throws IOException {
        ZipArchiveEntry entry = Utils.getEntry(zf, path);
        if (entry == null) {
            return null;
        }

        InputStream inputStream = zf.getInputStream(entry);
        return IOUtils.toByteArray(inputStream);
    }

    /**
     * check apk sign
     *
     * @return
     * @throws IOException
     */
    public ApkSignStatus verifyApk() throws IOException {
        ZipArchiveEntry entry = Utils.getEntry(zf, "META-INF/MANIFEST.MF");
        if (entry == null) {
            // apk is not signed;
            return ApkSignStatus.notSigned;
        }

        JarFile jarFile = new JarFile(this.apkFile);
        Enumeration<JarEntry> entries = jarFile.entries();
        byte[] buffer = new byte[8192];

        while (entries.hasMoreElements()) {
            JarEntry e = entries.nextElement();
            if (e.isDirectory()) {
                continue;
            }
            InputStream in = jarFile.getInputStream(e);
            try {
                // Read in each jar entry. A security exception will be thrown if a signature/digest check fails.
                int count;
                while ((count = in.read(buffer, 0, buffer.length)) != -1) {
                    // Don't care
                }
            } catch (SecurityException se) {
                return ApkSignStatus.incorrect;
            } finally {
                in.close();
            }
        }
        return ApkSignStatus.signed;
    }

    /**
     * parse resource table.
     */
    private void parseResourceTable() throws IOException {
        ZipArchiveEntry entry = Utils.getEntry(zf, AndroidConstants.RESOURCE_FILE);
        if (entry == null) {
            // if no resource entry has been found, we assume it is not needed by this APK
            this.resourceTable = new ResourceTable();
            this.locales = Collections.emptySet();
            return;
        }

        this.resourceTable = new ResourceTable();
        this.locales = Collections.emptySet();

        InputStream in = zf.getInputStream(entry);
        ByteBuffer buffer = ByteBuffer.wrap(IOUtils.toByteArray(in));
        ResourceTableParser resourceTableParser = new ResourceTableParser(buffer);
        //TODO: profile
        long begin = System.currentTimeMillis();
        resourceTableParser.parse();
        System.out.print("parse resource table ");
        System.out.println(System.currentTimeMillis() - begin);
        this.resourceTable = resourceTableParser.getResourceTable();
        this.locales = resourceTableParser.getLocales();
    }

    @Override
    public void close() throws IOException {
        this.certificateMetaList = null;
        this.resourceTable = null;
        this.certificateMetaList = null;
        zf.close();
    }

    public Locale getPreferredLocale() {
        return preferredLocale;
    }

    /**
     * The locale preferred. Will cause getManifestXml / getApkMeta to return different values.
     * The default value is from os default locale setting.
     */
    public void setPreferredLocale(Locale preferredLocale) {
        if (Objects.equals(this.preferredLocale, preferredLocale)) {
            this.preferredLocale = preferredLocale;
            this.manifestXml = null;
            this.apkMeta = null;
        }
    }
}
