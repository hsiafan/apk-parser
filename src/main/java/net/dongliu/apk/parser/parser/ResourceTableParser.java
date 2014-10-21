package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.io.TellableInputStream;
import net.dongliu.apk.parser.struct.*;
import net.dongliu.apk.parser.struct.resource.*;
import net.dongliu.apk.parser.utils.ParseUtils;

import java.io.IOException;
import java.io.InputStream;
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
     * By default the data in Chunks is in little-endian byte order both at runtime and when stored in files.
     */
    private ByteOrder byteOrder = ByteOrder.LITTLE;
    private StringPool stringPool;
    private TellableInputStream in;
    // the resource table file size
    private final long size;
    private ResourceTable resourceTable;

    private Set<Locale> locales;

    public ResourceTableParser(InputStream in, long size) {
        this.in = new TellableInputStream(in, byteOrder);
        this.locales = new HashSet<Locale>();
        this.size = size;
    }

    /**
     * parse resource table file.
     *
     * @throws IOException
     */
    public void parse() throws IOException {
        try {
            // read resource file header.
            ResourceTableHeader resourceTableHeader = (ResourceTableHeader) readChunkHeader();

            // read string pool chunk
            stringPool = ParseUtils.readStringPool(in, (StringPoolHeader) readChunkHeader());

            resourceTable = new ResourceTable();
            resourceTable.stringPool = stringPool;

            PackageHeader packageHeader = (PackageHeader) readChunkHeader();
            for (int i = 0; i < resourceTableHeader.packageCount; i++) {
                PackageHeader[] packageHeaders = new PackageHeader[1];
                ResourcePackage resourcePackage = readPackage(packageHeader, packageHeaders);
                resourceTable.addPackage(resourcePackage);
                packageHeader = packageHeaders[0];
            }
        } finally {
            in.close();
        }

    }

    // read one package
    private ResourcePackage readPackage(PackageHeader packageHeader, PackageHeader[] packageHeaders)
            throws IOException {
        //read packageHeader
        ResourcePackage resourcePackage = new ResourcePackage(packageHeader);

        long beginPos = in.tell();
        // read type string pool
        if (packageHeader.typeStrings > 0) {
            in.advanceToPos(beginPos + packageHeader.typeStrings - packageHeader.headerSize);
            resourcePackage.typeStringPool = ParseUtils.readStringPool(in,
                    (StringPoolHeader) readChunkHeader());
        }

        //read key string pool
        if (packageHeader.keyStrings > 0) {
            in.advanceToPos(beginPos + packageHeader.keyStrings - packageHeader.headerSize);
            resourcePackage.keyStringPool = ParseUtils.readStringPool(in,
                    (StringPoolHeader) readChunkHeader());
        }


        outer:
        while (in.tell() < size) {
            ChunkHeader chunkHeader = readChunkHeader();
            switch (chunkHeader.chunkType) {
                case ChunkType.TABLE_TYPE_SPEC:
                    long typeSpecChunkBegin = in.tell();
                    TypeSpecHeader typeSpecHeader = (TypeSpecHeader) chunkHeader;
                    long[] entryFlags = new long[(int) typeSpecHeader.entryCount];
                    for (int i = 0; i < typeSpecHeader.entryCount; i++) {
                        entryFlags[i] = in.readUInt();
                    }

                    TypeSpec typeSpec = new TypeSpec(typeSpecHeader);

                    typeSpec.entryFlags = entryFlags;
                    //id start from 1
                    typeSpec.name = resourcePackage.typeStringPool.get(typeSpecHeader.id - 1);

                    resourcePackage.addTypeSpec(typeSpec);
                    in.advanceToPos(typeSpecChunkBegin + typeSpecHeader.chunkSize -
                            typeSpecHeader.headerSize);
                    break;
                case ChunkType.TABLE_TYPE:
                    long typeChunkBegin = in.tell();
                    TypeHeader typeHeader = (TypeHeader) chunkHeader;
                    // read offsets table
                    long[] offsets = new long[(int) typeHeader.entryCount];
                    for (int i = 0; i < typeHeader.entryCount; i++) {
                        offsets[i] = in.readUInt();
                    }

                    long entryPos = typeChunkBegin + typeHeader.entriesStart - typeHeader.headerSize;
                    in.advanceToPos(entryPos);
                    // read Resource Entries
                    ResourceEntry[] resourceEntries = new ResourceEntry[offsets.length];
                    for (int i = 0; i < offsets.length; i++) {
                        if (offsets[i] != TypeHeader.NO_ENTRY) {
                            in.advanceToPos(entryPos + offsets[i]);
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
                    in.advanceToPos(typeChunkBegin + typeHeader.chunkSize - typeHeader.headerSize);
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

    private ResourceEntry readResourceEntry(StringPool keyStringPool) throws IOException {
        long beginPos = in.tell();
        ResourceEntry resourceEntry = new ResourceEntry();
        // size is always 8(simple), or 16(complex)
        resourceEntry.size = in.readUShort();
        resourceEntry.flags = in.readUShort();
        long keyRef = in.readInt();
        resourceEntry.key = keyStringPool.get((int) keyRef);

        if ((resourceEntry.flags & ResourceEntry.FLAG_COMPLEX) != 0) {
            ResourceMapEntry resourceMapEntry = new ResourceMapEntry(resourceEntry);

            // Resource identifier of the parent mapping, or 0 if there is none.
            resourceMapEntry.parent = in.readUInt();
            resourceMapEntry.count = in.readUInt();

            in.advanceToPos(beginPos + resourceEntry.size);

            //An individual complex Resource entry comprises an entry immediately followed by one or more fields.
            ResourceTableMap[] resourceTableMaps = new ResourceTableMap[(int) resourceMapEntry.count];
            for (int i = 0; i < resourceMapEntry.count; i++) {
                resourceTableMaps[i] = readResourceTableMap();
            }

            resourceMapEntry.resourceTableMaps = resourceTableMaps;
            return resourceMapEntry;
        } else {
            in.advanceToPos(beginPos + resourceEntry.size);
            resourceEntry.value = ParseUtils.readResValue(in, stringPool);
            return resourceEntry;
        }
    }

    private ResourceTableMap readResourceTableMap() throws IOException {
        //TODO: to be implemented.
        ResourceTableMap resourceTableMap = new ResourceTableMap();
        resourceTableMap.nameRef = in.readUInt();
        resourceTableMap.resValue = ParseUtils.readResValue(in, stringPool);

        if ((resourceTableMap.nameRef & 0x02000000) != 0) {
            //read arrays
            parseArrays(resourceTableMap);
        } else if ((resourceTableMap.nameRef & 0x01000000) != 0) {
            // read attrs
            parseAttrs(resourceTableMap);
        } else {

        }

        return resourceTableMap;
    }

    private void parseArrays(ResourceTableMap resourceTableMap) {

    }

    private void parseAttrs(ResourceTableMap resourceTableMap) {
        switch ((int) resourceTableMap.nameRef) {
            case ResourceTableMap.MapAttr.TYPE:
//                String name = "attr";
//                String format;
//                int i = Integer.parseInt(resourceTableMap.resValue.data);
//                switch (i) {
//                    case ResourceTableMap.AttributeType.BOOLEAN:
//                        format = "bool";
//                }
//                break;
            default:
                //resourceTableMap.data = "attr:" + resourceTableMap.nameRef;


        }
    }

    private ChunkHeader readChunkHeader() throws IOException {
        long begin = in.tell();

        int chunkType = in.readUShort();
        int headerSize = in.readUShort();
        long chunkSize = in.readUInt();

        switch (chunkType) {
            case ChunkType.TABLE:
                ResourceTableHeader resourceTableHeader = new ResourceTableHeader(chunkType,
                        headerSize, chunkSize);
                resourceTableHeader.packageCount = in.readUInt();
                in.advanceToPos(begin + headerSize);
                return resourceTableHeader;
            case ChunkType.STRING_POOL:
                StringPoolHeader stringPoolHeader = new StringPoolHeader(chunkType, headerSize,
                        chunkSize);
                stringPoolHeader.stringCount = in.readUInt();
                stringPoolHeader.styleCount = in.readUInt();
                stringPoolHeader.flags = in.readUInt();
                stringPoolHeader.stringsStart = in.readUInt();
                stringPoolHeader.stylesStart = in.readUInt();
                in.advanceToPos(begin + headerSize);
                return stringPoolHeader;
            case ChunkType.TABLE_PACKAGE:
                PackageHeader packageHeader = new PackageHeader(chunkType, headerSize, chunkSize);
                packageHeader.id = in.readUInt();
                packageHeader.name = ParseUtils.readStringUTF16(in, 128);
                packageHeader.typeStrings = in.readUInt();
                packageHeader.lastPublicType = in.readUInt();
                packageHeader.keyStrings = in.readUInt();
                packageHeader.lastPublicKey = in.readUInt();
                in.advanceToPos(begin + headerSize);
                return packageHeader;
            case ChunkType.TABLE_TYPE_SPEC:
                TypeSpecHeader typeSpecHeader = new TypeSpecHeader(chunkType, headerSize, chunkSize);
                typeSpecHeader.id = in.readUByte();
                typeSpecHeader.res0 = in.readUByte();
                typeSpecHeader.res1 = in.readUShort();
                typeSpecHeader.entryCount = in.readUInt();
                in.advanceToPos(begin + headerSize);
                return typeSpecHeader;
            case ChunkType.TABLE_TYPE:
                TypeHeader typeHeader = new TypeHeader(chunkType, headerSize, chunkSize);
                typeHeader.id = in.readUByte();
                typeHeader.res0 = in.readUByte();
                typeHeader.res1 = in.readUShort();
                typeHeader.entryCount = in.readUInt();
                typeHeader.entriesStart = in.readUInt();
                typeHeader.config = readResTableConfig();
                in.advanceToPos(begin + headerSize);
                return typeHeader;
            case ChunkType.NULL:
                //in.skip((int) (chunkSize - headerSize));
            default:
                throw new ParserException("Unexpected chunk Type:" + Integer.toHexString(chunkType));
        }
    }

    private ResTableConfig readResTableConfig() throws IOException {
        long beginPos = in.tell();
        ResTableConfig config = new ResTableConfig();
        long size = in.readUInt();
        in.skip(4);
        //read locale
        config.language = in.readChars(2);
        config.country = in.readChars(2);

        long endPos = in.tell();
        in.skip((int) (size - (endPos - beginPos)));
        return config;
    }

    public ResourceTable getResourceTable() {
        return resourceTable;
    }

    public Set<Locale> getLocales() {
        return this.locales;
    }
}
