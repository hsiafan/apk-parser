package net.dongliu.apk.parser.bean;

/**
 * service provided by apk
 *
 * @author Dong Liu
 */
public class Service extends AndroidComponent {

    @Override
    public String toString() {
        return "Service:" + getName();
    }
}
