package net.dongliu.apk.parser.struct.xml;

import net.dongliu.apk.parser.struct.ChunkHeader;

/**
 * @author dongliu
 */
public class XmlResourceMapHeader extends ChunkHeader {
    public XmlResourceMapHeader(final int chunkType, final int headerSize, final long chunkSize) {
        super(chunkType, headerSize, chunkSize);
    }
}
