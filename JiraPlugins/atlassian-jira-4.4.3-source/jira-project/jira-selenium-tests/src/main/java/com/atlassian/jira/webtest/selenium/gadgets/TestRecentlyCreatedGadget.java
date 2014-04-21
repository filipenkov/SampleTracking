package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.framework.Window;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Assert;
import org.apache.commons.lang.ArrayUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Selenium test for the Recently Created Gadget.
 */
@SkipInBrowser(browsers={Browser.IE}) //Pop-up not found problem - Responsibility: Hamish
@WebTest({Category.SELENIUM_TEST })
public class TestRecentlyCreatedGadget extends BaseChartGadgetTest
{
    //All issues are created and resolve (if resolved) at 9pm
    protected static final DateFormat DF_RESTORE_XML = new SimpleDateFormat("yyyy-MM-dd 21:00:00.000");
    protected static final DateFormat DF_JQL_DATE = new SimpleDateFormat("yyyy-MM-dd");
    protected static final DateFormat DF_ISSUE_NAV = new SimpleDateFormat("dd/MMM/yy");

    private static final String RECENTLY_CREATED_GADGET = "Recently Created Chart";
    private static final String MONKEY_PROJECT = "monkey";
    private static final String MONKEY_PROJECT_KEY = "MKY";
    private static final String MONKEY_PROJECT_ID = "project-10001";
    private static final String MONKEY_ISSUE_KEY = MONKEY_PROJECT_KEY + "-";
    private static final int NO_OF_ISSUES = 7;
    private static final int RESOLVED_STATUS = 1;
    private static final int UNRESOLVED_STATUS = 0;

    public void onSetUp()
    {
        super.onSetUp();
        addGadget(RECENTLY_CREATED_GADGET);
    }

    @Override
    protected void restoreGadgetData()
    {
        restoreDataWithReplacedTokens("TestRecentlyCreatedGadget.xml", getReplacements());
    }

    public void testRecentlyCreated() throws InterruptedException
    {
        _testConfiguration();
        _testChartMapPresentAndValid();
        
        gotoDashboard();
        selectGadget(RECENTLY_CREATED_GADGET);
        _testProjectAndIssueLinks();
        _testPeriods();
    }

    public void _testConfiguration()
    {
        waitForGadgetConfiguration();
        assertThat.textPresent("Project or Saved Filter:");

        assertPeriodFieldPresent();
        assertDaysPreviouslyFieldPresent();
        assertDaysForPeriodValid();
        assertRefreshIntervalFieldPresent();
    }

    public void _testChartMapPresentAndValid()
    {
        final int days = 5;
        final int[] resolved = { 0, 1, 0, 1, 0 };
        final int[] unresolved = { 0, 1, 0, 1, 0 };
        final int[] created = { 0, 2, 0, 2, 0 };

        configureGadget(MONKEY_PROJECT, MONKEY_PROJECT_ID, DAILY_PERIOD, days);

        // Check image map is linked to image
        final String useMapReference = getSeleniumClient().getAttribute("css=#chart img@usemap");
        Assert.assertNotNull("Image Map Reference missing",useMapReference);
        Assert.assertEquals("Image Map Reference for chart does not begin with a #",'#',useMapReference.charAt(0));
        final String useMapReferenceNoHash = useMapReference.substring(1);
        assertThat.elementPresent("id="+useMapReferenceNoHash);

        Calendar period = Calendar.getInstance();
        period.add(Calendar.DAY_OF_YEAR, (days * -1) + 1);
        for (int i = 0; i < days; i++)
        {
            assertChartMapAndTablePresent(period, resolved[i], unresolved[i], created[i]);
            period.add(Calendar.DAY_OF_YEAR, 1);
        }

        assertThat.textPresent("Issues: 4");
        assertThat.textPresent("Period: last 5 days (grouped Daily)");
        assertThat.elementPresentByTimeout("css=div#chart", 10000);
    }

    public void _testProjectAndIssueLinks()
    {
        final int days = 5;
        final int[][] resolvedIssues = { { }, { 5 }, { }, { 7 }, { } };
        final int[][] unresolvedIssues = { { }, { 4 }, { }, { 6 }, { } };

        configureGadget(MONKEY_PROJECT, MONKEY_PROJECT_ID, DAILY_PERIOD, days);

        assertProjectLinkValid(MONKEY_PROJECT_KEY, MONKEY_PROJECT);

        Calendar period = Calendar.getInstance();
        period.add(Calendar.DAY_OF_YEAR, (days * -1) + 1);
        for (int i = 0; i < days; i++)
        {
            String url = getSeleniumClient().getAttribute("//table//td[contains(., '" + DF_DAILY_CHART.format(period.getTime()) + "')]/following-sibling::td[position()=1]/a/@href");
            assertLinksValidWithIssues(url, DF_ISSUE_NAV.format(period.getTime()), RESOLVED_STATUS, resolvedIssues[i]);
            url = getSeleniumClient().getAttribute("//table//td[contains(., '" + DF_DAILY_CHART.format(period.getTime()) + "')]/following-sibling::td[position()=2]/a/@href");
            assertLinksValidWithIssues(url, DF_ISSUE_NAV.format(period.getTime()), UNRESOLVED_STATUS, unresolvedIssues[i]);

            period.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    public void _testPeriods() throws InterruptedException
    {
        _testAllPeriods(RECENTLY_CREATED_GADGET);
    }

    private void assertLinksValidWithIssues(String link, String period, int status, int[] issueKeys)
    {
        String winId = period + "-" + status + "-window";
        Window.openAndSelect(getSeleniumClient(), link, winId);

        for (int issueKey : issueKeys)
        {
            String locator;
            if (status == RESOLVED_STATUS)
            {
                locator = "css=tr[id='issuerow1000" + issueKey + "'] > td.status:contains('Resolved')";
            }
            else
            {
                locator = "css=tr[id='issuerow1000" + issueKey + "'] > td.resolution:contains('Unresolved')";
            }
            assertThat.textPresent(MONKEY_ISSUE_KEY + issueKey);
            assertThat.elementPresent(locator);
        }

        if (issueKeys.length == 0)
        {
            assertThat.textPresent("No matching issues found.");
        }

        assertUnpresentIssues(issueKeys);

        Window.close(getSeleniumClient(), winId);
        selectGadget(RECENTLY_CREATED_GADGET);
    }

    private void assertUnpresentIssues(int[] issueKeys)
    {
        for (int i = 1; i <= NO_OF_ISSUES; i++)
        {
            if (!ArrayUtils.contains(issueKeys, i))
            {
                assertThat.textNotPresent(MONKEY_PROJECT_KEY + i);
            }
        }
    }

    private void assertChartMapAndTablePresent(Calendar period, int resolved, int unresolved, int created)
    {
        String jqlDate = DF_JQL_DATE.format(period.getTime());
        final String baseUrl = getBaseUrl();
        String jqlResolved = baseUrl + "/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+MKY+AND+created+%3E%3D+" + jqlDate + "+AND+created+%3C%3D+%22" + jqlDate + "+23%3A59%22+AND+NOT+resolution+%3D+Unresolved";
        String jqlUnresolved = baseUrl + "/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+MKY+AND+created+%3E%3D+" + jqlDate + "+AND+created+%3C%3D+%22" + jqlDate + "+23%3A59%22+AND+resolution+%3D+Unresolved";
        String locator;

        final String dailyDate = DF_DAILY_CHART.format(period.getTime());

        assertThat.elementPresentByTimeout("//table//tr[1]", 20000);

        locator = "//table//td[contains(., '" + dailyDate + "')]/following-sibling::td[position()=1]/a[@href='" + jqlResolved + "']";
        assertThat.elementPresent(locator);
        locator = "//table//td[contains(., '" + dailyDate + "')]/following-sibling::td[position()=2]/a[@href='" + jqlUnresolved + "']";
        assertThat.elementPresent(locator);

        locator = "jquery=table td:contains('" + dailyDate + "') + td:contains('" + resolved + "') + td:contains('" + unresolved + "') + td:contains('" + created + "')";
        assertThat.elementPresent(locator);

        locator = "css=map>area[title=" + dailyDate + ": " + resolved + " / " + created + " issues resolved.][href=" + jqlResolved + "]";
        assertThat.elementPresent(locator);
        locator = "css=map>area[title=" + dailyDate + ": " + unresolved + " / " + created + " issues unresolved.][href=" + jqlUnresolved + "]";
        assertThat.elementPresent(locator);
    }

    private void assertProjectLinkValid(final String projKey, final String projName)
    {
        // JRA-19112: test that link to issues doesn't go to Browse Project but Issue Navigator instead
        String projAnchorLocator = "//div[@id='chart']//a";
        assertThat.elementPresent(projAnchorLocator);
        String projUrl = getSeleniumClient().getAttribute(projAnchorLocator + "/@href");
        Window.openAndSelect(getSeleniumClient(), projUrl, projKey);
        String title = getSeleniumClient().getTitle();

        assertTrue("Window title [" + title + "] does not contain 'Issue Navigator'", title.contains("Issue Navigator - JIRA Gadgets Testing"));
        assertThat.elementContainsText("css=#filter-summary", projName);

        Window.close(client, projKey);
        selectGadget(RECENTLY_CREATED_GADGET);
    }

    private void configureGadget(String projectHint, String projectId, String period, int days)
    {
        clickConfigButton();
        waitForGadgetConfiguration();

        final String clickTarget = projectId + "_" + "quickfind" + "_listitem";
        client.type("quickfind", projectHint);
        client.keyPress("quickfind", "\\16");
        if (projectId != null)
        {
            assertThat.visibleByTimeout(clickTarget, TIMEOUT);
        }
        client.click(clickTarget);

        client.select("periodName", period);
        client.type("daysprevious", "" + days);
        client.select("refresh", REFRESH_NEVER);

        submitGadgetConfig();
        waitForGadgetView("chart");

        maximizeGadget(RECENTLY_CREATED_GADGET);
        visibleByTimeoutWithDelay("//div[@id='chart']", 20000);
    }

    private Map<String, String> getReplacements()
    {
        Calendar date;
        final Map<String, String> replacements = new HashMap<String, String>();
        int[] relativeDays = { 0, -100, -31, -6, -3, -3, -1, -1 };  //Creation day relative to today
        int[] age = { 0, 80, 10, 4, -1, 2, -1, 0 };  //Age of issues, -1 for unresolved issues

        for (int i = 1; i < relativeDays.length; i++)
        {
            date = Calendar.getInstance();
            date.add(Calendar.DAY_OF_YEAR, relativeDays[i]);
            replacements.put("@issue_date_" + i + "@", DF_RESTORE_XML.format(date.getTime()));
            if (age[i] >= 0)
            {
                date = Calendar.getInstance();
                date.add(Calendar.DAY_OF_YEAR, relativeDays[i] + age[i]);
                replacements.put("@resolve_date_" + i + "@", DF_RESTORE_XML.format(date.getTime()));
            }
        }

        return replacements;
    }
}
