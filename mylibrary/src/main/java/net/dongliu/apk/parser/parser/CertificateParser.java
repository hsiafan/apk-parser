package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.ApkParsers;
import net.dongliu.apk.parser.bean.CertificateMeta;

import java.security.cert.CertificateException;
import java.util.List;

/**
 * Parser certificate info.
 * One apk may have multi certificates(certificate chain).
 *
 * @author dongliu
 */
public abstract class CertificateParser {

    protected final byte[] data;

    public CertificateParser(final byte[] data) {
        this.data = data;
    }

    public static CertificateParser getInstance(final byte[] data) {
        if (ApkParsers.useBouncyCastle()) {
            return new BCCertificateParser(data);
        }
        return new JSSECertificateParser(data);
    }

    /**
     * get certificate info
     */
    public abstract List<CertificateMeta> parse() throws CertificateException;

}
