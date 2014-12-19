package net.dongliu.apk.parser.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * common parent class for activity, service, receiver
 *
 * @author Dong Liu dongliu@wandoujia.com
 */
public abstract class AndroidComponent {
    private String name;
    private boolean exported;
    private String process;
    private List<IntentFilter> intentFilters = new ArrayList<>();

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

    public List<IntentFilter> getIntentFilters() {
        return intentFilters;
    }

    public void addIntentFilter(IntentFilter intentFilter) {
        intentFilter.setOwner(this);
        this.intentFilters.add(intentFilter);
    }
}
