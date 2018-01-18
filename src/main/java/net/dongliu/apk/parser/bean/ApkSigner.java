package net.dongliu.apk.parser.bean;

import java.util.List;

/**
 * ApkSignV1 certificate file.
 */
public class ApkSigner {
    /**
     * The cert file path in apk file
     */
    private String path;
    /**
     * The meta info of certificate contained in this cert file.
     */
    private List<CertificateMeta> certificateMetas;

    public ApkSigner(String path, List<CertificateMeta> certificateMetas) {
        this.path = path;
        this.certificateMetas = certificateMetas;
    }

    public String getPath() {
        return path;
    }

    public List<CertificateMeta> getCertificateMetas() {
        return certificateMetas;
    }

}
