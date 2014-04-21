package com.atlassian.core.util;

import com.opensymphony.util.TextUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.io.UnsupportedEncodingException;

public class StringUtils
{
    private static int MAX_LENGTH = 9000;
    protected static Map stringCharMappings = new HashMap(4);   // this may be a race condition due to no synchronisation.  However, it hasn't yet been a problem


    private static char[][] getMappings(String encoding)
    {
        char[][] stringChars = (char[][]) stringCharMappings.get(encoding);

        if (stringChars == null)
        {
            stringChars = new char[MAX_LENGTH][];
            if ("UTF-8".equalsIgnoreCase(encoding) ||
                    "Big5".equalsIgnoreCase(encoding) ||
                    "Windows-1252".equalsIgnoreCase(encoding))
            {
                // FIXME: These characters are valid in utf-8
                addMapping(8216, "'", stringChars);
                addMapping(8217, "'", stringChars);
                addMapping(8220, "\"", stringChars);
                addMapping(8221, "\"", stringChars);
                addMapping(8230, "...", stringChars);
                addMapping(8211, "-", stringChars);
                addMapping(183, "- ", stringChars);  // replace bullets
            } else if ("ISO-8859-1".equalsIgnoreCase(encoding))
            {
                addMapping(145, "'", stringChars);
                addMapping(146, "'", stringChars);
                addMapping(147, "\"", stringChars);
                addMapping(148, "\"", stringChars);
                addMapping(133, "...", stringChars);
                addMapping(150, "-", stringChars);
                addMapping(183, "- ", stringChars);  // replace bullets
            }
            // unicode control characters should be chopped off
            for (int i = 0; i < 32; i++)
            {
                if (i == 9 || i == 10 || i == 13)
                {
                    continue; // 9, 10, 13 are line feed and carriage return chars
                } else
                {
                    addMapping(i, "", stringChars);
                }
            }

            stringCharMappings.put(encoding, stringChars);
        }

        return stringChars;
    }

    private static void addMapping(int charsNumericValue, String replaceStr, char[][] mappings)
    {
        mappings[charsNumericValue] = replaceStr.toCharArray();
    }

    /**
     * replaces "smart quotes" and other problematic characters that appear in JIRA when data is cut and pasted
     * from a Microsoft word document. <p>
     * These include smart single and double quotes, ellipses, em-dashes and bullets
     * (these characters belong to the Windows Code Page 1252 encoding)
     *
     * @param s        string to simplify
     * @param encoding eg. UTF-8, Big5, ISO-8859-1 etc.
     * @return
     */
    public static final String escapeCP1252(String s, String encoding)
    {
        if (s == null)
            return null;

        int len = s.length();
        if (len == 0)
            return s;

        // if extended empty string just return it
        String trimmed = s.trim();
        if (trimmed.length() == 0 || ("\"\"").equals(trimmed))
            return trimmed;

        // initialise the Mapping before encoding
        char[][] stringChars = getMappings(encoding);

        int i = 0;
        // First loop through String and check if escaping is needed at all
        // No buffers are copied at this time
        do
        {
            int index = s.charAt(i);
            if (index >= MAX_LENGTH)
                continue;
            if (stringChars[index] != null)
            {
                break;
            }
        }
        while (++i < len);

        // If the check went to the end with no escaping then i should be == len now
        // otherwise we must continue escaping for real
        if (i == len)
        {
            return s;
        }

        // We found a character to escape and broke out at position i
        // Now copy all characters before that to StringBuffer sb
        // Since a char[] will be used for copying we might as well get
        // a complete copy of it so that we can use array indexing instead of charAt
        StringBuffer sb = new StringBuffer(len + 40);
        char[] chars = new char[len];
        // Copy all chars from the String s to the chars buffer
        s.getChars(0, len, chars, 0);
        // Append the first i characters that we have checked to the resulting StringBuffer
        sb.append(chars, 0, i);
        int last = i;
        char[] subst = null;
        for (; i < len; i++)
        {
            char c = chars[i];
            int index = c;

            if (index < stringChars.length)
                subst = stringChars[index];
            else
                subst = null;

            // It is faster to append a char[] than a String which is why we use this
            if (subst != null)
            {
                if (i > last)
                    sb.append(chars, last, i - last);
                sb.append(subst);
                last = i + 1;
            }
        }
        if (i > last)
        {
            sb.append(chars, last, i - last);
        }
        return sb.toString();
    }


    /**
     * Crop a string if it is longer than a certain length, adding the specified suffix.
     * <p/>
     * If the string is shorter than the cropAt length, then it is returned unchanged.
     */
    public static String crop(String original, int cropAt, String suffix)
    {
        if (original == null)
            return null;

        if (original.length() > cropAt)
        {
            original = original.substring(0, cropAt) + suffix;
        }
        return original;
    }

    /**
     * Tests to see is a string contains any of the string in the list passed
     */
    public static boolean contains(String value, List possiblyContains)
    {
        if (value == null)
        {
            if ((possiblyContains == null || possiblyContains.isEmpty()))
                return true;
            else
                return false;
        } else
        {
            if ((possiblyContains == null || possiblyContains.isEmpty()))
                return false;

            for (int i = 0; i < possiblyContains.size(); i++)
            {
                if (value.indexOf((String) possiblyContains.get(i)) > -1)
                    return true;
            }
            return false;
        }
    }

    /**
     * Replaces all occurrences of one string with another.
     */
    public static String replaceAll(final String str, final String oldPattern, final String newPattern)
    {
        if (str == null) return null;
        if (oldPattern == null || oldPattern.equals("")) return str;
        String remainder = str;
        StringBuffer buf = new StringBuffer(str.length() * 2);
        while (true)
        {
            int i = remainder.indexOf(oldPattern);
            if (i != -1)
            {
                buf.append(remainder.substring(0, i));
                buf.append(newPattern);
                remainder = remainder.substring(i + oldPattern.length());
            } else
            {
                buf.append(remainder);
                break;
            }
        }
        return buf.toString();
    }

    /**
     * Tests if all the characters in the string is an ASCII character
     */
    public static boolean isStringAllASCII(String str)
    {
        if (str == null)
            return true;

        for (int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);
            if (c < 0 || c > 127)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all the characters in the string are from the specified character set
     */
    public static boolean isStringOfCharSet(String string, String charset)
    {
        if (string == null)
            return true;

        try
        {
            return (string.equals(new String(string.getBytes(charset), charset)));
        }
        catch (UnsupportedEncodingException e)
        {
        }
        return false;
    }

    public static boolean isStringISO_8859_1(String string)
    {
        return isStringOfCharSet(string, "ISO-8859-1");
    }

    public static boolean equalsIgnoreLineTerminators(String s1, String s2)
    {
        String normalisedValue = normalise(TextUtils.noNull(s1));
        String normalisedCurrentValue = normalise(TextUtils.noNull(s2));

        return normalisedValue.equals(normalisedCurrentValue);
    }

    public static String normalise(String value)
    {
        // Replace all instances of '\r\n' with '\n'
        String normalised = replaceAll(value, "\r\n", "\n");
        // As all '\r\n' combinations have been replaced, the only thing that should be left are single '\r' characters (if any)
        // Replace these with '\n'
        return replaceAll(normalised, "\r", "\n");
    }

    /**
     * Method will turn a String of comma seperated entities into a String Array.
     * Spaces before or after the comma will be cropped.
     *
     * @param entryString A comma seperated String
     * @return String Array
     */
    public static String[] splitCommaSeparatedString(String entryString)
    {
        Pattern commaPattern = Pattern.compile(",");
        String[] parsed = commaPattern.split(entryString);
        for (int i = 0; i < parsed.length; i++)
        {
            String s = parsed[i];
            s = s.trim();
        }
        return parsed;
    }

    /**
     * Create a String of comma seperated entries from a Collection.
     *
     * @param entries A collection of entries
     * @return Comma seperated String
     */
    public static String createCommaSeperatedString(Collection entries)
    {
        if (entries != null)
        {
            StringBuffer sb = new StringBuffer();
            for (Iterator iterator = entries.iterator(); iterator.hasNext();)
            {
                String groupName = (String) iterator.next();
                sb.append(groupName);
                if (iterator.hasNext())
                    sb.append(",");
            }
            return sb.toString();
        } else return null;
    }
}
