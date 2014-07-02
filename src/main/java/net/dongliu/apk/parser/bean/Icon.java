package net.dongliu.apk.parser.bean;

/**
 * Apk icon
 *
 * @author dongliu
 */
public class Icon {

    private String format;

    private String dpiLevel;

    private byte[] data;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getDpiLevel() {
        return dpiLevel;
    }

    public void setDpiLevel(String dpiLevel) {
        this.dpiLevel = dpiLevel;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
