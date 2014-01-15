package net.dongliu.apk.parser.bean;

/**
 * @author dongliu
 */
public class Locale {

    private String country;

    private String language;

    public static final Locale any = new Locale("", "");
    public static final Locale zh_CN = new Locale("CN", "zh");
    public static final Locale zh_TW = new Locale("TW", "zh");
    public static final Locale en_US = new Locale("US", "en");
    public static final Locale es_US = new Locale("US", "es");

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
        if (this.language.isEmpty() || this.language.equals(locale.language)) {
            if (this.language.isEmpty() || this.country.equals(locale.country)) {
                return 2;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }
}
