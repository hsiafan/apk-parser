package net.dongliu.apk.parser.bean;

/**
 * the glEsVersion apk used.
 *
 * @author dongliu
 */
public class GlEsVersion {
    private int major;
    private int minor;
    private boolean required;

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public String toString() {
        return this.major + "." + this.minor;
    }
}
