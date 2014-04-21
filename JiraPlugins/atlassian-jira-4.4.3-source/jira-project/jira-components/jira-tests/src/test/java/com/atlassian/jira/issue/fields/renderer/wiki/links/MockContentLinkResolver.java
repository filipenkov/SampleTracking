package com.atlassian.jira.issue.fields.renderer.wiki.links;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.links.ContentLinkResolver;
import com.atlassian.renderer.links.GenericLinkParser;
import com.atlassian.renderer.links.Link;

import java.text.ParseException;

public class MockContentLinkResolver implements ContentLinkResolver
{
    private Link link;

    public MockContentLinkResolver(Link link)
    {
        this.link = link;
    }

    public Link createContentLink(RenderContext context, GenericLinkParser parsedLink) throws ParseException
    {
        return link;
    }
}
