package net.dongliu.apk.parser.utils;

import net.dongliu.apk.parser.bean.Locales;
import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.io.TellableInputStream;
import net.dongliu.apk.parser.parser.StringPoolEntry;
import net.dongliu.apk.parser.struct.*;
import net.dongliu.apk.parser.struct.resource.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author dongliu
 */
public class ParseUtils {


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
            len += in.readUShort();
        } else {
            len = i;
        }
        return len;
    }


    /**
     * read String pool, for apk binary xml file and resource table.
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
        in.advanceToPos(stringPos);

        StringPoolEntry[] entries = new StringPoolEntry[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            entries[i] = new StringPoolEntry(i, stringPos + offsets[i]);
        }
        Arrays.sort(entries);

        String lastStr = null;
        long lastOffset = -1;
        StringPool stringPool = new StringPool((int) stringPoolHeader.stringCount);
        for (StringPoolEntry entry : entries) {
            if (entry.getOffset() == lastOffset) {
                stringPool.set(entry.getIdx(), lastStr);
                continue;
            }

            in.advanceToPos(entry.getOffset());
            lastOffset = entry.getOffset();
            String str = ParseUtils.readString(in, stringEncoding);
            lastStr = str;
            stringPool.set(entry.getIdx(), str);
        }

        // read styles
        if (stringPoolHeader.styleCount > 0) {
            // now we just skip it
        }

        in.advanceToPos(beginPos + stringPoolHeader.chunkSize - stringPoolHeader.headerSize);

        return stringPool;
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
    public static ResourceEntity readResValue(TellableInputStream in, StringPool stringPool,
                                              boolean isStyle) throws IOException {
        ResValue resValue = new ResValue();
        resValue.size = in.readUShort();
        resValue.res0 = in.readUByte();
        resValue.dataType = in.readUByte();

        switch (resValue.dataType) {
            case ResValue.ResType.INT_DEC:
            case ResValue.ResType.INT_HEX:
                resValue.data = new ResourceEntity(in.readInt());
                break;
            case ResValue.ResType.STRING:
                int strRef = in.readInt();
                if (strRef > 0) {
                    resValue.data = new ResourceEntity(stringPool.get(strRef));
                }
                break;
            case ResValue.ResType.REFERENCE:
                long resourceId = in.readUInt();
                resValue.data = new ResourceEntity(resourceId, isStyle);
                break;
            case ResValue.ResType.INT_BOOLEAN:
                resValue.data = new ResourceEntity(in.readInt() != 0);
                break;
            case ResValue.ResType.NULL:
                resValue.data = new ResourceEntity("");
                break;
            case ResValue.ResType.INT_COLOR_RGB8:
            case ResValue.ResType.INT_COLOR_RGB4:
                resValue.data = new ResourceEntity(readRGBs(in, 6));
                break;
            case ResValue.ResType.INT_COLOR_ARGB8:
            case ResValue.ResType.INT_COLOR_ARGB4:
                resValue.data = new ResourceEntity(readRGBs(in, 8));
                break;
            case ResValue.ResType.DIMENSION:
                resValue.data = new ResourceEntity(getDemension(in));
                break;
            case ResValue.ResType.FRACTION:
                resValue.data = new ResourceEntity(getFraction(in));
                break;
            default:
                resValue.data = new ResourceEntity("{" + resValue.dataType + ":" + in.readUInt()
                        + "}");
        }
        return resValue.data;
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

    /**
     * get resource value by string-format via resourceId.
     *
     * @param resourceId
     * @param resourceTable
     * @param locale
     * @return
     */
    public static String getResourceByid(long resourceId, boolean isStyle,
                                         ResourceTable resourceTable, Locale locale) {
//        An Android Resource id is a 32-bit integer. It comprises
//        an 8-bit Package id [bits 24-31]
//        an 8-bit Type id [bits 16-23]
//        a 16-bit Entry index [bits 0-15]

        // android system styles.
        if (isStyle && (resourceId & AndroidConstants.STYLE_ID_START) == AndroidConstants.STYLE_ID_START) {
            return "@android:style/" + ResourceTable.styleMap.get((int) resourceId);
        }

        String str = "resourceId:0x" + Long.toHexString(resourceId);
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
        String ref = null;
        int currentLevel = -1;
        for (Type type : types) {
            ResourceEntry resource = type.getResourceEntry(entryIndex);
            if (resource == null) {
                continue;
            }
            ref = resource.key;
            int level = Locales.match(locale, type.locale);
            if (level == 2) {
                result = resource.toStringValue(resourceTable, locale);
                break;
            } else if (level > currentLevel) {
                result = resource.toStringValue(resourceTable, locale);
            }
        }
        if (locale == null || result == null) {
            result = "@" + typeSpec.name + "/" + ref;
        }
        return result;
    }

    /**
     * read res value. for resource table parser
     *
     * @param in
     * @param stringPool
     * @return
     */
    public static ResourceEntity readResValue(TellableInputStream in, StringPool stringPool)
            throws IOException {
        return readResValue(in, stringPool, false);
    }
}
