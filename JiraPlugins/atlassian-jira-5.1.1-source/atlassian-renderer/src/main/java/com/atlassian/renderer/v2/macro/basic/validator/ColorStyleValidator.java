package com.atlassian.renderer.v2.macro.basic.validator;

import org.apache.commons.lang.StringUtils;

import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * Validates that a parameter is a valid CSS2 color property value.
 */
public class ColorStyleValidator implements ParameterValidator
{
    public static final Set VALID_COLORS = createColorValues();

    public static final Pattern HEX_SHORT_COLOR_PATTERN = Pattern.compile("^#([\\da-fA-F]){3}$");
    public static final Pattern HEX_LONG_COLOR_MATCH = Pattern.compile("^#([\\da-fA-F]){6}$");
    public static final Pattern RGB_COLOR_PATTERN = Pattern.compile("^#rgb\\((\\d+),\\s*(\\d+),\\s*(\\d+)\\)$");
    public static final Pattern PERC_COLOR_PATTERN = Pattern.compile("^#rgb\\((\\d+)%,\\s*(\\d+)%,\\s*(\\d+)%\\)$");

    public void assertValid(String propertyValue) throws MacroParameterValidationException
    {
        if (StringUtils.isBlank(propertyValue))
        {
            return;
        }

        if (VALID_COLORS.contains(propertyValue)
            || HEX_SHORT_COLOR_PATTERN.matcher(propertyValue).matches()
            || HEX_LONG_COLOR_MATCH.matcher(propertyValue).matches()
            || RGB_COLOR_PATTERN.matcher(propertyValue).matches()
            || PERC_COLOR_PATTERN.matcher(propertyValue).matches())
        {
            return;
        }

        throw new MacroParameterValidationException("Color value is not a valid CSS2 color");
    }

    private static Set createColorValues()
    {
        Set strings = new HashSet();
        strings.add("aqua");
        strings.add("black");
        strings.add("blue");
        strings.add("fuchsia");
        strings.add("gray");
        strings.add("green");
        strings.add("lime");
        strings.add("maroon");
        strings.add("navy");
        strings.add("olive");
        strings.add("orange");
        strings.add("purple");
        strings.add("red");
        strings.add("silver");
        strings.add("teal;");
        strings.add("white");
        strings.add("yellow");
        return strings;
    }
}
