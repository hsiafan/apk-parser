package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.bean.*;
import net.dongliu.apk.parser.struct.ResourceValue;
import net.dongliu.apk.parser.struct.resource.Densities;
import net.dongliu.apk.parser.struct.resource.ResourceEntry;
import net.dongliu.apk.parser.struct.resource.ResourceTable;
import net.dongliu.apk.parser.struct.resource.Type;
import net.dongliu.apk.parser.struct.xml.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * trans binary xml to text
 *
 * @author Liu Dong dongliu@live.cn
 */
public class ApkMetaTranslator implements XmlStreamer {
    private String[] tagStack = new String[100];
    private int depth = 0;
    private ApkMeta apkMeta = new ApkMeta();
    private List<IconPath> iconPaths = Collections.emptyList();

    @Nonnull
    private ResourceTable resourceTable;
    @Nullable
    private Locale locale;

    public ApkMetaTranslator(@Nonnull ResourceTable resourceTable, @Nullable Locale locale) {
        this.resourceTable = Objects.requireNonNull(resourceTable);
        this.locale = locale;
    }

    @Override
    public void onStartTag(XmlNodeStartTag xmlNodeStartTag) {
        Attributes attributes = xmlNodeStartTag.getAttributes();
        switch (xmlNodeStartTag.getName()) {
            case "application":
                String label = attributes.getString("label");
                if (label != null) {
                    apkMeta.setLabel(label);
                }
                Attribute iconAttr = attributes.get("icon");
                if (iconAttr != null) {
                    ResourceValue resourceValue = iconAttr.getTypedValue();
                    if (resourceValue != null && resourceValue instanceof ResourceValue.ReferenceResourceValue) {
                        long resourceId = ((ResourceValue.ReferenceResourceValue) resourceValue).getReferenceResourceId();
                        List<ResourceTable.Resource> resources = this.resourceTable.getResourcesById(resourceId);
                        if (!resources.isEmpty()) {
                            List<IconPath> icons = new ArrayList<>();
                            boolean hasDefault = false;
                            for (ResourceTable.Resource resource : resources) {
                                Type type = resource.getType();
                                ResourceEntry resourceEntry = resource.getResourceEntry();
                                String path = resourceEntry.toStringValue(resourceTable, locale);
                                if (type.getDensity() == Densities.DEFAULT) {
                                    hasDefault = true;
                                    apkMeta.setIcon(path);
                                }
                                IconPath iconPath = new IconPath(path, type.getDensity());
                                icons.add(iconPath);
                            }
                            if (!hasDefault) {
                                apkMeta.setIcon(icons.get(0).getPath());
                            }
                            this.iconPaths = icons;
                        }
                    } else {
                        String value = iconAttr.getValue();
                        if (value != null) {
                            apkMeta.setIcon(value);
                            IconPath iconPath = new IconPath(value, Densities.DEFAULT);
                            this.iconPaths = Collections.singletonList(iconPath);
                        }
                    }
                }
                break;
            case "manifest":
                apkMeta.setPackageName(attributes.getString("package"));
                apkMeta.setVersionName(attributes.getString("versionName"));
                apkMeta.setVersionCode(attributes.getLong("versionCode"));
                String installLocation = attributes.getString("installLocation");
                if (installLocation != null) {
                    apkMeta.setInstallLocation(installLocation);
                }
                apkMeta.setCompileSdkVersion(attributes.getString("compileSdkVersion"));
                apkMeta.setCompileSdkVersionCodename(attributes.getString("compileSdkVersionCodename"));
                apkMeta.setPlatformBuildVersionCode(attributes.getString("platformBuildVersionCode"));
                apkMeta.setPlatformBuildVersionName(attributes.getString("platformBuildVersionName"));
                break;
            case "uses-sdk":
                apkMeta.setMinSdkVersion(attributes.getString("minSdkVersion"));
                apkMeta.setTargetSdkVersion(attributes.getString("targetSdkVersion"));
                apkMeta.setMaxSdkVersion(attributes.getString("maxSdkVersion"));
                break;
            case "supports-screens":
                apkMeta.setAnyDensity(attributes.getBoolean("anyDensity", false));
                apkMeta.setSmallScreens(attributes.getBoolean("smallScreens", false));
                apkMeta.setNormalScreens(attributes.getBoolean("normalScreens", false));
                apkMeta.setLargeScreens(attributes.getBoolean("largeScreens", false));
                break;
            case "uses-feature":
                String name = attributes.getString("name");
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
                apkMeta.addUsesPermission(attributes.getString("name"));
                break;
            case "permission":
                Permission permission = new Permission();
                permission.setName(attributes.getString("name"));
                permission.setLabel(attributes.getString("label"));
                permission.setIcon(attributes.getString("icon"));
                permission.setGroup(attributes.getString("group"));
                permission.setDescription(attributes.getString("description"));
                String protectionLevel = attributes.getString("android:protectionLevel");
                if (protectionLevel != null) {
                    permission.setProtectionLevel(protectionLevel);
                }
                apkMeta.addPermission(permission);
                break;
        }
        tagStack[depth++] = xmlNodeStartTag.getName();
    }

    @Override
    public void onEndTag(XmlNodeEndTag xmlNodeEndTag) {
        depth--;
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

    @Nonnull
    public ApkMeta getApkMeta() {
        return apkMeta;
    }

    @Nonnull
    public List<IconPath> getIconPaths() {
        return iconPaths;
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
