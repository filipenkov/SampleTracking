package com.atlassian.core.filters;

import junit.framework.TestCase;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class TestAbstractEncodingFilterContentLocationHandling extends TestCase
{
    private ServletStubs.Request request;
    private ServletStubs.Response response;
    private Filter encodingFilter;

    protected void setUp() throws Exception
    {
        super.setUp();
        request = ServletStubs.getRequestInstance();
        response = ServletStubs.getResponseInstance();
        encodingFilter = new StubEncodingFilter();
    }

    public void testContentLocationHeaderCannotBeSet() throws Exception
    {
        encodingFilter.doFilter(request, response, new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
            {
                ((HttpServletResponse) servletResponse).setHeader("Content-Location", "/context");
            }
        });

        assertNull("content location should not be set", response.getHeader("Content-Location"));
    }

    public void testContentLocationHeaderCannotBeAdded() throws Exception
    {
        encodingFilter.doFilter(request, response, new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
            {
                ((HttpServletResponse) servletResponse).addHeader("Content-Location", "/context");
            }
        });

        assertNull("content location should not be set", response.getHeader("Content-Location"));
    }
}
