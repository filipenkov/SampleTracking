package com.atlassian.crowd.embedded.impl;

import com.google.common.base.Function;
import org.apache.commons.lang.StringUtils;

import java.util.Locale;

public final class IdentifierUtils
{
    /**
     * Not for instantiation.
     */
    private IdentifierUtils()
    {
    }

    private static Locale IDENTIFIER_COMPARE_LOCALE;

    static
    {
        prepareIdentifierCompareLocale();
    }

    public static void prepareIdentifierCompareLocale()
    {
        // This system property defines the language rule
        // by which all the identifiers in crowd are normalized for comparison.
        final String preferredLang = System.getProperty("crowd.identifier.language");
        IDENTIFIER_COMPARE_LOCALE = StringUtils.isNotBlank(preferredLang)? new Locale(preferredLang):Locale.ENGLISH;
    }

    /**
     * Converts the given identifier string to lowercase.
     * The rule of conversion is subject to the language defined in crowd.identifier.language system property.
     *
     * @param identifier the identifier string, not null.
     *
     * @return lowercase identifier.
     */
    public static String toLowerCase(String identifier)
    {
        return identifier.toLowerCase(IDENTIFIER_COMPARE_LOCALE);
    }

    /**
     * Converts the two given identifier strings to lowercase and compare them.
     * The rule of conversion is subject to the language defined in crowd.identifier.language system property.
     *
     * @param identifier1 identifier.
     * @param identifier2 identifier.
     *
     * @return comparison result similar to as {@link java.util.Comparator#compare(Object, Object)}}
     */
    public static int compareToInLowerCase(String identifier1, String identifier2)
    {
        return toLowerCase(identifier1).compareTo(toLowerCase(identifier2));
    }

    /**
     * Converts the two given identifier strings to lowercase and check for equality.
     * The rule of conversion is subject to the language defined in crowd.identifier.language system property.
     *
     * @param identifier1 identifier.
     * @param identifier2 identifier.
     *
     * @return true if equal, otherwise false.
     */
    public static boolean equalsInLowerCase(String identifier1, String identifier2)
    {
        return compareToInLowerCase(identifier1, identifier2) == 0;
    }

    /**
     * Function of {@link #toLowerCase(String)} method.
     */
    public static final Function<String, String> TO_LOWER_CASE = new Function<String, String>()
    {
        @Override
        public String apply(String from)
        {
            return from.toLowerCase(IDENTIFIER_COMPARE_LOCALE);
        }
    };
}
