package com.atlassian.renderer.v2.macro.basic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class used to extract the size out of CSS value (dropping the unit of measurement off)
 */
public class CssSizeValue
{
    private static final Pattern PATTERN = Pattern.compile("(\\d+)\\s*(px|pt|em)?");
    private final String raw;

    public CssSizeValue(String raw)
    {
        this.raw = raw;
    }

    /**
     * Returns the actual size excluding units of measurement. If a size could not be parsed, returns 0.
     * @return returns the actual size excluding units of measurement. If a size could not be parsed, returns 0.
     */
    public int value()
    {
        final Matcher matcher = PATTERN.matcher(raw);
        if (matcher.matches())
            return Integer.parseInt(matcher.group(1));
        else
            return 0;
    }

    /**
     * Returns true if a size value can be parsed. Value must be an number or have an optional unit suffix (px, pt, em)
     * @return returns true if a size value can be parsed. Value must be an number or have an optional unit suffix (px, pt, em)
     */
    public boolean isValid()
    {
        return PATTERN.matcher(raw).matches();
    }
}
