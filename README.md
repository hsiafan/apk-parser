Apk parser lib, for decoding binary xml file, getting apk meta info.

Table of Contents
=================

* [Features](#features)
* [Get apk-parser](#get-apk-parser)
* [Usage](#usage)
    * [1. Apk info](#1-apk-info)
    * [2. Get binary xml and manifest xml file](#2-get-binary-xml-and-manifest-xml-file)
    * [3. Get dex classes](#3-get-dex-classes)
    * [4. Get Apk Sign info](#4-get-apk-sign-info)
    * [5. Locales](#5-locales)
* [Open Issue](#open-issue)

#### Features

* Retrieve apk meta info, such as title, icon, package name, version, etc.
* Parse and convert binary xml file to text 
* Get classes from dex file
* Get apk singer info

#### Get apk-parser

Get apk-parser from maven central repo:
```xml
<dependency>
    <groupId>net.dongliu</groupId>
    <artifactId>apk-parser</artifactId>
    <version>2.6.5</version>
</dependency>
```
From version 2.0, apk-parser requires java7. The last version support java6 is 1.7.4.

#### Usage

The ordinary way is using the ApkFile class, which contains convenient methods to get AndroidManifest.xml, apk info, etc.ApkFile need to be closed when no longer used. 
There is also a ByteArrayApkFile class for reading apk file from byte array.

##### 1. Apk info

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

##### 4. Get Apk Sign info

To get apk signer certificate info and other messages, using:

```java
try(ApkFile apkFile = new ApkFile(new File(filePath))) {
    List<ApkSigner> signers = apkFile.getApkSingers(); // apk v1 signers
    List<ApkV2Signer> v2signers = apkFile.getApkV2Singers(); // apk v2 signers
}
```

##### 5. Locales

Apk may have different info(title, icon, etc.) for different regions and languages——or we can call it Locale.
If locale is not set, the default "en_US" locale(<code>Locale.US</code>) is used. You can set one preferred locale by:

```java
try (ApkFile apkFile = new ApkFile(new File(filePath))) {
    apkFile.setPreferredLocale(Locale.SIMPLIFIED_CHINESE);
    ApkMeta apkMeta = apkFile.getApkMeta();
}
```

Apk parser will find best match languages with locale you specified.

If locale is set to null, ApkFile will not translate resource tag, just give the resource id.
For example, the title will be something like '@string/app_name' instead of the real name.


#### Open Issue
If this parser has any problem with a specific apk, open a new issue, **with the link to download the apk file**.