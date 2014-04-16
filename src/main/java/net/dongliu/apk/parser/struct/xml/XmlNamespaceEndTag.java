package net.dongliu.apk.parser.struct.xml;

/**
 * @author dongliu
 */
public class XmlNamespaceEndTag {
    public String prefix;
    public String uri;

    @Override
    public String toString() {
        return prefix + "=" + uri;
    }
}
