package com.atlassian.streams.jira.util;

import java.awt.Dimension;

import com.atlassian.plugin.webresource.WebResourceManager;

import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RenderingUtilitiesTest extends TestCase
{
    public void testScaleToThumbnailSizeLowerBounds()
    {
        try
        {
            RenderingUtilities.scaleToThumbnailSize(0,1);
            fail();
        }
        catch(IllegalArgumentException e)
        {
            // we expect this exception - that's the point of this test!
        }
        try
        {
            RenderingUtilities.scaleToThumbnailSize(-1,1);
            fail();
        }
        catch(IllegalArgumentException e)
        {
            // we expect this exception - that's the point of this test!
        }
        try
        {
            RenderingUtilities.scaleToThumbnailSize(1,0);
            fail();
        }
        catch(IllegalArgumentException e)
        {
            // we expect this exception - that's the point of this test!
        }
        try
        {
            RenderingUtilities.scaleToThumbnailSize(1,-1);
            fail();
        }
        catch(IllegalArgumentException e)
        {
            // we expect this exception - that's the point of this test!
        }

        Dimension d = RenderingUtilities.scaleToThumbnailSize(1,1);
        assertEquals(1, d.height);
        assertEquals(1, d.width);
    }

    public void testScaleToThumbnailSizeUpperBounds()
    {{
        Dimension d = RenderingUtilities.scaleToThumbnailSize(100,100);
        assertEquals(100, d.height);
        assertEquals(100, d.width);
    }{
        Dimension d = RenderingUtilities.scaleToThumbnailSize(99,99);
        assertEquals(99, d.height);
        assertEquals(99, d.width);
    }{
        Dimension d = RenderingUtilities.scaleToThumbnailSize(101,101);
        assertEquals(100, d.height);
        assertEquals(100, d.width);
    }{
        Dimension d = RenderingUtilities.scaleToThumbnailSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals(100, d.height);
        assertEquals(100, d.width);
    }
    }

    public void testHtmlEncodeNullSafety()
    {
        final String result = RenderingUtilities.htmlEncode(null);
        assertNull(result);
    }

    public void testSpanNullSensitivity()
    {
        try
        {
            RenderingUtilities.span(null, "");
            fail();
        }
        catch(IllegalArgumentException e)
        {}
        try
        {
            RenderingUtilities.span("", null);
            fail();
        }
        catch(IllegalArgumentException e)
        {}
    }

    public void testSpan()
    {
        String result = RenderingUtilities.span("class-attribute", "htmltext");
        assertEquals("<span class='class-attribute'>htmltext</span>", result);
    }

    public void testLinkNullSensitivity()
    {
        try
        {
            RenderingUtilities.link("http://example.com", null);
        }
        catch(IllegalArgumentException e)
        {}

        final String s = RenderingUtilities.link(null, "somelabel");
        assertEquals("somelabel", s);
    }

    public void testLink()
    {
        final String s = RenderingUtilities.link("http://example.com", "someLabel");
        assertEquals("<a href=\"http://example.com\">someLabel</a>", s);
    }

    public void testIncludeactivityStreamResources()
    {
        WebResourceManager webResourceManager = mock(WebResourceManager.class);
        RenderingUtilities.includeActivityStreamResources(webResourceManager);

        verify(webResourceManager).requireResource("com.atlassian.streams.streams-jira-plugin:streamsWebResources");
    }
}
