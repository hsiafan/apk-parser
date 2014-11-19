A pure java apk parser, to read encoded xml file and get apk infos, with no need for appt/dexdump binaries.

#### Features
For now, the following is (partially) supported:
* resource table by ResourceTableParser
* binary xml file by BinaryXmlParser
* dex file by DexParser
* certificate by CertificateParser

#### Get apk-parser
Apk-parser has been submited to maven central repo, maven, gradle, ivy and other build tools can be used to get this lib.
With maven, you can add apk-parser as dependency by:
```xml
<dependency>
    <groupId>net.dongliu</groupId>
    <artifactId>apk-parser</artifactId>
    <version>1.6.1</version>
</dependency>
```

#### Usage
The easiest way is to use the ApkParser class, which contains convenient methods to get AndroidManifest.xml, apk meta infos, etc.
#####1. Apk meta info
```java
try(ApkParser apkParser = new ApkParser(new File(filePath))) {
    System.out.println(apkMeta.getLabel());
    System.out.println(apkMeta.getPackageName());
    System.out.println(apkMeta.getVersionCode());
    for (UseFeature feature : apkMeta.getUsesFeatures()) {
        System.out.println(feature.getName());
    }
}
```
#####2. Get binary xml and menifest xml file
```java
try(ApkParser apkParser = new ApkParser(new File(filePath))) {
    String manifestXml = apkParser.getManifestXml();
    String xml = apkParser.transBinaryXml("res/menu/main.xml");
}
```
#####3. Get dex classes
```java
try(ApkParser apkParser = new ApkParser(new File(filePath))) {
    DexClass[] classes = apkParser.getDexClasses();
    for (DexClass dexClass : classes) {
        System.out.println(dexClass);
    }
}
```

#####4. Get certificate and verify apk signature
```java
try(ApkParser apkParser = new ApkParser(new File(filePath))) {
    ApkSignStatus signStatus = apkParser.verifyApk();
    List<CertificateMeta> certs = apkParser.getCertificateMetas();
    for (CertificateMeta certificateMeta : certs) {
        System.out.println(certificateMeta.getSignAlgorithm());
    }
}
```

#####5. Locales
Apk may appear different infos(title, icon, etc.) for different region and language, which is determined by Locales.
If locale is not set, the default Locale of of OS is used. You can set locale like this:
```java
ApkParser apkParser = new ApkParser(new File(filePath));
apkParser.setPreferredLocale(Locale.ENGLISH);
ApkMeta apkMeta = apkParser.getApkMeta();
```
The PreferredLocale parameter work for getApkMeta, getManifestXml, and other binary xmls.
Apk parser will find best match languages with locale you specified.

If locale is set to null, ApkParser will not translate resource tag, just give the resource id.
For example, apk title will be '@string/app_name' instead of 'WeChat'.
