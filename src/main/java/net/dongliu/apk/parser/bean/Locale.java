package net.dongliu.apk.parser.bean;

/**
 * The java use ISO-639 and ISO-3166 to represent a local, with language lower case and country upper case.
 * <p>
 * Note: we do not use java.util.Locale here.
 * </p>
 *
 * @author dongliu
 */
public class Locale {

    private String country;

    private String language;

    /**
     * when do localize, any locale will match this
     */
    public static final Locale any = new Locale("");

    /**
     * do not translate any resource to localize text
     */
    public static final Locale none = new Locale("none");

    // the most widely used locales
    public static final Locale en = new Locale("en");
    public static final Locale en_US = new Locale("en", "US");
    /**
     * Chinese
     */
    public static final Locale zh_CN = new Locale("zh", "CN");
    public static final Locale zh_TW = new Locale("zh", "TW");
    public static final Locale zh_HK = new Locale("zh", "HK");
    public static final Locale zh = new Locale("zh");
    /**
     * Japanese
     */
    public static final Locale ja = new Locale("ja");
    /**
     * German
     */
    public static final Locale de = new Locale("de");
    /**
     * Korean
     */
    public static final Locale ko = new Locale("ko");
    /**
     * French
     */
    public static final Locale fr = new Locale("fr");
    /**
     * Spanish; Castilian
     */
    public static final Locale es = new Locale("es");
    /**
     * Russian
     */
    public static final Locale ru = new Locale("ru");

    public Locale(String language) {
        setLanguage(language);
        setCountry("");
    }

    public Locale(String language, String country) {
        setLanguage(language);
        setCountry(country);
    }

    public void setCountry(String country) {
        if (country == null || country.isEmpty() || country.charAt(0) == 0) {
            this.country = "";
        } else {
            this.country = country;
        }
    }

    public void setLanguage(String language) {
        if (language == null || language.isEmpty() || language.charAt(0) == 0) {
            this.language = "";
        } else {
            this.language = language;
        }
    }

    public String toString() {
        if (!this.country.isEmpty()) {
            return this.language + "_" + this.country;
        } else {
            return this.language;
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Locale locale = (Locale) o;
        return language.equals(locale.language) && country.equals(locale.country);
    }

    @Override
    public int hashCode() {
        int result = language.hashCode();
        result = 31 * result + country.hashCode();
        return result;
    }
}
