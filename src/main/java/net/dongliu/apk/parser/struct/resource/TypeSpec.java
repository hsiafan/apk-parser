package net.dongliu.apk.parser.struct.resource;

/**
 * @author dongliu
 */
public class TypeSpec {

    public long[] entryFlags;
    public String name;
    public Short id;

    public TypeSpec(TypeSpecHeader header) {
        this.id = header.id;
    }

    public boolean exists(int id) {
        return id < entryFlags.length;
    }
}
