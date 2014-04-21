package com.atlassian.renderer.v2.macro.basic.validator;

import junit.framework.TestCase;

public abstract class AbstractTestValidator extends TestCase
{
    ParameterValidator validator;

    protected void setUp(ParameterValidator validator) throws Exception
    {
        super.setUp();
        this.validator = validator;
    }

    protected void assertInvalid(String value)
    {
        try
        {
            validator.assertValid(value);
            fail("Expected validation exception was not thrown");
        }
        catch (MacroParameterValidationException e)
        {
            // ignore expected exception
        }
    }

    protected void assertValid(String value) throws MacroParameterValidationException
    {
        validator.assertValid(value);
    }

}
