package net.dongliu.apk.parser.bean;

/**
 * the permission used by apk
 *
 * @author dongliu
 */
public class UseFeature implements Feature {
    private String name;
    private boolean required = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
