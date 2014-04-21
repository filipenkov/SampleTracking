package com.atlassian.renderer.v2.components;

import java.util.regex.Pattern;

public final class HtmlEscaper
{
    private static final Pattern ENTITY_PATTERN = Pattern.compile("^&[#A-Za-z0-9][A-Za-z0-9]{1,7};");
    private static final CharMap ESCAPE_ALL_CHAR_MAP = new CharMap()
    {
        public String get(char c)
        {
            if (c == '\'')
                return "&#39;";
            if (c == '"')
                return "&quot;";
            return ESCAPE_ALL_EXCEPT_QUOTES_CHAR_MAP.get(c);
        }
    };
    private static final CharMap ESCAPE_ALL_EXCEPT_QUOTES_CHAR_MAP = new CharMap()
    {
        public String get(char c)
        {
            switch (c)
            {
                case '&':
                    return "&amp;";
                case '>':
                    return "&gt;";
                case '<':
                    return "&lt;";
                case 145: // Microsoft broken smart quote
                    return "&lsquo;";
                case 146: // Microsoft broken smart quote
                    return "&rsquo;";
                case 147: // Microsoft broken smart quote
                    return "&ldquo;";
                case 148: // Microsoft broken smart quote
                    return "&rdquo;";
                default:
                    return Character.toString(c);
            }
        }
    };
    private static final CharMap ESCAPE_AMPERSAND_CHAR_MAP = new CharMap()
    {
        public String get(char c)
        {
            if (c == '&') return "&amp;";
            return Character.toString(c);
        }
    };

    private HtmlEscaper()
    {
    }

    /**
     * Replaces the HTML "special characters" &lt;, &gt;, &quot;, ', and &amp; with their equivalent entities in HTML 4
     * and returns the result. Also replaces the Microsoft "smart quotes" characters (extended ASCII 145-148) with their
     * equivalent HTML entities.
     * <p/>
     * Passing <tt>true</tt> for preserveExistingEntities will try to not break existing entities already found in the
     * input string, by avoiding escaping ampersands which form part of an existing entity like &amp;lt;. Passing
     * <tt>false</tt> will do the normal behaviour of escaping everything.
     *
     * @param s the String to escape
     * @param preserveExistingEntities if <tt>true</tt>, will avoid escaping the ampersand in an existing entity like
     * &amp;lt;. If false, the method will do a normal escaping by replace all matched characters.
     * @return the string with special characters replaced by entities.
     */
    public static String escapeAll(String s, boolean preserveExistingEntities)
    {
        return doReplacement(s, preserveExistingEntities, ESCAPE_ALL_CHAR_MAP);
    }

    /**
     * Does the same as {@link #escapeAll(String,boolean)}, except doesn't replace the quotation mark characters &quot;
     * and '.
     *
     * @param s the String to escape
     * @param preserveExistingEntities if <tt>true</tt>, will avoid escaping the ampersand in an existing entity like
     * &amp;lt;. If false, the method will do a normal escaping by replace all matched characters.
     * @return the string with special characters replaced by entities.
     */
    public static String escapeAllExceptQuotes(String s, boolean preserveExistingEntities)
    {
        return doReplacement(s, preserveExistingEntities, ESCAPE_ALL_EXCEPT_QUOTES_CHAR_MAP);
    }

    /**
     * Replaces the HTML "special character" &amp; with its equivalent entity in HTML 4 and returns the result.
     * <p/>
     * Passing <tt>true</tt> for preserveExistingEntities will try to not break existing entities already found in the
     * input string, by avoiding escaping ampersands which form part of an existing entity like &amp;lt;. Passing
     * <tt>false</tt> will do the normal behaviour of escaping everything.
     *
     * @param s the String to escape
     * @param preserveExistingEntities if <tt>true</tt>, will avoid escaping the ampersand in an existing entity like
     * &amp;lt;. If false, the method will do a normal escaping by replace all matched characters.
     * @return the string with special characters replaced by entities.
     */
    public static String escapeAmpersands(String s, boolean preserveExistingEntities)
    {
        return doReplacement(s, preserveExistingEntities, ESCAPE_AMPERSAND_CHAR_MAP);
    }

    private static String doReplacement(String s, boolean preserveExistingEntities, CharMap charMap)
    {
        if (s == null)
            return null;
        StringBuffer out = new StringBuffer(s.length() + 50);
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if (c == '&' && preserveExistingEntities && entityAt(s, i))
                out.append(c);
            else
                out.append(charMap.get(c));
        }
        return out.toString();
    }

    private static boolean entityAt(String s, int startIndex)
    {
        String substring = s.substring(startIndex);
        return ENTITY_PATTERN.matcher(substring).find();
    }

    private interface CharMap
    {
        String get(char c);
    }
}
