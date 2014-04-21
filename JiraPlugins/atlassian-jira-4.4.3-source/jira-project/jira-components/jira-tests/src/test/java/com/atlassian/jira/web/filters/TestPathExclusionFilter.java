package com.atlassian.jira.web.filters;

import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.mock.servlet.MockFilter;
import com.atlassian.jira.mock.servlet.MockFilterChain;
import com.atlassian.jira.mock.servlet.MockFilterConfig;
import com.atlassian.jira.local.ListeningTestCase;
import mock.servlet.MockHttpServletRequest;
import mock.servlet.MockHttpServletResponse;

import java.io.IOException;
import javax.servlet.ServletException;

/**
 * Test case for the path exclusion filter,
 *
 * @since v4.2
 */
public class TestPathExclusionFilter extends ListeningTestCase
{
    private PathExclusionFilter tested = new PathExclusionFilter();

    private MockFilterConfig mockFilterConfig = new MockFilterConfig("PathExclusion");
    private MockFilterChain mockChain = new MockFilterChain();
    private MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    private MockHttpServletResponse mockResponse = new MockHttpServletResponse();

    @Test
    public void testAcceptsNonExcludedPath() throws Exception
    {
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.FILTER_CLASS.key(), MockFilter.class.getName());
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.EXCLUDED_PATHS.key(), "/one,/two,/four");
        mockRequest.setServletPath("/three");
        runTestedFilterLifecycle();
        assertLifecyclePerformedOnDecoratedFilter();
        assertDecoratedFilterCalled();
    }

    @Test
    public void testAcceptsNonExcludedPathWithExcludedBase() throws Exception
    {
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.FILTER_CLASS.key(), MockFilter.class.getName());
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.EXCLUDED_PATHS.key(), "/one");
        mockRequest.setServletPath("/one/three");
        runTestedFilterLifecycle();
        assertLifecyclePerformedOnDecoratedFilter();
        assertDecoratedFilterCalled();
    }

    @Test
    public void testExactExcludedPathMatch() throws Exception
    {
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.FILTER_CLASS.key(), MockFilter.class.getName());
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.EXCLUDED_PATHS.key(), "/one");
        mockRequest.setServletPath("/one");
        runTestedFilterLifecycle();
        assertLifecyclePerformedOnDecoratedFilter();
        assertDecoratedFilterNotCalled();
    }

    @Test
    public void testWildcardMatch() throws Exception
    {
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.FILTER_CLASS.key(), MockFilter.class.getName());
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.EXCLUDED_PATHS.key(), "/one,/two/*");
        mockRequest.setServletPath("/two/three");
        runTestedFilterLifecycle();
        assertLifecyclePerformedOnDecoratedFilter();
        assertDecoratedFilterNotCalled();
    }

    @Test
    public void testWildcardAfterSlashDoesNotMatchFullServletPath() throws Exception
    {
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.FILTER_CLASS.key(), MockFilter.class.getName());
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.EXCLUDED_PATHS.key(), "/one/two/*,/one");
        mockRequest.setServletPath("/one/two");
        runTestedFilterLifecycle();
        assertLifecyclePerformedOnDecoratedFilter();
        assertDecoratedFilterCalled();
    }

    @Test
    public void testWildcardWithoutSlashMatchesFullServletPath() throws Exception
    {
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.FILTER_CLASS.key(), MockFilter.class.getName());
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.EXCLUDED_PATHS.key(), "/one/two*");
        mockRequest.setServletPath("/one/two");
        runTestedFilterLifecycle();
        assertLifecyclePerformedOnDecoratedFilter();
        assertDecoratedFilterNotCalled();
    }

    @Test
    public void testExclusionPathsTrimming() throws Exception
    {
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.FILTER_CLASS.key(), MockFilter.class.getName());
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.EXCLUDED_PATHS.key(), "  /one,\n/one/two  ,/three");
        mockRequest.setServletPath("/one/two");
        runTestedFilterLifecycle();
        assertLifecyclePerformedOnDecoratedFilter();
        assertDecoratedFilterNotCalled();
    }

    @Test
    public void testFilterClassNameRequired() throws Exception
    {
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.FILTER_CLASS.key(), MockFilter.class.getName());
        try
        {
            runTestedFilterLifecycle();
            fail("Expected exception because of missing init param");
        }
        catch(IllegalStateException expected)
        {
        }
    }

    @Test
    public void testFilterClassNameMustBeValid() throws Exception
    {
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.FILTER_CLASS.key(), "if.such.class.existed.it.would.be.Ridiculous");
        try
        {
            runTestedFilterLifecycle();
            fail("Expected exception because of invalid filter class init param");
        }
        catch(IllegalStateException expected)
        {
        }
    }

    @Test
    public void testExcludedPathsParamRequired() throws Exception
    {
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.FILTER_CLASS.key(), MockFilter.class.getName());
        mockFilterConfig.addInitParam(PathExclusionFilter.InitParams.EXCLUDED_PATHS.key(), null);
        try
        {
            runTestedFilterLifecycle();
            fail("Expected exception because of missing excluded paths init param");
        }
        catch(IllegalStateException expected)
        {
        }
    }
    
    private void runTestedFilterLifecycle() throws IOException, ServletException
    {
        tested.init(mockFilterConfig);
        tested.doFilter(mockRequest, mockResponse, mockChain);
        tested.destroy();
    }

    private void assertLifecyclePerformedOnDecoratedFilter()
    {
        MockFilter mockFilter = retrieveDecoratedFilterInAHackyWay();
        assertEquals(1, mockFilter.initCalls);
        assertSame(mockFilterConfig, mockFilter.lastConfig);
        assertEquals(1, mockFilter.destroyCalls);
    }

    private void assertDecoratedFilterCalled()
    {
        MockFilter mockFilter = retrieveDecoratedFilterInAHackyWay(); 
        assertEquals(1, mockFilter.filterCalls);
        assertSame(mockRequest, mockFilter.lastRequest);
        assertSame(mockResponse, mockFilter.lastResponse);
        assertSame(mockChain, mockFilter.lastChain);
    }

    private void assertDecoratedFilterNotCalled()
    {
        MockFilter mockFilter = retrieveDecoratedFilterInAHackyWay();
        assertEquals("Mock filter was not supposed to be called (actual call count", 0, mockFilter.filterCalls);
        assertEquals(1, mockChain.filterCalls);
        assertSame(mockRequest, mockChain.lastRequest);
        assertSame(mockResponse, mockChain.lastResponse);
    }

    private MockFilter retrieveDecoratedFilterInAHackyWay()
    {
        return (MockFilter) tested.realFilter;
    }
}
