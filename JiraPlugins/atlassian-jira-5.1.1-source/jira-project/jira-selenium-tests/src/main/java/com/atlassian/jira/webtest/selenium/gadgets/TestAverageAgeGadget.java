package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.framework.Window;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Selenium test for Average Age Gadget.
 *
 * @since v4.0
 */
@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestAverageAgeGadget extends BaseChartGadgetTest
{
    //All issues are created and resolve (if resolved) at 9pm
    protected static final DateFormat DF_RESTORE_XML = new SimpleDateFormat("yyyy-MM-dd 21:00:00.000");
    private static final String MONKEY_PROJECT = "monkey";
    private static final String MONKEY_PROJECT_KEY = "MKY";
    private static final String HOMOSAPIEN_PROJECT = "homosapien";
    private static final String AVERAGE_AGE_GADGET = "Average Age Chart";
    private static final String ADD_AVERAGE_AGE_GADGET = "Average Age Chart";
    private static final String MONKEY_ID = "project-10001";
    private static final String HOMO_ID = "project-10000";

    private static final DateFormat DF_DAILY_CHART_MAP = new SimpleDateFormat("dd-MMMM-yyyy");

    public void onSetUp()
    {
        super.onSetUp();
        addGadget(ADD_AVERAGE_AGE_GADGET);
    }

    @Override
    protected void restoreGadgetData()
    {
        restoreDataWithReplacedTokens("TestAverageAgeGadget.xml", getReplacements());
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

    public void testAverageAgeGadget() throws InterruptedException
    {
        _testConfiguration();
        _testChartMapAndTable();
        _testProjectLinkValid();
        _testNoIssueData();
        _testPeriods();
    }

    public void _testConfiguration() throws InterruptedException
    {
        clickConfigButton();
        waitForGadgetConfiguration();

        assertThat.textPresent("Project or Saved Filter:");

        assertPeriodFieldPresent();
        assertDaysPreviouslyFieldPresent();
        assertDaysForPeriodValid();
        assertRefreshIntervalFieldPresent();
    }

    public void _testChartMapAndTable()
    {
        final int days = 5;
        final int[] issuesUnresolved = { 1, 1, 3, 2, 2 };
        final int[] avgAge = { 2, 3, 2, 2, 0 };
        final int[] totalAge = { 2, 3, 6, 4, 0 };

        /* Because issues are created at 9pm and there should be 2 issues unresolved,
           if the age of each issue in the current period is > 12 hours, this will change
           the total age of the period. This depends on the time the test is run.
        */
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 9)
        {
            totalAge[4] = 2;
            avgAge[4] = 1;
        }
        else if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 21)
        {
            totalAge[4] = 3;
            avgAge[4] = 1;
        }
        else
        {
            totalAge[4] = 4;
            avgAge[4] = 2;
        }

        configureGadget(MONKEY_PROJECT, MONKEY_ID, DAILY_PERIOD, days);

        Calendar period = Calendar.getInstance();
        period.add(Calendar.DAY_OF_YEAR, (days * -1) + 1);
        for (int i = 0; i < days; i++)
        {
            assertChartMapAndTablePresent(DF_DAILY_CHART_MAP.format(period.getTime()), DF_DAILY_CHART.format(period.getTime()), issuesUnresolved[i], totalAge[i], avgAge[i]);
            period.add(Calendar.DAY_OF_YEAR, 1);
        }

        final String baseUrl = getBaseUrl();

        assertThat.elementPresentByTimeout("css=div#chart", 10000);
        assertThat.textPresent("This chart shows the average number of days issues were unresolved for over a given period.");
        assertThat.textPresent("Period: last 5 days (grouped Daily)");
    }

    public void _testNoIssueData()
    {
        final int days = 5;
        configureGadget(HOMOSAPIEN_PROJECT, HOMO_ID, DAILY_PERIOD, days);

        Calendar period = Calendar.getInstance();
        period.add(Calendar.DAY_OF_YEAR, (days * -1) + 1);

        final Calendar current = Calendar.getInstance();
        while (period.before(current) || period.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR))
        {
            assertThat.textNotPresent(DF_DAILY_CHART.format(period.getTime()));
            period.add(Calendar.YEAR, 1);
        }
    }

    public void _testProjectLinkValid()
    {
        // JRA-19112: test that link to issues doesn't go to Browse Project but Issue Navigator instead
        String projAnchorLocator = "//div[@id='chart']//a";
        assertThat.elementPresent(projAnchorLocator);
        String projUrl = getSeleniumClient().getAttribute(projAnchorLocator + "/@href");
        Window.openAndSelect(client, projUrl, MONKEY_PROJECT_KEY);
        String title = getSeleniumClient().getTitle();

        assertTrue("Window title [" + title + "] does not contain 'Issue Navigator'", title.contains("Issue Navigator - JIRA Gadgets Testing"));
        assertThat.elementContainsText("css=#filter-summary", MONKEY_PROJECT);

        Window.close(client, MONKEY_PROJECT_KEY);
        selectGadget(AVERAGE_AGE_GADGET);
    }

    public void _testPeriods() throws InterruptedException
    {
        _testAllPeriods(AVERAGE_AGE_GADGET);
    }

    private void assertChartMapAndTablePresent(String periodMap, final String periodTable, int issuesUnresolved, int totalAge, int avgAge)
    {
        String locator;

        // period as used in the map is a different format to the one used in the table
        locator = "css=map>area[title=" + periodMap + ": " + avgAge + " days unresolved]";
        assertThat.elementPresentByTimeout(locator, 50000);

        locator = "jquery=table td:contains('" + periodTable + "') + td:contains('" + issuesUnresolved + "') + td:contains('" + totalAge + "') + td:contains('" + avgAge + "')";
        assertThat.elementPresentByTimeout(locator, 50000);
    }

    private void configureGadget(String projectHint, String projectId, String period, int days)
    {
        clickConfigButton();
        waitForGadgetConfiguration();

        //selectProjectOrFilterFromAutoComplete("quickfind", projectHint, projectId);
        final String clickTarget = projectId + "_" + "quickfind" + "_listitem";
        // NOTE: must use type because selenium will not send !
        client.type("quickfind", projectHint);
        // Send a right arrow with an event to kick the js in
        client.keyPress("quickfind", "\\16");
        assertThat.visibleByTimeout(clickTarget, TIMEOUT);
        client.click(clickTarget);

        client.select("periodName", period);
        client.type("daysprevious", "" + days);
        client.select("refresh", REFRESH_NEVER);

        submitGadgetConfig();
        waitForGadgetView("chart");

        maximizeGadget(AVERAGE_AGE_GADGET);
        waitForGadgetView("chart");
    }
}
