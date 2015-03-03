package net.dongliu.apk.parser;

import net.dongliu.apk.parser.bean.*;
import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.parser.*;
import net.dongliu.apk.parser.struct.AndroidConstants;
import net.dongliu.apk.parser.struct.resource.ResourceTable;
import net.dongliu.apk.parser.utils.Utils;
import net.dongliu.apk.parser.utils.XmlUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
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

    public ApkParser(String filePath) throws IOException {
        this(new File(filePath));
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
            parseApkMeta();
        }
        return apkMetaMap.get(preferredLocale);
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
            ZipArchiveEntry ne = enu.nextElement();
            if (ne.isDirectory()) {
                continue;
            }
            if (ne.getName().toUpperCase().endsWith(".RSA")
                    || ne.getName().toUpperCase().endsWith(".DSA")) {
                entry = ne;
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
     * parse manifest.xml, get apkMeta.
     *
     * @throws IOException
     */
    private void parseApkMeta() throws IOException {
        if (!manifestXmlMap.containsKey(preferredLocale)) {
            parseManifestXml();
        }
        String xml = this.manifestXmlMap.get(preferredLocale);

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        Document document;
        try {
            //DOM parser instance
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            //parse an XML file into a DOM tree
            document = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new ParserException("Parse manifest xml failed", e);
        }

        ApkMeta apkMeta = new ApkMeta();
        Node manifestNode = document.getElementsByTagName("manifest").item(0);
        NamedNodeMap manifestAttr = manifestNode.getAttributes();
        apkMeta.setPackageName(XmlUtils.getAttribute(manifestAttr, "package"));
        apkMeta.setVersionCode(XmlUtils.getLongAttribute(manifestAttr, "android:versionCode"));
        apkMeta.setVersionName(XmlUtils.getAttribute(manifestAttr, "android:versionName"));
        String installLocation = XmlUtils.getAttribute(manifestAttr, "android:installLocation");
        if (installLocation != null) {
            apkMeta.setInstallLocation(Constants.InstallLocation.valueOf(installLocation));
        }

        NodeList nodes = manifestNode.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String nodeName = node.getNodeName();
            if (nodeName.equals("uses-sdk")) {
                parseSdk(apkMeta, node);
            } else if (nodeName.equals("supports-screens")) {
                parseScreens(apkMeta, node);
            } else if (nodeName.equals("uses-feature")) {
                parseUsesFeature(apkMeta, node);
            } else if (nodeName.equals("application")) {
                parseApplication(apkMeta, node);
            } else if (nodeName.equals("uses-permission")) {
                parseUsesPermission(apkMeta, node);
            } else if (nodeName.equals("permission")) {
                // provided permissions
                parsePermission(apkMeta, node);
            }
        }
        apkMetaMap.put(preferredLocale, apkMeta);
    }

    private void parseApplication(ApkMeta apkMeta, Node node) throws IOException {
        NamedNodeMap attributes = node.getAttributes();
        apkMeta.setLabel(XmlUtils.getAttribute(attributes, "android:label"));
        String iconPath = XmlUtils.getAttribute(attributes, "android:icon");
        if (iconPath != null) {
            Icon icon = getIcon(iconPath);
            apkMeta.setIcon(icon);
        }

        // activity, service, receiver, intent ...
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String childName = child.getNodeName();
            if (childName.equals("service")) {
                parseService(apkMeta, child);
            } else if (childName.equals("activity")) {
                parseActivity(apkMeta, child);
            } else if (childName.equals("receiver")) {
                parseReceiver(apkMeta, child);
            }
        }
    }

    private void parseActivity(ApkMeta apkMeta, Node node) {
        Activity activity = new Activity();
        fillComponent(node, activity);
        apkMeta.addActivity(activity);
        apkMeta.addIntentFilters(activity.getIntentFilters());
    }

    private void parseService(ApkMeta apkMeta, Node node) {
        Service service = new Service();
        fillComponent(node, service);
        apkMeta.addService(service);
        apkMeta.addIntentFilters(service.getIntentFilters());
    }

    private void parseReceiver(ApkMeta apkMeta, Node node) {
        Receiver receiver = new Receiver();
        fillComponent(node, receiver);
        apkMeta.addReceiver(receiver);
        apkMeta.addIntentFilters(receiver.getIntentFilters());
    }

    /**
     * get and fill common android component data.
     */
    private void fillComponent(Node node, AndroidComponent component) {
        NamedNodeMap attributes = node.getAttributes();
        component.setName(XmlUtils.getAttribute(attributes, "android:name"));
        component.setExported(XmlUtils.getBoolAttribute(attributes, "android:exported", false));
        component.setProcess(XmlUtils.getAttribute(attributes, "android:process"));

        // intent
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String childName = child.getNodeName();
            if (childName.equals("intent-filter")) {
                IntentFilter intentFilter = getIntentFilter(child);
                intentFilter.setOwner(component);
                component.addIntentFilter(intentFilter);
            }
        }
    }

    private IntentFilter getIntentFilter(Node intentNode) {
        NodeList intentChildren = intentNode.getChildNodes();
        IntentFilter intentFilter = new IntentFilter();
        for (int j = 0; j < intentChildren.getLength(); j++) {
            Node intentChild = intentChildren.item(j);
            String intentChildName = intentChild.getNodeName();
            NamedNodeMap intentChildAttributes = intentChild.getAttributes();
            if (intentChildName.equals("action")) {
                intentFilter.addAction(XmlUtils.getAttribute(intentChildAttributes, "android:name"));
            } else if (intentChildName.equals("category")) {
                intentFilter.addCategory(XmlUtils.getAttribute(intentChildAttributes, "android:name"));
            } else if (intentChildName.equals("data")) {
                String scheme = XmlUtils.getAttribute(intentChildAttributes, "android:scheme");
                String host = XmlUtils.getAttribute(intentChildAttributes, "android:host");
                String pathPrefix = XmlUtils.getAttribute(intentChildAttributes, "android:pathPrefix");
                String mimeType = XmlUtils.getAttribute(intentChildAttributes, "android:mimeType");
                String type = XmlUtils.getAttribute(intentChildAttributes, "android:type");
                IntentFilter.IntentData data = new IntentFilter.IntentData();
                data.setScheme(scheme);
                data.setMimeType(mimeType);
                data.setHost(host);
                data.setPathPrefix(pathPrefix);
                data.setType(type);
                intentFilter.addData(data);
            }
        }
        return intentFilter;
    }

    private void parseUsesPermission(ApkMeta apkMeta, Node node) {
        NamedNodeMap attributes = node.getAttributes();
        apkMeta.addUsesPermission(XmlUtils.getAttribute(attributes, "android:name"));
    }

    private void parseUsesFeature(ApkMeta apkMeta, Node node) {
        NamedNodeMap attributes = node.getAttributes();
        String name = XmlUtils.getAttribute(attributes, "android:name");
        boolean required = XmlUtils.getBoolAttribute(attributes, "android:required", true);
        if (name != null) {
            UseFeature useFeature = new UseFeature();
            useFeature.setName(name);
            useFeature.setRequired(required);
            apkMeta.addUseFeatures(useFeature);
        } else {
            Integer gl = XmlUtils.getIntAttribute(attributes, "android:glEsVersion");
            if (gl != null) {
                int v = gl;
                GlEsVersion glEsVersion = new GlEsVersion();
                glEsVersion.setMajor(v >> 16);
                glEsVersion.setMinor(v & 0xffff);
                glEsVersion.setRequired(required);
                apkMeta.setGlEsVersion(glEsVersion);
            }
        }
    }

    private void parseSdk(ApkMeta apkMeta, Node node) {
        NamedNodeMap attributes = node.getAttributes();
        apkMeta.setMinSdkVersion(XmlUtils.getAttribute(attributes, "android:minSdkVersion"));
        apkMeta.setMaxSdkVersion(XmlUtils.getAttribute(attributes, "android:maxSdkVersion"));
        apkMeta.setTargetSdkVersion(XmlUtils.getAttribute(attributes, "android:targetSdkVersion"));
    }

    private void parseScreens(ApkMeta apkMeta, Node node) {
        NamedNodeMap attributes = node.getAttributes();
        apkMeta.setSmallScreens(XmlUtils.getBoolAttribute(attributes, "android:minSdkVersion", false));
        apkMeta.setLargeScreens(XmlUtils.getBoolAttribute(attributes, "android:largeScreens", false));
        apkMeta.setNormalScreens(XmlUtils.getBoolAttribute(attributes, "android:normalScreens", false));
        apkMeta.setAnyDensity(XmlUtils.getBoolAttribute(attributes, "android:anyDensity", false));
    }


    private void parsePermission(ApkMeta apkMeta, Node node) {
        NamedNodeMap attributes = node.getAttributes();
        Permission permission = new Permission();
        permission.setName(XmlUtils.getAttribute(attributes, "android:name"));
        permission.setLabel(XmlUtils.getAttribute(attributes, "android:label"));
        permission.setIcon(XmlUtils.getAttribute(attributes, "android:icon"));
        permission.setGroup(XmlUtils.getAttribute(attributes, "android:group"));
        permission.setDescription(XmlUtils.getAttribute(attributes, "android:description"));
        String protectionLevel = XmlUtils.getAttribute(attributes, "android:protectionLevel");
        if (protectionLevel != null) {
            permission.setProtectionLevel(protectionLevel);
        }
        apkMeta.addPermission(permission);
    }

    /**
     * parse manifest.xml, get manifestXml as xml text.
     *
     * @throws IOException
     */
    private void parseManifestXml() throws IOException {
        String xml = transBinaryXml(AndroidConstants.MANIFEST_FILE);
        if (xml == null) {
            throw new ParserException("Manifest xml file not found");
        }
        manifestXmlMap.put(preferredLocale, xml);
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

        InputStream in = zf.getInputStream(entry);
        ByteBuffer buffer = ByteBuffer.wrap(IOUtils.toByteArray(in));
        BinaryXmlParser binaryXmlParser = new BinaryXmlParser(buffer);
        binaryXmlParser.setLocale(preferredLocale);
        XmlTranslator xmlTranslator = new XmlTranslator(resourceTable, preferredLocale);
        binaryXmlParser.setXmlStreamer(xmlTranslator);
        binaryXmlParser.parse();
        return xmlTranslator.getXml();
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
     * get app icon as binary data, the icon's file format should be png
     *
     * @return
     */
    private Icon getIcon(String iconPath) throws IOException {
        if (this.preferredLocale == null) {
            throw new ParserException("PreferredLocale must be set first");
        }

        ZipArchiveEntry entry = Utils.getEntry(zf, iconPath);
        if (entry == null) {
            return null;
        }

        Icon icon = new Icon();
        icon.setPath(entry.getName());
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
        icon.setData(IOUtils.toByteArray(inputStream));
        return icon;
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
        resourceTableParser.parse();
        this.resourceTable = resourceTableParser.getResourceTable();
        this.locales = resourceTableParser.getLocales();
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
