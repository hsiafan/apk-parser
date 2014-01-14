package net.dongliu.apk.parser;

import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.io.SU;
import net.dongliu.apk.parser.io.TellableInputStream;
import net.dongliu.apk.parser.struct.ByteOrder;
import net.dongliu.apk.parser.struct.dex.DexClass;
import net.dongliu.apk.parser.struct.dex.DexHeader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
            return;
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
            types[i] = stringpool[typeIds[i] - 1];
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
        return readString(strLen);
    }

    /**
     * read Modified UTF-8 encoding str.
     * @param strLen the java-utf16-char len, not strLen nor bytes len.
     */
    private String readString(int strLen) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < strLen; i++) {
            short s = in.readUByte();
            baos.write(s);
            if ((s & 0x80) == 0) {
                // ascii char
            } else if ((s | 0xc0) == 0xc0) {
                // read one more
                baos.write(in.readUByte());
            } else if ((s | 0xe0) == 0xe0) {
                baos.write(in.readBytes(2));
            } else if ((s | 0xf0) == 0xf0) {
                baos.write(in.readBytes(3));
            } else {
                // should not happen
            }
        }

        //TODO:should be m-utf-8
        String str = baos.toString("UTF-8");
        baos.close();
        return str;
    }


    /**
     * read varints.
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
        } while((s & 0x80) != 0);

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
