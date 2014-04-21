package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.RenderContext;

public class TestHtmlEscapeRendererComponent extends AbstractRendererComponentTest
{
    protected void setUp() throws Exception
    {
        super.setUp();
        component = new HtmlEscapeRendererComponent();
    }

    protected RenderMode getTestCaseRenderMode()
    {
        return RenderMode.allow(RenderMode.F_HTMLESCAPE | RenderMode.F_PRESERVE_ENTITIES);
    }

    protected long getRequiredRenderModeFlags()
    {
        return RenderMode.F_HTMLESCAPE;
    }

    public void testEscapes()
    {
        testBasicRender(">", "&gt;");
        testBasicRender("<", "&lt;");
        testBasicRender("&", "&amp;");
    }

    public void testNonEscapes()
    {
        testBasicRender("&amp;", "&amp;");
        testBasicRender("&dagger;", "&dagger;");
        testBasicRender("&#1234;", "&#1234;");
    }

    public void testNoEntityPreserving()
    {
        renderContext.pushRenderMode(RenderMode.suppress(RenderMode.F_PRESERVE_ENTITIES));
        testBasicRender("&amp;", "&amp;amp;");
        testBasicRender("&dagger;", "&amp;dagger;");
        testBasicRender("&#1234;", "&amp;#1234;");
    }

    protected void testBasicRender(String wikiText, String expected)
    {
        super.testBasicRender(wikiText, expected);
        super.testBasicRender("foo" + wikiText + "bar", "foo" + expected + "bar");
        super.testBasicRender("foo " + wikiText + " bar", "foo " + expected + " bar");
    }
}
