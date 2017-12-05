package net.dongliu.apk.parser;

import net.dongliu.apk.parser.bean.*;
import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.parser.*;
import net.dongliu.apk.parser.struct.AndroidConstants;
import net.dongliu.apk.parser.struct.resource.Densities;
import net.dongliu.apk.parser.struct.resource.ResourceTable;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.cert.CertificateException;
import java.util.*;

import static java.lang.System.arraycopy;

/**
 * Common Apk Parser methods.
 * This Class is not thread-safe.
 *
 * @author Liu Dong
 */
public abstract class AbstractApkFile implements Closeable {
    private DexClass[] dexClasses;

    private boolean resourceTableParsed;
    private ResourceTable resourceTable;
    private Set<Locale> locales;

    private boolean manifestParsed;
    private String manifestXml;
    private ApkMeta apkMeta;
    private List<IconPath> iconPaths;

    private Map<String, List<CertificateMeta>> certificateMetaMap;

    private static final Locale DEFAULT_LOCALE = Locale.US;

    /**
     * default use empty locale
     */
    private Locale preferredLocale = DEFAULT_LOCALE;

    /**
     * return decoded AndroidManifest.xml
     *
     * @return decoded AndroidManifest.xml
     */
    public String getManifestXml() throws IOException {
        parseManifest();
        return this.manifestXml;
    }

    /**
     * return decoded AndroidManifest.xml
     *
     * @return decoded AndroidManifest.xml
     */
    public ApkMeta getApkMeta() throws IOException {
        parseManifest();
        return this.apkMeta;
    }

    /**
     * get locales supported from resource file
     *
     * @return decoded AndroidManifest.xml
     * @throws IOException
     */
    public Set<Locale> getLocales() throws IOException {
        parseResourceTable();
        return this.locales;
    }

    /**
     * Get the apk's certificate meta. If have multi signature, return the certificate the first signature used.
     */
    public List<CertificateMeta> getCertificateMetaList() throws IOException,
            CertificateException {
        if (certificateMetaMap == null) {
            parseCertificates();
        }
        if (certificateMetaMap.isEmpty()) {
            throw new ParserException("ApkFile certificate not found");
        }
        return certificateMetaMap.values().iterator().next();
    }

    /**
     * Get the apk's all certificates.
     * For each entry, the key is certificate file path in apk file, the value is the certificates info of the certificate file.
     */
    public Map<String, List<CertificateMeta>> getAllCertificateMetas() throws IOException, CertificateException {
        if (certificateMetaMap == null) {
            parseCertificates();
        }
        return this.certificateMetaMap;
    }

    protected abstract Map<String, byte[]> getAllCertificateData() throws IOException;

    private void parseCertificates() throws IOException, CertificateException {
        Map<String, byte[]> dataMap = getAllCertificateData();
        certificateMetaMap = new LinkedHashMap<>();
        for (Map.Entry<String, byte[]> entry : dataMap.entrySet()) {
            CertificateParser parser = new CertificateParser(entry.getValue());
            parser.parse();
            certificateMetaMap.put(entry.getKey(), parser.getCertificateMetas());
        }
    }

    private void parseManifest() throws IOException {
        if (manifestParsed) {
            return;
        }
        parseResourceTable();
        XmlTranslator xmlTranslator = new XmlTranslator();
        ApkMetaTranslator apkTranslator = new ApkMetaTranslator(this.resourceTable, this.preferredLocale);
        XmlStreamer xmlStreamer = new CompositeXmlStreamer(xmlTranslator, apkTranslator);

        byte[] data = getFileData(AndroidConstants.MANIFEST_FILE);
        if (data == null) {
            throw new ParserException("Manifest file not found");
        }
        transBinaryXml(data, xmlStreamer);
        this.manifestXml = xmlTranslator.getXml();
        this.apkMeta = apkTranslator.getApkMeta();
        this.iconPaths = apkTranslator.getIconPaths();
        manifestParsed = true;
    }

    /**
     * read file in apk into bytes
     */
    public abstract byte[] getFileData(String path) throws IOException;


    /**
     * trans binary xml file to text xml file.
     *
     * @param path the xml file path in apk file
     * @return the text. null if file not exists
     * @throws IOException
     */
    public String transBinaryXml(String path) throws IOException {
        byte[] data = getFileData(path);
        if (data == null) {
            return null;
        }
        parseResourceTable();

        XmlTranslator xmlTranslator = new XmlTranslator();
        transBinaryXml(data, xmlTranslator);
        return xmlTranslator.getXml();
    }

    private void transBinaryXml(byte[] data, XmlStreamer xmlStreamer) throws IOException {
        parseResourceTable();

        ByteBuffer buffer = ByteBuffer.wrap(data);
        BinaryXmlParser binaryXmlParser = new BinaryXmlParser(buffer, resourceTable);
        binaryXmlParser.setLocale(preferredLocale);
        binaryXmlParser.setXmlStreamer(xmlStreamer);
        binaryXmlParser.parse();
    }

    /**
     * Get the default apk icon file.
     */
    public Icon getIconFile() throws IOException {
        ApkMeta apkMeta = getApkMeta();
        String iconPath = apkMeta.getIcon();
        if (iconPath == null) {
            return null;
        }
        return new Icon(iconPath, Densities.DEFAULT, getFileData(iconPath));
    }

    /**
     * Get all the icon paths, for different densities.
     */
    public List<IconPath> getIconPaths() throws IOException {
        parseManifest();
        return this.iconPaths;
    }

    /**
     * Get all the icons, for different densities.
     */
    public List<Icon> getIconFiles() throws IOException {
        List<IconPath> iconPaths = getIconPaths();
        List<Icon> icons = new ArrayList<>(iconPaths.size());
        for (IconPath iconPath : iconPaths) {
            Icon icon = new Icon(iconPath.getPath(), iconPath.getDensity(), getFileData(iconPath.getPath()));
            icons.add(icon);
        }
        return icons;
    }

    /**
     * get class infos form dex file. currently only class name
     */
    public DexClass[] getDexClasses() throws IOException {
        if (this.dexClasses == null) {
            parseDexFiles();
        }
        return this.dexClasses;
    }

    private DexClass[] mergeDexClasses(DexClass[] first, DexClass[] second) {
        DexClass[] result = new DexClass[first.length + second.length];
        arraycopy(first, 0, result, 0, first.length);
        arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private DexClass[] parseDexFile(String path) throws IOException {
        byte[] data = getFileData(path);
        if (data == null) {
            String msg = String.format("Dex file %s not found", path);
            throw new ParserException(msg);
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        DexParser dexParser = new DexParser(buffer);
        dexParser.parse();
        return dexParser.getDexClasses();
    }

    private void parseDexFiles() throws IOException {
        this.dexClasses = parseDexFile(AndroidConstants.DEX_FILE);
        for (int i = 2; i < 1000; i++) {
            String path = String.format(AndroidConstants.DEX_ADDITIONAL, i);
            try {
                DexClass[] classes = parseDexFile(path);
                this.dexClasses = mergeDexClasses(this.dexClasses, classes);
            } catch (ParserException e) {
                break;
            }
        }
    }

    /**
     * parse resource table.
     */
    private void parseResourceTable() throws IOException {
        if (resourceTableParsed) {
            return;
        }
        resourceTableParsed = true;
        byte[] data = getFileData(AndroidConstants.RESOURCE_FILE);
        if (data == null) {
            // if no resource entry has been found, we assume it is not needed by this APK
            this.resourceTable = new ResourceTable();
            this.locales = Collections.emptySet();
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        ResourceTableParser resourceTableParser = new ResourceTableParser(buffer);
        resourceTableParser.parse();
        this.resourceTable = resourceTableParser.getResourceTable();
        this.locales = resourceTableParser.getLocales();
    }

    /**
     * check apk sign
     */
    public abstract ApkSignStatus verifyApk() throws IOException;

    @Override
    public void close() throws IOException {
        this.certificateMetaMap = null;
        this.resourceTable = null;
        this.iconPaths = null;
    }

    public Locale getPreferredLocale() {
        return preferredLocale;
    }


    /**
     * The locale preferred. Will cause getManifestXml / getApkMeta to return different values.
     * The default value is from os default locale setting.
     */
    public void setPreferredLocale(Locale preferredLocale) {
        if (!Objects.equals(this.preferredLocale, preferredLocale)) {
            this.preferredLocale = preferredLocale;
            this.manifestXml = null;
            this.apkMeta = null;
        }
    }
}
