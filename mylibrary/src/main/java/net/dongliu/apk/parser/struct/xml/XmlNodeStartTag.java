package net.dongliu.apk.parser.struct.xml;

/**
 * @author dongliu
 */
public class XmlNodeStartTag {
    private String namespace;
    private String name;

    // Byte offset from the start of this structure where the attributes start. uint16
    //public int attributeStart;
    // Size of the ResXMLTree_attribute structures that follow. unit16
    //public int attributeSize;
    // Number of attributes associated with an ELEMENT. uint 16
    // These are available as an array of ResXMLTree_attribute structures immediately following this node.
    //public int attributeCount;
    // Index (1-based) of the "id" attribute. 0 if none. uint16
    //public short idIndex;
    // Index (1-based) of the "style" attribute. 0 if none. uint16
    //public short styleIndex;

    private Attributes attributes;

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

    public Attributes getAttributes() {
        return this.attributes;
    }

    public void setAttributes(final Attributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('<');
        if (this.namespace != null) {
            sb.append(this.namespace).append(":");
        }
        sb.append(this.name);
        sb.append('>');
        return sb.toString();
    }
}
