package net.dongliu.apk.parser.bean;

/**
 * the receiver this apk registered
 *
 * @author Dong Liu
 */
public class Receiver extends AndroidComponent {

    @Override
    public String toString() {
        return "Receiver:" + getName();
    }
}
