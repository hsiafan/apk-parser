package net.dongliu.apk.parser.struct.xml;

import net.dongliu.apk.parser.struct.ResValue;
import net.dongliu.apk.parser.utils.ResourceLoader;

import java.util.Map;

/**
 * xml node attribute
 *
 * @author dongliu
 */
public class Attribute {
    public String namespace;
    public String name;
    // The original raw string value of this 
    public String rawValue;
    // Processesd typed value of this 
    public ResValue typedValue;

    public String getValue() {
        if (rawValue != null) {
            return rawValue;
        } else {
            return typedValue.toString();
        }
    }

    @Override
    public String toString() {
        return name + '=' + '"' + getValue().replace("\"", "\\\"") + '"';
    }


    /**
     * These are attribute resource constants for the platform; as found in android.R.attr
     *
     * @author dongliu
     */
    public static class AttrIds {

        private static final Map<Integer, String> ids = ResourceLoader.loadSystemAttrIds();

        public static String getString(long id) {
            String value = ids.get((int) id);
            if (value == null) {
                value = "AttrId:0x" + Long.toHexString(id);
            }
            return value;
        }

    }
}
