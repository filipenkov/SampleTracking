package com.atlassian.jira.plugin.searchrequestview;

import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.web.RequestParameterKeys;
import com.atlassian.jira.web.bean.PagerFilter;
import com.mockobjects.servlet.MockHttpServletRequest;
import org.easymock.EasyMock;

/**
 * Tests the SearchRequestURLHandler class
 */
public class TestSearchRequestURLHandler extends LegacyJiraMockTestCase
{
    private BuildUtilsInfo buildUtilsInfo;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        buildUtilsInfo = EasyMock.createMock(BuildUtilsInfo.class);
    }

    public void testGetPagerFilter()
    {
        DefaultSearchRequestURLHandler urlHandler = new DefaultSearchRequestURLHandler(null, null, null, null, null, null, null, null, null, buildUtilsInfo, null);

        //test default values (URL params are not specified)
        PagerFilter pager = urlHandler.getPagerFilter(getPageFilterMockRequest(null, null));
        assertEquals(Integer.MAX_VALUE, pager.getMax());
        assertEquals(0, pager.getStart());

        //test default values (URL params are corrupt)
        pager = urlHandler.getPagerFilter(getPageFilterMockRequest("13zz", "e23d"));
        assertEquals(Integer.MAX_VALUE, pager.getMax());
        assertEquals(0, pager.getStart());

        //test values specified by URL param
        pager = urlHandler.getPagerFilter(getPageFilterMockRequest("10", "20"));
        assertEquals(10, pager.getMax());
        assertEquals(20, pager.getStart());
    }

    private MockHttpServletRequest getPageFilterMockRequest(String tempMax, String start)
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setupAddParameter(RequestParameterKeys.JIRA_SEARCH_REQUEST_VIEW_TEMP_MAX, tempMax);
        request.setupAddParameter(RequestParameterKeys.JIRA_SEARCH_REQUEST_VIEW_PAGER_START, start);
        return request;
    }


}
