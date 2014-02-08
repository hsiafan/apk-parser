package net.dongliu.apk.parser.struct.xml;

import net.dongliu.apk.parser.struct.ResValue;
import net.dongliu.apk.parser.utils.ResourceLoader;

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
     * see https://android.googlesource.com/platform/frameworks/base/+/master/core/res/res/values/public.xml
     *
     * @author dongliu
     */
    public static class AttrIds {

        // current the max is 0x0101021c
        public static final int MAX_ID = 0x010103f1;
        public static final int ID_START = 0x01010000;
        private static final String[] ids = ResourceLoader.loadSystemAttrIds();

        public static String getString(long id) {
            if (id <= MAX_ID) {
                String str = ids[((int) (id - ID_START))];
                if (str != null) {
                    return str;
                }
            }
            return "xmlMap_0x" + Long.toHexString(id);
        }

    }
}
