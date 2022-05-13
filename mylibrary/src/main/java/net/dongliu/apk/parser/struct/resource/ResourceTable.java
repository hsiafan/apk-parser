package net.dongliu.apk.parser.struct.resource;

import net.dongliu.apk.parser.struct.ResourceValue;
import net.dongliu.apk.parser.struct.StringPool;
import net.dongliu.apk.parser.utils.ResourceLoader;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The apk resource table
 *
 * @author dongliu
 */
public class ResourceTable {
    private final Map<Short, ResourcePackage> packageMap = new HashMap<>();
    private StringPool stringPool;

    public static final Map<Integer, String> sysStyle = ResourceLoader.loadSystemStyles();

    public void addPackage(final ResourcePackage resourcePackage) {
        this.packageMap.put(resourcePackage.getId(), resourcePackage);
    }

    public ResourcePackage getPackage(final short id) {
        return this.packageMap.get(id);
    }

    public StringPool getStringPool() {
        return this.stringPool;
    }

    public void setStringPool(final StringPool stringPool) {
        this.stringPool = stringPool;
    }


    /**
     * Get resources match the given resource id.
     */
    @NotNull
    public List<Resource> getResourcesById(final long resourceId) {
        // An Android Resource id is a 32-bit integer. It comprises
        // an 8-bit Package id [bits 24-31]
        // an 8-bit Type id [bits 16-23]
        // a 16-bit Entry index [bits 0-15]


        final short packageId = (short) (resourceId >> 24 & 0xff);
        final short typeId = (short) ((resourceId >> 16) & 0xff);
        final int entryIndex = (int) (resourceId & 0xffff);
        final ResourcePackage resourcePackage = this.getPackage(packageId);
        if (resourcePackage == null) {
            return Collections.emptyList();
        }
        final TypeSpec typeSpec = resourcePackage.getTypeSpec(typeId);
        final List<Type> types = resourcePackage.getTypes(typeId);
        if (typeSpec == null || types == null) {
            return Collections.emptyList();
        }
        if (!typeSpec.exists(entryIndex)) {
            return Collections.emptyList();
        }

        // read from type resource
        final List<Resource> result = new ArrayList<>();
        for (final Type type : types) {
            final ResourceEntry resourceEntry = type.getResourceEntry(entryIndex);
            if (resourceEntry == null) {
                continue;
            }
            final ResourceValue currentResourceValue = resourceEntry.getValue();
            if (currentResourceValue == null) {
                continue;
            }

            // cyclic reference detect
            if (currentResourceValue instanceof ResourceValue.ReferenceResourceValue) {
                if (resourceId == ((ResourceValue.ReferenceResourceValue) currentResourceValue)
                        .getReferenceResourceId()) {
                    continue;
                }
            }

            result.add(new Resource(typeSpec, type, resourceEntry));
        }
        return result;
    }

    /**
     * contains all info for one resource
     */
    public static class Resource {
        private final TypeSpec typeSpec;
        private final Type type;
        private final ResourceEntry resourceEntry;

        public Resource(final TypeSpec typeSpec, final Type type, final ResourceEntry resourceEntry) {
            this.typeSpec = typeSpec;
            this.type = type;
            this.resourceEntry = resourceEntry;
        }

        public TypeSpec getTypeSpec() {
            return this.typeSpec;
        }

        public Type getType() {
            return this.type;
        }

        public ResourceEntry getResourceEntry() {
            return this.resourceEntry;
        }
    }
}
