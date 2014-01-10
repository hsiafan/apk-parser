package net.dongliu.apk.parser.struct.resource;

import net.dongliu.apk.parser.struct.ChunkHeader;

/**
 * @author dongliu
 */
public class PackageHeader extends ChunkHeader {

    // If this is a base package, its ID.  ResourcePackage IDs start at 1 (corresponding to the value of
    // the package bits in a resource identifier).  0 means this is not a base package.
    // uint32_t
    public long id;

    // Actual name of this package, -terminated.
    // char16_t name[128]
    public String name;

    // Offset to a ResStringPool_header defining the resource type symbol table.
    //  If zero, this package is inheriting from another base package (overriding specific values in it).
    // uinit 32
    public long typeStrings;


    // Last index into typeStrings that is for public use by others.
    // uint32_t
    public long lastPublicType;

    // Offset to a ResStringPool_header defining the resource
    // key symbol table.  If zero, this package is inheriting from
    // another base package (overriding specific values in it).
    // uint32_t
    public long keyStrings;

    // Last index into keyStrings that is for public use by others.
    // uint32_t
    public long lastPublicKey;

    public PackageHeader(int chunkType, int headerSize, long chunkSize) {
        super(chunkType, headerSize, chunkSize);
    }
}
