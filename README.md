A pure java apk parser, to read encoded xml file and get apk infos, with no need for appt/dexdump binarys.

### Fuctions
For now, the following is (partially) supported:
* resource table by ResourceTableParser
* binary xml file by BinaryXmlParser
* dex file by DexParser
* certificate by CetificateParser

### Dev
If maven is used, you can add dependency:
```
        <dependency>
            <groupId>net.dongliu.apk-parser</groupId>
            <artifactId>apk-parser</artifactId>
            <version>1.3.0</version>
        </dependency>
```
to your pom file.

The easiest way is to use the ApkParser class, which contains convenient mehods to get AndroidManifest.xml, apk meta infos, etc.
```
ApkParser apkParser = new ApkParser(new File(filePath));
if (locale != null) {
    // set a locale to translate resource tag into specific strings in language the locale specified
    apkParser.setPreferredLocale(locale);
}

String xml = apkParser.getManifestXml();
System.out.println(xml);
ApkMeta apkMeta = apkParser.getApkMeta();
System.out.println(apkMeta);
Set<Locale> locales = apkParser.getLocales();
for (Locale l : locales) {
    System.out.println(l);
}
apkParser.close();
```

The apk-parser set locale to null and do not translate resource tag in default. If you want a specific resource string, for example, you want apk title 'WeChat' instead of '@string/app_name', just set the preferred Locale:
```
apkParser.setPreferredLocale(Locale.ENGLISH);
```
This paramerter work for getApkMeta, getManifestXml, and other binary xmls.Apk may contains multi languages, apk parser will find best match languages with locale you specified.

### Command-line use
Run
```
mvn assembly:assembly
```
to get all-in-one excuteable jar.

Usages:
```
java -jar apk-parser-all.jar -l en_US -t manifest [apkfile]     # get apk manifest file as text xml
java -jar apk-parser-all.jar -l en_US -t info [apkfile]         # get apk basic infos
```
Use java -jar apk-parser-all.jar -h to see more options.
