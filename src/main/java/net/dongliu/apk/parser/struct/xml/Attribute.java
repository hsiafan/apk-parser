package net.dongliu.apk.parser.struct.xml;

import net.dongliu.apk.parser.struct.ResValue;

/**
 * xml node attribute
 *
 * @author dongliu
 */
public class Attribute {
    public String namespace;
    public String name;
    // The original raw string value of this attribute.
    public String rawValue;
    // Processesd typed value of this attribute.
    public ResValue typedValue;

    public String toString(BinaryXmlEnv env) {
        StringBuilder sb = new StringBuilder();
        if (namespace != null) {
            if (namespace.equals(env.namespace.uri)) {
                if (env.namespace.prefix != null && !env.namespace.prefix.isEmpty()) {
                    sb.append(env.namespace.prefix).append(':');
                }
            } else {
                if (!namespace.isEmpty()) {
                    sb.append(namespace).append(':');
                }
            }
        }
        sb.append(name).append('=').append('"');
        if (rawValue != null) {
            sb.append(rawValue);
        } else {
            sb.append(typedValue.toString());
        }
        sb.append('"');
        return sb.toString();
    }
}
