package net.dongliu.apk.parser.struct.resource;

/**
 * @author dongliu
 */
public class TypeSpec {

    public long[] entryFlags;
    public String name;
    public TypeSpecHeader header;

    public TypeSpec(TypeSpecHeader header) {
        this.header = header;
    }

    public boolean exists(int id) {
        return id < entryFlags.length;
    }
}
