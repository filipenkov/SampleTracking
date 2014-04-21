package com.atlassian.jira.charts.report;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.util.ChartUtilsImpl;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.easymock.internal.AlwaysMatcher;

import java.util.Map;

public class TestPieReport extends AbstractChartReportTestCase
{
    public AbstractChartReport getChartReport()
    {
        return new PieReport(null, null, (ProjectManager) projectManager.proxy(),
                (SearchRequestService) searchRequestService.proxy(), null, null);
    }

    public void testProjectOrFilterIdValidation()
    {
        _testProjectOrFilterIdValidation();
    }

    public void testFilterIdValidation()
    {
        _testFilterIdValidation();
    }

    public void testProjectIdValidation()
    {
        _testProjectIdValidation();
    }

     public void testGenerateReportHtml() throws Exception
    {
        final MockController mockController = new MockController();

        final ApplicationProperties mockApplicationProperties = mockController.getMock(ApplicationProperties.class);
        mockApplicationProperties.getOption("jira.option.indexing");
        mockController.setReturnValue(true);

        final JiraAuthenticationContext jiraAuthenticationContext = mockController.getMock(JiraAuthenticationContext.class);
        jiraAuthenticationContext.getUser();
        mockController.setReturnValue(null, 2);

        MockControl mockSearchRequestControl = MockClassControl.createControl(SearchRequest.class);
        SearchRequest mockSearchRequest = (SearchRequest) mockSearchRequestControl.getMock();

        mockSearchRequestControl.replay();

        MockControl mockChartUtilsControl = MockClassControl.createControl(ChartUtilsImpl.class);
        ChartUtils mockChartUtils = (ChartUtils) mockChartUtilsControl.getMock();
        mockChartUtilsControl.setDefaultMatcher(new AlwaysMatcher());
        mockChartUtils.retrieveOrMakeSearchRequest("filter-1000", EasyMap.build("projectOrFilterId", "filter-1000"));
        mockChartUtilsControl.setReturnValue(mockSearchRequest);
        mockChartUtilsControl.replay();

        final Object chartObject = new Object();

        final ChartFactory mockChartFactory = mockController.getMock(ChartFactory.class);
        ChartFactory.ChartContext context = new ChartFactory.ChartContext(null, mockSearchRequest, 800, 500);
        mockChartFactory.generatePieChart(context, "type");
        mockController.setReturnValue(new Chart(null, null, null, EasyMap.build("chart", chartObject)));

        mockController.replay();
        PieReport pieReport =
                new PieReport(jiraAuthenticationContext, mockApplicationProperties, null, null,
                        mockChartUtils, mockChartFactory);

        MockControl mockReportModuleDescriptorControl = MockClassControl.createControl(ReportModuleDescriptor.class);
        ReportModuleDescriptor mockReportModuleDescriptor = (ReportModuleDescriptor) mockReportModuleDescriptorControl.getMock();

        mockReportModuleDescriptor.getHtml("view", EasyMap.build("user", null,
                "indexing", true, "action", null, "report", pieReport,
                "chart", chartObject, "projectOrFilterId", "filter-123"));
        mockReportModuleDescriptorControl.setReturnValue("<html>chart</html>");

        mockReportModuleDescriptorControl.replay();

        pieReport.init(mockReportModuleDescriptor);

        Map<String, Object> params = EasyMap.build("projectOrFilterId", "filter-123", "statistictype", "type");

        final String html = pieReport.generateReportHtml(null, params);
        assertEquals("<html>chart</html>", html);

        mockController.verify();
        mockReportModuleDescriptorControl.verify();
        mockSearchRequestControl.verify();
        mockChartUtilsControl.verify();
    }
}