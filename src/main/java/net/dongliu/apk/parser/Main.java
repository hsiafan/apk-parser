package net.dongliu.apk.parser;

import net.dongliu.apk.parser.bean.Icon;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * Main method for parser apk
 *
 * @author Liu Dong {@literal <dongliu@live.cn>}
 */
public class Main {
    public static void main(String[] args) throws IOException, CertificateException {
        try (ApkFile apkFile = new ApkFile(args[0])) {
            List<Icon> iconFiles = apkFile.getIconFiles();
            for (Icon iconFile : iconFiles) {
                System.out.println(iconFile);
            }
        }
    }
}
