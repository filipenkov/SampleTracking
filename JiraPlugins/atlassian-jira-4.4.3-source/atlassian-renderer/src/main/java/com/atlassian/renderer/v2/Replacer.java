package com.atlassian.renderer.v2;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Helper class for performing repeated pattern replacement. Why you can't do this with just the JDK regex
 * classes, I don't know.
 */
public class Replacer
{
    private final Pattern pattern;
    private final String replacement;
    private final String[] necessaryConstantParts;

    /**
     * Replaces a pattern with a replacement. Provided with an array of necessary constant parts. The substitution won't
     * be attamepted if any of those are missing in the target String. This is a performance enhancement.
     *
     * @param pattern
     * @param replacement
     * @param necessaryConstantParts
     */
    public Replacer(Pattern pattern, String replacement, String[] necessaryConstantParts)
    {
        this.pattern = pattern;
        this.replacement = replacement;
        this.necessaryConstantParts = necessaryConstantParts;
    }

    public String replaceAll(String str)
    {
        for (int i = 0; i < necessaryConstantParts.length; ++i)
        {
            if (str.indexOf(necessaryConstantParts[i]) == -1)
            {
                return str;
            }
        }
        Matcher matcher = pattern.matcher(str);
        return matcher.replaceAll(replacement);
    }

    public String replace(String str)
    {
        Matcher matcher = pattern.matcher(str);
        return matcher.replaceAll(replacement);
    }
}
