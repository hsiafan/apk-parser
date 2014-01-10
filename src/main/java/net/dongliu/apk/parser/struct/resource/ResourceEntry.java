package net.dongliu.apk.parser.struct.resource;

import net.dongliu.apk.parser.struct.ResValue;

/**
 * A Resource entry specifies the key (name) of the Resource.
 * It is immediately followed by the value of that Resource.
 *
 * @author dongliu
 */
public class ResourceEntry {
    // Number of bytes in this structure. uint16_t
    public int size;

    // If set, this is a complex entry, holding a set of name/value
    // mappings.  It is followed by an array of ResTable_map structures.
    public static final int FLAG_COMPLEX = 0x0001;
    // If set, this resource has been declared public, so libraries
    // are allowed to reference it.
    public static final int FLAG_PUBLIC = 0x0002;
    // uint16_t
    public int flags;

    // Reference into ResTable_package::keyStrings identifying this entry.
    //public long keyRef;

    public String key;

    // the resvalue following this resource entry.
    public ResValue value;

    @Override
    public String toString() {
        return value.toString();
    }
}
