package net.dongliu.apk.parser.utils;

import net.dongliu.apk.parser.bean.Locales;
import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.parser.StringPoolEntry;
import net.dongliu.apk.parser.struct.*;
import net.dongliu.apk.parser.struct.resource.*;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;

/**
 * @author dongliu
 */
public class ParseUtils {

    public static Charset charsetUTF8 = Charset.forName("UTF-8");

    /**
     * read string from input buffer. if get EOF before read enough data, throw IOException.
     */
    public static String readString(ByteBuffer buffer, boolean utf8) {
        if (utf8) {
            //  The lengths are encoded in the same way as for the 16-bit format
            // but using 8-bit rather than 16-bit integers.
            int strLen = readLen(buffer);
            int bytesLen = readLen(buffer);
            byte[] bytes = Buffers.readBytes(buffer, bytesLen);
            String str = new String(bytes, charsetUTF8);
            // zero
            int trailling = Buffers.readUByte(buffer);
            return str;
        } else {
            // The length is encoded as either one or two 16-bit integers as per the commentRef...
            int strLen = readLen16(buffer);
            String str = Buffers.readString(buffer, strLen);
            // zero
            int trailling = Buffers.readUShort(buffer);
            return str;
        }
    }

    /**
     * read utf-16 encoding str, use zero char to end str.
     */
    public static String readStringUTF16(ByteBuffer buffer, int strLen) {
        String str = Buffers.readString(buffer, strLen);
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
     * see StringPool.cpp ENCODE_LENGTH
     */
    private static int readLen(ByteBuffer buffer) {
        int len = 0;
        int i = Buffers.readUByte(buffer);
        if ((i & 0x80) != 0) {
            //read one more byte.
            len |= (i & 0x7f) << 7;
            len += Buffers.readUByte(buffer);
        } else {
            len = i;
        }
        return len;
    }

    /**
     * read encoding len.
     * see Stringpool.cpp ENCODE_LENGTH
     */
    private static int readLen16(ByteBuffer buffer) {
        int len = 0;
        int i = Buffers.readUShort(buffer);
        if ((i & 0x8000) != 0) {
            len |= (i & 0x7fff) << 15;
            len += Buffers.readUShort(buffer);
        } else {
            len = i;
        }
        return len;
    }


    /**
     * read String pool, for apk binary xml file and resource table.
     */
    public static StringPool readStringPool(ByteBuffer buffer, StringPoolHeader stringPoolHeader) {

        long beginPos = buffer.position();
        long[] offsets = new long[(int) stringPoolHeader.getStringCount()];
        // read strings offset
        if (stringPoolHeader.getStringCount() > 0) {
            for (int idx = 0; idx < stringPoolHeader.getStringCount(); idx++) {
                offsets[idx] = Buffers.readUInt(buffer);
            }
        }
        // read flag
        // the string index is sorted by the string values if true
        boolean sorted = (stringPoolHeader.getFlags() & StringPoolHeader.SORTED_FLAG) != 0;
        // string use utf-8 format if true, otherwise utf-16
        boolean utf8 = (stringPoolHeader.getFlags() & StringPoolHeader.UTF8_FLAG) != 0;

        // read strings. the head and metas have 28 bytes
        long stringPos = beginPos + stringPoolHeader.getStringsStart() - stringPoolHeader.getHeaderSize();
        buffer.position((int) stringPos);

        StringPoolEntry[] entries = new StringPoolEntry[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            entries[i] = new StringPoolEntry(i, stringPos + offsets[i]);
        }

        String lastStr = null;
        long lastOffset = -1;
        StringPool stringPool = new StringPool((int) stringPoolHeader.getStringCount());
        for (StringPoolEntry entry : entries) {
            if (entry.getOffset() == lastOffset) {
                stringPool.set(entry.getIdx(), lastStr);
                continue;
            }

            buffer.position((int) entry.getOffset());
            lastOffset = entry.getOffset();
            String str = ParseUtils.readString(buffer, utf8);
            lastStr = str;
            stringPool.set(entry.getIdx(), str);
        }

        // read styles
        if (stringPoolHeader.getStyleCount() > 0) {
            // now we just skip it
        }

        buffer.position((int) (beginPos + stringPoolHeader.getBodySize()));

        return stringPool;
    }

    /**
     * read res value, convert from different types to string.
     */
    @Nullable
    public static ResourceValue readResValue(ByteBuffer buffer, StringPool stringPool) {
//        ResValue resValue = new ResValue();
        int size = Buffers.readUShort(buffer);
        short res0 = Buffers.readUByte(buffer);
        short dataType = Buffers.readUByte(buffer);

        switch (dataType) {
            case ResValue.ResType.INT_DEC:
                return ResourceValue.decimal(buffer.getInt());
            case ResValue.ResType.INT_HEX:
                return ResourceValue.hexadecimal(buffer.getInt());
            case ResValue.ResType.STRING:
                int strRef = buffer.getInt();
                if (strRef >= 0) {
                    return ResourceValue.string(strRef, stringPool);
                } else {
                    return null;
                }
            case ResValue.ResType.REFERENCE:
                return ResourceValue.reference(buffer.getInt());
            case ResValue.ResType.INT_BOOLEAN:
                return ResourceValue.bool(buffer.getInt());
            case ResValue.ResType.NULL:
                return ResourceValue.nullValue();
            case ResValue.ResType.INT_COLOR_RGB8:
            case ResValue.ResType.INT_COLOR_RGB4:
                return ResourceValue.rgb(buffer.getInt(), 6);
            case ResValue.ResType.INT_COLOR_ARGB8:
            case ResValue.ResType.INT_COLOR_ARGB4:
                return ResourceValue.rgb(buffer.getInt(), 8);
            case ResValue.ResType.DIMENSION:
                return ResourceValue.dimension(buffer.getInt());
            case ResValue.ResType.FRACTION:
                return ResourceValue.fraction(buffer.getInt());
            default:
                return ResourceValue.raw(buffer.getInt(), dataType);
        }
    }

    public static void checkChunkType(int expected, int real) {
        if (expected != real) {
            throw new ParserException("Expect chunk type:" + Integer.toHexString(expected)
                    + ", but got:" + Integer.toHexString(real));
        }
    }

    /**
     * get resource value by string-format via resourceId.
     */
    public static String getResourceById(long resourceId, ResourceTable resourceTable, Locale locale) {
//        An Android Resource id is a 32-bit integer. It comprises
//        an 8-bit Package id [bits 24-31]
//        an 8-bit Type id [bits 16-23]
//        a 16-bit Entry index [bits 0-15]

        // android system styles.
        if (resourceId > AndroidConstants.SYS_STYLE_ID_START && resourceId < AndroidConstants.SYS_STYLE_ID_END) {
            return "@android:style/" + ResourceTable.sysStyle.get((int) resourceId);
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
        ResourceEntry resource = null;
        String ref = null;
        int currentLevel = -1;
        for (Type type : types) {
            ResourceEntry curResourceEntry = type.getResourceEntry(entryIndex);
            if (curResourceEntry == null) {
                continue;
            }
            ref = curResourceEntry.getKey();

            ResourceValue currentResourceValue = curResourceEntry.getValue();
            if (currentResourceValue == null) {
                continue;
            }

            // cyclic reference detect
            if (currentResourceValue instanceof ResourceValue.ReferenceResourceValue) {
                if (resourceId == ((ResourceValue.ReferenceResourceValue) currentResourceValue)
                        .getReferenceResourceId()) {
                    continue;
                }
            }

            int level = Locales.match(locale, type.getLocale());
            if (level == 2) {
                resource = curResourceEntry;
                break;
            } else if (level > currentLevel) {
                resource = curResourceEntry;
                currentLevel = level;
            }
        }
        String result;
        if (locale == null || resource == null) {
            result = "@" + typeSpec.getName() + "/" + ref;
        } else {
            result = resource.toStringValue(resourceTable, locale);
        }
        return result;
    }

}
