package net.dongliu.apk.parser.bean;

import java.util.List;

/**
 * ApkSignV1 certificate file.
 */
public class ApkCertificateFileInfo {
    /**
     * The cert file path in apk file
     */
    private String path;
    /**
     * The meta info of certificate contained in this cert file.
     */
    private List<CertificateMeta> certificateMetaList;

    public ApkCertificateFileInfo(String path, List<CertificateMeta> certificateMetaList) {
        this.path = path;
        this.certificateMetaList = certificateMetaList;
    }

    public String getPath() {
        return path;
    }

    public List<CertificateMeta> getCertificateMetaList() {
        return certificateMetaList;
    }

}
