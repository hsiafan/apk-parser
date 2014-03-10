package net.dongliu.apk.parser.struct.dex;

/**
 * @author dongliu
 */
public class DexClassStruct {
    /* index into typeIds for this class. u4 */
    public int classIdx;

    public int accessFlags;
    /* index into typeIds for superclass. u4 */
    public int superclassIdx;

    /* file offset to DexTypeList. u4 */
    public long interfacesOff;

    /* index into stringIds for source file name. u4 */
    public int sourceFileIdx;
    /* file offset to annotations_directory_item. u4 */
    public long annotationsOff;
    /* file offset to class_data_item. u4 */
    public long classDataOff;
    /* file offset to DexEncodedArray. u4 */
    public long staticValuesOff;

    public static int ACC_PUBLIC = 0x1;
    public static int ACC_PRIVATE = 0x2;
    public static int ACC_PROTECTED = 0x4;
    public static int ACC_STATIC = 0x8;
    public static int ACC_FINAL = 0x10;
    public static int ACC_SYNCHRONIZED = 0x20;
    public static int ACC_VOLATILE = 0x40;
    public static int ACC_BRIDGE = 0x40;
    public static int ACC_TRANSIENT = 0x80;
    public static int ACC_VARARGS = 0x80;
    public static int ACC_NATIVE = 0x100;
    public static int ACC_INTERFACE = 0x200;
    public static int ACC_ABSTRACT = 0x400;
    public static int ACC_STRICT = 0x800;
    public static int ACC_SYNTHETIC = 0x1000;
    public static int ACC_ANNOTATION = 0x2000;
    public static int ACC_ENUM = 0x4000;
    public static int ACC_CONSTRUCTOR = 0x10000;
    public static int ACC_DECLARED_SYNCHRONIZED = 0x20000;
}
