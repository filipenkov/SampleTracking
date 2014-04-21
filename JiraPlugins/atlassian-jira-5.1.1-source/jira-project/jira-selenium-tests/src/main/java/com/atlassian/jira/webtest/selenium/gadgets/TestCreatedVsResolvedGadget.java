package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.framework.Window;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import org.apache.commons.lang.ArrayUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Selenium Test for the CreatedVsResolvedGadget.
 *
 * @since v4.0
 */
@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestCreatedVsResolvedGadget extends BaseChartGadgetTest
{
    //All issues are created and resolve (if resolved) at 9pm
    protected static final DateFormat DF_RESTORE_XML = new SimpleDateFormat("yyyy-MM-dd 21:00:00.000");
    protected static final DateFormat DF_ISSUE_NAV = new SimpleDateFormat("dd/MMM/yy");
    protected static final DateFormat DF_JQL_DATE = new SimpleDateFormat("yyyy-MM-dd");

    private static final String CW = "selenium.browserbot.getCurrentWindow()";
    private static final String MONKEY_PROJECT = "monkey";
    private static final String CREATED_VS_RESOLVED_GADGET = "Created vs Resolved Chart";
    private static final String ALL_VERSIONS = "All versions";
    private static final String YES = "Yes";
    private static final String NO = "No";
    private static final String MONKEY_PROJECT_ID = "project-10001";
    private static final String MONKEY_PROJECT_KEY = "MKY";
    private static final String MONKEY_ISSUE_KEY = "MKY-";
    private static final int NO_OF_ISSUES = 7;
    private static final int RESOLVED_STATUS = 1;
    private static final int CREATED_STATUS = 0;

    public void onSetUp()
    {
        super.onSetUp();
        backdoor.plugins().disablePlugin("com.atlassian.jira.jira-issue-nav-plugin");
        addGadget(CREATED_VS_RESOLVED_GADGET);
    }

    @Override
    protected void restoreGadgetData()
    {
       restoreDataWithReplacedTokens("TestCreatedVsResolvedGadget.xml", getReplacements());
    }

    public void testCreatedVsResolved() throws InterruptedException
    {
        _testConfiguration();
        _testUncumulative();
        _testCumulative();
        _testNoTrend();
        _testProjectAndIssuesLinks();
        _testPeriods();
    }

    public void _testConfiguration()
    {
        waitForGadgetConfiguration();

        assertThat.textPresent("Project or Saved Filter:");

        assertThat.textPresent("Cumulative Totals:");
        assertThat.textPresent("Progressively add totals (1.. 2.. 3), or show individual values (1.. 1.. 1).");
        assertThat.textPresent(YES);
        assertThat.textPresent(NO);

        assertThat.textPresent("Display the Trend of Unresolved:");
        assertThat.textPresent("Show the number of unresolved issues over time in a subplot.");

        assertThat.textPresent("Display Versions:");
        assertThat.textPresent(ALL_VERSIONS);
        assertThat.textPresent("Only major versions");
        assertThat.textPresent("None");

        assertPeriodFieldPresent();
        assertDaysPreviouslyFieldPresent();
        assertDaysForPeriodValid();
        assertRefreshIntervalFieldPresent();
    }

    public void _testUncumulative()
    {
        final int days = 5;
        final int[] created = { 0, 2, 0, 2, 0 };
        final int[] resolved = { 0, 0, 1, 2, 0 };
        final int[] trend = { 0, 2, 1, 1, 1 };

        configureGadget(MONKEY_PROJECT, MONKEY_PROJECT_ID, DAILY_PERIOD, days, NO, YES, ALL_VERSIONS);

        Calendar period = Calendar.getInstance();
        period.add(Calendar.DAY_OF_YEAR, (days * -1) + 1);
        for (int i = 0; i < days; i++)
        {
            assertChartMapAndTablePresent(period, i  +1,  created[i], resolved[i], trend[i], false);
            period.add(Calendar.DAY_OF_YEAR, 1);
        }

        assertThat.elementPresentByTimeout("css=div#chart", 10000);
        assertThat.textPresent("Issues: 4 created and 3 resolved");
        assertThat.textPresent("Period: last 5 days (grouped Daily)");
    }

    public void _testCumulative()
    {
        final int days = 5;
        final int[] created = { 0, 2, 2, 4, 4 };
        final int[] resolved = { 0, 0, 1, 3, 3 };

        configureGadget(MONKEY_PROJECT, MONKEY_PROJECT_ID, DAILY_PERIOD, days, YES, YES, ALL_VERSIONS);

        Calendar period = Calendar.getInstance();
        period.add(Calendar.DAY_OF_YEAR, (days * -1) + 1);
        for (int i = 0; i < days; i++)
        {
            assertChartMapPresent(null, created[i], resolved[i], true);
            period.add(Calendar.DAY_OF_YEAR, 1);
        }

        assertNojQuerySelectorMatch("area[href]");
        assertNojQuerySelectorMatch("table > td[href]");

        assertThat.elementPresentByTimeout("css=div#chart", 10000);
        assertThat.textPresent("Issues: 4 created and 3 resolved");
        assertThat.textPresent("Period: last 5 days (grouped Daily)");
    }

    public void _testNoTrend()
    {
        final int days = 5;

        configureGadget(MONKEY_PROJECT, MONKEY_PROJECT_ID, DAILY_PERIOD, days, NO, NO, ALL_VERSIONS);

        assertThat.textNotPresent("Trend");
    }

    public void _testPeriods() throws InterruptedException
    {
        _testAllPeriods(CREATED_VS_RESOLVED_GADGET);
    }

    public void _testProjectAndIssuesLinks()
    {
        final int days = 5;
        final int[][] createdIssues = { { }, { 5, 4 }, { }, { 6, 7 }, { } };
        final int[][] resolvedIssues = { { }, { }, { 3 }, { 5, 7 }, { } };

        configureGadget(MONKEY_PROJECT, MONKEY_PROJECT_ID, DAILY_PERIOD, days, NO, NO, ALL_VERSIONS);

        assertProjectLinkValid(MONKEY_PROJECT_KEY, MONKEY_PROJECT);

        Calendar period = Calendar.getInstance();
        period.add(Calendar.DAY_OF_YEAR, (days * -1) + 1);
        for (int i = 0; i < days; i++)
        {
            String url = getSeleniumClient().getAttribute("//table//td[contains(., '" + DF_DAILY_CHART.format(period.getTime()) + "')]/following-sibling::td[position()=1]/a/@href");
            assertLinksValidWithIssues(url, DF_ISSUE_NAV.format(period.getTime()), CREATED_STATUS, createdIssues[i]);
            url = getSeleniumClient().getAttribute("//table//td[contains(., '" + DF_DAILY_CHART.format(period.getTime()) + "')]/following-sibling::td[position()=2]/a/@href");
            assertLinksValidWithIssues(url, DF_ISSUE_NAV.format(period.getTime()), RESOLVED_STATUS, resolvedIssues[i]);

            period.add(Calendar.DAY_OF_YEAR, 1);
        }
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
        selectGadget(CREATED_VS_RESOLVED_GADGET);
    }

    private void assertLinksValidWithIssues(String link, String period, int status, int[] issueKeys)
    {
        String winId = period + "-" + status + "-window";
        Window.openAndSelect(getSeleniumClient(), link, winId);

        for (int issueKey : issueKeys)
        {
            String locator;
            if (status == CREATED_STATUS)
            {
                locator = "css=tr[id='issuerow1000" + issueKey + "'] > td.created:contains('" + period + "')";
            }
            else
            {
                locator = "css=tr[id='issuerow1000" + issueKey + "'] > td.status:contains('Resolved')";
            }
            assertThat.textPresent(MONKEY_ISSUE_KEY + issueKey);
            assertThat.elementPresent(locator);
        }

        if (issueKeys.length == 0)
        {
            assertThat.textPresent("No matching issues found.");
        }

        assertUnpresentIssues(issueKeys);

        Window.close(client, winId);
        selectGadget(CREATED_VS_RESOLVED_GADGET);
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

    private void assertChartMapAndTablePresent(Calendar period, int row, int created, int resolved, int trend, boolean isCumulative)
    {
        String locator;
        if (!isCumulative)
        {
            String jqlDate = DF_JQL_DATE.format(period.getTime());
            final String baseUrl = getBaseUrl();
            String jqlCreated = baseUrl + "/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+MKY+AND+created+%3E%3D+" + jqlDate + "+AND+created+%3C%3D+%22" + jqlDate + "+23%3A59%22";
            String jqlResolved = baseUrl + "/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+MKY+AND+resolutiondate+%3E%3D+" + jqlDate + "+AND+resolutiondate+%3C%3D+%22" + jqlDate + "+23%3A59%22";

            assertThat.elementPresentByTimeout("//table//tr[" + row + "]", 10000);
            assertThat.elementHasText("//table//tr[" + row + "]/td[1]", DF_DAILY_CHART.format(period.getTime()));
            assertThat.elementHasText("//table//tr[" + row + " ]/td[2]", "" + created);
            assertThat.attributeContainsValue("//table//tr[" + row + "]/td[2]/a", "href", jqlCreated);

            assertThat.elementHasText("//table//tr[" + row + "]/td[3]", "" + resolved);
            assertThat.attributeContainsValue("//table//tr[" + row + "]/td[3]/a", "href", jqlResolved);

        }

        locator = "jquery=table td:contains('" + DF_DAILY_CHART.format(period.getTime()) + "') + td:contains('" + created + "') + td:contains('" + resolved + "') + td:contains('" + trend + "')";
        assertThat.elementPresentByTimeout(locator, 10000);

        assertChartMapPresent(DF_JQL_DATE.format(period.getTime()), created, resolved, isCumulative);
    }

    private void assertChartMapPresent(String period, int created, int resolved, boolean isCumulative)
    {
        String locator;

        if (isCumulative)
        {
            locator = "css=map>area[title=Resolved " + resolved + " issues]";
            assertThat.elementPresentByTimeout(locator, 10000);

            locator = "css=map>area[title=Created " + created + " issues]";
            assertThat.elementPresentByTimeout(locator, 10000);
        }
        else
        {
            final String baseUrl = getBaseUrl();
            String jqlCreated = baseUrl + "/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+MKY+AND+created+%3E%3D+" + period + "+AND+created+%3C%3D+%22" + period + "+23%3A59%22";
            String jqlResolved = baseUrl + "/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+MKY+AND+resolutiondate+%3E%3D+" + period + "+AND+resolutiondate+%3C%3D+%22" + period + "+23%3A59%22";

            locator = "css=map>area[title=Resolved " + resolved + " issues][href=" + jqlResolved + "]";
            assertThat.elementPresentByTimeout(locator, 10000);

            locator = "css=map>area[title=Created " + created + " issues][href=" + jqlCreated + "]";
            assertThat.elementPresentByTimeout(locator, 10000);
        }
    }

    void assertNojQuerySelectorMatch(String selector)
    {
        client.waitForCondition(CW + ".jQuery('" + selector + "').length === 0", 500);
    }

    private void configureGadget(String projectHint, String projectId, String period, int days, String cumulative, String showUnresolvedTrend, String versions)
    {
        gotoDashboard();
        selectGadget(CREATED_VS_RESOLVED_GADGET);
        clickConfigButton();
        waitForGadgetConfiguration();

        final String clickTarget = projectId + "_quickfind_listitem";
        // NOTE: must use type because selenium will not send !
        client.type("quickfind", projectHint);
        // Send a right arrow with an event to kick the js in
        client.keyPress("quickfind", "\\16");
        assertThat.elementPresentByTimeout(clickTarget, TIMEOUT);
        client.click(clickTarget);

        client.select("periodName", period);
        client.type("daysprevious", "" + days);
        client.select("isCumulative", cumulative);
        client.select("showUnresolvedTrend", showUnresolvedTrend);
        client.select("versionLabel", versions);
        client.select("refresh", REFRESH_NEVER);

        submitGadgetConfig();
        waitForGadgetView("chart");
        maximizeGadget(CREATED_VS_RESOLVED_GADGET);

        assertThat.elementPresentByTimeout("jquery=#chart:visible", 5000);
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
