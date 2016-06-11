package eu.daiad.web.util;

public class ValidationUtils {

    private static String[] locales = new String[] { "en", "en-GB", "el", "de", "es" };

    private ValidationUtils() {

    }

    /**
     * Validates a locale.
     * 
     * @param locale the locale.
     * @return true if locale is valid.
     */
    public static boolean isLocaleValid(String locale) {
        for (String l : locales) {
            if (l.equals(locale)) {
                return true;
            }
        }
        return false;
    }

}
