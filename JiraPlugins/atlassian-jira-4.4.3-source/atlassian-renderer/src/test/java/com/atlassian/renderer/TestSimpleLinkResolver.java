package com.atlassian.renderer;

import junit.framework.TestCase;
import com.atlassian.renderer.links.LinkResolver;
import com.atlassian.renderer.links.Link;

public class TestSimpleLinkResolver
    extends TestCase
{
    private LinkResolver resolver;

    protected void setUp()
    {
        resolver = new SimpleLinkResolver();
    }

    public void testInternalAnchor()
    {
        Link link = resolver.createLink(RendererFactory.getRenderContext(), "#foo");
        assertEquals("foo", link.getLinkBody());
        assertEquals("#foo", link.getUrl());
    }

    public void testExternalLink()
    {
        Link link = resolver.createLink(RendererFactory.getRenderContext(), "http://www.example.com");
        assertEquals("http://www.example.com", link.getLinkBody());
        assertEquals("http://www.example.com", link.getUrl());
    }

}