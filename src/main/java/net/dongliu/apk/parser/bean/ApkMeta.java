package net.dongliu.apk.parser.bean;

import net.dongliu.apk.parser.AbstractApkFile;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Apk meta info
 *
 * @author dongliu
 */
public class ApkMeta {

    private String packageName;
    private String label;
    private String icon;
    private String versionName;
    private Long versionCode;
    private String installLocation;
    private String minSdkVersion;
    private String targetSdkVersion;
    @Nullable
    private String maxSdkVersion;
    @Nullable
    private String compileSdkVersion;
    @Nullable
    private String compileSdkVersionCodename;
    @Nullable
    private String platformBuildVersionCode;
    @Nullable
    private String platformBuildVersionName;
    private GlEsVersion glEsVersion;
    private boolean anyDensity;
    private boolean smallScreens;
    private boolean normalScreens;
    private boolean largeScreens;

    private List<String> usesPermissions = new ArrayList<>();
    private List<UseFeature> usesFeatures = new ArrayList<>();
    private List<Permission> permissions = new ArrayList<>();

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

    public Long getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Long versionCode) {
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

    @Nullable
    public String getMaxSdkVersion() {
        return maxSdkVersion;
    }

    public void setMaxSdkVersion(@Nullable String maxSdkVersion) {
        this.maxSdkVersion = maxSdkVersion;
    }

    @Nullable
    public String getCompileSdkVersion() {
        return compileSdkVersion;
    }

    public void setCompileSdkVersion(@Nullable String compileSdkVersion) {
        this.compileSdkVersion = compileSdkVersion;
    }

    @Nullable
    public String getCompileSdkVersionCodename() {
        return compileSdkVersionCodename;
    }

    public void setCompileSdkVersionCodename(@Nullable String compileSdkVersionCodename) {
        this.compileSdkVersionCodename = compileSdkVersionCodename;
    }

    @Nullable
    public String getPlatformBuildVersionCode() {
        return platformBuildVersionCode;
    }

    public void setPlatformBuildVersionCode(@Nullable String platformBuildVersionCode) {
        this.platformBuildVersionCode = platformBuildVersionCode;
    }

    @Nullable
    public String getPlatformBuildVersionName() {
        return platformBuildVersionName;
    }

    public void setPlatformBuildVersionName(@Nullable String platformBuildVersionName) {
        this.platformBuildVersionName = platformBuildVersionName;
    }

    public List<String> getUsesPermissions() {
        return usesPermissions;
    }

    public void addUsesPermission(String permission) {
        this.usesPermissions.add(permission);
    }

    /**
     * the icon file path in apk
     *
     * @return null if not found
     * @deprecated use {@link AbstractApkFile#getAllIcons()} instead.
     */
    @Deprecated
    public String getIcon() {
        return icon;
    }

    @Deprecated
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * alias for getLabel
     */
    public String getName() {
        return label;
    }

    /**
     * get the apk's title(name)
     */
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

    public GlEsVersion getGlEsVersion() {
        return glEsVersion;
    }

    public void setGlEsVersion(GlEsVersion glEsVersion) {
        this.glEsVersion = glEsVersion;
    }

    public List<UseFeature> getUsesFeatures() {
        return usesFeatures;
    }

    public void addUseFeatures(UseFeature useFeature) {
        this.usesFeatures.add(useFeature);
    }

    public String getInstallLocation() {
        return installLocation;
    }

    public void setInstallLocation(String installLocation) {
        this.installLocation = installLocation;
    }

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public List<Permission> getPermissions() {
        return this.permissions;
    }

    @Override
    public String toString() {
        return "packageName: \t" + packageName + "\n"
                + "label: \t" + label + "\n"
                + "icon: \t" + icon + "\n"
                + "versionName: \t" + versionName + "\n"
                + "versionCode: \t" + versionCode + "\n"
                + "minSdkVersion: \t" + minSdkVersion + "\n"
                + "targetSdkVersion: \t" + targetSdkVersion + "\n"
                + "maxSdkVersion: \t" + maxSdkVersion;
    }

}
