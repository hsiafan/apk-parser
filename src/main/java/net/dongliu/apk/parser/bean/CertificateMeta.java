package net.dongliu.apk.parser.bean;

import java.util.Date;

/**
 * basic certificate info.
 *
 * @author dongliu
 */
public class CertificateMeta {

    /**
     * the sign algorithm name
     */
    private String signAlgorithm;

    /**
     * the signature algorithm OID string.
     * An OID is represented by a set of non-negative whole numbers separated by periods.
     * For example, the string "1.2.840.10040.4.3" identifies the SHA-1 with DSA signature algorithm defined in
     * <a href="http://www.ietf.org/rfc/rfc3279.txt">
     *     RFC 3279: Algorithms and Identifiers for the Internet X.509 Public Key Infrastructure Certificate and CRL Profile
     * </a>.
     */
    private String signAlgorithmOID;

    /**
     * the start date of the validity period.
     */
    private Date startDate;

    /**
     * the end date of the validity period.
     */
    private Date endDate;

    /**
     * certificate binary data.
     */
    private byte[] data;

    /**
     * first use base64 to encode certificate binary data, and then calculate md5 of base64b string.
     * some programs use this as the certMd5 of certificate
     */
    private String certBase64Md5;

    /**
     * use md5 to calculate certificate's certMd5.
     */
    private String certMd5;

    /**
     * use sha1 to calculate certificate's certSha1.
     */
    private String certSha1;

    /**
     * use sha256 to calculate certificate's certSha256.
     */
    private String certSha256;

    private String owner;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getCertBase64Md5() {
        return certBase64Md5;
    }

    public void setCertBase64Md5(String certBase64Md5) {
        this.certBase64Md5 = certBase64Md5;
    }

    public String getCertMd5() {
        return certMd5;
    }

    public void setCertMd5(String certMd5) {
        this.certMd5 = certMd5;
    }

    public String getSignAlgorithm() {
        return signAlgorithm;
    }

    public void setSignAlgorithm(String signAlgorithm) {
        this.signAlgorithm = signAlgorithm;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getSignAlgorithmOID() {
        return signAlgorithmOID;
    }

    public void setSignAlgorithmOID(String signAlgorithmOID) {
        this.signAlgorithmOID = signAlgorithmOID;
    }

    public String getCertSha1() {
        return certSha1;
    }

    public void setCertSha1(String certSha1) {
        this.certSha1 = certSha1;
    }

    public String getCertSha256() {
        return certSha256;
    }

    public void setCertSha256(String certSha256) {
        this.certSha256 = certSha256;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "signAlgorithm:\t" + signAlgorithm + '\n' +
                "certBase64Md5:\t" + certBase64Md5 + '\n' +
                "certMd5:\t" + certMd5 + '\n' +
                "certSha1:\t" + certSha1 + '\n' +
                "certSha256:\t" + certSha256 + '\n' +
                "owner:\t" + owner;
    }
}

