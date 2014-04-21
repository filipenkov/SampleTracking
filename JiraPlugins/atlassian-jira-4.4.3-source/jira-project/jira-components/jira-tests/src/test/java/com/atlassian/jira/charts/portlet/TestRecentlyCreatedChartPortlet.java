package com.atlassian.jira.charts.portlet;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.Chart;
import com.atlassian.core.util.map.EasyMap;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class TestRecentlyCreatedChartPortlet extends AbstractChartPortletTest
{

    // test that the RecentlyCreatedChartPortlet is called with the correct params.
    @Test
    public void testGetVelocityParams() throws Exception
    {
        setupVelocityParamsMocks();
        mockChartFactory.generateRecentlyCreated(context, 30, ChartFactory.PeriodName.daily);
        mockController.setReturnValue(new Chart(null, null, null, EasyMap.build("chart", new Object())));

        mockController.replay();

        final RecentlyCreatedChartPortlet portlet = new RecentlyCreatedChartPortlet(jiraAuthenticationContext,
                null, mockApplicationProperties, null, mockChartUtils, mockChartFactory)
        {
            public Map<String, Object> generateCommonParameters(final PortletConfiguration portletConfiguration)
            {
                return new HashMap<String, Object>();
            }
        };

        final Map<String, Object> params = portlet.getVelocityParams(mockPortletConfiguration);

        assertNotNull(params.get("chart"));
        assertEquals(true, params.get("showReportLink"));

        mockChartUtilsControl.verify();
        mockSearchRequestControl.verify();
        mockPortletConfigurationControl.verify();
    }

    //test that the CreatedVsResolvedChart is called with the correct params.
    @Test
    public void testGetSearchRequestViewHtml() throws ObjectConfigurationException
    {
        setupGetSearchRequestMocks();
        mockChartFactory.generateRecentlyCreated(context, 30, ChartFactory.PeriodName.daily);
        mockController.setReturnValue(new Chart(null, null, null, EasyMap.build("chart", chartObject)));

        mockController.replay();

        final RecentlyCreatedChartPortlet portlet = new RecentlyCreatedChartPortlet(jiraAuthenticationContext,
                null, mockApplicationProperties, null, mockChartUtils, mockChartFactory)
        {
            public Map<String, Object> generateCommonParameters(final PortletConfiguration portletConfiguration)
            {
                return new HashMap<String, Object>();
            }
        };

        portlet.init(mockPortletModuleDescriptor);

        String html = portlet.getSearchRequestViewHtml(mockPortletConfiguration, null);
        assertEquals("<html>mychart</html>", html);

        mockController.verify();
        mockChartUtilsControl.verify();
        mockSearchRequestControl.verify();
        mockPortletConfigurationControl.verify();
        mockPortletModuleDescriptorControl.verify();
    }
}
