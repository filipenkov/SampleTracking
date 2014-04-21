/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Nov 14, 2004
 * Time: 1:42:22 PM
 */
package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.links.LinkRenderer;
import com.atlassian.renderer.links.LinkResolver;
import com.atlassian.renderer.links.UnpermittedLink;
import com.atlassian.renderer.v2.MockLink;
import com.atlassian.renderer.v2.RenderMode;
import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;

public class TestLinkRendererComponent extends AbstractRendererComponentTest
{
    private Mock mockLinkResolver;
    private Mock mockLinkRenderer;

    protected void setUp() throws Exception
    {
        super.setUp();
        mockLinkResolver = new Mock(LinkResolver.class);
        mockLinkRenderer = new Mock(LinkRenderer.class);

        renderContext = new RenderContext();
        renderContext.setLinkRenderer((LinkRenderer) mockLinkRenderer.proxy());
        component = new LinkRendererComponent((LinkResolver) mockLinkResolver.proxy());
    }

    protected long getRequiredRenderModeFlags()
    {
        return RenderMode.F_LINKS;
    }

    public void testSimpleLinks()
    {
        assertLinkWorked("bob [and] mary", "bob <a href=\"http://example.com\">and</a> mary", "and", "<a href=\"http://example.com\">and</a>");
    }

    public void testMultipleLinks()
    {
        assertLinkWorked("bob [and] [more] mary", "bob 1 2 mary", "and", "1", "more", "2");
    }

    public void testLinkPlacementInText()
    {
        assertLinkWorked("[and] mary", "<a href=\"http://example.com\">and</a> mary", "and", "<a href=\"http://example.com\">and</a>");
        assertLinkWorked("[and]", "<a href=\"http://example.com\">and</a>", "and", "<a href=\"http://example.com\">and</a>");
        assertLinkWorked("bob [and]", "bob <a href=\"http://example.com\">and</a>", "and", "<a href=\"http://example.com\">and</a>");
        assertLinkWorked("bob\n[and]\nmary", "bob\n<a href=\"http://example.com\">and</a>\nmary", "and", "<a href=\"http://example.com\">and</a>");
    }

    public void testNoLinks()
    {
        assertLinkWorked("just text", "just text", null, null);
        assertLinkWorked("text [and", "text [and", null, null);
        assertLinkWorked("text ]and", "text ]and", null, null);
        assertLinkWorked("text [] and", "text [] and", null, null);
        assertLinkWorked("text [ x ] and", "text [ x ] and", null, null);
    }

    public void testShortCases()
    {
        assertLinkWorked("", "", null, null);
        assertLinkWorked("[", "[", null, null);
        assertLinkWorked("]", "]", null, null);
        assertLinkWorked("[]", "[]", null, null);
    }

    public void testBadLinkSyntax()
    {
        assertLinkWorked("[and] [mary", "1 [mary", "and", "1");
        assertLinkWorked("[an]d]", "1d]", "an", "1");
        assertLinkWorked("[an[d]", "[an1", "d", "1");
        assertLinkWorked("[[and]]", "[1]", "and", "1");
        assertLinkWorked("[[[[and]]]]", "[[[1]]]", "and", "1");
        assertLinkWorked("[an\nd]", "[an\nd]", null, null);
    }

    public void testEscapedLinks()
    {
        assertLinkWorked("[an\\[d]", "1", "an\\[d", "1");
        assertLinkWorked("[an\\]d]", "1", "an\\]d", "1");
        assertLinkWorked("\\[and]", "\\[and]", null, null);
        assertLinkWorked("[and\\]", "[and\\]", null, null);
    }

    public void testLinkWithAlias()
    {
        assertLinkWorked("[alias|somePage]", "<a href=\"http://example.com\">alias</a>", "alias|somePage", "<a href=\"http://example.com\">alias</a>");
        assertLinkWorked("[Some \"foo\" alias|somePage]", "<a href=\"http://example.com\">Some \"foo\" alias</a>", "Some \"foo\" alias|somePage", "<a href=\"http://example.com\">Some \"foo\" alias</a>");
    }

    public void testLinkWithInternationalCharacters()
    {
        assertLinkWorked("[Gûttt]", "<a href=\"http://example.com\">G&#xFB;ttt</a>", "Gûttt", "<a href=\"http://example.com\">G&#xFB;ttt</a>");
    }

    public void testUnpermittedLinkError()
    {
        final MockLink mockLink = new MockLink("noperm");
        mockLink.setLinkBody("worked");
        UnpermittedLink unpermLink = new UnpermittedLink(mockLink);
        mockLinkResolver.expectAndReturn("createLink", C.args(C.eq(renderContext), C.eq("noperm")), unpermLink);
        assertEquals("<span class=\"error\">&#91;worked&#93;</span>", render("[noperm]"));
    }

    private void assertLinkWorked(String wiki, String result, String originalLinkText, String resultLinkText)
    {
        assertLinkWorked(wiki, result, originalLinkText, resultLinkText, null, null);
    }

    private void assertLinkWorked(String wiki, String result, String originalLinkText, String resultLinkText, String originalLinkText2, String resultLinkText2)
    {
        if (originalLinkText != null)
        {
            final MockLink mockLink = new MockLink(originalLinkText);
            mockLinkResolver.expectAndReturn("createLink", C.args(C.eq(renderContext), C.eq(originalLinkText)), mockLink);
            if (resultLinkText != null)
                mockLinkRenderer.expectAndReturn("renderLink", C.args(C.eq(mockLink), C.eq(renderContext)), resultLinkText);
        }

        if (originalLinkText2 != null)
        {
            final MockLink mockLink = new MockLink(originalLinkText2);
            mockLinkResolver.expectAndReturn("createLink", C.args(C.eq(renderContext), C.eq(originalLinkText2)), mockLink);
            if (resultLinkText2 != null)
                mockLinkRenderer.expectAndReturn("renderLink", C.args(C.eq(mockLink), C.eq(renderContext)), resultLinkText2);
        }

        assertEquals(result, render(wiki));
    }
}