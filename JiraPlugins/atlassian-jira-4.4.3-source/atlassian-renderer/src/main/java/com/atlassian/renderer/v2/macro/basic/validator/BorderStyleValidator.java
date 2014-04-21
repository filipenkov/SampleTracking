package com.atlassian.renderer.v2.macro.basic.validator;

import org.apache.commons.lang.StringUtils;

import java.util.Set;
import java.util.HashSet;

/**
 * Validates that a parameter is a valid CSS2 border-style property value.
 */
public class BorderStyleValidator implements ParameterValidator
{
    private static final Set VALID_VALUES = createBorderStyleValues();

    public void assertValid(String propertyValue) throws MacroParameterValidationException
    {
        if (StringUtils.isBlank(propertyValue)) return;

        if (!VALID_VALUES.contains(propertyValue))
            throw new MacroParameterValidationException("Border style is not a valid CSS2 border-style value");
    }

    private static Set createBorderStyleValues()
    {
        Set strings = new HashSet();
        strings.add("none");
        strings.add("hidden");
        strings.add("dotted");
        strings.add("dashed");
        strings.add("solid");
        strings.add("double");
        strings.add("groove");
        strings.add("ridge");
        strings.add("inset");
        strings.add("outset");
        return strings;
    }
}
