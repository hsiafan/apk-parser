package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.struct.ChunkHeader;
import net.dongliu.apk.parser.struct.ChunkType;
import net.dongliu.apk.parser.struct.StringPool;
import net.dongliu.apk.parser.struct.StringPoolHeader;
import net.dongliu.apk.parser.struct.resource.LibraryEntry;
import net.dongliu.apk.parser.struct.resource.LibraryHeader;
import net.dongliu.apk.parser.struct.resource.NullHeader;
import net.dongliu.apk.parser.struct.resource.PackageHeader;
import net.dongliu.apk.parser.struct.resource.ResTableConfig;
import net.dongliu.apk.parser.struct.resource.ResourcePackage;
import net.dongliu.apk.parser.struct.resource.ResourceTable;
import net.dongliu.apk.parser.struct.resource.ResourceTableHeader;
import net.dongliu.apk.parser.struct.resource.Type;
import net.dongliu.apk.parser.struct.resource.TypeHeader;
import net.dongliu.apk.parser.struct.resource.TypeSpec;
import net.dongliu.apk.parser.struct.resource.TypeSpecHeader;
import net.dongliu.apk.parser.utils.Buffers;
import net.dongliu.apk.parser.utils.Pair;
import net.dongliu.apk.parser.utils.ParseUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static net.dongliu.apk.parser.struct.ChunkType.UNKNOWN_YET;

/**
 * Parse android resource table file.
 *
 * @author dongliu
 * @see <a href="https://github.com/aosp-mirror/platform_frameworks_base/blob/master/libs/androidfw/include/androidfw/ResourceTypes.h">ResourceTypes.h</a>
 * @see <a href="https://github.com/aosp-mirror/platform_frameworks_base/blob/master/libs/androidfw/ResourceTypes.cpp">ResourceTypes.cpp</a>
 */
public class ResourceTableParser {

    /**
     * By default the data buffer Chunks is buffer little-endian byte order both at runtime and when stored buffer files.
     */
    private final ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
    private StringPool stringPool;
    private final ByteBuffer buffer;
    // the resource table file size
    private ResourceTable resourceTable;

    private final Set<Locale> locales;

    public ResourceTableParser(final ByteBuffer buffer) {
        this.buffer = buffer.duplicate();
        this.buffer.order(this.byteOrder);
        this.locales = new HashSet<>();
    }

    /**
     * parse resource table file.
     */
    public void parse() {
        // read resource file header.
        final ResourceTableHeader resourceTableHeader = (ResourceTableHeader) this.readChunkHeader();

        // read string pool chunk
        this.stringPool = ParseUtils.readStringPool(this.buffer, (StringPoolHeader) this.readChunkHeader());

        this.resourceTable = new ResourceTable();
        this.resourceTable.setStringPool(this.stringPool);

        final long packageCount = resourceTableHeader.getPackageCount();
        if (packageCount != 0) {
            PackageHeader packageHeader = (PackageHeader) this.readChunkHeader();
            for (int i = 0; i < packageCount; i++) {
                final Pair<ResourcePackage, PackageHeader> pair = this.readPackage(packageHeader);
                this.resourceTable.addPackage(pair.getLeft());
                packageHeader = pair.getRight();
            }
        }
    }

    // read one package
    private Pair<ResourcePackage, PackageHeader> readPackage(final PackageHeader packageHeader) {
        final Pair<ResourcePackage, PackageHeader> pair = new Pair<>();
        //read packageHeader
        final ResourcePackage resourcePackage = new ResourcePackage(packageHeader);
        pair.setLeft(resourcePackage);

        final long beginPos = this.buffer.position();
        // read type string pool
        if (packageHeader.getTypeStrings() > 0) {
            Buffers.position(this.buffer, beginPos + packageHeader.getTypeStrings() - packageHeader.getHeaderSize());
            resourcePackage.setTypeStringPool(ParseUtils.readStringPool(this.buffer,
                    (StringPoolHeader) this.readChunkHeader()));
        }

        //read key string pool
        if (packageHeader.getKeyStrings() > 0) {
            Buffers.position(this.buffer, beginPos + packageHeader.getKeyStrings() - packageHeader.getHeaderSize());
            resourcePackage.setKeyStringPool(ParseUtils.readStringPool(this.buffer,
                    (StringPoolHeader) this.readChunkHeader()));
        }


        outer:
        while (this.buffer.hasRemaining()) {
            final ChunkHeader chunkHeader = this.readChunkHeader();
            final long chunkBegin = this.buffer.position();
            switch (chunkHeader.getChunkType()) {
                case ChunkType.TABLE_TYPE_SPEC:
                    final TypeSpecHeader typeSpecHeader = (TypeSpecHeader) chunkHeader;
                    final long[] entryFlags = new long[typeSpecHeader.getEntryCount()];
                    for (int i = 0; i < typeSpecHeader.getEntryCount(); i++) {
                        entryFlags[i] = Buffers.readUInt(this.buffer);
                    }

                    final TypeSpec typeSpec = new TypeSpec(typeSpecHeader);


                    typeSpec.setEntryFlags(entryFlags);
                    //id start from 1
                    typeSpec.setName(resourcePackage.getTypeStringPool()
                            .get(typeSpecHeader.getId() - 1));

                    resourcePackage.addTypeSpec(typeSpec);
                    Buffers.position(this.buffer, chunkBegin + typeSpecHeader.getBodySize());
                    break;
                case ChunkType.TABLE_TYPE:
                    final TypeHeader typeHeader = (TypeHeader) chunkHeader;
                    // read offsets table
                    final long[] offsets = new long[typeHeader.getEntryCount()];
                    for (int i = 0; i < typeHeader.getEntryCount(); i++) {
                        offsets[i] = Buffers.readUInt(this.buffer);
                    }

                    final Type type = new Type(typeHeader);
                    type.setName(resourcePackage.getTypeStringPool().get(typeHeader.getId() - 1));
                    final long entryPos = chunkBegin + typeHeader.getEntriesStart() - typeHeader.getHeaderSize();
                    Buffers.position(this.buffer, entryPos);
                    final ByteBuffer b = this.buffer.slice();
                    b.order(this.byteOrder);
                    type.setBuffer(b);
                    type.setKeyStringPool(resourcePackage.getKeyStringPool());
                    type.setOffsets(offsets);
                    type.setStringPool(this.stringPool);
                    resourcePackage.addType(type);
                    this.locales.add(type.getLocale());
                    Buffers.position(this.buffer, chunkBegin + typeHeader.getBodySize());
                    break;
                case ChunkType.TABLE_PACKAGE:
                    // another package. we should read next package here
                    pair.setRight((PackageHeader) chunkHeader);
                    break outer;
                case ChunkType.TABLE_LIBRARY:
                    // read entries
                    final LibraryHeader libraryHeader = (LibraryHeader) chunkHeader;
                    for (long i = 0; i < libraryHeader.getCount(); i++) {
                        final int packageId = this.buffer.getInt();
                        final String name = Buffers.readZeroTerminatedString(this.buffer, 128);
                        final LibraryEntry entry = new LibraryEntry(packageId, name);
                        //TODO: now just skip it..
                    }
                    Buffers.position(this.buffer, chunkBegin + chunkHeader.getBodySize());
                    break;
                case ChunkType.NULL:
//                    Buffers.position(buffer, chunkBegin + chunkHeader.getBodySize());
                    Buffers.position(this.buffer, this.buffer.position() + this.buffer.remaining());
                    break;
                default:
                    throw new ParserException("unexpected chunk type: 0x" + chunkHeader.getChunkType());
            }
        }

        return pair;

    }

    private ChunkHeader readChunkHeader() {
        final long begin = this.buffer.position();

        final int chunkType = Buffers.readUShort(this.buffer);
        final int headerSize = Buffers.readUShort(this.buffer);
        final int chunkSize = (int) Buffers.readUInt(this.buffer);

        switch (chunkType) {
            case ChunkType.TABLE:
                final ResourceTableHeader resourceTableHeader = new ResourceTableHeader(headerSize, chunkSize);
                resourceTableHeader.setPackageCount(Buffers.readUInt(this.buffer));
                Buffers.position(this.buffer, begin + headerSize);
                return resourceTableHeader;
            case ChunkType.STRING_POOL:
                final StringPoolHeader stringPoolHeader = new StringPoolHeader(headerSize, chunkSize);
                stringPoolHeader.setStringCount(Buffers.readUInt(this.buffer));
                stringPoolHeader.setStyleCount(Buffers.readUInt(this.buffer));
                stringPoolHeader.setFlags(Buffers.readUInt(this.buffer));
                stringPoolHeader.setStringsStart(Buffers.readUInt(this.buffer));
                stringPoolHeader.setStylesStart(Buffers.readUInt(this.buffer));
                Buffers.position(this.buffer, begin + headerSize);
                return stringPoolHeader;
            case ChunkType.TABLE_PACKAGE:
                final PackageHeader packageHeader = new PackageHeader(headerSize, chunkSize);
                packageHeader.setId(Buffers.readUInt(this.buffer));
                packageHeader.setName(ParseUtils.readStringUTF16(this.buffer, 128));
                packageHeader.setTypeStrings(Buffers.readUInt(this.buffer));
                packageHeader.setLastPublicType(Buffers.readUInt(this.buffer));
                packageHeader.setKeyStrings(Buffers.readUInt(this.buffer));
                packageHeader.setLastPublicKey(Buffers.readUInt(this.buffer));
                Buffers.position(this.buffer, begin + headerSize);
                return packageHeader;
            case ChunkType.TABLE_TYPE_SPEC:
                final TypeSpecHeader typeSpecHeader = new TypeSpecHeader(headerSize, chunkSize);
                typeSpecHeader.setId(Buffers.readUByte(this.buffer));
                typeSpecHeader.setRes0(Buffers.readUByte(this.buffer));
                typeSpecHeader.setRes1(Buffers.readUShort(this.buffer));
                typeSpecHeader.setEntryCount(Buffers.readUInt(this.buffer));
                Buffers.position(this.buffer, begin + headerSize);
                return typeSpecHeader;
            case ChunkType.TABLE_TYPE:
                final TypeHeader typeHeader = new TypeHeader(headerSize, chunkSize);
                typeHeader.setId(Buffers.readUByte(this.buffer));
                typeHeader.setRes0(Buffers.readUByte(this.buffer));
                typeHeader.setRes1(Buffers.readUShort(this.buffer));
                typeHeader.setEntryCount(Buffers.readUInt(this.buffer));
                typeHeader.setEntriesStart(Buffers.readUInt(this.buffer));
                typeHeader.setConfig(this.readResTableConfig());
                Buffers.position(this.buffer, begin + headerSize);
                return typeHeader;
            case ChunkType.TABLE_LIBRARY:
                //DynamicRefTable
                final LibraryHeader libraryHeader = new LibraryHeader(headerSize, chunkSize);
                libraryHeader.setCount(Buffers.readUInt(this.buffer));
                Buffers.position(this.buffer, begin + headerSize);
                return libraryHeader;
            case UNKNOWN_YET:
            case ChunkType.NULL:
                Buffers.position(this.buffer, begin + headerSize);
                return new NullHeader(headerSize, chunkSize);
            default:
                throw new ParserException("Unexpected chunk Type: 0x" + Integer.toHexString(chunkType));
        }
    }

    private ResTableConfig readResTableConfig() {
        final long beginPos = this.buffer.position();
        final ResTableConfig config = new ResTableConfig();
        final long size = Buffers.readUInt(this.buffer);

        // imsi
        config.setMcc(this.buffer.getShort());
        config.setMnc(this.buffer.getShort());
        //read locale
        config.setLanguage(new String(Buffers.readBytes(this.buffer, 2)).replace("\0", ""));
        config.setCountry(new String(Buffers.readBytes(this.buffer, 2)).replace("\0", ""));
        //screen type
        config.setOrientation(Buffers.readUByte(this.buffer));
        config.setTouchscreen(Buffers.readUByte(this.buffer));
        config.setDensity(Buffers.readUShort(this.buffer));
        // now just skip the others...
        final long endPos = this.buffer.position();
        Buffers.skip(this.buffer, (int) (size - (endPos - beginPos)));
        return config;
    }

    public ResourceTable getResourceTable() {
        return this.resourceTable;
    }

    public Set<Locale> getLocales() {
        return this.locales;
    }
}
