package net.dongliu.apk.parser.struct.xml;

import java.util.List;

/**
 * @author dongliu
 */
public class XmlNodeStartTag {
    public String namespace;
    public String name;

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

    public List<Attribute> attributeList;

    public String toString(BinaryXmlEnv env, boolean isRoot) {
        StringBuilder sb = new StringBuilder();
        sb.append('<');
        if (namespace != null) {
            sb.append(namespace).append(":");
        }
        sb.append(name);
        if (isRoot && env.namespace != null && env.namespace.uri != null) {
            sb.append(" xmlns:").append(env.namespace.prefix).append("=\"")
                    .append(env.namespace.uri)
                    .append("\"");
        }
        for (Attribute attribute : this.attributeList) {
            sb.append(' ').append(attribute.toString(env));
        }
        sb.append('>');
        return sb.toString();
    }
}
