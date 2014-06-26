package net.dongliu.apk.parser.bean;

import java.util.Locale;

/**
 * The java use ISO639 and ISO3166 to represent a local, with language lower case and country upper case.
 * <p>
 * Note: we do not use java.util.Locale here.
 * </p>
 *
 * @author dongliu
 */
public class Locales {

    /**
     * when do localize, any locale will match this
     */
    public static final Locale any = new Locale("", "");

    public static int match(Locale locale, Locale targetLocale) {
        if (locale == null) {
            return -1;
        }
        if (locale.getLanguage().equals(targetLocale.getLanguage())) {
            if (locale.getCountry().isEmpty() || locale.getCountry().equals(locale.getCountry())) {
                return 2;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }
}
