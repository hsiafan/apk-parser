package net.dongliu.apk.parser.struct;

import net.dongliu.apk.parser.utils.Unsigned;

/**
 * For read apk signing block
 *
 * @see <a href="https://source.android.com/security/apksigning/v2">apksigning v2 scheme</a>
 */
public class ApkSigningBlock {
    public static final int SIGNING_V2_ID = 0x7109871a;

    public static final String MAGIC = "APK Sig Block 42";

    // uint64, the size of this block
    public long size;

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = Unsigned.ensureUInt(size);
    }

    public static class Entry {
        // uint32 the entry id
        private int id;
        // the len of this pair
        private int size;
    }


}
