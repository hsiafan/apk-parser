package net.dongliu.apk.parser.bean;

/**
 * activity this apk registered
 *
 * @author Dong Liu
 */
public class Activity extends AndroidComponent {

    @Override
    public String toString() {
        return "Activity:" + getName();
    }
}
