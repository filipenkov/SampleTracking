package com.atlassian.renderer.v2.macro.basic.validator;

public class TestBorderStyleValidator extends AbstractTestValidator
{
    protected void setUp() throws Exception
    {
        super.setUp(new BorderStyleValidator());
    }

    public void testBorderStyle() throws Exception
    {
        assertValid("solid");
        assertInvalid(" solid; color: red; -moz-binding:");
        assertInvalid(" -moz-binding:");
    }
}
