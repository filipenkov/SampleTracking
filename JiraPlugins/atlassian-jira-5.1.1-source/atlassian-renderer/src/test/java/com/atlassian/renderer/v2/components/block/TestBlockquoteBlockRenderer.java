package com.atlassian.renderer.v2.components.block;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RenderedContentStore;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.SubRenderer;
import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import junit.framework.TestCase;

public class TestBlockquoteBlockRenderer extends TestCase
{
    protected BlockquoteBlockRenderer renderer;
    protected Mock mockSubRenderer;
    protected SubRenderer subRenderer;
    protected RenderContext renderContext;

    protected RenderedContentStore store;

    protected void setUp() throws Exception
    {
        super.setUp();

        renderer = new BlockquoteBlockRenderer();
        store = new RenderedContentStore();
        mockSubRenderer = new Mock(SubRenderer.class);
        subRenderer = (SubRenderer) mockSubRenderer.proxy();
        renderContext = new RenderContext();
    }

    public void testCantRender()
    {
        assertNull(render(""));
        assertNull(render("This is a blockquote"));
        assertNull(render("bq This is a blockquote"));
        assertNull(render("bt. This is a blockquote"));
    }

    public void testCanRender()
    {
        testRender("bq. This is a blockquote", "This is a blockquote");
        testRender("bq.This is a blockquote", "This is a blockquote");
        testRender("   bq.    This is a blockquote",  "This is a blockquote");
    }

    private void testRender(String wiki, String quotedText)
    {
        mockSubRenderer.expectAndReturn("render", C.args(C.eq(quotedText), C.eq(renderContext), C.eq(RenderMode.INLINE)), "Rendered Text");
        assertEquals("<blockquote><p>Rendered Text</p></blockquote>", render(wiki));
    }

    private String render(String line)
    {
        return renderer.renderNextBlock(line, null, renderContext, subRenderer);
    }
}
