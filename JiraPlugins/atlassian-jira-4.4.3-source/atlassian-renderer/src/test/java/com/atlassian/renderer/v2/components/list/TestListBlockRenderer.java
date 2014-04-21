package com.atlassian.renderer.v2.components.list;

import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.components.block.LineWalker;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.RenderedContentStore;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.C;
import junit.framework.TestCase;

public class TestListBlockRenderer extends TestCase
{
    protected ListBlockRenderer renderer;
    protected Mock mockSubRenderer;
    protected SubRenderer subRenderer;
    protected RenderContext context;

    protected RenderedContentStore store;

    protected void setUp() throws Exception
    {
        super.setUp();

        renderer = new ListBlockRenderer();
        store = new RenderedContentStore();
        mockSubRenderer = new Mock(SubRenderer.class);
        subRenderer = (SubRenderer) mockSubRenderer.proxy();
        context = new RenderContext();
    }

    public void testNoMatch()
    {
        String wiki = "";
        assertDoesntRender(wiki);
        assertDoesntRender("I like monkeys");
        assertDoesntRender("*Not a list");
        assertDoesntRender("   *Not a list");
        assertDoesntRender("*Not* a list!");
    }

    public void testNoRender()
    {
        context.pushRenderMode(RenderMode.suppress(RenderMode.F_LISTS));
        assertDoesntRender("* List item");
    }

    public void testSimple()
    {
        mockThreeLineList();
        testListRender("* Item One\n* Item Two\n* Item Three", "<ul>\n\t<li>Rendered Text</li>\n\t<li>Rendered Text</li>\n\t<li>Rendered Text</li>\n</ul>\n");
    }

    public void testNested()
    {
        mockThreeLineList();
        testListRender("* Item One\n** Item Two\n* Item Three", "<ul>\n\t<li>Rendered Text\n\t<ul>\n\t\t<li>Rendered Text</li>\n\t</ul>\n\t</li>\n\t<li>Rendered Text</li>\n</ul>\n");
    }

    public void testNestedSkippingDepths()
    {
        mockThreeLineList();
        testListRender("* Item One\n*** Item Two\n* Item Three", "<ul>\n\t<li>Rendered Text\n\t<ul>\n\t\t<li>\n\t\t<ul>\n\t\t\t<li>Rendered Text</li>\n\t\t</ul>\n\t\t</li>\n\t</ul>\n\t</li>\n\t<li>Rendered Text</li>\n</ul>\n");
    }

    public void testMultiTypedSameDepth()
    {
        mockThreeLineList();
        testListRender("* Item One\n# Item Two\n- Item Three", "<ul>\n\t<li>Rendered Text</li>\n</ul>\n" +
                        "<ol>\n\t<li>Rendered Text</li>\n</ol>\n" +
                        "<ul class=\"alternate\" type=\"square\">\n\t<li>Rendered Text</li>\n</ul>\n");
    }

    public void testMultiTypedNested()
    {
        mockThreeLineList();
        testListRender("*# Item One\n*# Item Two\n** Item Three",
                "<ul>\n\t<li>\n\t<ol>\n\t\t<li>Rendered Text</li>\n\t\t<li>Rendered Text</li>\n\t</ol>\n" +
            "\t<ul>\n\t\t<li>Rendered Text</li>\n\t</ul>\n\t</li>\n</ul>\n");
    }

    public void testNestedListsIgnoreUnmatchedPrefixes()
    {
        mockThreeLineList();
        testListRender("*# Item One\n## Item Two\n** Item Three",
                "<ul>\n\t<li>\n\t<ol>\n\t\t<li>Rendered Text</li>\n\t\t<li>Rendered Text</li>\n\t</ol>\n" +
            "\t<ul>\n\t\t<li>Rendered Text</li>\n\t</ul>\n\t</li>\n</ul>\n");
    }

    public void testLocateEndOfListAndLeaveNextLineCorrectly()
    {
        mockThreeLineList();
        String input = "* Item One\n* Item Two\n* Item Three\n\n* Item Four";
        LineWalker walker = new LineWalker(input);
        assertEquals("<ul>\n\t<li>Rendered Text</li>\n\t<li>Rendered Text</li>\n\t<li>Rendered Text</li>\n</ul>\n", renderer.renderNextBlock(walker.next(), walker, context, subRenderer));
        mockSubRenderer.verify();
        assertEquals("", walker.next());
        assertEquals("* Item Four", walker.next());
        assertFalse(walker.hasNext());
    }

    public void testMultiLineList()
    {
        mockSubRenderer.expectAndReturn("render", C.args(C.eq("Item One\nnext Line\nnext Line"), C.eq(context), C.eq(RenderMode.LIST_ITEM)), "Rendered Text");
        mockSubRenderer.expectAndReturn("render", C.args(C.eq("Item Two\nnext Line\nnext Line"), C.eq(context), C.eq(RenderMode.LIST_ITEM)), "Rendered Text");
        mockSubRenderer.expectAndReturn("render", C.args(C.eq("Item Three\nnext Line\nnext Line"), C.eq(context), C.eq(RenderMode.LIST_ITEM)), "Rendered Text");

        String input = "* Item One\nnext Line\nnext Line\n* Item Two\nnext Line\nnext Line\n* Item Three\nnext Line\nnext Line\n\n* Item Four";
        LineWalker walker = new LineWalker(input);
        assertEquals("<ul>\n\t<li>Rendered Text</li>\n\t<li>Rendered Text</li>\n\t<li>Rendered Text</li>\n</ul>\n", renderer.renderNextBlock(walker.next(), walker, context, subRenderer));
        mockSubRenderer.verify();
        assertEquals("", walker.next());
        assertEquals("* Item Four", walker.next());
        assertFalse(walker.hasNext());
    }

    // If the very first line of a list starts with -- or ---, we assume the user is trying to insert an em- or en- dash and
    // leave it alone. But subsequent lines of a list can use either
    public void testListCantStartWithDashEntity()
    {
        assertDoesntRender("--- by Charles Miller");
        assertDoesntRender("-- or else");
    }

    public void testMultiDashesWorkInsideList()
    {
        mockThreeLineList();
        String input = "- Item One\n-- Item Two\n--- Item Three";
        testListRender(input,
                "<ul class=\"alternate\" type=\"square\">\n\t<li>Rendered Text\n\t" +
                "<ul class=\"alternate\" type=\"square\">\n\t\t<li>Rendered Text\n\t\t" +
                "<ul class=\"alternate\" type=\"square\">\n\t\t\t<li>Rendered Text</li>\n" +
                "\t\t</ul>\n\t\t</li>\n\t</ul>\n\t</li>\n</ul>\n"
        );
    }

    private void mockThreeLineList()
    {
        mockSubRenderer.expectAndReturn("render", C.args(C.eq("Item One"), C.eq(context), C.eq(RenderMode.LIST_ITEM)), "Rendered Text");
        mockSubRenderer.expectAndReturn("render", C.args(C.eq("Item Two"), C.eq(context), C.eq(RenderMode.LIST_ITEM)), "Rendered Text");
        mockSubRenderer.expectAndReturn("render", C.args(C.eq("Item Three"), C.eq(context), C.eq(RenderMode.LIST_ITEM)), "Rendered Text");
    }

    private void testListRender(String input, String output)
    {
        LineWalker walker = new LineWalker(input);
        assertEquals(output, renderer.renderNextBlock(walker.next(), walker, context, subRenderer));
        mockSubRenderer.verify();
    }

    private void assertDoesntRender(String wiki)
    {
        LineWalker walker = new LineWalker("I like cheese");
        assertNull(renderer.renderNextBlock(wiki, walker, context, subRenderer));
        assertTrue("Walker hasn't been depleted", walker.hasNext());
        assertEquals("Walker is unmodified", "I like cheese", walker.next());
    }
}
