package net.dongliu.apk.parser.bean;

/**
 * cetificate infos.One apk may have multi cetificates.
 *
 * @author dongliu
 */
public class CertificateMeta {

    /**
     * cetificate binay data.
     */
    private byte[] data;

    /**
     * signature same with wdj
     */
    private String base64Signature;

    /**
     * use md5 to caculate cetificate signature.
     */
    private String signature;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getBase64Signature() {
        return base64Signature;
    }

    public void setBase64Signature(String base64Signature) {
        this.base64Signature = base64Signature;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
