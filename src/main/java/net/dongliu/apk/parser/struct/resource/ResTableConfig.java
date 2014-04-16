package net.dongliu.apk.parser.struct.resource;

/**
 * used by resource Type.
 *
 * @author dongliu
 */
public class ResTableConfig {
    // Number of bytes in this structure. uint32_t
    public long size;

    // Mobile country code (from SIM).  0 means "any". uint16_t
    public short mcc;
    // Mobile network code (from SIM).  0 means "any". uint16_t
    public short mnc;
    //uint32_t imsi;

    // 0 means "any".  Otherwise, en, fr, etc. char[2]
    public String language;
    // 0 means "any".  Otherwise, US, CA, etc.  char[2]
    public String country;
    // uint32_t locale;

    // uint8_t
    public short orientation;
    // uint8_t
    public short touchscreen;
    // uint16_t
    public int density;
    // uint32_t screenType;

    // uint8_t
    public short keyboard;
    // uint8_t
    public short navigation;
    // uint8_t
    public short inputFlags;
    // uint8_t
    public short inputPad0;
    // uint32_t input;

    // uint16_t
    public int screenWidth;
    // uint16_t
    public int screenHeight;
    // uint32_t screenSize;

    // uint16_t
    public int sdkVersion;
    // For now minorVersion must always be 0!!!  Its meaning is currently undefined.
    // uint16_t
    public int minorVersion;
    //uint32_t version;

    // uint8_t
    public short screenLayout;
    // uint8_t
    public short uiMode;
    // uint8_t
    public short screenConfigPad1;
    // uint8_t
    public short screenConfigPad2;
    //uint32_t screenConfig;
}
