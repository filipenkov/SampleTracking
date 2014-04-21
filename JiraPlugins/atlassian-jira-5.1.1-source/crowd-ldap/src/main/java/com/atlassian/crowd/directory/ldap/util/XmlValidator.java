package com.atlassian.crowd.directory.ldap.util;

public class XmlValidator
{
    /**
     * Verifies whether a string consists solely of valid XML characters.
     * @return false if there are any invalid XML characters in the string.
     */
    public static boolean isSafe(String s)
    {
        char[] chars = s.toCharArray();
        
        /* Iterate over characters, allowing for surrogates */
        int i = 0;
        while (i < chars.length)
        {
            int c = Character.codePointAt(chars, i);
            
            if (!isXmlCharacter(c))
            {
                return false;
            }
            
            i += Character.charCount(c);
        }
        return true;
    }

    /**
     * Recognise valid XML characters as defined by
     * <a href='http://www.w3.org/TR/REC-xml/#charsets'>Extensible Markup Language, section 2.2</a>.
     * 
     * @param c a single character
     * @return
     */
    public static boolean isXmlCharacter(int c)
    {
        return (c == 0x09 || c == 0x0A || c == 0x0D
                || (c >= 0x20 && c <= 0xD7FF)
                || (c >= 0xE000 && c <= 0xFFFD)
                || (c >= 0x10000 && c <= 0x10FFFF));
    }
}
