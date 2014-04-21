package com.atlassian.core.filters.legacy;

import junit.framework.TestCase;
import com.atlassian.core.filters.ServletStubs;

public class TestNoContentLocationHeaderResponseWrapper extends TestCase
{
    public void testCannotSetHeader() throws Exception
    {
        ServletStubs.Response response = ServletStubs.getResponseInstance();
        NoContentLocationHeaderResponseWrapper wrapper = new NoContentLocationHeaderResponseWrapper(response);

        wrapper.setHeader("Content-Location", "/test");
        assertNull("Content-Location should not be set", response.getHeader("Content-Location"));

        wrapper.setHeader("ConTENT-location", "/test");
        assertNull("ConTENT-location should not be set", response.getHeader("ConTENT-location"));

        wrapper.setHeader("Content-Type", "text/html");
        assertEquals("Other headers work fine", "text/html", response.getHeader("Content-Type"));
    }

    public void testCannotAddHeader() throws Exception
    {
        ServletStubs.Response response = ServletStubs.getResponseInstance();
        NoContentLocationHeaderResponseWrapper wrapper = new NoContentLocationHeaderResponseWrapper(response);

        wrapper.addHeader("Content-Location", "/test");
        assertNull("Content-Location should not be set", response.getHeader("Content-Location"));

        wrapper.addHeader("ConTENT-location", "/test");
        assertNull("Content-location should not be set", response.getHeader("Content-location"));

        wrapper.addHeader("Content-Type", "text/html");
        assertEquals("Other headers work fine", "text/html", response.getHeader("Content-Type"));
    }
}
