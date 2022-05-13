package net.dongliu.apk.parser.bean;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * ApkSignV1 certificate file.
 */
public class ApkSigner {
    /**
     * The cert file path in apk file
     */
    private final String path;
    /**
     * The meta info of certificate contained in this cert file.
     */
    private final List<CertificateMeta> certificateMetas;

    public ApkSigner(final String path, final List<CertificateMeta> certificateMetas) {
        this.path = path;
        this.certificateMetas = requireNonNull(certificateMetas);
    }

    public String getPath() {
        return this.path;
    }

    public List<CertificateMeta> getCertificateMetas() {
        return this.certificateMetas;
    }

    @Override
    public String toString() {
        return "ApkSigner{" +
                "path='" + this.path + '\'' +
                ", certificateMetas=" + this.certificateMetas +
                '}';
    }
}
