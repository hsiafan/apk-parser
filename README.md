A pure java apk parser, to read encoded xml file and get apk infos, with no need for appt/dexdump binarys.

### Fuctions
For now, the following is (partially) supported:
* resource table by ResourceTableParser
* binary xml file by BinaryXmlParser
* dex file by DexParser
* certificate by CetificateParser


### Build
This project use maven as build tool.
```
mvn package                 # build jar
mvn assembly:assembly       # build with-dependency and excuteable jar
```

### Dev
If maven is used, run
```
mvn install
```
to add jars to your locale maven repo, 
then add:
```
<dependency>
    <groupId>net.dongliu.apk-parser</groupId>
    <artifactId>apk-parser</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
to your pom file.

Else you need to copy and add jars to your classpath.

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
Also there is a cmd interface for direct using, just use dist/apk-parser-all.jar:
* java -jar apk-parser-all.jar -t manifest [apkfile]
* java -jar apk-parser-all.jar -t info [apkfile]

Use java -jar apk-parser-all.jar -h to see more options.
