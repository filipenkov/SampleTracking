package com.atlassian.renderer.v2.macro.basic.validator;

import com.atlassian.renderer.v2.macro.MacroException;

/**
 * Thrown when a macro parameter doesn't pass validation.
 */
public class MacroParameterValidationException extends MacroException
{
    public MacroParameterValidationException(String message)
    {
        super(message);
    }
}
