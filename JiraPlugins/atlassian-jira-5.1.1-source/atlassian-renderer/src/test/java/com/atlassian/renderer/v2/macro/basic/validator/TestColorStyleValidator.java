package com.atlassian.renderer.v2.macro.basic.validator;

public class TestColorStyleValidator extends AbstractTestValidator
{
    protected void setUp() throws Exception
    {
        super.setUp(new ColorStyleValidator());
    }

    public void testValidValues() throws Exception
    {
        assertValid("red");
        assertValid("blue");
        assertValid("#FFF");
        assertValid("#FFFFFF");

        assertValid("#rgb(255,255,255)");
        assertValid("#rgb(1,2,3)");
        assertValid("#rgb(1%,2%,3%)");
    }

    public void testInvalidValues() throws Exception
    {
        assertInvalid("solid -moz-binding:URL");
        assertInvalid("#FFFGGEE");
    }
}
