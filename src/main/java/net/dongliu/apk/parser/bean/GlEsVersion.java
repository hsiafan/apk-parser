package net.dongliu.apk.parser.bean;

/**
 * the glEsVersion apk used.
 *
 * @author dongliu
 */
public class GlEsVersion implements Feature {
    private int major;
    private int minor;
    private boolean required = true;

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

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public String toString() {
        return this.major + "." + this.minor;
    }
}
