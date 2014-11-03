package net.dongliu.apk.parser.bean;

/**
 * service provided by apk
 *
 * @author Dong Liu
 */
public class Service {
    private String name;
    private boolean exported;
    // the process this service run with
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
        return "Service:" + name;
    }
}
