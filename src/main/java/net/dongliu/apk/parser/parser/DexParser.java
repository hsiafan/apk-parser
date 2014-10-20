package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.bean.DexClass;
import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.io.TellableInputStream;
import net.dongliu.apk.parser.struct.ByteOrder;
import net.dongliu.apk.parser.struct.StringPool;
import net.dongliu.apk.parser.struct.dex.DexClassStruct;
import net.dongliu.apk.parser.struct.dex.DexHeader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;
import java.util.Arrays;

/**
 * parse dex file.
 * current we only get the class name.
 * see:
 * http://source.android.com/devices/tech/dalvik/dex-format.html
 * http://dexandroid.googlecode.com/svn/trunk/dalvik/libdex/DexFile.h
 *
 * @author dongliu
 */
public class DexParser {

    private TellableInputStream in;
    private ByteOrder byteOrder = ByteOrder.LITTLE;

    private static final int NO_INDEX = 0xffffffff;

    private DexClass[] dexClasses;

    public DexParser(InputStream in) {
        this.in = new TellableInputStream(in, byteOrder);
    }

    public void parse() throws IOException {
        // read magic
        String magic = in.readChars(8);
        if (!magic.startsWith("dex\n")) {
            return;
        }
        int version = Integer.parseInt(magic.substring(4, 7));
        // now the version is 035
        if (version < 35) {
            // version 009 was used for the M3 releases of the Android platform (November–December 2007),
            // and version 013 was used for the M5 releases of the Android platform (February–March 2008)
            throw new ParserException("Dex file version: " + version + " is not supported");
        }

        // read header
        DexHeader header = readDexHeader();
        header.version = version;

        // read string pool
        long[] stringOffsets = readStringPool(header.stringIdsOff, header.stringIdsSize);

        // read types
        int[] typeIds = readTypes(header.typeIdsOff, header.typeIdsSize);

        // read classes
        DexClassStruct[] dexClassStructs = readClass(header.classDefsOff, header.classDefsSize);

        StringPool stringpool = readStrings(stringOffsets);

        String[] types = new String[typeIds.length];
        for (int i = 0; i < typeIds.length; i++) {
            types[i] = stringpool.get(typeIds[i]);
        }

        dexClasses = new DexClass[dexClassStructs.length];
        for (int i = 0; i < dexClasses.length; i++) {
            dexClasses[i] = new DexClass();
        }
        for (int i = 0; i < dexClassStructs.length; i++) {
            DexClassStruct dexClassStruct = dexClassStructs[i];
            DexClass dexClass = dexClasses[i];
            dexClass.setClassType(types[dexClassStruct.classIdx]);
            if (dexClassStruct.superclassIdx != NO_INDEX) {
                dexClass.setSuperClass(types[dexClassStruct.superclassIdx]);
            }
            dexClass.setAccessFlags(dexClassStruct.accessFlags);
        }
    }

    /**
     * read class info.
     */
    private DexClassStruct[] readClass(long classDefsOff, int classDefsSize) throws IOException {
        in.advanceToPos(classDefsOff);

        DexClassStruct[] dexClassStructs = new DexClassStruct[classDefsSize];
        for (int i = 0; i < classDefsSize; i++) {
            DexClassStruct dexClassStruct = new DexClassStruct();
            dexClassStruct.classIdx = in.readInt();

            dexClassStruct.accessFlags = in.readInt();
            dexClassStruct.superclassIdx = in.readInt();

            dexClassStruct.interfacesOff = in.readUInt();
            dexClassStruct.sourceFileIdx = in.readInt();
            dexClassStruct.annotationsOff = in.readUInt();
            dexClassStruct.classDataOff = in.readUInt();
            dexClassStruct.staticValuesOff = in.readUInt();
            dexClassStructs[i] = dexClassStruct;
        }

        return dexClassStructs;
    }

    /**
     * read types.
     */
    private int[] readTypes(long typeIdsOff, int typeIdsSize) throws IOException {
        in.advanceToPos(typeIdsOff);
        int[] typeIds = new int[typeIdsSize];
        for (int i = 0; i < typeIdsSize; i++) {
            typeIds[i] = (int) in.readUInt();
        }
        return typeIds;
    }

    /**
     * read string pool for dex file.
     * dex file string pool diff a bit with binary xml file or resource table.
     *
     * @param offsets
     * @return
     * @throws IOException
     */
    private StringPool readStrings(long[] offsets) throws IOException {
        // read strings.
        // in some apk, the strings' offsets may not well ordered. we sort it first

        StringPoolEntry[] entries = new StringPoolEntry[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            entries[i] = new StringPoolEntry(i, offsets[i]);
        }
        Arrays.sort(entries);

        String lastStr = null;
        long lastOffset = -1;
        StringPool stringpool = new StringPool(offsets.length);
        for (StringPoolEntry entry : entries) {
            if (entry.getOffset() == lastOffset) {
                stringpool.set(entry.getIdx(), lastStr);
                continue;
            }
            in.advanceToPos(entry.getOffset());
            lastOffset = entry.getOffset();
            String str = readString();
            lastStr = str;
            stringpool.set(entry.getIdx(), str);
        }
        return stringpool;
    }

    /*
     * read string identifiers list.
     */
    private long[] readStringPool(long stringIdsOff, int stringIdsSize) throws IOException {
        in.advanceToPos(stringIdsOff);
        long offsets[] = new long[stringIdsSize];
        for (int i = 0; i < stringIdsSize; i++) {
            offsets[i] = in.readUInt();
        }

        return offsets;
    }

    /**
     * read dex encoding string.
     */
    private String readString() throws IOException {
        // the length is char len, not byte len
        int strLen = readVarInts();
        try {
            return readString(strLen);
        } catch (UTFDataFormatException e) {
            return "";
        }
    }

    /**
     * read Modified UTF-8 encoding str.
     *
     * @param strLen the java-utf16-char len, not strLen nor bytes len.
     */
    private String readString(int strLen) throws IOException {
        char[] chars = new char[strLen];
        for (int i = 0; i < strLen; i++) {
            short a = in.readUByte();
            if ((a & 0x80) == 0) {
                // ascii char
                chars[i] = (char) a;
            } else if ((a & 0xe0) == 0xc0) {
                // read one more
                short b = in.readUByte();
                chars[i] = (char) (((a & 0x1F) << 6) | (b & 0x3F));
            } else if ((a & 0xf0) == 0xe0) {
                short b = in.readUByte();
                short c = in.readUByte();
                chars[i] = (char) (((a & 0x0F) << 12) | ((b & 0x3F) << 6) | (c & 0x3F));
            } else if ((a & 0xf0) == 0xf0) {
                throw new UTFDataFormatException();
            } else {
                throw new UTFDataFormatException();
            }
            if (chars[i] == 0) {
                // the end of string.
            }
        }

        return new String(chars);
    }


    /**
     * read varints.
     *
     * @return
     * @throws IOException
     */
    private int readVarInts() throws IOException {
        int i = 0;
        int count = 0;
        short s;
        do {
            if (count > 4) {
                throw new ParserException("read varints error.");
            }
            s = in.readUByte();
            i |= (s & 0x7f) << (count * 7);
            count++;
        } while ((s & 0x80) != 0);

        return i;
    }

    private DexHeader readDexHeader() throws IOException {

        // check sum. skip
        in.readUInt();

        // signature skip
        in.readBytes(DexHeader.kSHA1DigestLen);

        DexHeader header = new DexHeader();
        header.fileSize = in.readUInt();
        header.headerSize = in.readUInt();

        // skip?
        in.readUInt();

        // static link data
        header.linkSize = in.readUInt();
        header.linkOff = in.readUInt();

        // the map data is just the same as dex header.
        header.mapOff = in.readUInt();

        header.stringIdsSize = in.readInt();
        header.stringIdsOff = in.readUInt();

        header.typeIdsSize = in.readInt();
        header.typeIdsOff = in.readUInt();

        header.protoIdsSize = in.readInt();
        header.protoIdsOff = in.readUInt();

        header.fieldIdsSize = in.readInt();
        header.fieldIdsOff = in.readUInt();

        header.methodIdsSize = in.readInt();
        header.methodIdsOff = in.readUInt();

        header.classDefsSize = in.readInt();
        header.classDefsOff = in.readUInt();

        header.dataSize = in.readInt();
        header.dataOff = in.readUInt();

        in.advanceToPos(header.headerSize);

        return header;
    }

    public DexClass[] getDexClasses() {
        return dexClasses;
    }

}

