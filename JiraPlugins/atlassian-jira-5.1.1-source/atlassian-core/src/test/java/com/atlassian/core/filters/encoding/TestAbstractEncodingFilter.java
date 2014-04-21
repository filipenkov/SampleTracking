package com.atlassian.core.filters.encoding;

import junit.framework.TestCase;
import com.atlassian.core.filters.StubEncodingFilter;
import com.atlassian.core.filters.ServletStubs;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * You might be looking for the test for the old class, this one tests the new simplified filter.
 *
 * @see com.atlassian.core.filters.TestAbstractEncodingFilter
 */
public class TestAbstractEncodingFilter extends TestCase
{
    public void testDefaultContentTypeAndEncodingAreSet() throws Exception
    {
        ServletStubs.Request request = ServletStubs.getRequestInstance();
        ServletStubs.Response response = ServletStubs.getResponseInstance();

        StubEncodingFilter encodingFilter = new StubEncodingFilter();
        encodingFilter.setEncoding("UTF-8");
        encodingFilter.setContentType("text/plain");
        encodingFilter.doFilter(request, response, new FilterChain() {
            public void doFilter(ServletRequest request, ServletResponse response)
            {
                assertEquals(FixedHtmlEncodingResponseWrapper.class, response.getClass());
            }
        });

        assertEquals("UTF-8", request.getCharacterEncoding());
        assertEquals("text/plain", response.getContentType());
    }
}
