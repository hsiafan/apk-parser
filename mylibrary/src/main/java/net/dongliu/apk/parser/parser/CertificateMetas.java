package net.dongliu.apk.parser.parser;

import net.dongliu.apk.parser.bean.CertificateMeta;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class CertificateMetas {

    public static List<CertificateMeta> from(final List<X509Certificate> certificates) throws CertificateEncodingException {
        final List<CertificateMeta> certificateMetas = new ArrayList<>(certificates.size());
        for (final X509Certificate certificate : certificates) {
            final CertificateMeta certificateMeta = CertificateMetas.from(certificate);
            certificateMetas.add(certificateMeta);
        }
        return certificateMetas;
    }

    public static CertificateMeta from(final X509Certificate certificate) throws CertificateEncodingException {
        final byte[] bytes = certificate.getEncoded();
        final String certMd5 = CertificateMetas.md5Digest(bytes);
        final String publicKeyString = CertificateMetas.byteToHexString(bytes);
        final String certBase64Md5 = CertificateMetas.md5Digest(publicKeyString);
        return new CertificateMeta(
                certificate.getSigAlgName().toUpperCase(),
                certificate.getSigAlgOID(),
                certificate.getNotBefore(),
                certificate.getNotAfter(),
                bytes, certBase64Md5, certMd5);
    }

    private static String md5Digest(final byte[] input) {
        final MessageDigest digest = CertificateMetas.getDigest("md5");
        digest.update(input);
        return CertificateMetas.getHexString(digest.digest());
    }

    private static String md5Digest(final String input) {
        final MessageDigest digest = CertificateMetas.getDigest("md5");
        digest.update(input.getBytes(StandardCharsets.UTF_8));
        return CertificateMetas.getHexString(digest.digest());
    }

    private static String byteToHexString(final byte[] bArray) {
        final StringBuilder sb = new StringBuilder(bArray.length);
        String sTemp;
        for (final byte aBArray : bArray) {
            sTemp = Integer.toHexString(0xFF & (char) aBArray);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    private static String getHexString(final byte[] digest) {
        final BigInteger bi = new BigInteger(1, digest);
        return String.format("%032x", bi);
    }

    private static MessageDigest getDigest(final String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
