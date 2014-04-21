package com.atlassian.core.filters.cache;

import junit.framework.TestCase;
import com.atlassian.core.filters.ServletStubs;

public class TestJspCachingStrategy extends TestCase
{
    private JspCachingStrategy strategy;
    private ServletStubs.Request request;

    protected void setUp() throws Exception
    {
        super.setUp();
        strategy = new JspCachingStrategy();
        request = ServletStubs.getRequestInstance();
    }

    public void testMatchesJspUrls() throws Exception
    {
        request.setRequestURI("super.jsp");
        assertTrue(strategy.matches(request));

        request.setRequestURI("super.jsp?param=value");
        assertTrue(strategy.matches(request));

    }

    public void testMatchesJspaUrls() throws Exception
    {
        request.setRequestURI("super.jspa");
        assertTrue(strategy.matches(request));

        request.setRequestURI("super.jspa?param=value");
        assertTrue(strategy.matches(request));
    }

    public void testDoesNotMatchOtherUrls() throws Exception
    {
        request.setRequestURI(".jsp"); // legacy case from AbstractEncodingFilter
        assertFalse(strategy.matches(request));

        request.setRequestURI(".jspa"); // legacy case from AbstractEncodingFilter
        assertFalse(strategy.matches(request));

        request.setRequestURI("super.action?param=value");
        assertFalse(strategy.matches(request));

        request.setRequestURI("this-is-not-a-jsp.gif");
        assertFalse(strategy.matches(request));
    }

    public void testCachingHeaders() throws Exception
    {
        ServletStubs.Response response = ServletStubs.getResponseInstance();
        strategy.setCachingHeaders(response);

        assertEquals("no-cache, no-store, must-revalidate", response.getHeader("Cache-Control"));
        assertEquals("no-cache", response.getHeader("Pragma"));
        assertEquals(0L, response.getDateHeader("Expires"));
    }
}
