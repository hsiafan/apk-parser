package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.io.TellableInputStream;
import net.dongliu.apk.parser.struct.ByteOrder;
import net.dongliu.apk.parser.struct.dex.DexClass;
import net.dongliu.apk.parser.struct.dex.DexHeader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * parse dex file.
 * current we only get the class name.
 * see http://dexandroid.googlecode.com/svn/trunk/dalvik/libdex/DexFile.h
 *
 * @author dongliu
 */
public class DexParser {

    private TellableInputStream in;
    private ByteOrder byteOrder = ByteOrder.LITTLE;

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
        if (version < 35) {
            //TODO: deal with old version
        }

        // read header
        DexHeader header = readDexHeader();
        header.version = version;

        // read string pool
        long[] stringOffsets = readStringPool(header.stringIdsOff, header.stringIdsSize);

        // read types
        int[] typeIds = readTypes(header.typeIdsOff, header.typeIdsSize);

        // read classes
        dexClasses = readClass(header.classDefsOff, (int) header.classDefsSize);

        String[] stringpool = readStrings(stringOffsets);

        String[] types = new String[typeIds.length];
        for (int i = 0; i < typeIds.length; i++) {
            types[i] = stringpool[typeIds[i]];
        }

        for (DexClass dexClass : dexClasses) {
            dexClass.classType = types[dexClass.classIdx];
        }
    }

    /**
     * read class info.
     */
    private DexClass[] readClass(long classDefsOff, int classDefsSize) throws IOException {
        in.advanceIfNotRearch(classDefsOff);

        DexClass[] dexClasses = new DexClass[classDefsSize];
        for (int i = 0; i < classDefsSize; i++) {
            DexClass dexClass = new DexClass();
            dexClass.classIdx = (int) in.readUInt();
            //now we just skip the other fields.
            in.skip(28);
            dexClasses[i] = dexClass;
        }

        return dexClasses;
    }

    /**
     * read types.
     */
    private int[] readTypes(long typeIdsOff, int typeIdsSize) throws IOException {
        in.advanceIfNotRearch(typeIdsOff);
        int[] typeIds = new int[typeIdsSize];
        for (int i = 0; i < typeIdsSize; i++) {
            typeIds[i] = (int) in.readUInt();
        }
        return typeIds;
    }

    private String[] readStrings(long[] offsets) throws IOException {
        // read strings.
        // in some apk, the strings' offsets may not well ordered. we sort it first

        StringPoolEntry[] entries = new StringPoolEntry[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            entries[i] = new StringPoolEntry(i, offsets[i]);
        }
        Arrays.sort(entries, new Comparator<StringPoolEntry>() {
            @Override
            public int compare(StringPoolEntry o1, StringPoolEntry o2) {
                return (int) (o1.getOffset() - o2.getOffset());
            }
        });

        String[] stringpool = new String[offsets.length];
        for (StringPoolEntry entry : entries) {
            in.advanceIfNotRearch(entry.getOffset());
            stringpool[entry.getIdx()] = readString();
        }
        return stringpool;
    }

    /*
     * read string identifiers list.
     */
    private long[] readStringPool(long stringIdsOff, int stringIdsSize) throws IOException {
        in.advanceIfNotRearch(stringIdsOff);
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

        //TODO:should be m-utf-8
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

        header.stringIdsSize = (int) in.readUInt();
        header.stringIdsOff = in.readUInt();

        header.typeIdsSize = (int) in.readUInt();
        header.typeIdsOff = in.readUInt();

        header.protoIdsSize = in.readUInt();
        header.protoIdsOff = in.readUInt();

        header.fieldIdsSize = in.readUInt();
        header.fieldIdsOff = in.readUInt();

        header.methodIdsSize = in.readUInt();
        header.methodIdsOff = in.readUInt();

        header.classDefsSize = in.readUInt();
        header.classDefsOff = in.readUInt();

        header.dataSize = in.readUInt();
        header.dataOff = in.readUInt();

        in.advanceIfNotRearch(header.headerSize);

        return header;
    }

    public DexClass[] getDexClasses() {
        return dexClasses;
    }

}

class StringPoolEntry {
    private int idx;
    private long offset;

    StringPoolEntry(int idx, long offset) {
        this.idx = idx;
        this.offset = offset;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
}