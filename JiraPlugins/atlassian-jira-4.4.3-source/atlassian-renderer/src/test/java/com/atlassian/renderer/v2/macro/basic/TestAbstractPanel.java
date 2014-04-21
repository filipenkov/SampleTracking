package com.atlassian.renderer.v2.macro.basic;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.macro.basic.validator.MacroParameterValidationException;
import com.atlassian.renderer.v2.V2SubRenderer;
import com.atlassian.renderer.v2.Renderer;

import java.util.Map;
import java.util.HashMap;

import junit.framework.TestCase;

public class TestAbstractPanel extends TestCase
{

    public void testSimpleAbstractPanelWithoutErrors() throws MacroException
    {
        AbstractPanelMacro macro = new FooMacro();

        Map params = new HashMap();
        params.put("borderStyle", "hidden");
        String out = macro.execute(params, "foo", new RenderContext());
        assertEquals("<div class=\"foo\" style=\"border-style: hidden;border-width: 1px;\"><div class=\"foobar\">\n" +
            "foo\n</div></div>", out);
    }

    public void testSimpleAbstractPanelWithBorderStyleErrors() throws Exception
    {
        AbstractPanelMacro macro = new FooMacro();

        Map params = new HashMap();
        params.put("borderStyle", "anInvalidValue");
        try
        {
            macro.execute(params, "foo", new RenderContext());
            fail("No exception thrown");
        }
        catch (MacroParameterValidationException e)
        {
            // expected exception
        }
    }


    public void testSimpleAbstractPanelMacroWithBgColorError() throws Exception
    {
        AbstractPanelMacro macro = new FooMacro();

        Map params = new HashMap();
        params.put("bgColor", "anInvalidValue2");
        try
        {
            macro.execute(params, "foo", new RenderContext());
            fail("No exception thrown");
        }
        catch (MacroParameterValidationException e)
        {
            // expected exception
        }
    }

    private static class FooMacro extends AbstractPanelMacro
    {

        {
            setSubRenderer(new V2SubRenderer(new Renderer()
            {
                public String render(String originalContent, RenderContext renderContext)
                {
                    return originalContent;
                }

                public String renderAsText(String originalContent, RenderContext context)
                {
                    return originalContent;
                }

                public String getRendererType()
                {
                    return "test";
                }
            }));
        }

        protected String getPanelCSSClass()
        {
            return "foo";
        }

        protected String getPanelHeaderCSSClass()
        {
            return "bar";
        }

        protected String getPanelContentCSSClass()
        {
            return "foobar";
        }
    }
}
