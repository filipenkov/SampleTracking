package com.atlassian.jira.issue.fields.renderer.wiki.links;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.renderer.links.GenericLinkParser;
import com.atlassian.renderer.links.UrlLink;
import com.atlassian.jira.local.ListeningTestCase;

/**
 * Test for {@link JiraUrlLink}.
 *
 * @since v3.13
 */
public class TestJiraUrlLink extends ListeningTestCase
{
    /**
     * JRA-15812: Make sure that the URL entered by the user is correctly escaped.
     */
    @Test
    public void testEscape()
    {
        final JiraUrlLink link = new JiraUrlLink(createParser("brenden|http://www.atlassian.com/\" onclick=\"alert('hello world')"));
        assertEquals("http://www.atlassian.com/&quot; onclick=&quot;alert(&#39;hello world&#39;)", link.getUrl());

        //make sure that the URL is not double escaped (at least for this simple case).
        final JiraUrlLink link2 = new JiraUrlLink(createParser(link.getUrl()));
        assertEquals("http://www.atlassian.com/&quot; onclick=&quot;alert(&#39;hello world&#39;)", link2.getUrl());
    }

    /**
     * JRA-15812: The bug in the {@link UrlLink} is fixed in atlassian-renderer-4.1. This test will start to fail when
     * we no longer need {@link JiraUrlLink}. When this fails, we should remove {@link JiraUrlLink} and start returning the
     * UrlLink directly again.
     */
    @Test
    public void testDontNeedMeAnyMore()
    {
        final UrlLink link = new UrlLink(createParser("brenden|http://www.atlassian.com/\" onclick=\"alert('hello world')"));
        assertEquals("http://www.atlassian.com/\" onclick=\"alert('hello world')", link.getUrl());
    }

    private static GenericLinkParser createParser(final String parser)
    {
        return new GenericLinkParser(parser);
    }
}
