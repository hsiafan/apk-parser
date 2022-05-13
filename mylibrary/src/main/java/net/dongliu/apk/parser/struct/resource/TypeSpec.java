package net.dongliu.apk.parser.struct.resource;

/**
 * @author dongliu
 */
public class TypeSpec {

    private long[] entryFlags;
    private String name;
    private short id;

    public TypeSpec(final TypeSpecHeader header) {
        this.id = header.getId();
    }

    public boolean exists(final int id) {
        return id < this.entryFlags.length;
    }

    public long[] getEntryFlags() {
        return this.entryFlags;
    }

    public void setEntryFlags(final long[] entryFlags) {
        this.entryFlags = entryFlags;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public short getId() {
        return this.id;
    }

    public void setId(final short id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "TypeSpec{" +
                "name='" + this.name + '\'' +
                ", id=" + this.id +
                '}';
    }
}
