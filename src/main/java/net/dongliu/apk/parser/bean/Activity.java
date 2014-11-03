package net.dongliu.apk.parser.bean;

/**
 * activity this apk registered
 *
 * @author Dong Liu
 */
public class Activity {
    private String name;
    private boolean exported;
    private String process;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isExported() {
        return exported;
    }

    public void setExported(boolean exported) {
        this.exported = exported;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    @Override
    public String toString() {
        return "Activity:" + name;
    }
}
