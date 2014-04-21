package com.atlassian.renderer.v2.macro.basic.validator;

import com.atlassian.renderer.v2.macro.basic.validator.MacroParameterValidationException;

import java.util.Map;
import java.util.HashMap;

/**
 * Wraps a parameter map of String -&gt; String mappings, providing validation by means of
 * a mapping of parameters to {@link ParameterValidator}s.
 */
public class ValidatedMacroParameters
{
    private final Map/*<String, String>*/ parameters;
    private final Map/*<String, ParameterValidator>*/ validators = new HashMap();

    public ValidatedMacroParameters(Map parameters)
    {
        this.parameters = parameters;
    }

    public void setValidator(String parameterName, ParameterValidator parameterValidator)
    {
        validators.put(parameterName, parameterValidator);
    }

    /**
     * Returns the value of the parameter from the underlying parameter map, or throws an exception
     * if the value does not pass the validation for that parameter configured by
     * {@link #setValidator(String,ParameterValidator)}. A parameter which does not have a validator configured
     * will return the value without validating it.
     *
     * @param parameterName the name of the parameter to retrieve
     * @return the parameter value, if it is valid or there is no validator for the parameter, or null if the parameter
     * is not specified.
     * @throws MacroParameterValidationException if the parameter is not valid
     */
    public String getValue(String parameterName) throws MacroParameterValidationException
    {
        String parameterValue = (String) parameters.get(parameterName);
        if (parameterValue == null)
        {
            return null;
        }

        ParameterValidator validator = (ParameterValidator) validators.get(parameterName);
        if (validator == null)
        {
            // if no validator, pass back unvalidated value
            return parameterValue;
        }

        validator.assertValid(parameterValue);
        return parameterValue;
    }
}
