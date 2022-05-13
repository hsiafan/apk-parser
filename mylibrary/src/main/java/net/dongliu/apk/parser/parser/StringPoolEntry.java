package net.dongliu.apk.parser.parser;

/**
 * class for sort string pool indexes
 */
public class StringPoolEntry {
    private int idx;
    private long offset;

    public StringPoolEntry(final int idx, final long offset) {
        this.idx = idx;
        this.offset = offset;
    }

    public int getIdx() {
        return this.idx;
    }

    public void setIdx(final int idx) {
        this.idx = idx;
    }

    public long getOffset() {
        return this.offset;
    }

    public void setOffset(final long offset) {
        this.offset = offset;
    }

}
