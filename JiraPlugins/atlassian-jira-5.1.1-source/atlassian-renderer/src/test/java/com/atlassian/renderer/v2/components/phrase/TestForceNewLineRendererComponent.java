package com.atlassian.renderer.v2.components.phrase;

import com.atlassian.renderer.v2.components.AbstractRendererComponentTest;
import com.atlassian.renderer.v2.RenderMode;

public class TestForceNewLineRendererComponent extends AbstractRendererComponentTest
{
    private static final String BREAK = "<br clear=\"all\" />";
    protected void setUp() throws Exception
    {
        super.setUp();
        component = new ForceNewLineRendererComponent();
    }

    protected long getRequiredRenderModeFlags()
    {
        return RenderMode.F_LINEBREAKS;
    }

    public void testNewLine()
    {
        testBasicRender("\\\\", BREAK);
        testBasicRender("foo\\\\bar", "foo" + BREAK + "bar");
        testBasicRender("foo \\\\ bar", "foo " + BREAK + " bar");
    }

    public void testTooManyBackslashes()
    {
        testBasicRender("foo\\\\\\bar", "foo\\\\\\bar");
    }

    public void testGuessUncPathMadness()
    {
        testBasicRender("\\\\foo\\bar\\baz", "\\\\foo\\bar\\baz");
        testBasicRender("Look at \\\\foo\\bar\\baz", "Look at \\\\foo\\bar\\baz");
    }
}
