package net.dongliu.apk.parser.struct;

/**
 * String pool chunk header.
 *
 * @author dongliu
 */
public class StringPoolHeader extends ChunkHeader {
    public StringPoolHeader(int chunkType, int headerSize, long chunkSize) {
        super(chunkType, headerSize, chunkSize);
    }

    // Number of style span arrays in the pool (number of uint32_t indices
    // follow the string indices).
    public long stringCount;
    // Number of style span arrays in the pool (number of uint32_t indices
    // follow the string indices).
    public long styleCount;

    // If set, the string index is sorted by the string values (based on strcmp16()).
    public static final int SORTED_FLAG = 1;
    // String pool is encoded in UTF-8
    public static final int UTF8_FLAG = 1 << 8;
    public long flags;

    // Index from header of the string data.
    public long stringsStart;
    // Index from header of the style data.
    public long stylesStart;
}
