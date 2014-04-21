package com.atlassian.jira.webtest.framework.util;

/**
 * Utility methods for JQuery selectors.
 *
 * @since v4.3
 */
public class JQueryUtils
{
    /**
     * The JQuery meta-characters, as per <a href="http://api.jquery.com/category/selectors/">http://api.jquery.com/category/selectors/</a>.
     */
    private static final char[] META_CHARS = {
            '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '.', '/', ':', ';', '?', '@', '[', '\\', ']', '^', '`', '{', '|', '}', '~'
    };

    /**
     * The regex used to escape meta characters.
     */
    private static final String META_CHAR_REGEX;

    /**
     * Builds up the {@link #META_CHAR_REGEX} string, making sure to regex-escape every character because it may be a
     * regex special character.
     */
    static
    {
        StringBuilder sb = new StringBuilder();
        sb.append("([");
        for (char metaChar : META_CHARS)
        {
            sb.append("\\").append(metaChar);
        }
        sb.append("])");

        META_CHAR_REGEX = sb.toString();
    }

    private JQueryUtils()
    {
        // prevent instantiation
    }

    /**
     * Escapes all JQuery meta-characters, namely: <code>!"#$%&'()*+,./:;?@[\]^`{|}~</code>, by prefixing with
     * <code>\\</code>.
     * <p/>
     * See <a href="http://api.jquery.com/category/selectors/">http://api.jquery.com/category/selectors/</a>.
     *
     * @param string a String to escape
     * @return a String with all meta-characters escaped
     */
    public static String escapeJQuery(String string)
    {
        return string.replaceAll(META_CHAR_REGEX, "\\\\\\\\$1");
    }
}
