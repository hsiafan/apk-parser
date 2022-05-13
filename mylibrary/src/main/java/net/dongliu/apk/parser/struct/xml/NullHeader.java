package net.dongliu.apk.parser.struct.xml;

import net.dongliu.apk.parser.struct.ChunkHeader;

/**
 * Null header.
 *
 * @author dongliu
 */
public class NullHeader extends ChunkHeader {
    public NullHeader(final int chunkType, final int headerSize, final long chunkSize) {
        super(chunkType, headerSize, chunkSize);
    }
}
