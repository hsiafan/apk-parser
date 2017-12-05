package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.struct.ChunkHeader;
import net.dongliu.apk.parser.struct.ChunkType;
import net.dongliu.apk.parser.struct.StringPool;
import net.dongliu.apk.parser.struct.StringPoolHeader;
import net.dongliu.apk.parser.struct.resource.*;
import net.dongliu.apk.parser.utils.ByteBuffers;
import net.dongliu.apk.parser.utils.Pair;
import net.dongliu.apk.parser.utils.ParseUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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
    private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
    private StringPool stringPool;
    private ByteBuffer buffer;
    // the resource table file size
    private ResourceTable resourceTable;

    private Set<Locale> locales;

    public ResourceTableParser(ByteBuffer buffer) {
        this.buffer = buffer.duplicate();
        this.buffer.order(byteOrder);
        this.locales = new HashSet<>();
    }

    /**
     * parse resource table file.
     */
    public void parse() {
        // read resource file header.
        ResourceTableHeader resourceTableHeader = (ResourceTableHeader) readChunkHeader();

        // read string pool chunk
        stringPool = ParseUtils.readStringPool(buffer, (StringPoolHeader) readChunkHeader());

        resourceTable = new ResourceTable();
        resourceTable.setStringPool(stringPool);

        PackageHeader packageHeader = (PackageHeader) readChunkHeader();
        for (int i = 0; i < resourceTableHeader.getPackageCount(); i++) {
            Pair<ResourcePackage, PackageHeader> pair = readPackage(packageHeader);
            resourceTable.addPackage(pair.getLeft());
            packageHeader = pair.getRight();
        }
    }

    // read one package
    private Pair<ResourcePackage, PackageHeader> readPackage(PackageHeader packageHeader) {
        Pair<ResourcePackage, PackageHeader> pair = new Pair<>();
        //read packageHeader
        ResourcePackage resourcePackage = new ResourcePackage(packageHeader);
        pair.setLeft(resourcePackage);

        long beginPos = buffer.position();
        // read type string pool
        if (packageHeader.getTypeStrings() > 0) {
            ByteBuffers.position(buffer, beginPos + packageHeader.getTypeStrings() - packageHeader.getHeaderSize());
            resourcePackage.setTypeStringPool(ParseUtils.readStringPool(buffer,
                    (StringPoolHeader) readChunkHeader()));
        }

        //read key string pool
        if (packageHeader.getKeyStrings() > 0) {
            ByteBuffers.position(buffer, beginPos + packageHeader.getKeyStrings() - packageHeader.getHeaderSize());
            resourcePackage.setKeyStringPool(ParseUtils.readStringPool(buffer,
                    (StringPoolHeader) readChunkHeader()));
        }


        outer:
        while (buffer.hasRemaining()) {
            ChunkHeader chunkHeader = readChunkHeader();
            long chunkBegin = buffer.position();
            switch (chunkHeader.getChunkType()) {
                case ChunkType.TABLE_TYPE_SPEC:
                    TypeSpecHeader typeSpecHeader = (TypeSpecHeader) chunkHeader;
                    long[] entryFlags = new long[(int) typeSpecHeader.getEntryCount()];
                    for (int i = 0; i < typeSpecHeader.getEntryCount(); i++) {
                        entryFlags[i] = ByteBuffers.readUInt(buffer);
                    }

                    TypeSpec typeSpec = new TypeSpec(typeSpecHeader);


                    typeSpec.setEntryFlags(entryFlags);
                    //id start from 1
                    typeSpec.setName(resourcePackage.getTypeStringPool()
                            .get(typeSpecHeader.getId() - 1));

                    resourcePackage.addTypeSpec(typeSpec);
                    ByteBuffers.position(buffer, chunkBegin + typeSpecHeader.getBodySize());
                    break;
                case ChunkType.TABLE_TYPE:
                    TypeHeader typeHeader = (TypeHeader) chunkHeader;
                    // read offsets table
                    long[] offsets = new long[(int) typeHeader.getEntryCount()];
                    for (int i = 0; i < typeHeader.getEntryCount(); i++) {
                        offsets[i] = ByteBuffers.readUInt(buffer);
                    }

                    Type type = new Type(typeHeader);
                    type.setName(resourcePackage.getTypeStringPool().get(typeHeader.getId() - 1));
                    long entryPos = chunkBegin + typeHeader.getEntriesStart() - typeHeader.getHeaderSize();
                    ByteBuffers.position(buffer, entryPos);
                    ByteBuffer b = buffer.slice();
                    b.order(byteOrder);
                    type.setBuffer(b);
                    type.setKeyStringPool(resourcePackage.getKeyStringPool());
                    type.setOffsets(offsets);
                    type.setStringPool(stringPool);
                    resourcePackage.addType(type);
                    locales.add(type.getLocale());
                    ByteBuffers.position(buffer, chunkBegin + typeHeader.getBodySize());
                    break;
                case ChunkType.TABLE_PACKAGE:
                    // another package. we should read next package here
                    pair.setRight((PackageHeader) chunkHeader);
                    break outer;
                case ChunkType.TABLE_LIBRARY:
                    // read entries
                    LibraryHeader libraryHeader = (LibraryHeader) chunkHeader;
                    for (long i = 0; i < libraryHeader.getCount(); i++) {
                        int packageId = buffer.getInt();
                        String name = ByteBuffers.readZeroTerminatedString(buffer, 128);
                        LibraryEntry entry = new LibraryEntry(packageId, name);
                        //TODO: now just skip it..
                    }
                    ByteBuffers.position(buffer, chunkBegin + chunkHeader.getBodySize());
                    break;
                case ChunkType.NULL:
//                    ByteBuffers.position(buffer, chunkBegin + chunkHeader.getBodySize());
                    ByteBuffers.position(buffer, buffer.position() + buffer.remaining());
                    break;
                default:
                    throw new ParserException("unexpected chunk type: 0x" + chunkHeader.getChunkType());
            }
        }

        return pair;

    }

    private ChunkHeader readChunkHeader() {
        long begin = buffer.position();

        int chunkType = ByteBuffers.readUShort(buffer);
        int headerSize = ByteBuffers.readUShort(buffer);
        long chunkSize = ByteBuffers.readUInt(buffer);

        switch (chunkType) {
            case ChunkType.TABLE:
                ResourceTableHeader resourceTableHeader = new ResourceTableHeader(chunkType,
                        headerSize, chunkSize);
                resourceTableHeader.setPackageCount(ByteBuffers.readUInt(buffer));
                ByteBuffers.position(buffer, begin + headerSize);
                return resourceTableHeader;
            case ChunkType.STRING_POOL:
                StringPoolHeader stringPoolHeader = new StringPoolHeader(chunkType, headerSize,
                        chunkSize);
                stringPoolHeader.setStringCount(ByteBuffers.readUInt(buffer));
                stringPoolHeader.setStyleCount(ByteBuffers.readUInt(buffer));
                stringPoolHeader.setFlags(ByteBuffers.readUInt(buffer));
                stringPoolHeader.setStringsStart(ByteBuffers.readUInt(buffer));
                stringPoolHeader.setStylesStart(ByteBuffers.readUInt(buffer));
                ByteBuffers.position(buffer, begin + headerSize);
                return stringPoolHeader;
            case ChunkType.TABLE_PACKAGE:
                PackageHeader packageHeader = new PackageHeader(chunkType, headerSize, chunkSize);
                packageHeader.setId(ByteBuffers.readUInt(buffer));
                packageHeader.setName(ParseUtils.readStringUTF16(buffer, 128));
                packageHeader.setTypeStrings(ByteBuffers.readUInt(buffer));
                packageHeader.setLastPublicType(ByteBuffers.readUInt(buffer));
                packageHeader.setKeyStrings(ByteBuffers.readUInt(buffer));
                packageHeader.setLastPublicKey(ByteBuffers.readUInt(buffer));
                ByteBuffers.position(buffer, begin + headerSize);
                return packageHeader;
            case ChunkType.TABLE_TYPE_SPEC:
                TypeSpecHeader typeSpecHeader = new TypeSpecHeader(chunkType, headerSize, chunkSize);
                typeSpecHeader.setId(ByteBuffers.readUByte(buffer));
                typeSpecHeader.setRes0(ByteBuffers.readUByte(buffer));
                typeSpecHeader.setRes1(ByteBuffers.readUShort(buffer));
                typeSpecHeader.setEntryCount(ByteBuffers.readUInt(buffer));
                ByteBuffers.position(buffer, begin + headerSize);
                return typeSpecHeader;
            case ChunkType.TABLE_TYPE:
                TypeHeader typeHeader = new TypeHeader(chunkType, headerSize, chunkSize);
                typeHeader.setId(ByteBuffers.readUByte(buffer));
                typeHeader.setRes0(ByteBuffers.readUByte(buffer));
                typeHeader.setRes1(ByteBuffers.readUShort(buffer));
                typeHeader.setEntryCount(ByteBuffers.readUInt(buffer));
                typeHeader.setEntriesStart(ByteBuffers.readUInt(buffer));
                typeHeader.setConfig(readResTableConfig());
                ByteBuffers.position(buffer, begin + headerSize);
                return typeHeader;
            case ChunkType.TABLE_LIBRARY:
                //DynamicRefTable
                LibraryHeader libraryHeader = new LibraryHeader(chunkType, headerSize, chunkSize);
                libraryHeader.setCount(ByteBuffers.readUInt(buffer));
                ByteBuffers.position(buffer, begin + headerSize);
                return libraryHeader;

            case ChunkType.NULL:
                ByteBuffers.position(buffer, begin + headerSize);
                return new NullHeader(chunkType, headerSize, chunkSize);
            default:
                throw new ParserException("Unexpected chunk Type: 0x" + Integer.toHexString(chunkType));
        }
    }

    private ResTableConfig readResTableConfig() {
        long beginPos = buffer.position();
        ResTableConfig config = new ResTableConfig();
        long size = ByteBuffers.readUInt(buffer);

        // imsi
        config.setMcc(ByteBuffers.readUShort(buffer));
        config.setMnc(ByteBuffers.readUShort(buffer));
        //read locale
        config.setLanguage(new String(ByteBuffers.readBytes(buffer, 2)).replace("\0", ""));
        config.setCountry(new String(ByteBuffers.readBytes(buffer, 2)).replace("\0", ""));
        //screen type
        config.setOrientation(ByteBuffers.readUByte(buffer));
        config.setTouchscreen(ByteBuffers.readUByte(buffer));
        config.setDensity(ByteBuffers.readUShort(buffer));
        // now just skip the others...
        long endPos = buffer.position();
        ByteBuffers.skip(buffer, (int) (size - (endPos - beginPos)));
        return config;
    }

    public ResourceTable getResourceTable() {
        return resourceTable;
    }

    public Set<Locale> getLocales() {
        return this.locales;
    }
}
