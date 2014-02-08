package net.dongliu.apk.parser.struct.xml;

import net.dongliu.apk.parser.struct.ResValue;

/**
 * @author dongliu
 */
public class XmlCData {

    public static final String CDATA_START = "<![CDATA[";
    public static final String CDATA_END = "]]>";

    // The raw CDATA character data.
    public String data;

    // The typed value of the character data if this is a CDATA node.
    public ResValue typedData;

    @Override
    public String toString() {
        if (data != null) {
            return CDATA_START + data + CDATA_END;
        } else {
            return CDATA_START + typedData.toString() + CDATA_END;
        }
    }
}
