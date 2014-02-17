package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.bean.Locale;
import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.io.SU;
import net.dongliu.apk.parser.io.TellableInputStream;
import net.dongliu.apk.parser.struct.*;
import net.dongliu.apk.parser.struct.resource.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
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
    private ResourceTable resourceTable;
    private ChunkHeader chunkHeader;

    private Set<Locale> locales;

    public ResourceTableParser(InputStream in) {
        this.in = new TellableInputStream(in, byteOrder);
        this.locales = new HashSet<Locale>();
    }

    /**
     * parse resource table file.
     *
     * @throws IOException
     */
    public void parse() throws IOException {
        try {
            // read resource file header.
            chunkHeader = readChunkHeader();
            SU.checkChunkType(ChunkType.TABLE, chunkHeader.chunkType);
            ResourceTableHeader resourceTableHeader = (ResourceTableHeader) chunkHeader;

            // read string pool chunk
            chunkHeader = readChunkHeader();
            SU.checkChunkType(ChunkType.STRING_POOL, chunkHeader.chunkType);
            stringPool = SU.readStringPool(in, (StringPoolHeader) chunkHeader);

            resourceTable = new ResourceTable();
            resourceTable.stringPool = stringPool;

            chunkHeader = readChunkHeader();
            for (int i = 0; i < resourceTableHeader.packageCount; i++) {
                ResourcePackage resourcePackage = readPackage((PackageHeader) chunkHeader);
                resourceTable.addPackage(resourcePackage);
            }
        } finally {
            in.close();
        }

    }

    // read one package
    private ResourcePackage readPackage(PackageHeader packageHeader) throws IOException {
        //read packageHeader
        ResourcePackage resourcePackage = new ResourcePackage(packageHeader);

        long beginPos = in.tell();
        // read type string pool
        if (packageHeader.typeStrings > 0) {
            in.advanceIfNotRearch(beginPos + packageHeader.typeStrings - packageHeader.headerSize);
            chunkHeader = readChunkHeader();
            SU.checkChunkType(ChunkType.STRING_POOL, chunkHeader.chunkType);
            resourcePackage.typeStringPool = SU.readStringPool(in, (StringPoolHeader) chunkHeader);
        }

        //read key string pool
        if (packageHeader.keyStrings > 0) {
            in.advanceIfNotRearch(beginPos + packageHeader.keyStrings - packageHeader.headerSize);
            chunkHeader = readChunkHeader();
            SU.checkChunkType(ChunkType.STRING_POOL, chunkHeader.chunkType);
            resourcePackage.keyStringPool = SU.readStringPool(in, (StringPoolHeader) chunkHeader);
        }


        boolean flag = true;
        do {
            try {
                chunkHeader = readChunkHeader();
            } catch (IOException e) {
                //TODO: better way to detect eof
                break;
            }
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
                    in.advanceIfNotRearch(typeSpecChunkBegin + typeSpecHeader.chunkSize -
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
                    in.advanceIfNotRearch(entryPos);
                    // read Resource Entries
                    ResourceEntry[] resourceEntries = new ResourceEntry[offsets.length];
                    for (int i = 0; i < offsets.length; i++) {
                        if (offsets[i] != TypeHeader.NO_ENTRY) {
                            in.advanceIfNotRearch(entryPos + offsets[i]);
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
                    in.advanceIfNotRearch(typeChunkBegin + typeHeader.chunkSize - typeHeader.headerSize);
                    break;
                case ChunkType.TABLE_PACKAGE:
                    flag = false;
                    break;
                default:
                    throw new ParserException("unexpected chunk type:" + chunkHeader.chunkType);
            }
        } while (flag);

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

            in.advanceIfNotRearch(beginPos + resourceEntry.size);

            //An individual complex Resource entry comprises an entry immediately followed by one or more fields.
            ResourceTableMap[] resourceTableMaps = new ResourceTableMap[(int) resourceMapEntry.count];
            for (int i = 0; i < resourceMapEntry.count; i++) {
                resourceTableMaps[i] = readResourceTableMap();
            }

            resourceMapEntry.resourceTableMaps = resourceTableMaps;
            return resourceMapEntry;
        } else {
            in.advanceIfNotRearch(beginPos + resourceEntry.size);
            resourceEntry.value = SU.readResValue(in, stringPool, null, null);
            return resourceEntry;
        }
    }

    private ResourceTableMap readResourceTableMap() throws IOException {
        //TODO: to be implemented.
        ResourceTableMap resourceTableMap = new ResourceTableMap();
        resourceTableMap.nameRef = in.readUInt();
        resourceTableMap.resValue = SU.readResValue(in, stringPool, null, null);

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
        int chunkType = in.readUShort();

        int headSize = in.readUShort();
        long chunkSize = in.readUInt();

        switch (chunkType) {
            case ChunkType.TABLE:
                ResourceTableHeader resourceTableHeader = new ResourceTableHeader(chunkType,
                        headSize, chunkSize);
                resourceTableHeader.packageCount = in.readUInt();
                return resourceTableHeader;
            case ChunkType.STRING_POOL:
                StringPoolHeader stringPoolHeader = new StringPoolHeader(chunkType, headSize,
                        chunkSize);
                stringPoolHeader.stringCount = in.readUInt();
                stringPoolHeader.styleCount = in.readUInt();
                stringPoolHeader.flags = in.readUInt();
                stringPoolHeader.stringsStart = in.readUInt();
                stringPoolHeader.stylesStart = in.readUInt();
                return stringPoolHeader;
            case ChunkType.TABLE_PACKAGE:
                PackageHeader packageHeader = new PackageHeader(chunkType, headSize, chunkSize);
                packageHeader.id = in.readUInt();
                packageHeader.name = SU.readStringUTF16(in, 128);
                packageHeader.typeStrings = in.readUInt();
                packageHeader.lastPublicType = in.readUInt();
                packageHeader.keyStrings = in.readUInt();
                packageHeader.lastPublicKey = in.readUInt();
                return packageHeader;
            case ChunkType.TABLE_TYPE_SPEC:
                TypeSpecHeader typeSpecHeader = new TypeSpecHeader(chunkType, headSize, chunkSize);
                typeSpecHeader.id = in.readUByte();
                typeSpecHeader.res0 = in.readUByte();
                typeSpecHeader.res1 = in.readUShort();
                typeSpecHeader.entryCount = in.readUInt();
                return typeSpecHeader;
            case ChunkType.TABLE_TYPE:
                TypeHeader typeHeader = new TypeHeader(chunkType, headSize, chunkSize);
                typeHeader.id = in.readUByte();
                typeHeader.res0 = in.readUByte();
                typeHeader.res1 = in.readUShort();
                typeHeader.entryCount = in.readUInt();
                typeHeader.entriesStart = in.readUInt();
                typeHeader.config = readResTableConfig();
                return typeHeader;
            case ChunkType.NULL:
                in.skip((int) (chunkSize - headSize));
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
