package net.dongliu.apk.parser.bean;


import org.jetbrains.annotations.Nullable;

/**
 * permission provided by the app
 *
 * @author Liu Dong
 */
public class Permission {
    private final String name;
    private final String label;
    private final String icon;
    private final String description;
    private final String group;
    private final String protectionLevel;

    public Permission(final String name, final String label, final String icon, final String description, final String group,
                      final String protectionLevel) {
        this.name = name;
        this.label = label;
        this.icon = icon;
        this.description = description;
        this.group = group;
        this.protectionLevel = protectionLevel;
    }

    public String getName() {
        return this.name;
    }

    public String getLabel() {
        return this.label;
    }

    public String getIcon() {
        return this.icon;
    }

    public String getDescription() {
        return this.description;
    }

    public String getGroup() {
        return this.group;
    }

    @Nullable
    public String getProtectionLevel() {
        return this.protectionLevel;
    }

}
