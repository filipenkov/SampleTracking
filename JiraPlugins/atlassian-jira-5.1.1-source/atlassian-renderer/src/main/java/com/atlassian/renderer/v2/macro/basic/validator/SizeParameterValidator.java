package com.atlassian.renderer.v2.macro.basic.validator;

import com.atlassian.renderer.v2.macro.basic.CssSizeValue;
import org.apache.commons.lang.StringUtils;

public class SizeParameterValidator implements ParameterValidator
{
    public void assertValid(String propertyValue) throws MacroParameterValidationException
    {
        if (StringUtils.isBlank(propertyValue)) return;

        CssSizeValue sizeValue = new CssSizeValue(propertyValue);

        if (!sizeValue.isValid())
            throw new MacroParameterValidationException("Size parameter must be a number (optionally followed by 'px', 'pt' or 'em').");
    }
}
