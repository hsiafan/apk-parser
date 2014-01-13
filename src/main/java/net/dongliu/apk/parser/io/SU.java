package net.dongliu.apk.parser.io;

import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.struct.ResValue;
import net.dongliu.apk.parser.struct.StringEncoding;
import net.dongliu.apk.parser.struct.StringPool;
import net.dongliu.apk.parser.struct.StringPoolHeader;
import net.dongliu.apk.parser.struct.resource.*;

import java.io.IOException;
import java.util.List;

/**
 * @author dongliu
 */
public class SU {


    /**
     * read string from input stream. if get EOF before read enough data, throw IOException.
     */
    public static String readString(TellableInputStream in, StringEncoding encoding)
            throws IOException {
        if (encoding == StringEncoding.UTF8) {
            //  The lengths are encoded in the same way as for the 16-bit format
            // but using 8-bit rather than 16-bit integers.
            int strLen = readLen(in);
            int bytesLen = readLen(in);
            byte[] bytes = in.readBytes(bytesLen);
            String str = new String(bytes, "UTF-8");
            // zero
            int trailling = in.readUByte();
            return str;

        } else {
            // The length is encoded as either one or two 16-bit integers as per the commentRef...
            int strLen = readLen16(in);
            String str = in.readStringUTF16(strLen);
            // zero
            int trailling = in.readUShort();
            return str;
        }
    }

    /**
     * read utf-16 encoding str, use zero char to end str.
     *
     * @param in
     * @param strLen
     * @return
     * @throws IOException
     */
    public static String readStringUTF16(TellableInputStream in, int strLen) throws IOException {
        String str = in.readStringUTF16(strLen);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == 0) {
                return str.substring(0, i);
            }
        }
        return str;
    }

    /**
     * read encoding len.
     * see Stringpool.cpp ENCODE_LENGTH
     *
     * @param in
     * @return
     * @throws IOException
     */
    private static int readLen(TellableInputStream in) throws IOException {
        int len = 0;
        int i = in.read();
        if ((i & 0x80) != 0) {
            //read one more byte.
            len |= (i & 0x7f) << 7;
            len += in.read();
        } else {
            len = i;
        }
        return len;
    }

    /**
     * read encoding len.
     * see Stringpool.cpp ENCODE_LENGTH
     *
     * @param in
     * @return
     * @throws IOException
     */
    private static int readLen16(TellableInputStream in) throws IOException {
        int len = 0;
        int i = in.readUShort();
        if ((i & 0x8000) != 0) {
            len |= (i & 0x7fff) << 15;
            len += in.readUByte();
        } else {
            len = i;
        }
        return len;
    }


    /**
     * read String pool
     *
     * @param in
     * @param stringPoolHeader
     * @return
     * @throws IOException
     */
    public static StringPool readStringPool(TellableInputStream in,
                                            StringPoolHeader stringPoolHeader) throws IOException {

        long beginPos = in.tell();
        long[] offsets = new long[(int) stringPoolHeader.stringCount];
        // read strings offset
        if (stringPoolHeader.stringCount > 0) {
            for (int idx = 0; idx < stringPoolHeader.stringCount; idx++) {
                offsets[idx] = in.readUInt();
            }
        }
        // read flag
        boolean sorted = (stringPoolHeader.flags & StringPoolHeader.SORTED_FLAG) != 0;
        StringEncoding stringEncoding = (stringPoolHeader.flags & StringPoolHeader.UTF8_FLAG) != 0 ?
                StringEncoding.UTF8 : StringEncoding.UTF16;

        // read strings. the head and metas have 28 bytes
        long stringPos = beginPos + stringPoolHeader.stringsStart - stringPoolHeader.headerSize;
        in.advanceIfNotRearch(stringPos);
        StringPool stringPool = new StringPool((int) stringPoolHeader.stringCount);
        for (int idx = 0; idx < offsets.length; idx++) {
            in.advanceIfNotRearch(stringPos + offsets[idx]);
            String str = SU.readString(in, stringEncoding);
            stringPool.set(idx, str);
        }

        // read styles
        if (stringPoolHeader.styleCount > 0) {
            // now we just skip it
        }

        in.advanceIfNotRearch(beginPos + stringPoolHeader.chunkSize - stringPoolHeader.headerSize);

        return stringPool;
    }

    /**
     * read bytes as c++ chars.
     *
     * @param in
     * @param len
     * @return
     */
    public static String readChars(TellableInputStream in, int len) throws IOException {
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = (char) in.readUByte();
        }
        return new String(chars);
    }

    /**
     * method to read resource value RGB/ARGB type.
     *
     * @return
     */
    public static String readRGBs(TellableInputStream in, int strLen)
            throws IOException {
        long l = in.readUInt();
        StringBuilder sb = new StringBuilder();
        for (int i = strLen / 2 - 1; i >= 0; i--) {
            sb.append(Integer.toHexString((int) ((l >> i * 8) & 0xff)));
        }
        return sb.toString();
    }

    /**
     * read res value, convert from different types to string.
     *
     * @param in
     * @param stringPool
     * @return
     * @throws IOException
     */
    public static ResValue readResValue(TellableInputStream in, StringPool stringPool,
                                        ResourceTable resourceTable, String local)
            throws IOException {
        ResValue resValue = new ResValue();
        resValue.size = in.readUShort();
        resValue.res0 = in.readUByte();
        resValue.dataType = in.readUByte();

        switch (resValue.dataType) {
            case ResValue.ResType.INT_DEC:
                resValue.data = String.valueOf(in.readUInt());
                break;
            case ResValue.ResType.STRING:
                int strRef = in.readInt();
                if (strRef > 0) {
                    resValue.data = stringPool.get(strRef);
                }
                break;
            case ResValue.ResType.REFERENCE:
                long resourceId = in.readUInt();
                resValue.data = getResourceByid(resourceId, resourceTable, local);
                break;
            case ResValue.ResType.INT_BOOLEAN:
                resValue.data = String.valueOf(in.readInt() != 0);
                break;
            case ResValue.ResType.INT_HEX:
                resValue.data = "0x" + Long.toHexString(in.readUInt());
                break;
            case ResValue.ResType.NULL:
                resValue.data = "";
                break;
            case ResValue.ResType.INT_COLOR_RGB8:
            case ResValue.ResType.INT_COLOR_RGB4:
                resValue.data = readRGBs(in, 6);
                break;
            case ResValue.ResType.INT_COLOR_ARGB8:
            case ResValue.ResType.INT_COLOR_ARGB4:
                resValue.data = readRGBs(in, 8);
                break;
            case ResValue.ResType.DIMENSION:
                resValue.data = getDemension(in);
                break;
            case ResValue.ResType.FRACTION:
                resValue.data = getFraction(in);
                break;
            default:
                resValue.data = "{" + resValue.dataType + ":" + in.readUInt() + "}";
        }
        return resValue;
    }

    private static String getDemension(TellableInputStream in) throws IOException {
        long l = in.readUInt();
        short unit = (short) (l & 0xff);
        String unitStr;
        switch (unit) {
            case ResValue.ResDataCOMPLEX.UNIT_MM:
                unitStr = "mm";
                break;
            case ResValue.ResDataCOMPLEX.UNIT_PX:
                unitStr = "px";
                break;
            case ResValue.ResDataCOMPLEX.UNIT_DIP:
                unitStr = "dp";
                break;
            case ResValue.ResDataCOMPLEX.UNIT_SP:
                unitStr = "sp";
                break;
            case ResValue.ResDataCOMPLEX.UNIT_PT:
                unitStr = "pt";
                break;
            case ResValue.ResDataCOMPLEX.UNIT_IN:
                unitStr = "in";
                break;
            default:
                unitStr = "unknow unit:0x" + Integer.toHexString(unit);
        }
        return (l >> 8) + unitStr;
    }

    private static String getFraction(TellableInputStream in) throws IOException {
        long l = in.readUInt();
        // The low-order 4 bits of the data value specify the type of the fraction
        short type = (short) (l & 0xf);
        String pstr;
        switch (type) {
            case ResValue.ResDataCOMPLEX.UNIT_FRACTION:
                pstr = "%";
                break;
            case ResValue.ResDataCOMPLEX.UNIT_FRACTION_PARENT:
                pstr = "%p";
                break;
            default:
                pstr = "unknow type:0x" + Integer.toHexString(type);
        }
        float value = Float.intBitsToFloat((int) (l >> 4));
        return value + pstr;
    }

    public static void checkChunkType(int expected, int real) {
        if (expected != real) {
            throw new ParserException("Excepct chunk type:" + Integer.toHexString(expected)
                    + ", but got:" + Integer.toHexString(real));
        }
    }

    public static String getResourceByid(long resourceId, ResourceTable resourceTable,
                                         String local) {
//        An Android Resource id is a 32-bit integer. It comprises
//        an 8-bit Package id [bits 24-31]
//        an 8-bit Type id [bits 16-23]
//        a 16-bit Entry index [bits 0-15]
        String str = "invalid resource:0x" + Long.toHexString(resourceId);
        if (resourceTable == null) {
            return str;
        }

        short packageId = (short) (resourceId >> 24 & 0xff);
        short typeId = (short) ((resourceId >> 16) & 0xff);
        int entryIndex = (int) (resourceId & 0xffff);
        ResourcePackage resourcePackage = resourceTable.getPackage(packageId);
        if (resourcePackage == null) {
            return str;
        }
        TypeSpec typeSpec = resourcePackage.getTypeSpec(typeId);
        List<Type> types = resourcePackage.getTypes(typeId);
        if (typeSpec == null || types == null) {
            return str;
        }
        if (!typeSpec.exists(entryIndex)) {
            return str;
        }

        // read from type resource
        String result = null;
        String wideResult = null;
        String ref = null;
        String wideRef = null;

        for (Type type : types) {
            String tlocal = type.header.config.country + "_" + type.header.config.language;
            ResourceEntry resource = type.getResourceEntry(entryIndex);
            if (resource == null) {
                continue;
            }
            if (local != null && local.equals(tlocal)) {
                ref = resource.key;
                result = resource.toString();
                break;
            } else if (wideResult == null) {
                wideRef = resource.key;
                wideResult = resource.toString();
                if (local == null) {
                    break;
                }
            }
        }
        if (result == null) {
            result = wideResult;
        }
        if (result == null) {
            if (ref != null) {
                result = "@" + typeSpec.name + "/" + ref;
            } else {
                result = "@" + typeSpec.name + "/" + wideRef;
            }
        }
        return result;
    }

}
