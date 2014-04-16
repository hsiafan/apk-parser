package net.dongliu.apk.parser.struct;

/**
 * String encoding.
 * If you create an Android project using the Eclipse ADT plugin the string data in the StringPool
 * chunks in the Resource Table will be in the UTF-8 format.
 * <p>
 * If you create an Android project using the android command line tool and then build it from
 * the command line using ant the string data in the StringPool chunks in the Resource Table
 * will be in the 16-bit format.
 * </p>
 * Strange but true.
 *
 * @author dongliu
 */
public enum StringEncoding {
    UTF16, UTF8
}
