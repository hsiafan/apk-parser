package net.dongliu.apk.parser.struct.resource;

import net.dongliu.apk.parser.struct.ChunkHeader;
import net.dongliu.apk.parser.struct.ChunkType;
import net.dongliu.apk.parser.utils.Unsigned;

/**
 * resource file header
 *
 * @author dongliu
 */
public class ResourceTableHeader extends ChunkHeader {
    // The number of ResTable_package structures. uint32
    private int packageCount;

    public ResourceTableHeader(final int headerSize, final int chunkSize) {
        super(ChunkType.TABLE, headerSize, chunkSize);
    }

    public long getPackageCount() {
        return Unsigned.toLong(this.packageCount);
    }

    public void setPackageCount(final long packageCount) {
        this.packageCount = Unsigned.toUInt(packageCount);
    }
}
