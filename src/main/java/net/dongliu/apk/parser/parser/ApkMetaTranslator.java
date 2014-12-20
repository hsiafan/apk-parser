package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.bean.*;
import net.dongliu.apk.parser.struct.xml.*;

/**
 * trans binary xml to text
 *
 * @author Dong Liu dongliu@wandoujia.com
 */
public class ApkMetaTranslator implements XmlStreamer {
    private String[] tagStack = new String[100];
    private int depth = 0;
    private ApkMeta apkMeta = new ApkMeta();

    private IntentFilter intentFilter;
    private AndroidComponent component;

    @Override
    public void onStartTag(XmlNodeStartTag xmlNodeStartTag) {
        tagStack[depth++] = xmlNodeStartTag.getName();
        Attributes attributes = xmlNodeStartTag.getAttributes();
        switch (xmlNodeStartTag.getName()) {
            case "application":
                apkMeta.setLabel(attributes.get("label"));
                apkMeta.setIcon(attributes.get("icon"));
                break;
            case "manifest":
                apkMeta.setPackageName(attributes.get("package"));
                apkMeta.setVersionName(attributes.get("versionName"));
                apkMeta.setVersionCode(attributes.getLong("versionCode"));
                String installLocation = attributes.get("installLocation");
                if (installLocation != null) {
                    apkMeta.setInstallLocation(installLocation);
                }
                break;
            case "uses-sdk":
                apkMeta.setMinSdkVersion(attributes.get("minSdkVersion"));
                apkMeta.setTargetSdkVersion(attributes.get("targetSdkVersion"));
                apkMeta.setMaxSdkVersion(attributes.get("maxSdkVersion"));
                break;
            case "supports-screens":
                apkMeta.setAnyDensity(attributes.getBoolean("anyDensity", false));
                apkMeta.setSmallScreens(attributes.getBoolean("smallScreens", false));
                apkMeta.setNormalScreens(attributes.getBoolean("normalScreens", false));
                apkMeta.setLargeScreens(attributes.getBoolean("largeScreens", false));
                break;
            case "uses-feature":
                String name = attributes.get("name");
                boolean required = attributes.getBoolean("required", false);
                if (name != null) {
                    UseFeature useFeature = new UseFeature();
                    useFeature.setName(name);
                    useFeature.setRequired(required);
                    apkMeta.addUseFeatures(useFeature);
                } else {
                    Integer gl = attributes.getInt("glEsVersion");
                    if (gl != null) {
                        int v = gl;
                        GlEsVersion glEsVersion = new GlEsVersion();
                        glEsVersion.setMajor(v >> 16);
                        glEsVersion.setMinor(v & 0xffff);
                        glEsVersion.setRequired(required);
                        apkMeta.setGlEsVersion(glEsVersion);
                    }
                }
                break;
            case "uses-permission":
                apkMeta.addUsesPermission(attributes.get("name"));
                break;
            case "permission":
                Permission permission = new Permission();
                permission.setName(attributes.get("name"));
                permission.setLabel(attributes.get("label"));
                permission.setIcon(attributes.get("icon"));
                permission.setGroup(attributes.get("group"));
                permission.setDescription(attributes.get("description"));
                String protectionLevel = attributes.get("android:protectionLevel");
                if (protectionLevel != null) {
                    permission.setProtectionLevel(protectionLevel);
                }
                apkMeta.addPermission(permission);
                break;
            // below for server / activity / receiver
            case "service":
                Service service = new Service();
                fillComponent(attributes, service);
                component = service;
                break;
            case "activity-alias":
                //TODO: activity-alias
            case "activity":
                Activity activity = new Activity();
                fillComponent(attributes, activity);
                component = activity;
                break;
            case "receiver":
                Receiver receiver = new Receiver();
                fillComponent(attributes, receiver);
                component = receiver;
                break;
            // below is for intent filter
            case "intent-filter":
                intentFilter = new IntentFilter();
                break;
            case "action":
                if (matchLastTag("intent-filter")) {
                    intentFilter.addAction(attributes.get("name"));
                }
                break;
            case "category":
                if (matchLastTag("intent-filter")) {
                    intentFilter.addCategory(attributes.get("name"));
                }
                break;
            case "data":
                if (matchLastTag("intent-filter")) {
                    String scheme = attributes.get("scheme");
                    String host = attributes.get("host");
                    String pathPrefix = attributes.get("pathPrefix");
                    String mimeType = attributes.get("mimeType");
                    String type = attributes.get("type");
                    IntentFilter.IntentData data = new IntentFilter.IntentData();
                    data.setScheme(scheme);
                    data.setMimeType(mimeType);
                    data.setHost(host);
                    data.setPathPrefix(pathPrefix);
                    data.setType(type);
                    intentFilter.addData(data);
                }
                break;
        }
    }

    private void fillComponent(Attributes attributes, AndroidComponent component) {
        component.setName(attributes.get("name"));
        component.setExported(attributes.getBoolean("exported", false));
        component.setProcess(attributes.get("process"));
    }

    @Override
    public void onEndTag(XmlNodeEndTag xmlNodeEndTag) {
        depth--;
        switch (xmlNodeEndTag.getName()) {
            // below for server / activity / receiver
            case "service":
                apkMeta.addService((Service) component);
                component = null;
                break;
            case "activity":
                apkMeta.addActivity((Activity) component);
                component = null;
                break;
            case "receiver":
                apkMeta.addReceiver((Receiver) component);
                component = null;
                break;
            case "intent-filter":
                apkMeta.addIntentFilter(intentFilter);
                component.addIntentFilter(intentFilter);
                intentFilter = null;
                break;
        }
    }

    @Override
    public void onCData(XmlCData xmlCData) {

    }

    @Override
    public void onNamespaceStart(XmlNamespaceStartTag tag) {

    }

    @Override
    public void onNamespaceEnd(XmlNamespaceEndTag tag) {

    }

    public ApkMeta getApkMeta() {
        return apkMeta;
    }

    private boolean matchTagPath(String... tags) {
        // the root should always be "manifest"
        if (depth != tags.length + 1) {
            return false;
        }
        for (int i = 1; i < depth; i++) {
            if (!tagStack[i].equals(tags[i - 1])) {
                return false;
            }
        }
        return true;
    }

    private boolean matchLastTag(String tag) {
        // the root should always be "manifest"
        return tagStack[depth - 1].endsWith(tag);
    }
}
