package net.dongliu.apk.parser.struct.signingv2;

public class Digest {
    private final int algorithmID;
    private final byte[] value;

    public Digest(final int algorithmID, final byte[] value) {
        this.algorithmID = algorithmID;
        this.value = value;
    }

    public int getAlgorithmID() {
        return this.algorithmID;
    }

    public byte[] getValue() {
        return this.value;
    }
}
