package com.atlassian.crowd.plugin.rest.exception.mapper;

import org.apache.commons.lang.StringUtils;

public final class ExceptionMapperUtil
{
    private ExceptionMapperUtil()
    {
    }

    /**
     * Strips out characters which cannot be legally represented in XML document.
     *
     * Following is excerpted from Wikipedia:-
     *
     * Unicode code points in the following ranges are valid in XML 1.0 documents:[9]
     * U+0009, U+000A, U+000D: these are the only C0 controls accepted in XML 1.0;
     * U+0020–U+D7FF, U+E000–U+FFFD: this excludes some (not all) non-characters in the BMP (all surrogates, U+FFFE and U+FFFF are forbidden);
     * U+10000–U+10FFFF: this includes all code points in supplementary planes, including non-characters.
     *
     * @param in input string.
     * @return cleansed string.
     */
    public static String stripNonValidXMLCharacters(String in)
    {
        if (StringUtils.isEmpty(in))
        {
            return in;
        }

        final StringBuilder out = new StringBuilder();
        char[] chars = in.toCharArray();
        int i = 0;

        while (i < chars.length)
        {
            int current = Character.codePointAt(chars, i);

            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF)))
            {
                out.appendCodePoint(current);
            }

            i+= Character.charCount(current);
        }

        return out.toString();
    }
}