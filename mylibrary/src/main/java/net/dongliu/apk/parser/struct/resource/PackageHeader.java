package net.dongliu.apk.parser.struct.resource;

import net.dongliu.apk.parser.struct.ChunkHeader;
import net.dongliu.apk.parser.struct.ChunkType;
import net.dongliu.apk.parser.utils.Unsigned;

/**
 * @author dongliu
 */
public class PackageHeader extends ChunkHeader {

    // ResourcePackage IDs start at 1 (corresponding to the value of the package bits in a resource identifier).
    // 0 means this is not a base package.
    // uint32_t
    // 0 framework-res.apk
    // 2-9 other framework files
    // 127 application package

    // Anroid 5.0+: Shared libraries will be assigned a package ID of 0x00 at build-time.
    // At runtime, all loaded shared libraries will be assigned a new package ID.
    private int id;

    // Actual name of this package, -terminated.
    // char16_t name[128]
    private String name;

    // Offset to a ResStringPool_header defining the resource type symbol table.
    //  If zero, this package is inheriting from another base package (overriding specific values in it).
    // uinit 32
    private int typeStrings;


    // Last index into typeStrings that is for public use by others.
    // uint32_t
    private int lastPublicType;

    // Offset to a ResStringPool_header defining the resource
    // key symbol table.  If zero, this package is inheriting from
    // another base package (overriding specific values in it).
    // uint32_t
    private int keyStrings;

    // Last index into keyStrings that is for public use by others.
    // uint32_t
    private int lastPublicKey;

    public PackageHeader(final int headerSize, final long chunkSize) {
        super(ChunkType.TABLE_PACKAGE, headerSize, chunkSize);
    }

    public long getId() {
        return Unsigned.toLong(this.id);
    }

    public void setId(final long id) {
        this.id = Unsigned.toUInt(id);
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getTypeStrings() {
        return this.typeStrings;
    }

    public void setTypeStrings(final long typeStrings) {
        this.typeStrings = Unsigned.ensureUInt(typeStrings);
    }

    public int getLastPublicType() {
        return this.lastPublicType;
    }

    public void setLastPublicType(final long lastPublicType) {
        this.lastPublicType = Unsigned.ensureUInt(lastPublicType);
    }

    public int getKeyStrings() {
        return this.keyStrings;
    }

    public void setKeyStrings(final long keyStrings) {
        this.keyStrings = Unsigned.ensureUInt(keyStrings);
    }

    public int getLastPublicKey() {
        return this.lastPublicKey;
    }

    public void setLastPublicKey(final long lastPublicKey) {
        this.lastPublicKey = Unsigned.ensureUInt(lastPublicKey);
    }
}
