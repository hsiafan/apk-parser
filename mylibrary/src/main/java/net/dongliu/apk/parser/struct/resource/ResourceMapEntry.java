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
    private long parent;

    // Number of name/value pairs that follow for FLAG_COMPLEX. uint32_t
    private long count;

    private ResourceTableMap[] resourceTableMaps;

    public ResourceMapEntry(final ResourceEntry resourceEntry) {
        this.setSize(resourceEntry.getSize());
        this.setFlags(resourceEntry.getFlags());
        this.setKey(resourceEntry.getKey());
    }

    public long getParent() {
        return this.parent;
    }

    public void setParent(final long parent) {
        this.parent = parent;
    }

    public long getCount() {
        return this.count;
    }

    public void setCount(final long count) {
        this.count = count;
    }

    public ResourceTableMap[] getResourceTableMaps() {
        return this.resourceTableMaps;
    }

    public void setResourceTableMaps(final ResourceTableMap[] resourceTableMaps) {
        this.resourceTableMaps = resourceTableMaps;
    }

    /**
     * get value as string
     *
     * @return
     */
    @Override
    public String toStringValue(final ResourceTable resourceTable, final Locale locale) {
        if (this.resourceTableMaps.length > 0) {
            return this.resourceTableMaps[0].toString();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "ResourceMapEntry{" +
                "parent=" + this.parent +
                ", count=" + this.count +
                ", resourceTableMaps=" + Arrays.toString(this.resourceTableMaps) +
                '}';
    }
}
