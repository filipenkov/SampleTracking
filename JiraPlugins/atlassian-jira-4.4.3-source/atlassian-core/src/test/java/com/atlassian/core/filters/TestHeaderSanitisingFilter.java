package com.atlassian.core.filters;

import com.mockobjects.constraint.IsEqual;
import com.mockobjects.constraint.IsInstanceOf;
import com.mockobjects.dynamic.FullConstraintMatcher;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.servlet.MockHttpServletResponse;
import junit.framework.TestCase;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * @since v4.2
 */
public class TestHeaderSanitisingFilter extends TestCase
{
    public void testAlreadyFilteredNonHttpServletRequest() throws Exception
    {
        HeaderSanitisingFilter filter = new HeaderSanitisingFilter();

        Mock mockServletRequest = new Mock(ServletRequest.class);
        mockServletRequest.expectAndReturn("getAttribute", new FullConstraintMatcher(new IsEqual(HeaderSanitisingFilter.ALREADY_FILTERED)), Boolean.TRUE);

        final ServletRequest request = (ServletRequest) mockServletRequest.proxy();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        Mock mockFilterChain = new Mock(FilterChain.class);
        mockFilterChain.expect("doFilter", new FullConstraintMatcher(new IsEqual(request), new IsEqual(response)));

        final FilterChain filterChain = (FilterChain) mockFilterChain.proxy();
        filter.doFilter(request, response, filterChain);

        mockFilterChain.verify();
    }

    public void testAlreadyFilteredHttpServletRequest() throws Exception
    {
        HeaderSanitisingFilter filter = new HeaderSanitisingFilter();

        Mock mockServletRequest = new Mock(HttpServletRequest.class);
        mockServletRequest.expectAndReturn("getAttribute", new FullConstraintMatcher(new IsEqual(HeaderSanitisingFilter.ALREADY_FILTERED)), Boolean.TRUE);

        final ServletRequest request = (ServletRequest) mockServletRequest.proxy();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        Mock mockFilterChain = new Mock(FilterChain.class);
        mockFilterChain.expect("doFilter", new FullConstraintMatcher(new IsEqual(request), new IsEqual(response)));

        final FilterChain filterChain = (FilterChain) mockFilterChain.proxy();
        filter.doFilter(request, response, filterChain);

        mockFilterChain.verify();
    }

    public void testNotAlreadyFilteredHttpServletRequest() throws Exception
    {
        HeaderSanitisingFilter filter = new HeaderSanitisingFilter();

        Mock mockServletRequest = new Mock(HttpServletRequest.class);
        mockServletRequest.expectAndReturn("getAttribute", new FullConstraintMatcher(new IsEqual(HeaderSanitisingFilter.ALREADY_FILTERED)), null);
        mockServletRequest.expect("setAttribute", new FullConstraintMatcher(new IsEqual(HeaderSanitisingFilter.ALREADY_FILTERED), new IsEqual(Boolean.TRUE)));

        final ServletRequest request = (ServletRequest) mockServletRequest.proxy();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        Mock mockFilterChain = new Mock(FilterChain.class);
        mockFilterChain.expect("doFilter", new FullConstraintMatcher(new IsEqual(request), new IsInstanceOf(HeaderSanitisingResponseWrapper.class)));

        final FilterChain filterChain = (FilterChain) mockFilterChain.proxy();
        filter.doFilter(request, response, filterChain);

        mockFilterChain.verify();
    }

    public void testNotAlreadyFilteredNonHttpServletRequest() throws Exception
    {
        HeaderSanitisingFilter filter = new HeaderSanitisingFilter();

        Mock mockServletRequest = new Mock(ServletRequest.class);
        mockServletRequest.expectAndReturn("getAttribute", new FullConstraintMatcher(new IsEqual(HeaderSanitisingFilter.ALREADY_FILTERED)), null);
        mockServletRequest.expect("setAttribute", new FullConstraintMatcher(new IsEqual(HeaderSanitisingFilter.ALREADY_FILTERED), new IsEqual(Boolean.TRUE)));

        final ServletRequest request = (ServletRequest) mockServletRequest.proxy();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        Mock mockFilterChain = new Mock(FilterChain.class);
        mockFilterChain.expect("doFilter", new FullConstraintMatcher(new IsEqual(request), new IsEqual(response)));

        final FilterChain filterChain = (FilterChain) mockFilterChain.proxy();
        filter.doFilter(request, response, filterChain);

        mockFilterChain.verify();
    }
}
