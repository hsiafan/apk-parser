APK parser lib, for decoding binary XML files, getting APK meta info.

Table of Contents
=================

* [Features](#features)
* [Get APK-parser](#get-apk-parser)
* [Usage](#usage)
    * [1. APK Info](#1-apk-info)
    * [2. Get Binary XML and Manifest XML Files](#2-get-binary-xml-and-manifest-xml-file)
    * [3. Get DEX Classes](#3-get-dex-classes)
    * [4. Get APK Signing Info](#4-get-apk-sign-info)
    * [5. Locales](#5-locales)
* [Reporting Issues](#open-issue)

#### Features

* Retrieve APK meta info, such as title, icon, package name, version, etc.
* Parse and convert binary XML files to text 
* Get classes from DEX files
* Get APK singer info

#### Get APK-parser

Get APK-parser from the Maven Central Reposotiry:
```xml
<dependency>
    <groupId>net.dongliu</groupId>
    <artifactId>apk-parser</artifactId>
    <version>2.6.10</version>
</dependency>
```
From version 2.0, apk-parser requires Java 7. The last version to support Java 6 is 1.7.4.

#### Usage

The ordinary way is using the ApkFile class, which contains convenient methods to get AndroidManifest.xml, APK info, etc.
The ApkFile need to be closed when no longer used. 
There is also a ByteArrayApkFile class for reading APK files from byte array.

##### 1. APK Info

ApkMeta contains name(label), packageName, version, SDK, used features, etc.

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
##### 2. Get Binary XML and Manifest XML Files

```java
try (ApkFile apkFile = new ApkFile(new File(filePath))) {
    String manifestXml = apkFile.getManifestXml();
    String xml = apkFile.transBinaryXml("res/menu/main.xml");
}
```

##### 3. Get DEX Classes

```java
try(ApkFile apkFile = new ApkFile(new File(filePath))) {
    DexClass[] classes = apkFile.getDexClasses();
    for (DexClass dexClass : classes) {
        System.out.println(dexClass);
    }
}
```

##### 4. Get APK Signing Info

Get the APK signer certificate info and other messages, using:

```java
try(ApkFile apkFile = new ApkFile(new File(filePath))) {
    List<ApkSigner> signers = apkFile.getApkSingers(); // apk v1 signers
    List<ApkV2Signer> v2signers = apkFile.getApkV2Singers(); // apk v2 signers
}
```

##### 5. Locales

An APK may have different info (title, icon, etc.) for different regions and languages——or we can call it a "locale".
If a locale is not set, the default "en_US" locale (<code>Locale.US</code>) is used. You can set a preferred locale by:

```java
try (ApkFile apkFile = new ApkFile(new File(filePath))) {
    apkFile.setPreferredLocale(Locale.SIMPLIFIED_CHINESE);
    ApkMeta apkMeta = apkFile.getApkMeta();
}
```

APK-parser will find the best matching languages for the locale you specified.

If locale is set to null, ApkFile will not translate the resource tag, and instead just give the resource ID.
For example, the title will be something like '@string/app_name' instead of the real name.


#### Reporting Issues
If this parser has any problem with a specific APK, open a new issue, **with a link to download the APK file**.
