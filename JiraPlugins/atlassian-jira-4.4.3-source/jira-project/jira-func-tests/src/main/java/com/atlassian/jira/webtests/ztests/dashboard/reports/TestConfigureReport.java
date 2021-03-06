package com.atlassian.jira.webtests.ztests.dashboard.reports;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.REPORTS })
public class TestConfigureReport extends FuncTestCase
{

    /** JRA-13939 */
    public void testConfigureReportWithInvalidReportKey()
    {
        administration.restoreBlankInstance();
        //first go to a valid configure report URL.
        tester.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:singlelevelgroupby");
        tester.assertTextPresent("Report: Single Level Group By Report");
        //now lets go to an invalid URL as may be submitted by a shit crawler
        tester.gotoPage("/secure/ConfigureReport!default.jspa?selectedProjectId=10000&amp;reportKey=com.atlassian.jira.plugin.system.reports:singlelevelgroupby");
        assertions.getJiraFormAssertions().assertFormErrMsg("No report was specified.  The 'reportKey' parameter was empty.");

        //lets try to execute a good report.
        tester.gotoPage("/secure/ConfigureReport.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:singlelevelgroupby");
        //fails because not filters are setup.
        text.assertTextSequence(new WebPageLocator(tester),
                new String[] { "Report:", "Single Level Group By Report",
                               "Filter is a required field" });

        //lets try to execute a report with a stuffed URL.
        tester.gotoPage("/secure/ConfigureReport.jspa?selectedProjectId=10000&amp;reportKey=com.atlassian.jira.plugin.system.reports:singlelevelgroupby");
        assertions.getJiraFormAssertions().assertFormErrMsg("No report was specified.  The 'reportKey' parameter was empty.");
    }

    public void testConfigureReportXSS()
    {
        administration.restoreData("TestConfigureReport.xml");

        tester.gotoPage("/secure/ConfigureReport.jspa?atl_token=" + page.getXsrfToken() + "&versionId=10000&displayUnknown=yes&subtaskInclusion='all\"/><script>alert(4452)</script>'&selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:version-workload&Next=Next");
        tester.assertTextNotPresent("'all\"/><script>alert(4452)</script>'");
        tester.assertTextPresent("&#39;all&quot;/&gt;&lt;script&gt;alert(4452)&lt;/script&gt;&#39;");

        tester.gotoPage("/secure/ConfigureReport.jspa?atl_token=Pi7Pim9fcC&versionId=10000&sortingOrder=least&completedFilter=all&subtaskInclusion='all\"/><script>alert(4452)</script>'&selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:time-tracking&Next=Next");
        tester.assertTextNotPresent("'all\"/><script>alert(4452)</script>'");
        tester.assertTextPresent("&#39;all&quot;/&gt;&lt;script&gt;alert(4452)&lt;/script&gt;&#39;");
    }

}
