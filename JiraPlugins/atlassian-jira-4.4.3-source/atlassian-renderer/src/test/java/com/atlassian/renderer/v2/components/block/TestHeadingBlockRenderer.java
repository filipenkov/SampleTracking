package com.atlassian.renderer.v2.components.block;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RenderedContentStore;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.SubRenderer;
import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import junit.framework.TestCase;

public class TestHeadingBlockRenderer extends TestCase
{
    protected HeadingBlockRenderer renderer;
    protected Mock mockSubRenderer;
    protected SubRenderer subRenderer;
    protected RenderContext renderContext;

    protected RenderedContentStore store;

    protected void setUp() throws Exception
    {
        super.setUp();

        renderer = new HeadingBlockRenderer();
        store = new RenderedContentStore();
        mockSubRenderer = new Mock(SubRenderer.class);
        subRenderer = (SubRenderer) mockSubRenderer.proxy();
        renderContext = new RenderContext();
        renderContext.setCharacterEncoding("UTF-8");
    }

    public void testCantRender()
    {
        assertNull(render(""));
        assertNull(render("h7. This is a heading"));
        assertNull(render("h1  This is a heading"));
        assertNull(render("eh1.This is a heading"));
        assertNull(render("e h1.This is a heading"));
    }

    public void testCanRender()
    {
        testRender("h1. This is a heading", "1", "This is a heading");
        testRender("   h1.    This is a heading", "1", "This is a heading");
        testRender("h1.This is a heading", "1", "This is a heading");
        testRender("h4. This is a heading", "4", "This is a heading");
        testRender("h6. This is a heading", "6", "This is a heading");
    }

    private void testRender(String wiki, String headingLevel, String headingText)
    {
        mockSubRenderer.expectAndReturn("render", C.args(C.eq(headingText), C.eq(renderContext), C.eq(RenderMode.INLINE)), "Rendered Heading");
        HeadingBlockRenderer hbr = new HeadingBlockRenderer();
        assertEquals("<h" + headingLevel + "><a name=\"" + hbr.getAnchor(renderContext, headingText) + "\"></a>Rendered Heading</h" + headingLevel + ">", render(wiki));
    }

    private String render(String line)
    {
        return renderer.renderNextBlock(line, null, renderContext, subRenderer);
    }
}
