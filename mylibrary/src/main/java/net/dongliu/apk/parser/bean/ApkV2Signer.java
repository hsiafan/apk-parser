package net.dongliu.apk.parser.bean;

import java.util.List;

/**
 * ApkSignV1 certificate file.
 */
public class ApkV2Signer {
    /**
     * The meta info of certificate contained in this cert file.
     */
    private final List<CertificateMeta> certificateMetas;

    public ApkV2Signer(final List<CertificateMeta> certificateMetas) {
        this.certificateMetas = certificateMetas;
    }

    public List<CertificateMeta> getCertificateMetas() {
        return this.certificateMetas;
    }

}
