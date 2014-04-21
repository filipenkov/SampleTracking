package com.atlassian.jira.issue.fields.renderer.wiki.links;

import com.atlassian.jira.issue.fields.renderer.wiki.AbstractWikiTestCase;

/**
 * Tests that the anchor link in Jira generates correct anchor tags.
 * This also test the portion of the JiraLinkResolver that delegates
 * to anchorLinks.
 */
public class TestWikiAnchorLink extends AbstractWikiTestCase
{
    public void testAnchor()
    {
        if (is14OrGreater())
        {
            final String expectedHtml = "<p><a href=\"#testAnchor\">testAnchor</a></p>";
            assertEquals(expectedHtml, getRenderer().convertWikiToXHtml(getRenderContext(), "[#testAnchor]"));
        }
    }

    /**
     * JRA-15812: Make sure the anchor tag is escaped correctly.
     */
    public void testAnchorEscaping()
    {
        final String expectedHtml = "<p><a href=\"#bbb&quot; onclick=&quot;alert(&#39;XSS&#39;)\">xss</a></p>";
        assertEquals(expectedHtml, getRenderer().convertWikiToXHtml(getRenderContext(), "[xss|#bbb\" onclick=\"alert('XSS')]"));
    }

    /**
     * JRA-15812: What does JIRA do when we pass in an already escaped URL? It should not re-encode '&'s that look like
     * entity references. 
     */
    public void testAnchorDoubleEscaping()
    {
        final String expectedHtml = "<p><a href=\"#bbb&quot; onclick=&quot;alert(&#39;XSS&#39;)\">xss</a></p>";
        assertEquals(expectedHtml, getRenderer().convertWikiToXHtml(getRenderContext(), "[xss|#bbb&quot; onclick=&quot;alert(&#39;XSS&#39;)]"));
    }
}
