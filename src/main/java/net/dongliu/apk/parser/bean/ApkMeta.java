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

    private int minSdkVersion = -1;

    private int targetSdkVersion = -1;

    private int maxSdkVersion = -1;

    private GlEsVersion glEsVersion;

    private boolean anyDensity;

    private boolean smallScreens;

    private boolean normalScreens;

    private boolean largeScreens;

    private List<String> permissions = new ArrayList<String>();

    private List<UsePermission> usePermissions = new ArrayList<UsePermission>();

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

    public int getMinSdkVersion() {
        return minSdkVersion;
    }

    public void setMinSdkVersion(int minSdkVersion) {
        this.minSdkVersion = minSdkVersion;
    }

    public int getTargetSdkVersion() {
        return targetSdkVersion;
    }

    public void setTargetSdkVersion(int targetSdkVersion) {
        this.targetSdkVersion = targetSdkVersion;
    }

    public int getMaxSdkVersion() {
        return maxSdkVersion;
    }

    public void setMaxSdkVersion(int maxSdkVersion) {
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

    public List<UsePermission> getUsePermissions() {
        return usePermissions;
    }

    public void setUsePermissions(List<UsePermission> usePermissions) {
        this.usePermissions = usePermissions;
    }

    public void addUsePermission(UsePermission usePermission) {
        this.usePermissions.add(usePermission);
    }
}
