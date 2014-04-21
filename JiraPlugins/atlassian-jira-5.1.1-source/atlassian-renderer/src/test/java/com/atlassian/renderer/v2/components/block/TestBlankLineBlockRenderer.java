package com.atlassian.renderer.v2.components.block;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RenderedContentStore;
import com.atlassian.renderer.v2.SubRenderer;
import com.mockobjects.dynamic.Mock;
import junit.framework.TestCase;

public class TestBlankLineBlockRenderer extends TestCase
{
    protected BlankLineBlockRenderer renderer;
    protected Mock mockSubRenderer;
    protected SubRenderer subRenderer;
    protected RenderContext renderContext;

    protected RenderedContentStore store;

    protected void setUp() throws Exception
    {
        super.setUp();

        renderer = new BlankLineBlockRenderer();
        mockSubRenderer = new Mock(SubRenderer.class);
        subRenderer = (SubRenderer) mockSubRenderer.proxy();
        renderContext = new RenderContext();
    }

    public void testRender()
    {
        assertNull(render("a"));
        assertNull(render(" a "));
        assertNull(render("     This    "));
        assertNull(render("This isn't blank"));

        assertEquals("", render(""));
        assertEquals("", render("    "));
        assertEquals("", render(" \t \t \t \t "));
    }

    private String render(String line)
    {
        return renderer.renderNextBlock(line, null, renderContext, subRenderer);
    }
}
