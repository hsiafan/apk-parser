package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.Feature;
import net.dongliu.apk.parser.bean.GlEsVersion;
import net.dongliu.apk.parser.bean.UseFeature;
import net.dongliu.apk.parser.struct.resource.ResourceTable;
import net.dongliu.apk.parser.struct.xml.*;

import java.util.Locale;

/**
 * construct apk meta infos when parse AndroidManifest.xml
 *
 * @author dongliu
 */
public class ApkMetaConstructor implements XmlStreamer {

    private String currentTag;

    private ApkMeta apkMeta;

    private Feature currentFeature;

    private ResourceTable resourceTable;

    private Locale locale;

    public ApkMetaConstructor(ResourceTable resourceTable, Locale locale) {
        this.resourceTable = resourceTable;
        this.locale = locale;
        apkMeta = new ApkMeta();
    }

    @Override
    public void onStartTag(XmlNodeStartTag xmlNodeStartTag) {
        currentTag = xmlNodeStartTag.name;
    }

    @Override
    public void onEndTag(XmlNodeEndTag xmlNodeEndTag) {
        currentFeature = null;
    }

    @Override
    public void onAttribute(Attribute attribute) {
        String name = attribute.name;
        String value = attribute.toStringValue(resourceTable, locale);
        // get basic apk metas
        if (currentTag.equals("manifest")) {
            if (name.equals("package")) {
                apkMeta.setPackageName(value);
            } else if (name.equals("versionCode")) {
                apkMeta.setVersionCode(Long.parseLong(value));
            } else if (name.equals("versionName")) {
                apkMeta.setVersionName(value);
            }
        } else if (currentTag.equals("application")) {
            if (name.equals("label")) {
                apkMeta.setLabel(value);
            } else if (name.equals("icon")) {
                apkMeta.setIcon(value);
            }
        } else if (currentTag.equals("uses-sdk")) {
            if (name.equals("minSdkVersion")) {
                apkMeta.setMinSdkVersion(value);
            } else if (name.equals("maxSdkVersion")) {
                apkMeta.setMaxSdkVersion(value);
            } else if (name.equals("targetSdkVersion")) {
                apkMeta.setTargetSdkVersion(value);
            }
        } else if (currentTag.equals("uses-permission")) {
            if (name.equals("name")) {
                apkMeta.addPermission(value);
            }
        } else if (currentTag.equals("supports-screens")) {
            if (name.equals("anyDensity")) {
                apkMeta.setAnyDensity(Boolean.parseBoolean(value));
            } else if (name.equals("smallScreens")) {
                apkMeta.setSmallScreens(Boolean.parseBoolean(value));
            } else if (name.equals("normalScreens")) {
                apkMeta.setNormalScreens(Boolean.parseBoolean(value));
            } else if (name.equals("largeScreens")) {
                apkMeta.setLargeScreens(Boolean.parseBoolean(value));
            }
        } else if (currentTag.equals("uses-feature")) {
            if (name.equals("glEsVersion")) {
                int v = Integer.parseInt(value);
                GlEsVersion glEsVersion = new GlEsVersion();
                glEsVersion.setMajor(v >> 16);
                glEsVersion.setMinor(v & 0xffff);
                apkMeta.setGlEsVersion(glEsVersion);
                currentFeature = glEsVersion;
            } else if (name.equals("name")) {
                UseFeature useFeature = new UseFeature();
                useFeature.setName(value);
                apkMeta.addUsePermission(useFeature);
                currentFeature = useFeature;
            } else if (name.equals("required")) {
                if (currentFeature != null) {
                    currentFeature.setRequired(Boolean.parseBoolean(value));
                }
            }
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
}
