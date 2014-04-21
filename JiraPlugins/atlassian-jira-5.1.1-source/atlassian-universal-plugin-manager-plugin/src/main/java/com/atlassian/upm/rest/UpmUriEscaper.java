package com.atlassian.upm.rest;

/**
 * Helps in escaping plugin-related URIs.
 */
public class UpmUriEscaper
{
    public static final String SUFFIX = "-key";

    /**
     * Unescapes a String.
     *
     * @param original the escaped String
     * @return the (unescaped) String)
     */
    public static String unescape(String original)
    {
        if (original.endsWith(SUFFIX))
        {
            return original.substring(0, original.lastIndexOf(SUFFIX));
        }
        else
        {
            throw new UnsupportedOperationException("Cannot unescape: " + original);
        }
    }

    /**
     * Escapes a String.
     *
     * @param original the String
     * @return the escaped String
     */
    public static String escape(String original)
    {
        return original + SUFFIX;
    }
}
