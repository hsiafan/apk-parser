package net.dongliu.apk.parser;

import net.dongliu.apk.parser.bean.*;
import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.parser.*;
import net.dongliu.apk.parser.struct.AndroidConstants;
import net.dongliu.apk.parser.struct.resource.ResourceTable;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

    private Map<Locale, String> manifestXmlMap;
    private Map<Locale, ApkMeta> apkMetaMap;
    private Set<Locale> locales;
    private List<CertificateMeta> certificateMetas;
    private final ZipFile zf;
    private File apkFile;

    /**
     * default is null
     */
    private Locale preferredLocale = Locale.getDefault();

    public ApkParser(File apkFile) throws IOException {
        this.apkFile = apkFile;
        this.zf = new ZipFile(apkFile);
        this.manifestXmlMap = new HashMap<Locale, String>();
        this.apkMetaMap = new HashMap<Locale, ApkMeta>();
    }

    /**
     * return decoded AndroidManifest.xml
     *
     * @return decoded AndroidManifest.xml
     */
    public String getManifestXml() throws IOException {
        if (!manifestXmlMap.containsKey(preferredLocale)) {
            parseManifestXml();
        }
        return manifestXmlMap.get(preferredLocale);
    }

    /**
     * return decoded AndroidManifest.xml
     *
     * @return decoded AndroidManifest.xml
     */
    public ApkMeta getApkMeta() throws IOException {
        if (!apkMetaMap.containsKey(preferredLocale)) {
            parseManifestXml();
        }
        ApkMeta apkMeta = apkMetaMap.get(preferredLocale);
        if (apkMeta != null) {
            findNativeLib(apkMeta);
        }
        return apkMeta;
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
    public List<CertificateMeta> getCertificateMetas() throws IOException,
            CertificateEncodingException {
        if (this.certificateMetas == null) {
            parseCertificate();
        }
        return this.certificateMetas;
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
        InputStream in = zf.getInputStream(entry);
        try {
            CertificateParser parser = new CertificateParser(in);
            parser.parse();
            this.certificateMetas = parser.getCertificateMetas();
        } finally {
            in.close();
        }
    }

    /**
     * parse manifest.xml, get apkMeta and manifestXml text.
     *
     * @throws IOException
     */
    private void parseManifestXml() throws IOException {
        ZipArchiveEntry manifestEntry = getEntry(AndroidConstants.MANIFEST_FILE);
        if (manifestEntry == null) {
            throw new ParserException("manifest xml file not found");
        }

        if (this.resourceTable == null) {
            parseResourceTable();
        }

        InputStream in = zf.getInputStream(manifestEntry);

        try {
            BinaryXmlParser binaryXmlParser = new BinaryXmlParser(in, manifestEntry.getSize());
            binaryXmlParser.setLocale(preferredLocale);
            XmlTranslator xmlTranslator = new XmlTranslator(resourceTable, preferredLocale);
            ApkMetaConstructor apkMetaConstructor = new ApkMetaConstructor(resourceTable,
                    preferredLocale);
            CompositeXmlStreamer xmlStreamer = new CompositeXmlStreamer(xmlTranslator,
                    apkMetaConstructor);
            binaryXmlParser.setXmlStreamer(xmlStreamer);
            binaryXmlParser.parse();
            manifestXmlMap.put(preferredLocale, xmlTranslator.getXml());
            apkMetaMap.put(preferredLocale, apkMetaConstructor.getApkMeta());
        } finally {
            in.close();
        }
    }

    /**
     * trans binary xml file to text xml file.
     *
     * @param path the xml file path in apk file
     * @return the text. null if file not exists
     * @throws IOException
     */
    public String transBinaryXml(String path) throws IOException {
        ZipArchiveEntry entry = getEntry(path);
        if (entry == null) {
            return null;
        }

        InputStream in = zf.getInputStream(entry);
        try {
            BinaryXmlParser binaryXmlParser = new BinaryXmlParser(in, entry.getSize());
            binaryXmlParser.setLocale(preferredLocale);
            XmlTranslator xmlTranslator = new XmlTranslator(resourceTable, preferredLocale);
            ApkMetaConstructor apkMetaConstructor = new ApkMetaConstructor(resourceTable,
                    preferredLocale);
            CompositeXmlStreamer xmlStreamer = new CompositeXmlStreamer(xmlTranslator,
                    apkMetaConstructor);
            binaryXmlParser.setXmlStreamer(xmlStreamer);
            binaryXmlParser.parse();
            return xmlTranslator.getXml();
        } finally {
            in.close();
        }
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
        ZipArchiveEntry resourceEntry = getEntry(AndroidConstants.DEX_FILE);
        if (resourceEntry == null) {
            throw new ParserException("resource table not found");
        }
        InputStream in = zf.getInputStream(resourceEntry);
        try {
            DexParser dexParser = new DexParser(in);
            dexParser.parse();
            this.dexClasses = dexParser.getDexClasses();
        } finally {
            in.close();
        }
    }

    /**
     * get app icon as binary data, the icon's file format should be png
     *
     * @return
     */
    public Icon getIcon() throws IOException {
        if (this.preferredLocale == null) {
            throw new ParserException("PreferredLocale must be set first");
        }

        ApkMeta apkMeta = getApkMeta();
        String iconPath = apkMeta.getIcon();
        ZipArchiveEntry entry = getEntry(iconPath);
        if (entry == null) {
            return null;
        }

        Icon icon = new Icon();
        icon.setFormat(iconPath.substring(iconPath.indexOf(".") + 1));
        int idx = iconPath.indexOf("dpi/");
        if (idx > 0) {
            icon.setDpiLevel(iconPath.substring(
                    iconPath.lastIndexOf("-", idx) + 1,
                    idx + "dpi".length()));
        } else {
            icon.setDpiLevel("");
        }
        InputStream inputStream = zf.getInputStream(entry);
        try {
            icon.setData(IOUtils.toByteArray(inputStream));
            return icon;
        } finally {
            inputStream.close();
        }
    }

    /**
     * check apk sign
     *
     * @return
     * @throws IOException
     */
    public ApkSignStatus verifyApk() throws IOException {
        ZipArchiveEntry entry = getEntry("META-INF/MANIFEST.MF");
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
                // Read in each jar entry. A security exception will be thrown
                // if a signature/digest check fails.
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

    private ZipArchiveEntry getEntry(String path) {
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

    /**
     * parse resource table.
     */
    private void parseResourceTable() throws IOException {
        ZipArchiveEntry resourceEntry = getEntry(AndroidConstants.RESOURCE_FILE);
        if (resourceEntry == null) {
            // if no resource entry has been found, we assume it is not needed by this APK
            this.resourceTable = new ResourceTable();
            this.locales = Collections.emptySet();
        } else {
            InputStream in = zf.getInputStream(resourceEntry);
            try {
                ResourceTableParser resourceTableParser = new ResourceTableParser(in,
                        resourceEntry.getSize());
                resourceTableParser.parse();
                this.resourceTable = resourceTableParser.getResourceTable();
                this.locales = resourceTableParser.getLocales();
            } finally {
                in.close();
            }
        }
    }

    private void findNativeLib(ApkMeta apkMeta) {
        boolean hasNative = false;
        Set<String> supportArches = new HashSet<String>();

        Enumeration<ZipArchiveEntry> enu = zf.getEntries();
        while (enu.hasMoreElements()) {
            ZipArchiveEntry entry = enu.nextElement();
            if (entry.isDirectory()) {
                continue;
            }
            String path = entry.getName();
            // some apk put .so under assert/, copy to data/data/xxx and use System.loadLibrary
            // to load the dynamic library, this can be very complicated,
            // the developer did not need to abey ordinary rules to name the .so file path.
            // we do not take it into account now
            if (path.endsWith(".so") && (path.startsWith(AndroidConstants.LIB_PREFIX))) {
                int idx = path.lastIndexOf('/');
                if (idx < 0) {
                    //should not happen
                    continue;
                }
                String archStr = path.substring(0, idx);
                idx = archStr.lastIndexOf('/');
                if (idx > 0) {
                    archStr = archStr.substring(idx + 1);
                }
                try {
                    supportArches.add(archStr);
                    hasNative = true;
                } catch (IllegalArgumentException ignore) {
                    // unknown arch string... just ignore it
                }
            }
        }

        apkMeta.setHasNative(hasNative);
        if (hasNative) {
            apkMeta.setSupportArches(new ArrayList<String>(supportArches));
        }
    }

    @Override
    public void close() throws IOException {
        this.certificateMetas = null;
        this.apkMetaMap = null;
        this.manifestXmlMap = null;
        this.resourceTable = null;
        this.certificateMetas = null;
        zf.close();
    }

    public Locale getPreferredLocale() {
        return preferredLocale;
    }

    /**
     * The locale preferred. Will cause getManifestXml / getApkMeta to return different values.
     * The default value if Locale.none, which will not translate resource strings. you need to set
     * one locale if wanted localized resources(app title, themes name, etc.)
     */
    public void setPreferredLocale(Locale preferredLocale) {
        this.preferredLocale = preferredLocale;
    }
}
