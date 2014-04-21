package com.atlassian.jira.web.filters.steps;

import com.atlassian.jira.mock.servlet.MockFilterChain;
import mock.servlet.MockHttpServletRequest;
import mock.servlet.MockHttpServletResponse;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 */
public class FilterStepContextImplTest
{
    private MockHttpServletRequest httpServletRequest;
    private MockHttpServletResponse httpServletResponse;
    private MockFilterChain filterChain;

    @Before
    public void setUp() throws Exception
    {
        httpServletRequest = new MockHttpServletRequest();
        httpServletResponse = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    public void testGetters()
    {
        FilterCallContextImpl context = new FilterCallContextImpl(httpServletRequest, httpServletResponse, filterChain);

        assertEquals(httpServletRequest, context.getHttpServletRequest());
        assertEquals(httpServletResponse, context.getHttpServletResponse());
        assertEquals(filterChain, context.getFilterChain());
    }

}
