package com.atlassian.renderer.v2.components.block;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.SubRenderer;
import com.mockobjects.dynamic.Mock;
import junit.framework.TestCase;

public class TestHorizontalRuleBlockRenderer extends TestCase
{
    protected HorizontalRuleBlockRenderer renderer;
    protected Mock mockSubRenderer;
    protected SubRenderer subRenderer;
    protected RenderContext renderContext;

    protected void setUp() throws Exception
    {
        super.setUp();
        renderer = new HorizontalRuleBlockRenderer();
    }

    public void testCantRender()
    {
        assertNull(render(""));
        assertNull(render("This is a line"));
        assertNull(render("Hi ----"));
        assertNull(render("---- Hi"));
        assertNull(render("--t--"));
        assertNull(render("----t"));
        assertNull(render("Hi ---- Hi"));
        assertNull(render("---"));
        assertNull(render("------"));
    }

    public void testCanRender()
    {
        testRender("----");
        testRender("-----");
        testRender("  -----");
        testRender("\t-----");
        testRender("-----  ");
        testRender("  ----   ");
    }

    private void testRender(String wiki)
    {
        assertEquals("<hr />", render(wiki));
    }

    private String render(String line)
    {
        return renderer.renderNextBlock(line, null, renderContext, subRenderer);
    }
}
