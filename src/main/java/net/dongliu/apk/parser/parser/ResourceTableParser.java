package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.struct.ChunkHeader;
import net.dongliu.apk.parser.struct.ChunkType;
import net.dongliu.apk.parser.struct.StringPool;
import net.dongliu.apk.parser.struct.StringPoolHeader;
import net.dongliu.apk.parser.struct.resource.*;
import net.dongliu.apk.parser.utils.Buffers;
import net.dongliu.apk.parser.utils.ParseUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * parse android resource table file.
 * see http://justanapplication.wordpress.com/category/android/android-resources/
 *
 * @author dongliu
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
        resourceTable.stringPool = stringPool;

        PackageHeader packageHeader = (PackageHeader) readChunkHeader();
        for (int i = 0; i < resourceTableHeader.packageCount; i++) {
            PackageHeader[] packageHeaders = new PackageHeader[1];
            ResourcePackage resourcePackage = readPackage(packageHeader, packageHeaders);
            resourceTable.addPackage(resourcePackage);
            packageHeader = packageHeaders[0];
        }
    }

    // read one package
    private ResourcePackage readPackage(PackageHeader packageHeader, PackageHeader[] packageHeaders) {
        //read packageHeader
        ResourcePackage resourcePackage = new ResourcePackage(packageHeader);

        long beginPos = buffer.position();
        // read type string pool
        if (packageHeader.typeStrings > 0) {
            buffer.position((int) (beginPos + packageHeader.typeStrings - packageHeader.headerSize));
            resourcePackage.typeStringPool = ParseUtils.readStringPool(buffer,
                    (StringPoolHeader) readChunkHeader());
        }

        //read key string pool
        if (packageHeader.keyStrings > 0) {
            buffer.position((int) (beginPos + packageHeader.keyStrings - packageHeader.headerSize));
            resourcePackage.keyStringPool = ParseUtils.readStringPool(buffer,
                    (StringPoolHeader) readChunkHeader());
        }


        outer:
        while (buffer.hasRemaining()) {
            ChunkHeader chunkHeader = readChunkHeader();
            switch (chunkHeader.chunkType) {
                case ChunkType.TABLE_TYPE_SPEC:
                    long typeSpecChunkBegin = buffer.position();
                    TypeSpecHeader typeSpecHeader = (TypeSpecHeader) chunkHeader;
                    long[] entryFlags = new long[(int) typeSpecHeader.entryCount];
                    for (int i = 0; i < typeSpecHeader.entryCount; i++) {
                        entryFlags[i] = Buffers.readUInt(buffer);
                    }

                    TypeSpec typeSpec = new TypeSpec(typeSpecHeader);

                    typeSpec.entryFlags = entryFlags;
                    //id start from 1
                    typeSpec.name = resourcePackage.typeStringPool.get(typeSpecHeader.id - 1);

                    resourcePackage.addTypeSpec(typeSpec);
                    buffer.position((int) (typeSpecChunkBegin + typeSpecHeader.chunkSize -
                            typeSpecHeader.headerSize));
                    break;
                case ChunkType.TABLE_TYPE:
                    long typeChunkBegin = buffer.position();
                    TypeHeader typeHeader = (TypeHeader) chunkHeader;
                    // read offsets table
                    long[] offsets = new long[(int) typeHeader.entryCount];
                    for (int i = 0; i < typeHeader.entryCount; i++) {
                        offsets[i] = Buffers.readUInt(buffer);
                    }

                    long entryPos = typeChunkBegin + typeHeader.entriesStart - typeHeader.headerSize;
                    buffer.position((int) entryPos);
                    // read Resource Entries
                    ResourceEntry[] resourceEntries = new ResourceEntry[offsets.length];
                    for (int i = 0; i < offsets.length; i++) {
                        if (offsets[i] != TypeHeader.NO_ENTRY) {
                            buffer.position((int) (entryPos + offsets[i]));
                            resourceEntries[i] = readResourceEntry(resourcePackage.keyStringPool);
                        } else {
                            resourceEntries[i] = null;
                        }
                    }
                    Type type = new Type(typeHeader);
                    type.name = resourcePackage.typeStringPool.get(typeHeader.id - 1);
                    type.resourceEntries = resourceEntries;
                    resourcePackage.addType(type);
                    locales.add(type.locale);
                    buffer.position((int) (typeChunkBegin + typeHeader.chunkSize - typeHeader.headerSize));
                    break;
                case ChunkType.TABLE_PACKAGE:
                    // another package. we should read next package here
                    packageHeaders[0] = (PackageHeader) chunkHeader;
                    break outer;
                default:
                    throw new ParserException("unexpected chunk type:" + chunkHeader.chunkType);
            }
        }

        return resourcePackage;

    }

    private ResourceEntry readResourceEntry(StringPool keyStringPool) {
        long beginPos = buffer.position();
        ResourceEntry resourceEntry = new ResourceEntry();
        // size is always 8(simple), or 16(complex)
        resourceEntry.size = Buffers.readUShort(buffer);
        resourceEntry.flags = Buffers.readUShort(buffer);
        long keyRef = buffer.getInt();
        resourceEntry.key = keyStringPool.get((int) keyRef);

        if ((resourceEntry.flags & ResourceEntry.FLAG_COMPLEX) != 0) {
            ResourceMapEntry resourceMapEntry = new ResourceMapEntry(resourceEntry);

            // Resource identifier of the parent mapping, or 0 if there is none.
            resourceMapEntry.parent = Buffers.readUInt(buffer);
            resourceMapEntry.count = Buffers.readUInt(buffer);

            buffer.position((int) (beginPos + resourceEntry.size));

            //An individual complex Resource entry comprises an entry immediately followed by one or more fields.
            ResourceTableMap[] resourceTableMaps = new ResourceTableMap[(int) resourceMapEntry.count];
            for (int i = 0; i < resourceMapEntry.count; i++) {
                resourceTableMaps[i] = readResourceTableMap();
            }

            resourceMapEntry.resourceTableMaps = resourceTableMaps;
            return resourceMapEntry;
        } else {
            buffer.position((int) (beginPos + resourceEntry.size));
            resourceEntry.value = ParseUtils.readResValue(buffer, stringPool);
            return resourceEntry;
        }
    }

    private ResourceTableMap readResourceTableMap() {
        ResourceTableMap resourceTableMap = new ResourceTableMap();
        resourceTableMap.nameRef = Buffers.readUInt(buffer);
        resourceTableMap.resValue = ParseUtils.readResValue(buffer, stringPool);

        if ((resourceTableMap.nameRef & 0x02000000) != 0) {
            //read arrays
        } else if ((resourceTableMap.nameRef & 0x01000000) != 0) {
            // read attrs
        } else {
        }

        return resourceTableMap;
    }

    private ChunkHeader readChunkHeader() {
        long begin = buffer.position();

        int chunkType = Buffers.readUShort(buffer);
        int headerSize = Buffers.readUShort(buffer);
        long chunkSize = Buffers.readUInt(buffer);

        switch (chunkType) {
            case ChunkType.TABLE:
                ResourceTableHeader resourceTableHeader = new ResourceTableHeader(chunkType,
                        headerSize, chunkSize);
                resourceTableHeader.packageCount = Buffers.readUInt(buffer);
                buffer.position((int) (begin + headerSize));
                return resourceTableHeader;
            case ChunkType.STRING_POOL:
                StringPoolHeader stringPoolHeader = new StringPoolHeader(chunkType, headerSize,
                        chunkSize);
                stringPoolHeader.stringCount = Buffers.readUInt(buffer);
                stringPoolHeader.styleCount = Buffers.readUInt(buffer);
                stringPoolHeader.flags = Buffers.readUInt(buffer);
                stringPoolHeader.stringsStart = Buffers.readUInt(buffer);
                stringPoolHeader.stylesStart = Buffers.readUInt(buffer);
                buffer.position((int) (begin + headerSize));
                return stringPoolHeader;
            case ChunkType.TABLE_PACKAGE:
                PackageHeader packageHeader = new PackageHeader(chunkType, headerSize, chunkSize);
                packageHeader.id = Buffers.readUInt(buffer);
                packageHeader.name = ParseUtils.readStringUTF16(buffer, 128);
                packageHeader.typeStrings = Buffers.readUInt(buffer);
                packageHeader.lastPublicType = Buffers.readUInt(buffer);
                packageHeader.keyStrings = Buffers.readUInt(buffer);
                packageHeader.lastPublicKey = Buffers.readUInt(buffer);
                buffer.position((int) (begin + headerSize));
                return packageHeader;
            case ChunkType.TABLE_TYPE_SPEC:
                TypeSpecHeader typeSpecHeader = new TypeSpecHeader(chunkType, headerSize, chunkSize);
                typeSpecHeader.id = Buffers.readUByte(buffer);
                typeSpecHeader.res0 = Buffers.readUByte(buffer);
                typeSpecHeader.res1 = Buffers.readUShort(buffer);
                typeSpecHeader.entryCount = Buffers.readUInt(buffer);
                buffer.position((int) (begin + headerSize));
                return typeSpecHeader;
            case ChunkType.TABLE_TYPE:
                TypeHeader typeHeader = new TypeHeader(chunkType, headerSize, chunkSize);
                typeHeader.id = Buffers.readUByte(buffer);
                typeHeader.res0 = Buffers.readUByte(buffer);
                typeHeader.res1 = Buffers.readUShort(buffer);
                typeHeader.entryCount = Buffers.readUInt(buffer);
                typeHeader.entriesStart = Buffers.readUInt(buffer);
                typeHeader.config = readResTableConfig();
                buffer.position((int) (begin + headerSize));
                return typeHeader;
            case ChunkType.NULL:
                //buffer.skip((int) (chunkSize - headerSize));
            default:
                throw new ParserException("Unexpected chunk Type:" + Integer.toHexString(chunkType));
        }
    }

    private ResTableConfig readResTableConfig() {
        long beginPos = buffer.position();
        ResTableConfig config = new ResTableConfig();
        long size = Buffers.readUInt(buffer);
        Buffers.skip(buffer, 4);
        //read locale
        config.language = new String(Buffers.readBytes(buffer, 2)).replace("\0", "");
        config.country = new String(Buffers.readBytes(buffer, 2)).replace("\0", "");

        long endPos = buffer.position();
        Buffers.skip(buffer, (int) (size - (endPos - beginPos)));
        return config;
    }

    public ResourceTable getResourceTable() {
        return resourceTable;
    }

    public Set<Locale> getLocales() {
        return this.locales;
    }
}
