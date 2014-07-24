package net.dongliu.apk.parser;

import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.CertificateMeta;
import net.dongliu.apk.parser.bean.DexClass;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * for command line.
 *
 * @author dongliu
 */
public class Main {

    public static void main(String args[]) throws IOException, CertificateEncodingException {
        Options opt = new Options();
        opt.addOption("t", "type", true, "type, which couble be: manifest | info | locale | dex | sign");
        opt.addOption("l", "locale", true, "locale, the language and country, as en_US, en, etc.");
        opt.addOption("h", "help", false, "show helps.");

        String cmdLineSyntax = "Usage: java -jar apk-parser-all.jar [-t/--type type] apkfileName";

        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLineParser commandLineParser = new PosixParser();
        CommandLine commandLine;
        try {
            commandLine = commandLineParser.parse(opt, args);
        } catch (ParseException e) {
            helpFormatter.printHelp(cmdLineSyntax, opt);
            return;
        }
        if (commandLine.hasOption("h")) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp(cmdLineSyntax, "", opt, "");
            return;
        }

        String type;
        if (commandLine.hasOption("t")) {
            type = commandLine.getOptionValue('t');
        } else {
            System.out.println("Need type parameter.");
            helpFormatter.printHelp(cmdLineSyntax, opt);
            return;
        }


        String[] argv = commandLine.getArgs();
        if (argv.length != 1) {
            System.out.println("Should have one apk file patch parameter.");
            helpFormatter.printHelp(cmdLineSyntax, opt);
            return;
        }

        String filePath = argv[0];

        Locale locale = null;
        if (commandLine.hasOption("l")) {
            String localeStr = commandLine.getOptionValue("l");
            String language;
            String country;
            if (localeStr.contains("_")) {
                String[] items = localeStr.split("_");
                language = items[0];
                country = items[1];
            } else {
                language = localeStr;
                country = "";
            }
            if (language.length() != 2 || (country.length() != 0 && country.length() != 2)) {
                System.out.println("Incorrect locale:" + localeStr);
                return;
            }
            locale = new Locale(language, country);
        }

        ApkParser apkParser = new ApkParser(new File(filePath));
        try {
            if (locale != null) {
                apkParser.setPreferredLocale(locale);
            }

            if (type.equals("manifest")) {
                String xml = apkParser.getManifestXml();
                System.out.println(xml);
            } else if (type.equals("info")) {
                ApkMeta apkMeta = apkParser.getApkMeta();
                System.out.println(apkMeta);
            } else if (type.equals("locale")) {
                Set<Locale> locales = apkParser.getLocales();
                for (Locale l : locales) {
                    System.out.println(l.toLanguageTag());
                }
            } else if (type.equals("dex")) {
                DexClass[] dexClasses = apkParser.getDexClasses();
                for (DexClass dexClass : dexClasses) {
                    System.out.println(dexClass);
                }
            } else if (type.equals("sign")) {
                List<CertificateMeta> certs = apkParser.getCertificateMetas();
                for (CertificateMeta cert : certs) {
                    System.out.println("Certficate md5:" + cert.getCertMd5());
                    System.out.println("Certficate base64 md5:" + cert.getCertBase64Md5());
                }
            } else {
                System.out.println("Unknow type:" + type);
                helpFormatter.printHelp(cmdLineSyntax, opt);
            }
        } finally {
            apkParser.close();
        }
    }
}
