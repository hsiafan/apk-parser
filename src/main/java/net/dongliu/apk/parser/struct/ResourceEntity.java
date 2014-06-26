package net.dongliu.apk.parser.struct;

import net.dongliu.apk.parser.struct.resource.ResourceTable;
import net.dongliu.apk.parser.utils.ParseUtils;

import java.util.Locale;

/**
 * one entity, may be one entry in resource table, or string value
 * one apk only has one resource table
 *
 * @author dongliu
 */
public class ResourceEntity {

    /**
     * the resource id
     */
    private long resourceId;

    /**
     * if is style resource. just translate this into "@style/xxx/xx"
     */
    boolean isStyle;

    /**
     * the resource's value
     */
    private String value;

    public ResourceEntity(int i) {
        this.value = String.valueOf(i);
    }

    public ResourceEntity(String s) {
        this.value = s;
    }

    public ResourceEntity(boolean b) {
        this.value = String.valueOf(b);
    }

    public ResourceEntity(long resourceId, boolean isStype) {
        this.resourceId = resourceId;
        this.isStyle = isStype;
    }

    public long getResourceId() {
        return resourceId;
    }

    public void setResourceId(long resourceId) {
        this.resourceId = resourceId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * get value as string
     *
     * @return
     */
    public String toStringValue(ResourceTable resourceTable, Locale locale) {
        if (this.value != null) {
            return this.value;
        }
        return ParseUtils.getResourceByid(this.resourceId, isStyle, resourceTable, locale);
    }

    @Override
    public String toString() {
        return "ResourceEntity{" +
                "resourceId=" + resourceId +
                ", isStyle=" + isStyle +
                ", value='" + value + '\'' +
                '}';
    }
}
