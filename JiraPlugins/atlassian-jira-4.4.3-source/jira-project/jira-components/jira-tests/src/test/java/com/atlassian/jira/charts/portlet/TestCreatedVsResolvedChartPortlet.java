package com.atlassian.jira.charts.portlet;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.util.ChartUtilsImpl;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.portlet.PortletModuleDescriptor;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class TestCreatedVsResolvedChartPortlet extends MockControllerTestCase
{

    /**
     * test that the CreatedVsResolvedChart is called with the correct params.
     */
    @Test
    public void testGetVelocityParams() throws ObjectConfigurationException
    {
        MockControl mockPortletConfigurationControl = MockControl.createControl(PortletConfiguration.class);
        PortletConfiguration mockPortletConfiguration = (PortletConfiguration) mockPortletConfigurationControl.getMock();
        mockPortletConfiguration.getProperty("projectOrFilterId");
        mockPortletConfigurationControl.setReturnValue("filter-1000");
        mockPortletConfiguration.getProperty("periodName");
        mockPortletConfigurationControl.setReturnValue("daily");
        mockPortletConfiguration.getProperty("versionLabels");
        mockPortletConfigurationControl.setReturnValue(null);
        mockPortletConfiguration.getProperty("cumulative");
        mockPortletConfigurationControl.setReturnValue("true");
        mockPortletConfiguration.getProperty("showUnresolvedTrend");
        mockPortletConfigurationControl.setReturnValue("false");
        mockPortletConfiguration.getLongProperty("daysprevious");
        mockPortletConfigurationControl.setReturnValue(new Long(30));
        mockPortletConfigurationControl.replay();

        final JiraAuthenticationContext jiraAuthenticationContext = mockController.getMock(JiraAuthenticationContext.class);
        jiraAuthenticationContext.getUser();
        mockController.setDefaultReturnValue(null);

        MockControl mockSearchRequestControl = MockClassControl.createControl(SearchRequest.class);
        SearchRequest mockSearchRequest = (SearchRequest) mockSearchRequestControl.getMock();

        mockSearchRequestControl.replay();

        MockControl mockChartUtilsControl = MockClassControl.createControl(ChartUtilsImpl.class);
        ChartUtils mockChartUtils = (ChartUtils) mockChartUtilsControl.getMock();
        mockChartUtils.retrieveOrMakeSearchRequest("filter-1000", EasyMap.build("portletConfig", mockPortletConfiguration, "indexing", true, "loggedin", false, "projectOrFilterId", "filter-1000"));
        mockChartUtilsControl.setReturnValue(mockSearchRequest);
        mockChartUtilsControl.replay();

        final ChartFactory mockChartFactory = mockController.getMock(ChartFactory.class);
        ChartFactory.ChartContext context = new ChartFactory.ChartContext(null, mockSearchRequest, 450, 300);
        mockChartFactory.generateCreatedVsResolvedChart(context, 30, ChartFactory.PeriodName.daily, ChartFactory.VersionLabel.none, true, false);
        mockController.setReturnValue(new Chart(null, null, null, EasyMap.build("chart", new Object())));

        final ApplicationProperties mockApplicationProperties = mockController.getMock(ApplicationProperties.class);
        mockApplicationProperties.getOption("jira.option.indexing");
        mockController.setReturnValue(true);

        mockController.replay();
        CreatedVsResolvedChartPortlet portlet = new CreatedVsResolvedChartPortlet(jiraAuthenticationContext,
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

        mockController.verify();
        mockChartUtilsControl.verify();
        mockSearchRequestControl.verify();
        mockPortletConfigurationControl.verify();
    }

    /**
     * test that the CreatedVsResolvedChart is called with the correct params.
     */
    @Test
    public void testGetSearchRequestViewHtml() throws ObjectConfigurationException
    {
        MockControl mockPortletConfigurationControl = MockControl.createControl(PortletConfiguration.class);
        PortletConfiguration mockPortletConfiguration = (PortletConfiguration) mockPortletConfigurationControl.getMock();
        mockPortletConfiguration.getProperty("projectOrFilterId");
        mockPortletConfigurationControl.setReturnValue("filter-1000");
        mockPortletConfiguration.getProperty("periodName");
        mockPortletConfigurationControl.setReturnValue("daily");
        mockPortletConfiguration.getProperty("versionLabels");
        mockPortletConfigurationControl.setReturnValue(null);
        mockPortletConfiguration.getProperty("cumulative");
        mockPortletConfigurationControl.setReturnValue("true");
        mockPortletConfiguration.getProperty("showUnresolvedTrend");
        mockPortletConfigurationControl.setReturnValue("false");
        mockPortletConfiguration.getLongProperty("daysprevious");
        mockPortletConfigurationControl.setReturnValue(new Long(30));
        mockPortletConfigurationControl.replay();

        final JiraAuthenticationContext jiraAuthenticationContext = mockController.getMock(JiraAuthenticationContext.class);
        jiraAuthenticationContext.getUser();
        mockController.setDefaultReturnValue(null);

        MockControl mockSearchRequestControl = MockClassControl.createControl(SearchRequest.class);
        SearchRequest mockSearchRequest = (SearchRequest) mockSearchRequestControl.getMock();

        mockSearchRequestControl.replay();

        MockControl mockChartUtilsControl = MockClassControl.createControl(ChartUtilsImpl.class);
        ChartUtils mockChartUtils = (ChartUtils) mockChartUtilsControl.getMock();
        mockChartUtils.retrieveOrMakeSearchRequest("filter-1000", EasyMap.build("portletConfig", mockPortletConfiguration, "indexing", true, "loggedin", false, "projectOrFilterId", "filter-1000"));
        mockChartUtilsControl.setReturnValue(mockSearchRequest);
        mockChartUtilsControl.replay();

        final Object chartObject = new Object();

        final ApplicationProperties mockApplicationProperties = mockController.getMock(ApplicationProperties.class);
        mockApplicationProperties.getOption("jira.option.indexing");
        mockController.setReturnValue(true);


        MockControl mockPortletModuleDescriptorControl = MockClassControl.createControl(PortletModuleDescriptor.class);
        PortletModuleDescriptor mockPortletModuleDescriptor = (PortletModuleDescriptor) mockPortletModuleDescriptorControl.getMock();

        mockPortletModuleDescriptor.getHtml("view",
                EasyMap.build("searchRequest", mockSearchRequest, "indexing", true, "portletConfig", mockPortletConfiguration,
                        "showReportLink", false, "chart", chartObject, "loggedin", false, "projectOrFilterId", "filter-1000"));
        mockPortletModuleDescriptorControl.setReturnValue("<html>mychart</html>");

        mockPortletModuleDescriptorControl.replay();

        final ChartFactory mockChartFactory = mockController.getMock(ChartFactory.class);
        ChartFactory.ChartContext context = new ChartFactory.ChartContext(null, mockSearchRequest, 450, 300);
        mockChartFactory.generateCreatedVsResolvedChart(context, 30, ChartFactory.PeriodName.daily, ChartFactory.VersionLabel.none, true, false);
        mockController.setReturnValue(new Chart(null, null, null, EasyMap.build("chart", chartObject)));

        mockController.replay();
        CreatedVsResolvedChartPortlet portlet = new CreatedVsResolvedChartPortlet(jiraAuthenticationContext,
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
//        mockCreatedVsResolvedChartControl.verify();
        mockChartUtilsControl.verify();
        mockSearchRequestControl.verify();
        mockPortletConfigurationControl.verify();
        mockPortletModuleDescriptorControl.verify();
    }
}
