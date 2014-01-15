package net.dongliu.apk.parser;

import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.Feature;
import net.dongliu.apk.parser.bean.GlEsVersion;
import net.dongliu.apk.parser.bean.UseFeature;

/**
 * get apk meta infos when parse AndroidManifest.xml
 *
 * @author dongliu
 */
public class ApkMetaParserReader implements XmlStreamReader {

    private String currentTag;

    private ApkMeta apkMeta;

    private Feature currentFeature;

    public ApkMetaParserReader() {
        apkMeta = new ApkMeta();
    }

    @Override
    public void onStartTagStart(String name) {
        currentTag = name;
    }

    @Override
    public void onStartTagEnd(String name) {
        if ("uses-feature".equals(currentTag)) {
            currentFeature = null;
        }
        currentTag = null;
    }

    @Override
    public void onEndTag(String name) {
    }

    @Override
    public void onAttribute(String name, String value) {
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
                apkMeta.setMinSdkVersion(Integer.parseInt(value));
            } else if (name.equals("maxSdkVersion")) {
                apkMeta.setMaxSdkVersion(Integer.parseInt(value));
            } else if (name.equals("targetSdkVersion")) {
                apkMeta.setTargetSdkVersion(Integer.parseInt(value));
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

    public ApkMeta getApkMeta() {
        return apkMeta;
    }
}
