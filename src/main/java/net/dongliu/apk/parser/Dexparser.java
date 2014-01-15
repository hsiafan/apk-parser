package net.dongliu.apk.parser;

import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.io.TellableInputStream;
import net.dongliu.apk.parser.struct.ByteOrder;
import net.dongliu.apk.parser.struct.dex.DexClass;
import net.dongliu.apk.parser.struct.dex.DexHeader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;

/**
 * parse dex file.
 * current we only get the class name.
 * see http://dexandroid.googlecode.com/svn/trunk/dalvik/libdex/DexFile.h
 *
 * @author dongliu
 */
public class Dexparser {

    private TellableInputStream in;
    private ByteOrder byteOrder = ByteOrder.LITTLE;
    private DexHeader header;

    private DexClass[] dexClasses;

    public Dexparser(InputStream in) {
        this.in = new TellableInputStream(in, byteOrder);
    }

    public void parse() throws IOException {
        String magic = in.readChars(8);
        if (!magic.startsWith("dex\n")) {
            return;
        }
        String version = magic.substring(4, 7);
        header = new DexHeader();
        header.version = Integer.parseInt(version);

        if (header.version < 35) {
            //TODO: deal with old version
        }

        readDexHeader();

        // read string pool
        long[] stringOffsets = readStringPool();

        // read types
        int[] typeIds = readTypes();

        // read classes
        dexClasses = readClass();

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
    private DexClass[] readClass() throws IOException {
        in.advanceIfNotRearch(header.classDefsOff);

        DexClass[] dexClasses = new DexClass[(int) header.classDefsSize];
        for (int i = 0; i < header.classDefsSize; i++) {
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
    private int[] readTypes() throws IOException {
        in.advanceIfNotRearch(header.typeIdsOff);
        int[] typeIds = new int[header.typeIdsSize];
        for (int i = 0; i < header.typeIdsSize; i++) {
            typeIds[i] = (int) in.readUInt();
        }
        return typeIds;
    }

    private String[] readStrings(long[] offsets) throws IOException {
        // read strings.
        String[] stringpool = new String[header.stringIdsSize];
        for (int i = 0; i < offsets.length; i++) {
            in.advanceIfNotRearch(offsets[i]);
            stringpool[i] = readString();
        }
        return stringpool;
    }

    /*
     * read string identifiers list.
     */
    private long[] readStringPool() throws IOException {
        in.advanceIfNotRearch(header.stringIdsOff);
        long offsets[] = new long[header.stringIdsSize];
        for (int i = 0; i < header.stringIdsSize; i++) {
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
