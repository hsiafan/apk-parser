Apk parser with java, for decoding xml file and getting meta infos from apk file.

#### Features

* Retrieve basic apk meta info, such as title, icon, package name, version, etc.
* Parse and convert binary xml file to text 
* Get classes names from dex file
* Get certificate meta info and verify apk signature

#### Get apk-parser

Apk-parser has been submitted to maven central repo. With maven, you can add apk-parser as dependency by:
```xml
<dependency>
    <groupId>net.dongliu</groupId>
    <artifactId>apk-parser</artifactId>
    <version>2.2.1</version>
</dependency>
```
From version 2.0, apk-parser requires java7. The last version support java6 is 1.7.4.

#### Usage

The ordinary way is using the ApkFile class, which contains convenient methods to get AndroidManifest.xml, apk meta info, etc.
There is also a ByteArrayApkFile class for reading apk file from byte array.
ApkFile need to be closed when no longer used. If you need to get info more than once for one apk file, you can reuse the same ApkFile instance.

If only want to get meta info or manifest xml file, you can use a utils class ApkParsers.

##### 1. Apk meta info

ApkMeta contains name(label), packageName, version, sdk, used features, etc.

```java
try (ApkFile apkFile = new ApkFile(new File(filePath))) {
    ApkMeta apkMeta = apkFile.getApkMeta();
    System.out.println(apkMeta.getLabel());
    System.out.println(apkMeta.getPackageName());
    System.out.println(apkMeta.getVersionCode());
    for (UseFeature feature : apkMeta.getUsesFeatures()) {
        System.out.println(feature.getName());
    }
}
```
##### 2. Get binary xml and manifest xml file

```java
try (ApkFile apkFile = new ApkFile(new File(filePath))) {
    String manifestXml = apkFile.getManifestXml();
    String xml = apkFile.transBinaryXml("res/menu/main.xml");
}
```

##### 3. Get dex classes

```java
try(ApkFile apkFile = new ApkFile(new File(filePath))) {
    DexClass[] classes = apkFile.getDexClasses();
    for (DexClass dexClass : classes) {
        System.out.println(dexClass);
    }
}
```

##### 4. Get certificate and verify apk signature

```java
try(ApkFile apkFile = new ApkFile(new File(filePath))) {
    ApkSignStatus signStatus = apkFile.verifyApk();
    List<CertificateMeta> certs = apkFile.getCertificateMetas();
    for (CertificateMeta certificateMeta : certs) {
        System.out.println(certificateMeta.getSignAlgorithm());
    }
}
```

##### 5. Locales

Apk may appear different info(title, icon, etc.) for different regions and languages——or we can called it Locales.
If locale is not set, the "en_US" locale(<code>Locale.US</code>) is used. You can set locale as blow:

```java
try (ApkFile apkFile = new ApkFile(new File(filePath))) {
    apkFile.setPreferredLocale(Locale.SIMPLIFIED_CHINESE);
    ApkMeta apkMeta = apkFile.getApkMeta();
}
```

Apk parser will find best match languages with locale you specified.

If locale is set to null, ApkFile will not translate resource tag, just give the resource id.
For example, apk title will be '@string/app_name' instead of 'WeChat'.
