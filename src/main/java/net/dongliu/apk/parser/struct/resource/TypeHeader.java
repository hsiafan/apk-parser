package net.dongliu.apk.parser.struct.resource;

import net.dongliu.apk.parser.struct.ChunkHeader;

/**
 * @author dongliu
 */
public class TypeHeader extends ChunkHeader {

    public static final long NO_ENTRY = 0xFFFFFFFFL;

    // The type identifier this chunk is holding.  Type IDs start at 1 (corresponding to the value
    // of the type bits in a resource identifier).  0 is invalid.
    // uint8_t
    public short id;

    // Must be 0. uint8_t
    public short res0;
    // Must be 0. uint16_t
    public int res1;

    // Number of uint32_t entry indices that follow. uint32
    public long entryCount;

    // Offset from header where ResTable_entry data starts.uint32_t
    public long entriesStart;

    // Configuration this collection of entries is designed for.
    public ResTableConfig config;

    public TypeHeader(int chunkType, int headerSize, long chunkSize) {
        super(chunkType, headerSize, chunkSize);
    }
}
