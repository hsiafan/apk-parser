package net.dongliu.apk.parser.struct.resource;

import net.dongliu.apk.parser.struct.StringPool;
import net.dongliu.apk.parser.utils.Buffers;
import net.dongliu.apk.parser.utils.ParseUtils;

import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * @author dongliu
 */
public class Type {

    private String name;
    private short id;

    private Locale locale;

    private StringPool keyStringPool;
    private ByteBuffer buffer;
    private long[] offsets;
    private StringPool stringPool;

    // see Densities.java for values
    private final int density;

    public Type(final TypeHeader header) {
        this.id = header.getId();
        final ResTableConfig config = header.getConfig();
        this.locale = new Locale(config.getLanguage(), config.getCountry());
        this.density = config.getDensity();
    }

    public ResourceEntry getResourceEntry(final int id) {
        if (id >= this.offsets.length) {
            return null;
        }

        if (this.offsets[id] == TypeHeader.NO_ENTRY) {
            return null;
        }

        // read Resource Entries
        Buffers.position(this.buffer, this.offsets[id]);
        return this.readResourceEntry();
    }

    private ResourceEntry readResourceEntry() {
        final long beginPos = this.buffer.position();
        final ResourceEntry resourceEntry = new ResourceEntry();
        // size is always 8(simple), or 16(complex)
        resourceEntry.setSize(Buffers.readUShort(this.buffer));
        resourceEntry.setFlags(Buffers.readUShort(this.buffer));
        final long keyRef = this.buffer.getInt();
        final String key = this.keyStringPool.get((int) keyRef);
        resourceEntry.setKey(key);

        if ((resourceEntry.getFlags() & ResourceEntry.FLAG_COMPLEX) != 0) {
            final ResourceMapEntry resourceMapEntry = new ResourceMapEntry(resourceEntry);

            // Resource identifier of the parent mapping, or 0 if there is none.
            resourceMapEntry.setParent(Buffers.readUInt(this.buffer));
            resourceMapEntry.setCount(Buffers.readUInt(this.buffer));

            Buffers.position(this.buffer, beginPos + resourceEntry.getSize());

            //An individual complex Resource entry comprises an entry immediately followed by one or more fields.
            final ResourceTableMap[] resourceTableMaps = new ResourceTableMap[(int) resourceMapEntry.getCount()];
            for (int i = 0; i < resourceMapEntry.getCount(); i++) {
                resourceTableMaps[i] = this.readResourceTableMap();
            }

            resourceMapEntry.setResourceTableMaps(resourceTableMaps);
            return resourceMapEntry;
        } else {
            Buffers.position(this.buffer, beginPos + resourceEntry.getSize());
            resourceEntry.setValue(ParseUtils.readResValue(this.buffer, this.stringPool));
            return resourceEntry;
        }
    }

    private ResourceTableMap readResourceTableMap() {
        final ResourceTableMap resourceTableMap = new ResourceTableMap();
        resourceTableMap.setNameRef(Buffers.readUInt(this.buffer));
        resourceTableMap.setResValue(ParseUtils.readResValue(this.buffer, this.stringPool));

        //noinspection StatementWithEmptyBody
        if ((resourceTableMap.getNameRef() & 0x02000000) != 0) {
            //read arrays
        } else //noinspection StatementWithEmptyBody
            if ((resourceTableMap.getNameRef() & 0x01000000) != 0) {
                // read attrs
            } else {
            }

        return resourceTableMap;
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

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    public StringPool getKeyStringPool() {
        return this.keyStringPool;
    }

    public void setKeyStringPool(final StringPool keyStringPool) {
        this.keyStringPool = keyStringPool;
    }

    public ByteBuffer getBuffer() {
        return this.buffer;
    }

    public void setBuffer(final ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public long[] getOffsets() {
        return this.offsets;
    }

    public void setOffsets(final long[] offsets) {
        this.offsets = offsets;
    }

    public StringPool getStringPool() {
        return this.stringPool;
    }

    public void setStringPool(final StringPool stringPool) {
        this.stringPool = stringPool;
    }

    public int getDensity() {
        return this.density;
    }

    @Override
    public String toString() {
        return "Type{" +
                "name='" + this.name + '\'' +
                ", id=" + this.id +
                ", locale=" + this.locale +
                '}';
    }
}
