package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.WebResponseUtil;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

@WebTest ({ Category.FUNC_TEST, Category.REPORTS, Category.TIME_TRACKING })
public class TestTimeTrackingExcelReport extends JIRAWebTest
{
    private static String EXCEL_REPORT_URL = "/secure/ConfigureReport!excelView.jspa?selectedProjectId=10000&reportKey=com.atlassian.jira.plugin.system.reports:time-tracking&Next=Next&versionId=10001&sortingOrder=least&completedFilter=all";

    public TestTimeTrackingExcelReport(String name)
    {
        super(name);
    }

    public void testTimeTrackingExcelReport() throws SAXException
    {
        //same values as used by TestTimeTrackingReport
        restoreData("TestTimeTrackingReport.xml");
        gotoPage("/secure/ConfigureReport!excelView.jspa?Next=Next&versionId=-1&reportKey=com.atlassian.jira.plugin.system.reports:time-tracking&sortingOrder=least&selectedProjectId=10000&completedFilter=all");

        if (!WebResponseUtil.replaceResponseContentType(getDialog().getResponse(), "text/html"))
        {
            fail("Failed to replace response content type with 'text/html'");
        }

        WebTable reportTable = getDialog().getResponse().getTableWithID("time_tracking_report_table");
        assertTableRowEquals(reportTable, 3, new Object[]{"Bug", "HSP-1", "Open", "Major", "massive bug", "14400", "8370", "6030", "0"});
        assertTableRowEquals(reportTable, 4, new Object[]{"Bug", "HSP-2", "Open", "Major", "bug2", "1440", "1230", "210", "0"});
        assertTableRowEquals(reportTable, 5, new Object[]{"", "", "", "", "Total", "15840", "9600", "6240", "0", "0"});
    }

    public void testInternationalizationOfExcelReport()
    {
        restoreData("TestTimeTrackingExcelReport.xml");
        activateTimeTracking();

        try
        {
            //need to change the content type to "text/html" so that HTTPUnit understands the response.
            //This can only be done from within the com.meterware.httpunit package and we therefore use the
            //WebResponseUtil class.
            if (!WebResponseUtil.replaceResponseContentType(getDialog().getResponse(), "text/html"))
            {
                log("Failed to replace response content type with 'text/html'");
                fail();
            }
            else
            {
                gotoPage(EXCEL_REPORT_URL);

                // English issue types
                assertTextPresent("Bug");
                assertTextPresent("Improvement");

                // English statuses
                assertTextPresent("In Progress");
                assertTextPresent("Open");

                // English priorities
                assertTextPresent("Blocker");
                assertTextPresent("Trivial");

                // Slovak issue types
                assertTextNotPresent("Chyba");
                assertTextNotPresent("Vylepsenie");

                // Slovak statuses
                assertTextNotPresent("V progrese");
                assertTextNotPresent("Otvorena");

                // Slovak priorities
                assertTextNotPresent("Zastavca");
                assertTextNotPresent("Jednoducha");
            }
        }
        catch (Exception e)
        {
            log("Failed to parse the printable view", e);
            fail();
        }

        // change locale to Slovak
        gotoPage("/secure/Dashboard.jspa");
        setLocaleTo("Slovak (Slovakia)");

        try
        {
            //need to change the content type to "text/html" so that HTTPUnit understands the response.
            //This can only be done from within the com.meterware.httpunit package and we therefore use the
            //WebResponseUtil class.
            if (!WebResponseUtil.replaceResponseContentType(getDialog().getResponse(), "text/html"))
            {
                log("Failed to replace response content type with 'text/html'");
                fail();
            }
            else
            {
                gotoPage(EXCEL_REPORT_URL);

                // Slovak issue types
                assertTextPresent("Chyba");
                assertTextPresent("Vylepsenie");

                // Slovak statuses
                assertTextPresent("V progrese");
                assertTextPresent("Otvorena");

                // Slovak priorities
                assertTextPresent("Zastavca");
                assertTextPresent("Jednoducha");

                // English issue types
                assertTextNotPresent("Bug");
                assertTextNotPresent("Improvement");

                // English statuses
                assertTextNotPresent("In Progress");
                assertTextNotPresent("Open");

                // English priorities
                assertTextNotPresent("Blocker");
                assertTextNotPresent("Trivial");
            }
        }
        catch (Exception e)
        {
            log("Failed to parse the printable view", e);
            fail();
        }

    }

    public void testExcelReportResponseCanBeCached() throws SAXException
    {
        //same values as used by TestTimeTrackingReport
        restoreData("TestTimeTrackingReport.xml");
        gotoPage("/secure/ConfigureReport!excelView.jspa?Next=Next&versionId=-1&reportKey=com.atlassian.jira.plugin.system.reports:time-tracking&sortingOrder=least&selectedProjectId=10000&completedFilter=all");

        if (!WebResponseUtil.replaceResponseContentType(getDialog().getResponse(), "text/html"))
        {
            fail("Failed to replace response content type with 'text/html'");
        }

        // JRA-14030: Make sure the Excel report response does not set Cache-control: no-cache headers
        assertResponseCanBeCached();
    }

    private void setLocaleTo(String localeName)
    {
        navigation.userProfile().gotoCurrentUserProfile();
        tester.clickLink("edit_prefs_lnk");
        selectOption("userLocale", localeName);
        submit();
    }

}
