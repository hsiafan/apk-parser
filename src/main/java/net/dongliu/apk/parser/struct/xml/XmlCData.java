package net.dongliu.apk.parser.struct.xml;

import net.dongliu.apk.parser.struct.ResourceEntity;
import net.dongliu.apk.parser.struct.resource.ResourceTable;

import java.util.Locale;

/**
 * @author dongliu
 */
public class XmlCData {

    public static final String CDATA_START = "<![CDATA[";
    public static final String CDATA_END = "]]>";

    // The raw CDATA character data.
    public String data;

    // The typed value of the character data if this is a CDATA node.
    public ResourceEntity typedData;

    /**
     * get value as string
     *
     * @return
     */
    public String toStringValue(ResourceTable resourceTable, Locale locale) {
        if (data != null) {
            return CDATA_START + data + CDATA_END;
        } else {
            return CDATA_START + typedData.toStringValue(resourceTable, locale) + CDATA_END;
        }
    }

    @Override
    public String toString() {
        return "XmlCData{" +
                "data='" + data + '\'' +
                ", typedData=" + typedData +
                '}';
    }
}
