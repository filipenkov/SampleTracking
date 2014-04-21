package com.atlassian.core.filters;

import junit.framework.TestCase;

public class TestAbstractEncodingFilterCachingHeaders extends TestCase
{
    private StubEncodingFilter encodingFilter = new StubEncodingFilter();
    private ServletStubs.Request request;
    private ServletStubs.Response response;

    protected void setUp() throws Exception
    {
        super.setUp();
        request = ServletStubs.getRequestInstance();
        response = ServletStubs.getResponseInstance();
    }

    public void testNoCachingHeadersByDefault() throws Exception
    {
        request.setRequestURI("/some-request");
        encodingFilter.doFilter(request, response);

        assertNull("should be no cache-control header", response.getHeader("Cache-Control"));
        assertNull("should be no pragma header", response.getHeader("Pragma"));
        assertEquals(-1, response.getDateHeader("Expires"));
    }

    public void testCachingHeadersAppliedToJspRequests() throws Exception
    {
        request.setRequestURI("/test-caching.jsp");
        encodingFilter.doFilter(request, response);

        assertEquals("no-cache, no-store, must-revalidate", response.getHeader("Cache-Control"));
        assertEquals("no-cache", response.getHeader("Pragma"));
        assertEquals(0L, response.getDateHeader("Expires"));
    }

    public void testCachingHeadersAppliedToJspaRequests() throws Exception
    {
        request.setRequestURI("/test-caching.jspa");
        encodingFilter.doFilter(request, response);

        assertEquals("no-cache, no-store, must-revalidate", response.getHeader("Cache-Control"));
        assertEquals("no-cache", response.getHeader("Pragma"));
        assertEquals(0L, response.getDateHeader("Expires"));
    }

}
