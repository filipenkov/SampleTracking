/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Nov 15, 2004
 * Time: 8:53:03 AM
 */
package com.atlassian.renderer.v2.components.table;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RenderedContentStore;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.components.block.LineWalker;
import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import junit.framework.TestCase;

public class TestTableRendererComponent extends TestCase
{
    protected TableBlockRenderer renderer;
    protected Mock mockSubRenderer;
    protected SubRenderer subRenderer;
    protected RenderContext context;

    protected RenderedContentStore store;

    protected void setUp() throws Exception
    {
        super.setUp();

        renderer = new TableBlockRenderer();
        store = new RenderedContentStore();
        mockSubRenderer = new Mock(SubRenderer.class);
        subRenderer = (SubRenderer) mockSubRenderer.proxy();
        context = new RenderContext();
    }

    public void testShouldRender()
    {
        context.pushRenderMode(RenderMode.suppress(RenderMode.F_TABLES));
        assertNotTable("| one | two | three |");
    }

    public void testNoRender()
    {
        assertNotTable("");
        assertNotTable("I like monkeys");
        assertNotTable("one | two | three |");
    }

    private void assertNotTable(String wiki)
    {
        assertNull(renderer.renderNextBlock(wiki, null, context, subRenderer));
    }

    public void testSingleCell()
    {
        mockSubRenderer.expectAndReturn("render", C.args(C.eq("|one cell|"), C.eq(context), C.eq(RenderMode.allow(RenderMode.F_LINKS | RenderMode.F_IMAGES | RenderMode.F_MACROS | RenderMode.F_TEMPLATE))), "prerender");
        mockSubRenderer.expectAndReturn("render", C.args(C.eq("prerender"), C.eq(context), C.eq(RenderMode.TABLE_CELL)), "Rendered Text");
        LineWalker walker = new LineWalker("|one cell|");
        assertEquals("<table class='confluenceTable'><tbody>\n<tr>\n<td class='confluenceTd'>Rendered Text</td>\n</tr>\n</tbody></table>", renderer.renderNextBlock(walker.next(), walker, context, subRenderer));
        mockSubRenderer.verify();
    }
}