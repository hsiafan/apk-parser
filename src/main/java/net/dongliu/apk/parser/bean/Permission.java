package net.dongliu.apk.parser.bean;

/**
 * permission provided by the app
 *
 * @author Dong Liu
 */
public class Permission {
    private String name;
    private String label;
    private String icon;
    private String description;
    private String group;
    private Constants.ProtectionLevel protectionLevel;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Constants.ProtectionLevel getProtectionLevel() {
        return protectionLevel;
    }

    public void setProtectionLevel(Constants.ProtectionLevel protectionLevel) {
        this.protectionLevel = protectionLevel;
    }
}
