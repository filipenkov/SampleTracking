package com.atlassian.renderer.v2.macro.basic;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.macro.basic.validator.MacroParameterValidationException;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class TestColorMacro extends TestCase
{

    public void testColorMacroWithInvalidColor() throws Exception
    {
        ColorMacro macro = new ColorMacro();
        Map params = new HashMap();
        params.put("0","red\"><script>alert('not good')</script><br x=\"");
        try
        {
            macro.execute(params, "foo", new RenderContext());
            fail("No exception thrown");
        }
        catch (MacroParameterValidationException e)
        {
            // ignore expected exception
        }
    }

    public void testColorMacroWithValidColor() throws MacroException
    {
        ColorMacro macro = new ColorMacro();
        Map params = new HashMap();
        params.put("0","red");
        assertEquals("<font color=\"red\">foo</font>", macro.execute(params, "foo", new RenderContext()));
    }
}
