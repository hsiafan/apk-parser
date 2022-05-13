package net.dongliu.apk.parser.struct.signingv2;

public class Signature {
    private final int algorithmID;
    private final byte[] data;

    public Signature(final int algorithmID, final byte[] data) {
        this.algorithmID = algorithmID;
        this.data = data;
    }

    public int getAlgorithmID() {
        return this.algorithmID;
    }

    public byte[] getData() {
        return this.data;
    }
}
