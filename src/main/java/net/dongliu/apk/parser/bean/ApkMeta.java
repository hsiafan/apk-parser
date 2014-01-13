package net.dongliu.apk.parser.bean;

/**
 * @author dongliu
 */
public class ApkMeta {

    private String packageName;

    private String name;

    private String versionName;

    private String versionCode;

    private String minSdkVersion;

    private String targetSdkVersion;

    private String maxSdkVersion;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
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
}
