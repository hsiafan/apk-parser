package net.dongliu.apk.parser.struct.xml;

import net.dongliu.apk.parser.struct.ChunkHeader;

/**
 * @author dongliu
 */
public class XmlNodeHeader extends ChunkHeader {
    public XmlNodeHeader(int chunkType, int headerSize, long chunkSize) {
        super(chunkType, headerSize, chunkSize);
    }

    // Line number in original source file at which this element appeared.
    public int lineNum;
    // Optional XML comment string pool ref, -1 if none
    public int commentRef;
}
