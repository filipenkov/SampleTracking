package com.atlassian.renderer.util;

/**
 * This is a simple utility class used to escape regular expressions
 */
public class RegExpUtil
{
    private static final String regExpKeywords = "\\!-?*+.^|:{}[]()~";

    public static String convertToRegularExpression(String str)
    {
        String pattern = "";

        for (int i = 0; i < str.length(); i++)
        {
            char ch = str.charAt(i);
            boolean alreadyEscaped = ( i>0 && str.charAt(i-1)=='\\' );

            if( !alreadyEscaped && regExpKeywords.indexOf(ch)!=-1 )
                pattern += "\\";

            pattern += ch;
        }

        return pattern;
    }

}
