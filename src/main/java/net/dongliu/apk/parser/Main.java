package net.dongliu.apk.parser;

import net.dongliu.apk.parser.bean.IconFace;

import java.io.IOException;
import java.util.List;

/**
 * Main method for parser apk
 *
 * @author Liu Dong {@literal <dongliu@live.cn>}
 */
public class Main {
    public static void main(String[] args) throws IOException {
        try (ApkFile apkFile = new ApkFile(args[0])) {
            List<IconFace> allIcons = apkFile.getAllIcons();
            System.out.println(allIcons);
        }
    }
}
