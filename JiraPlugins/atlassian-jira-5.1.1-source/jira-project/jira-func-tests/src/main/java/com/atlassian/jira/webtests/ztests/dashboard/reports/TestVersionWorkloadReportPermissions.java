package com.atlassian.jira.webtests.ztests.dashboard.reports;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.COMPONENTS_AND_VERSIONS, Category.PERMISSIONS, Category.REPORTS,
        Category.SCHEMES })
public class TestVersionWorkloadReportPermissions extends JIRAWebTest
{

    public TestVersionWorkloadReportPermissions(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();

        restoreData("TestVersionWorkloadReportPermissions.xml");

    }


    public void tearDown()
    {
        restoreBlankInstance();
        super.tearDown();
    }

    public void testPermissions()
    {
        _testBasicPermissions();
        _testParentSubDiffPerms();

    }

    public void _testBasicPermissions()
    {
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoPage("/secure/ConfigureReport.jspa?displayUnknown=yes&versionId=10000&selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports%3Aversion-workload&Next=Next");

        assertTextNotPresent("HSP-1");
        assertTextNotPresent("HSP-2");
        assertTextNotPresent("HSP-3");
        assertTextNotPresent("HSP-4");
        assertTextNotPresent("HSP-5");
        assertTextNotPresent("HSP-6");
        assertTextNotPresent("HSP-7");
        assertTextNotPresent("HSP-8");
        assertTextNotPresent("HSP-9");

        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoPage("/secure/ConfigureReport.jspa?displayUnknown=yes&versionId=10000&selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports%3Aversion-workload&Next=Next");
        assertTextPresent("HSP-1");
        assertTextPresent("HSP-2");
        assertTextPresent("HSP-3");
        assertTextPresent("HSP-4");
        assertTextPresent("HSP-5");
        assertTextPresent("HSP-6");
        assertTextPresent("HSP-7");
        assertTextPresent("HSP-8");
        assertTextNotPresent("HSP-9");
    }

    public void _testParentSubDiffPerms()
    {
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoPage("/secure/ConfigureReport.jspa?displayUnknown=yes&versionId=10010&selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports%3Aversion-workload&Next=Next");

        assertTextPresent("HSP-9");
        assertTextPresent("HSP-13");
        assertTextNotPresent("HSP-10");
        assertTextNotPresent("HSP-14");
        assertTextPresent("HSP-11");
        assertTextNotPresent("HSP-15");
        assertTextPresent("HSP-16");
        assertTextPresent("HSP-12");  // this will fail once JRA-13469 is fixed.  change to assertTextPresent
        assertTextNotPresent("HSP-3");

        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoPage("/secure/ConfigureReport.jspa?displayUnknown=yes&versionId=10010&selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports%3Aversion-workload&Next=Next");
        assertTextPresent("HSP-9");
        assertTextPresent("HSP-10");
        assertTextPresent("HSP-11");
        assertTextPresent("HSP-12");
        assertTextPresent("HSP-13");
        assertTextPresent("HSP-14");
        assertTextPresent("HSP-15");
        assertTextPresent("HSP-16");
        assertTextNotPresent("HSP-3");
    }


}
