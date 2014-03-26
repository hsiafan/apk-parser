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
            <version>1.0.1</version>
        </dependency>
```
to your pom file.

The easiest way is to use the ApkParser class, which contains convenient mehods to get AndroidManifest.xml, apk meta infos, etc.
```
ApkParser apkParser = new ApkParser(new File(filePath));
if (locale != null) {
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

### Command-line use
Run
```
mvn assembly:assembly
```
to get all-in-one excuteable jar.

Usages:
```
java -jar apk-parser-all.jar -t manifest [apkfile]     # get apk manifest file as text xml
java -jar apk-parser-all.jar -t info [apkfile]         # get apk basic infos
```
Use java -jar apk-parser-all.jar -h to see more options.
