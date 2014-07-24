package net.dongliu.apk.parser.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dongliu
 */
public class ApkMeta {

    private String packageName;

    private String label;

    private String icon;

    private String versionName;

    private long versionCode = -1;

    private String minSdkVersion;

    private String targetSdkVersion;

    private String maxSdkVersion;

    private GlEsVersion glEsVersion;

    private boolean anyDensity;

    private boolean smallScreens;

    private boolean normalScreens;

    private boolean largeScreens;

    private List<String> permissions = new ArrayList<String>();

    private List<UseFeature> useFeatures = new ArrayList<UseFeature>();

    /**
     * this may not be accurate
     */
    private boolean hasNative;

    /**
     * may be x86, mips, armeabi, armeabi-v7a
     * this may not be accurate
     */
    private List<String> supportArches;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public long getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(long versionCode) {
        this.versionCode = versionCode;
    }

    public String getMinSdkVersion() {
        return minSdkVersion;
    }

    public void setMinSdkVersion(String minSdkVersion) {
        this.minSdkVersion = minSdkVersion;
    }

    public String getTargetSdkVersion() {
        return targetSdkVersion;
    }

    public void setTargetSdkVersion(String targetSdkVersion) {
        this.targetSdkVersion = targetSdkVersion;
    }

    public String getMaxSdkVersion() {
        return maxSdkVersion;
    }

    public void setMaxSdkVersion(String maxSdkVersion) {
        this.maxSdkVersion = maxSdkVersion;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public void addPermission(String permission) {
        this.permissions.add(permission);
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isAnyDensity() {
        return anyDensity;
    }

    public void setAnyDensity(boolean anyDensity) {
        this.anyDensity = anyDensity;
    }

    public boolean isSmallScreens() {
        return smallScreens;
    }

    public void setSmallScreens(boolean smallScreens) {
        this.smallScreens = smallScreens;
    }

    public boolean isNormalScreens() {
        return normalScreens;
    }

    public void setNormalScreens(boolean normalScreens) {
        this.normalScreens = normalScreens;
    }

    public boolean isLargeScreens() {
        return largeScreens;
    }

    public void setLargeScreens(boolean largeScreens) {
        this.largeScreens = largeScreens;
    }

    @Override
    public String toString() {
        return "packageName: \t" + packageName + "\n"
                + "label: \t" + label + "\n"
                + "versionName: \t" + versionName + "\n"
                + "versionCode: \t" + versionCode + "\n"
                + "minSdkVersion: \t" + minSdkVersion + "\n"
                + "targetSdkVersion: \t" + targetSdkVersion + "\n"
                + "maxSdkVersion: \t" + maxSdkVersion;
    }

    public GlEsVersion getGlEsVersion() {
        return glEsVersion;
    }

    public void setGlEsVersion(GlEsVersion glEsVersion) {
        this.glEsVersion = glEsVersion;
    }

    public List<UseFeature> getUseFeatures() {
        return useFeatures;
    }

    public void setUseFeatures(List<UseFeature> useFeatures) {
        this.useFeatures = useFeatures;
    }

    public void addUsePermission(UseFeature useFeature) {
        this.useFeatures.add(useFeature);
    }

    /**
     * this may not be accurate.
     */
    public boolean isHasNative() {
        return hasNative;
    }

    public void setHasNative(boolean hasNative) {
        this.hasNative = hasNative;
    }

    /**
     * may be x86, mips, armeabi, armeabi-v7a.
     * this may not be accurate.
     *
     * some apk put .so under assert/, copy to data/data/xxx and use System.loadLibrary
     * to load the dynamic library, this can be very complicated,
     * the developer did not need to abey ordinary rules to name the .so file path.
     * we do not take it into account now
     *
     * @return null if hasNative is false, otherwise the support arches as string
     */
    public List<String> getSupportArches() {
        return supportArches;
    }

    public void setSupportArches(List<String> supportArches) {
        this.supportArches = supportArches;
    }
}
