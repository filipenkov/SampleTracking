package com.atlassian.jira.charts.util;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class TestChartUtils extends MockControllerTestCase
{
    @Test
    public void testRetrieveOrMakeFilter()
    {
        final JiraAuthenticationContext jiraAuthenticationContext = mockController.getMock(JiraAuthenticationContext.class);
        jiraAuthenticationContext.getUser();
        mockController.setReturnValue(null);

        MockControl mockSearchRequestControl = MockClassControl.createControl(SearchRequest.class);
        SearchRequest mockSearchRequest = (SearchRequest) mockSearchRequestControl.getMock();        

        final SearchRequestService searchRequestService = mockController.getMock(SearchRequestService.class);
        searchRequestService.getFilter(new JiraServiceContextImpl(null, new SimpleErrorCollection()), 10000L);
        mockController.setReturnValue(mockSearchRequest);


        mockSearchRequestControl.replay();
        final ChartUtils chartUtils = mockController.instantiate(ChartUtilsImpl.class);

        Map<String, Object> params = new HashMap<String, Object>();
        final SearchRequest request = chartUtils.retrieveOrMakeSearchRequest("filter-10000", params);
        assertEquals(mockSearchRequest, request);
        assertTrue(params.containsKey("searchRequest"));
        assertEquals(mockSearchRequest, params.get("searchRequest"));
        assertFalse(params.containsKey("project"));

        mockSearchRequestControl.verify();

        //cant test the project case since that crates a ProjectClause, which initialises the world via the
        //ComponentManager.getInstance.
    }

}
