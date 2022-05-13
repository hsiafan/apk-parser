package net.dongliu.apk.parser.struct.xml;

/**
 * @author dongliu
 */
public class XmlNodeEndTag {
    private String namespace;
    private String name;

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("</");
        if (this.namespace != null) {
            sb.append(this.namespace).append(":");
        }
        sb.append(this.name).append('>');
        return sb.toString();
    }
}
