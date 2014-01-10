package net.dongliu.apk.parser.struct.xml;

/**
 * @author dongliu
 */
public class XmlNodeEndTag {
    public String namespace;
    public String name;

    public String toString(BinaryXmlEnv env) {
        StringBuilder sb = new StringBuilder();
        sb.append("</");
        if (namespace != null) {
            sb.append(namespace).append(":");
        }
        sb.append(name).append('>');
        return sb.toString();
    }
}
