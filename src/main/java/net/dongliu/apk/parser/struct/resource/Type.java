package net.dongliu.apk.parser.struct.resource;

/**
 * @author dongliu
 */
public class Type {

    public String name;
    public ResourceEntry[] resourceEntries;
    public TypeHeader header;

    public Type(TypeHeader header) {
        this.header = header;
    }

    public ResourceEntry getResourceEntry(int id) {
        if (id >= resourceEntries.length) {
            return null;
        } else {
            return this.resourceEntries[id];
        }
    }
}
