package net.dongliu.apk.parser;

import java.io.IOException;

/**
 * Main method for parser apk
 *
 * @author Liu Dong {@literal <dongliu@live.cn>}
 */
public class Main {
    public static void main(String[] args) throws IOException {
        try (ApkFile apkFile = new ApkFile(args[0])) {
            String xml = apkFile.getManifestXml();
            System.out.println(xml);
        }
    }
}
