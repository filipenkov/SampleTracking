package com.atlassian.renderer.v2.macro.basic.validator;

/**
 * Validates a user supplied property that may be used in "style" attributes, leading to Cross Site Scripting
 * vulnerabilities.
 */
public interface ParameterValidator
{
    /**
     * Throws a MacroParameterValidationException if the property value is invalid. Implementations should check for
     * null and handle it appropriately appropriately.
     *
     * @param parameterValue the property value to validate
     * @throws MacroParameterValidationException if the property value is invalid
     */
    void assertValid(String parameterValue) throws MacroParameterValidationException;
}
