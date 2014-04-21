package com.atlassian.jira.charts.portlet;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.util.ChartUtilsImpl;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.plugin.portlet.PortletModuleDescriptor;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

/**
 * @since v4.0
 */
public abstract class AbstractChartPortletTest extends MockControllerTestCase
{
    protected MockControl mockPortletConfigurationControl;
    protected MockControl mockSearchRequestControl;
    protected MockControl mockChartUtilsControl;
    protected MockControl mockPortletModuleDescriptorControl;

    protected PortletConfiguration mockPortletConfiguration;
    protected JiraAuthenticationContext jiraAuthenticationContext;
    protected ChartUtils mockChartUtils;
    protected ApplicationProperties mockApplicationProperties;
    protected PortletModuleDescriptor mockPortletModuleDescriptor;
    protected ChartFactory mockChartFactory;
    protected Object chartObject;
    protected ChartFactory.ChartContext context;

    protected void setupVelocityParamsMocks() throws Exception
    {
        mockPortletConfigurationControl = MockControl.createControl(PortletConfiguration.class);
        mockPortletConfiguration = (PortletConfiguration) mockPortletConfigurationControl.getMock();
        mockPortletConfiguration.getProperty("projectOrFilterId");
        mockPortletConfigurationControl.setReturnValue("filter-1000");
        mockPortletConfiguration.getProperty("periodName");
        mockPortletConfigurationControl.setReturnValue("daily");
        mockPortletConfiguration.getLongProperty("daysprevious");
        mockPortletConfigurationControl.setReturnValue(new Long(30));
        mockPortletConfigurationControl.replay();

        jiraAuthenticationContext = mockController.getMock(JiraAuthenticationContext.class);
        jiraAuthenticationContext.getUser();
        mockController.setDefaultReturnValue(null);

        mockSearchRequestControl = MockClassControl.createControl(SearchRequest.class);
        SearchRequest mockSearchRequest = (SearchRequest) mockSearchRequestControl.getMock();

        mockSearchRequestControl.replay();

        mockChartUtilsControl = MockClassControl.createControl(ChartUtilsImpl.class);
        mockChartUtils = (ChartUtils) mockChartUtilsControl.getMock();
        mockChartUtils.retrieveOrMakeSearchRequest("filter-1000", EasyMap.build("indexing", true, "loggedin", false, "projectOrFilterId", "filter-1000"));
        mockChartUtilsControl.setReturnValue(mockSearchRequest);
        mockChartUtilsControl.replay();

        mockChartFactory = mockController.getMock(ChartFactory.class);
        context = new ChartFactory.ChartContext(null, mockSearchRequest, 450, 300);

        mockApplicationProperties = mockController.getMock(ApplicationProperties.class);
        mockApplicationProperties.getOption("jira.option.indexing");
        mockController.setReturnValue(true);
    }

    protected void setupGetSearchRequestMocks() throws ObjectConfigurationException
    {
        mockPortletConfigurationControl = MockControl.createControl(PortletConfiguration.class);
        mockPortletConfiguration = (PortletConfiguration) mockPortletConfigurationControl.getMock();
        mockPortletConfiguration.getProperty("projectOrFilterId");
        mockPortletConfigurationControl.setReturnValue("filter-1000");
        mockPortletConfiguration.getProperty("periodName");
        mockPortletConfigurationControl.setReturnValue("daily");
        mockPortletConfiguration.getLongProperty("daysprevious");
        mockPortletConfigurationControl.setReturnValue(new Long(30));
        mockPortletConfigurationControl.replay();

        jiraAuthenticationContext = mockController.getMock(JiraAuthenticationContext.class);
        jiraAuthenticationContext.getUser();
        mockController.setDefaultReturnValue(null);

        mockSearchRequestControl = MockClassControl.createControl(SearchRequest.class);
        SearchRequest mockSearchRequest = (SearchRequest) mockSearchRequestControl.getMock();

        mockSearchRequestControl.replay();

        mockChartUtilsControl = MockClassControl.createControl(ChartUtilsImpl.class);
        mockChartUtils = (ChartUtils) mockChartUtilsControl.getMock();
        mockChartUtils.retrieveOrMakeSearchRequest("filter-1000", EasyMap.build("indexing", true, "loggedin", false, "projectOrFilterId", "filter-1000"));
        mockChartUtilsControl.setReturnValue(mockSearchRequest);
        mockChartUtilsControl.replay();

        chartObject = new Object();

        mockApplicationProperties = mockController.getMock(ApplicationProperties.class);
        mockApplicationProperties.getOption("jira.option.indexing");
        mockController.setReturnValue(true);


        mockPortletModuleDescriptorControl = MockClassControl.createControl(PortletModuleDescriptor.class);
        mockPortletModuleDescriptor = (PortletModuleDescriptor) mockPortletModuleDescriptorControl.getMock();

        mockPortletModuleDescriptor.getHtml("view",
                EasyMap.build("searchRequest", mockSearchRequest, "indexing", true,
                        "showReportLink", false, "chart", chartObject, "loggedin", false, "projectOrFilterId", "filter-1000"));
        mockPortletModuleDescriptorControl.setReturnValue("<html>mychart</html>");

        mockPortletModuleDescriptorControl.replay();

        mockChartFactory = mockController.getMock(ChartFactory.class);
        context = new ChartFactory.ChartContext(null, mockSearchRequest, 450, 300);
    }


}
