package net.dongliu.apk.parser.struct.resource;

import net.dongliu.apk.parser.struct.ResourceValue;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * A Resource entry specifies the key (name) of the Resource.
 * It is immediately followed by the value of that Resource.
 *
 * @author dongliu
 */
public class ResourceEntry {
    // Number of bytes in this structure. uint16_t
    private int size;

    // If set, this is a complex entry, holding a set of name/value
    // mappings.  It is followed by an array of ResTable_map structures.
    public static final int FLAG_COMPLEX = 0x0001;
    // If set, this resource has been declared public, so libraries
    // are allowed to reference it.
    public static final int FLAG_PUBLIC = 0x0002;
    // uint16_t
    private int flags;

    // Reference into ResTable_package::keyStrings identifying this entry.
    //public long keyRef;

    private String key;

    // the resvalue following this resource entry.
    private ResourceValue value;

    /**
     * get value as string
     *
     * @return
     */
    public String toStringValue(final ResourceTable resourceTable, final Locale locale) {
        if (this.value != null) {
            return this.value.toStringValue(resourceTable, locale);
        } else {
            return "null";
        }
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    public int getFlags() {
        return this.flags;
    }

    public void setFlags(final int flags) {
        this.flags = flags;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    @Nullable
    public ResourceValue getValue() {
        return this.value;
    }

    public void setValue(@Nullable final ResourceValue value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ResourceEntry{" +
                "size=" + this.size +
                ", flags=" + this.flags +
                ", key='" + this.key + '\'' +
                ", value=" + this.value +
                '}';
    }
}
