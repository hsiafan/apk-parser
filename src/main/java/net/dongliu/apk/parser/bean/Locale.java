package net.dongliu.apk.parser.bean;

/**
 * The java use ISO-639 and ISO-3166 to represent a local, with language lower case and country upper case.
 *
 * @author dongliu
 */
public class Locale {

    private String country;

    private String language;

    // the most widely used locales

    public static final Locale any = new Locale("", "");
    public static final Locale en_US = new Locale("US", "en");
    public static final Locale zh_CN = new Locale("CN", "zh");
    public static final Locale zh_TW = new Locale("TW", "zh");
    /**
     * Japanese
     */
    public static final Locale ja = new Locale("ja", "");
    /**
     * German
     */
    public static final Locale de = new Locale("ja", "");
    public static final Locale en = new Locale("en", "");
    /**
     * Korean
     */
    public static final Locale ko = new Locale("ko", "");
    /**
     * French
     */
    public static final Locale fr = new Locale("fr", "");
    /**
     * Spanish; Castilian
     */
    public static final Locale es = new Locale("es", "");
    /**
     * Russian
     */
    public static final Locale ru = new Locale("ru", "");

    public Locale(String country, String language) {
        setCountry(country);
        setLanguage(language);
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        if (country == null || country.isEmpty() || country.charAt(0) == 0) {
            this.country = "";
        } else {
            this.country = country;
        }
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        if (language == null || language.isEmpty() || language.charAt(0) == 0) {
            this.language = "";
        } else {
            this.language = language;
        }
    }

    public String toString() {
        return this.language + "_" + this.country;
    }

    public int match(Locale locale) {
        if (this.language.equals(locale.language)) {
            if (this.country.isEmpty() || this.country.equals(locale.country)) {
                return 2;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }
}
