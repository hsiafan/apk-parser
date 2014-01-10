package net.dongliu.apk.parser.struct.resource;

import net.dongliu.apk.parser.struct.StringPool;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dongliu
 */
public class ResourceTable {
    private Map<Short, ResourcePackage> packageMap = new HashMap<Short, ResourcePackage>();
    public StringPool stringPool;

    public void addPackage(ResourcePackage resourcePackage) {
        this.packageMap.put((short) resourcePackage.header.id, resourcePackage);
    }

    public ResourcePackage getPackage(short id) {
        return this.packageMap.get(id);
    }
}
