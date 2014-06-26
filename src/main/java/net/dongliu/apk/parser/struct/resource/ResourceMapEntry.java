package net.dongliu.apk.parser.struct.resource;

import java.util.Arrays;
import java.util.Locale;

/**
 * @author dongliu.
 */
public class ResourceMapEntry extends ResourceEntry {
    // Resource identifier of the parent mapping, or 0 if there is none.
    //ResTable_ref specifies the parent Resource, if any, of this Resource.
    // struct ResTable_ref { uint32_t ident; };
    public long parent;

    // Number of name/value pairs that follow for FLAG_COMPLEX. uint32_t
    public long count;

    public ResourceTableMap[] resourceTableMaps;

    public ResourceMapEntry(ResourceEntry resourceEntry) {
        this.size = resourceEntry.size;
        this.flags = resourceEntry.flags;
        this.key = resourceEntry.key;
    }

    /**
     * get value as string
     *
     * @return
     */
    public String toStringValue(ResourceTable resourceTable, Locale locale) {
        if (resourceTableMaps.length > 0) {
            return resourceTableMaps[0].toString();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "ResourceMapEntry{" +
                "parent=" + parent +
                ", count=" + count +
                ", resourceTableMaps=" + Arrays.toString(resourceTableMaps) +
                '}';
    }
}
