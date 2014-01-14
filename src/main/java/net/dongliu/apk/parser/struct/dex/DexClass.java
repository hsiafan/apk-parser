package net.dongliu.apk.parser.struct.dex;

/**
 * @author dongliu
 */
public class DexClass {
    /* index into typeIds for this class. u4 */
    public int classIdx;
    public String classType;

    // u4
    public long accessFlags;
    /* index into typeIds for superclass. u4 */
    public long superclassIdx;

    /* file offset to DexTypeList. u4 */
    public long interfacesOff;

    /* index into stringIds for source file name. u4 */
    public long sourceFileIdx;
    /* file offset to annotations_directory_item. u4 */
    public long annotationsOff;
    /* file offset to class_data_item. u4 */
    public long classDataOff;
    /* file offset to DexEncodedArray. u4 */
    public long staticValuesOff;
}
