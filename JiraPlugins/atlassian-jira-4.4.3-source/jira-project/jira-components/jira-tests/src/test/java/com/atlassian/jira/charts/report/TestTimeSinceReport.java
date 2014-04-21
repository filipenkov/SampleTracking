package com.atlassian.jira.charts.report;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.charts.Chart;
import com.atlassian.jira.charts.ChartFactory;
import com.atlassian.jira.charts.jfreechart.TimePeriodUtils;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.charts.util.ChartUtilsImpl;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import org.easymock.MockControl;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.MockClassControl;
import org.easymock.internal.AlwaysMatcher;

import java.util.Map;
import java.util.TimeZone;

import static com.atlassian.jira.charts.ChartFactory.PeriodName;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;

public class TestTimeSinceReport extends AbstractChartReportTestCase
{
    private TimeZoneManager timeZoneManager;

    public AbstractChartReport getChartReport()
    {
        return new CreatedVsResolvedReport(null, (ApplicationProperties) applicationProperties.proxy(), (ProjectManager) projectManager.proxy(),
                (SearchRequestService) searchRequestService.proxy(),
                null, null, EasyMock.createMock(TimeZoneManager.class));
    }

    public void testDaysPreviousValidation()
    {
        _testDaysPreviousValidation();
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
        ChartFactory.ChartContext context = new ChartFactory.ChartContext(null, mockSearchRequest,  800, 500);
        mockChartFactory.generateTimeSinceChart(context, 30, PeriodName.weekly, false, "updated");
        mockController.setReturnValue(new Chart(null, null, null, EasyMap.build("chart", chartObject)));

        mockController.replay();
        TimeSinceReport timeSinceReport =
                new TimeSinceReport(jiraAuthenticationContext, mockApplicationProperties, null, null,
                        mockChartUtils, mockChartFactory, timeZoneManager);

        MockControl mockReportModuleDescriptorControl = MockClassControl.createControl(ReportModuleDescriptor.class);
        ReportModuleDescriptor mockReportModuleDescriptor = (ReportModuleDescriptor) mockReportModuleDescriptorControl.getMock();

        mockReportModuleDescriptor.getHtml("view", EasyMap.build("user", null,
                "indexing", true, "action", null, "report", timeSinceReport,
                "chart", chartObject, "projectOrFilterId", "filter-123", "timePeriods", new TimePeriodUtils(timeZoneManager)));
        mockReportModuleDescriptorControl.setReturnValue("<html>chart</html>");

        mockReportModuleDescriptorControl.replay();

        timeSinceReport.init(mockReportModuleDescriptor);

        Map<String, Object> params = EasyMap.build("projectOrFilterId", "filter-123", "periodName", "weekly", "dateField", "updated");

        final String html = timeSinceReport.generateReportHtml(null, params);
        assertEquals("<html>chart</html>", html);

        mockController.verify();
        mockReportModuleDescriptorControl.verify();
        mockSearchRequestControl.verify();
        mockChartUtilsControl.verify();
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        timeZoneManager = EasyMock.createMock(TimeZoneManager.class);
        expect(timeZoneManager.getLoggedInUserTimeZone()).andStubReturn(TimeZone.getDefault());
        replay(timeZoneManager);
    }
}