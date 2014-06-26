package net.dongliu.apk.parser.struct.resource;

import java.util.Locale;

/**
 * @author dongliu
 */
public class Type {

    public String name;
    public ResourceEntry[] resourceEntries;
    public Short id;

    public Locale locale;

    public Type(TypeHeader header) {
        this.id = header.id;
        locale = new Locale(header.config.language, header.config.country);
    }

    public ResourceEntry getResourceEntry(int id) {
        if (id >= resourceEntries.length) {
            return null;
        } else {
            return this.resourceEntries[id];
        }
    }
}
