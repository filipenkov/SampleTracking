package com.atlassian.core.filters.encoding;

import junit.framework.TestCase;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.core.filters.StubEncodingFilter;
import com.atlassian.core.filters.ServletStubs;

public class TestFixedHtmlEncodingResponseWrapper extends TestCase
{
    private StubEncodingFilter encodingFilter;
    private ServletStubs.Request request;
    private ServletStubs.Response response;

    protected void setUp() throws Exception
    {
        super.setUp();
        encodingFilter = new StubEncodingFilter();

        request = ServletStubs.getRequestInstance();
        response = ServletStubs.getResponseInstance();
    }

    public void testSetRawContentTypeAppendsEncoding() throws Exception
    {
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.setContentType("text/plain");
        response.setCharacterEncoding("ISO-8859-1"); // not sure where this comes from normally

        encodingFilter.doFilter(request, response, new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
            {
                servletResponse.setContentType("text/html");
            }
        });

        assertEquals("UTF-8", request.getCharacterEncoding());
        assertEquals("text/html;charset=ISO-8859-1", response.getContentType());
    }

    public void testSetHtmlContentTypeWithCharsetIsIgnored() throws Exception
    {
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.setContentType("text/plain");

        encodingFilter.doFilter(request, response, new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
            {
                servletResponse.setContentType("text/html; charset=UTF-8");
            }
        });

        assertEquals("UTF-8", request.getCharacterEncoding());
        assertEquals("content-type is unchanged", "text/plain", response.getContentType());
    }

    public void testSetNonHtmlContentTypeWithCharsetWorks() throws Exception
    {
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.setContentType("text/plain");

        encodingFilter.doFilter(request, response, new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
            {
                servletResponse.setContentType("application/xml; charset=UTF-8");
            }
        });

        assertEquals("UTF-8", request.getCharacterEncoding());
        assertEquals("application/xml; charset=UTF-8", response.getContentType());
    }

    public void testSetContentTypeHeaderIsAppliedWithoutChanges() throws Exception
    {
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.setContentType("text/plain");

        encodingFilter.doFilter(request, response, new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
            {
                ((HttpServletResponse) servletResponse).setHeader("Content-Type", "text/html");
            }
        });

        assertEquals("UTF-8", request.getCharacterEncoding());

        // these differ because we're using a stub implementation
        assertEquals("text/plain", response.getContentType());
        assertEquals("text/html", response.getHeader("Content-Type"));
    }
}
