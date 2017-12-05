package net.dongliu.apk.parser;

import java.io.IOException;
import java.security.cert.CertificateException;

/**
 * Main method for parser apk
 *
 * @author Liu Dong {@literal <dongliu@live.cn>}
 */
public class Main {
    public static void main(String[] args) throws IOException, CertificateException {
        try (ApkFile apkFile = new ApkFile(args[0])) {
            System.out.println(apkFile.getAllCertificateMetas());
        }
    }
}
