package net.dongliu.apk.parser.struct.dex;

/**
 * dex file header.
 * see http://dexandroid.googlecode.com/svn/trunk/dalvik/libdex/DexFile.h
 *
 * @author dongliu
 */
public class DexHeader {

    public static final int kSHA1DigestLen = 20;
    public static final int kSHA1DigestOutputLen = kSHA1DigestLen * 2 + 1;

    // includes version number. 8 bytes.
    //public short magic;
    public int version;

    // adler32 checksum. u4
    //public long checksum;

    // SHA-1 hash len = kSHA1DigestLen
    public byte signature[];

    // length of entire file. u4
    public long fileSize;

    // len of header.offset to start of next section. u4
    public long headerSize;

    // u4
    //public long endianTag;

    // u4
    public long linkSize;

    // u4
    public long linkOff;

    // u4
    public long mapOff;

    // u4
    public int stringIdsSize;

    // u4
    public long stringIdsOff;

    // u4
    public int typeIdsSize;

    // u4
    public long typeIdsOff;

    // u4
    public int protoIdsSize;

    // u4
    public long protoIdsOff;

    // u4
    public int fieldIdsSize;

    // u4
    public long fieldIdsOff;

    // u4
    public int methodIdsSize;

    // u4
    public long methodIdsOff;

    // u4
    public int classDefsSize;

    // u4
    public long classDefsOff;

    // u4
    public int dataSize;

    // u4
    public long dataOff;
}
