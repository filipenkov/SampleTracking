package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.webtests.JIRAWebTest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Base class for testing the standard charts in gadgets
 *
 * @since v4.0
 */
public abstract class BaseChartGadgetTest extends GadgetTest
{

    protected static final DateFormat DF_DAILY_CHART = new SimpleDateFormat("d-MMMM-yyyy");
    protected static final DateFormat DF_HOURLY_CHART = new SimpleDateFormat("EEE MMM dd HH:00:00 z yyyy");
    protected static final DateFormat DF_WEEKLY_CHART = new SimpleDateFormat("'Week' w, yyyy");
    protected static final DateFormat DF_MONTHLY_CHART = new SimpleDateFormat("MMMM yyyy");
    protected static final DateFormat DF_QUARTERLY_CHART = new SimpleDateFormat("/yyyy");
    protected static final DateFormat DF_YEAR_CHART = new SimpleDateFormat("yyyy");
    protected static final String DAILY_PERIOD = "Daily";
    protected static final String HOURLY_PERIOD = "Hourly";
    protected static final String WEEKLY_PERIOD = "Weekly";
    protected static final String MONTHLY_PERIOD = "Monthly";
    protected static final String QUARTERLY_PERIOD = "Quarterly";
    protected static final String YEARLY_PERIOD = "Yearly";
    protected static final String REFRESH_NEVER = "Never";
    private static final String SUMMARY = "test summary";
    private static final String ADMIN = "admin";
    private static final String EAGLE_KEY = "EAG";
    private static final String EAGLE = "eagle";
    private static final String HAWK = "hawk";
    private static final String HAWK_KEY = "HAW";
    private static final String EAGLE_ID = "project-10010";

    public void _testAllPeriods(String gadgetName) throws InterruptedException
    {
        getWebUnitTest().getAdministration().project().addProject(EAGLE, EAGLE_KEY, ADMIN);
        getWebUnitTest().getAdministration().project().addProject(HAWK, HAWK_KEY, ADMIN);
        getWebUnitTest().getNavigation().issue().createIssue(EAGLE, JIRAWebTest.ISSUE_TYPE_BUG, SUMMARY);

        gotoDashboard();

        selectGadget(gadgetName);
        _testHourlyPeriod(gadgetName);
        _testDailyPeriod(gadgetName);
        _testWeeklyPeriod(gadgetName);
        _testMonthlyPeriod(gadgetName);
    }

    public void _testHourlyPeriod(String gadgetName)
    {
        final int days = 3;
        configureGadget(gadgetName, EAGLE, EAGLE_ID, HOURLY_PERIOD, days);

        Calendar period = Calendar.getInstance();
        period.add(Calendar.DAY_OF_YEAR, (days * -1) + 1);

        period.add(Calendar.HOUR, -1);
        assertThat.textNotPresentByTimeout(DF_HOURLY_CHART.format(period.getTime()), TIMEOUT);
        period.add(Calendar.HOUR, 1);

        final Calendar current = Calendar.getInstance();
        while (period.before(current) || period.get(Calendar.HOUR_OF_DAY) == current.get(Calendar.HOUR_OF_DAY))
        {
            assertThat.textPresentByTimeout(DF_HOURLY_CHART.format(period.getTime()), TIMEOUT);
            period.add(Calendar.HOUR, 1);
        }

        assertThat.textNotPresent(DF_HOURLY_CHART.format(period.getTime()));
    }

    public void _testDailyPeriod(String gadgetName)
    {
        final int days = 7;
        configureGadget(gadgetName, EAGLE, EAGLE_ID, DAILY_PERIOD, days);

        Calendar period = Calendar.getInstance();
        period.add(Calendar.DAY_OF_YEAR, (days * -1));

        assertThat.textNotPresentByTimeout(DF_DAILY_CHART.format(period.getTime()), TIMEOUT);
        period.add(Calendar.DAY_OF_YEAR, 1);

        final Calendar current = Calendar.getInstance();
        while (period.before(current) || period.get(Calendar.DAY_OF_YEAR) == current.get(Calendar.DAY_OF_YEAR))
        {
            assertThat.textPresentByTimeout(DF_DAILY_CHART.format(period.getTime()), TIMEOUT);
            period.add(Calendar.DAY_OF_YEAR, 1);
        }

        assertThat.textNotPresent(DF_DAILY_CHART.format(period.getTime()));
    }

    public void _testWeeklyPeriod(String gadgetName)
    {
        final int days = 30;
        configureGadget(gadgetName, EAGLE, EAGLE_ID, WEEKLY_PERIOD, days);

        Calendar period = Calendar.getInstance();
        period.setLenient(true);
        period.add(Calendar.DAY_OF_YEAR, (days * -1) + 1);

        period.add(Calendar.WEEK_OF_YEAR, -1);
        assertThat.textNotPresentByTimeout(DF_WEEKLY_CHART.format(period.getTime()), TIMEOUT);
        period.add(Calendar.WEEK_OF_YEAR, 1);

        final Calendar current = Calendar.getInstance();
        while (period.before(current) || period.get(Calendar.WEEK_OF_YEAR) == current.get(Calendar.WEEK_OF_YEAR))
        {
            // If the start period is in the previous year, i.e. we're at week 1 of the year
            // but the start of the week is in the previous year, doing period.getTime()
            // will give us the previous year, which is wrong since instead of
            // Week 1 2010, we get Week 1 2009. So we set the period to be the 1st day of the
            // year if this is the case.
            if (period.get(Calendar.WEEK_OF_YEAR) == 1 && (period.get(Calendar.YEAR) < current.get(Calendar.YEAR)))
            {
                period.set(period.get(Calendar.YEAR) + 1, 0, 1);
            }
            assertThat.textPresentByTimeout(DF_WEEKLY_CHART.format(period.getTime()), TIMEOUT);

            if (period.get(Calendar.WEEK_OF_YEAR) == 52)
            {
                period.set(period.get(Calendar.YEAR) + 1, 0, 1);
            }

           period.add(Calendar.WEEK_OF_YEAR, 1);
        }

        assertThat.textNotPresent(DF_WEEKLY_CHART.format(period.getTime()));
    }

    public void _testMonthlyPeriod(String gadgetName)
    {
        final int days = 120;
        configureGadget(gadgetName, EAGLE, EAGLE_ID, MONTHLY_PERIOD, days);

        Calendar period = Calendar.getInstance();
        period.add(Calendar.DAY_OF_YEAR, (days * -1) + 1);

        period.add(Calendar.MONTH, -1);
        assertThat.textNotPresentByTimeout(DF_MONTHLY_CHART.format(period.getTime()), TIMEOUT);
        period.add(Calendar.MONTH, 1);

        final Calendar current = Calendar.getInstance();
        while (period.before(current) || period.get(Calendar.MONTH) == current.get(Calendar.MONTH))
        {
            assertThat.textPresentByTimeout(DF_MONTHLY_CHART.format(period.getTime()), TIMEOUT);
            if (period.get(Calendar.MONTH) == 12)
            {
                period.add(Calendar.YEAR, 1);
            }
            period.add(Calendar.MONTH, 1);
        }

        assertThat.textNotPresent(DF_MONTHLY_CHART.format(period.getTime()));
    }

    public void _testQuarterlyPeriod(String gadgetName)
    {
        final int days = 600;
        configureGadget(gadgetName, EAGLE, EAGLE_ID, QUARTERLY_PERIOD, days);

        Calendar period = Calendar.getInstance();
        period.add(Calendar.DAY_OF_YEAR, (days * -1) + 1);

        period.add(Calendar.MONTH, -3);
        assertThat.textNotPresentByTimeout("Q" + whichQuarter(period) + DF_QUARTERLY_CHART.format(period.getTime()), TIMEOUT);
        period.add(Calendar.MONTH, 3);

        final Calendar current = Calendar.getInstance();
        while (period.before(current) || whichQuarter(period) == whichQuarter(period))
        {
            assertThat.textPresentByTimeout("Q" + whichQuarter(period) + DF_QUARTERLY_CHART.format(period.getTime()), TIMEOUT);
            period.add(Calendar.MONTH, 3);
        }

        assertThat.textNotPresent("Q" + whichQuarter(period) + DF_QUARTERLY_CHART.format(period.getTime()));
    }

    public void _testYearlyPeriod(String gadgetName)
    {
        final int days = 1000;
        configureGadget(gadgetName, EAGLE, EAGLE_ID, YEARLY_PERIOD, days);

        Calendar period = Calendar.getInstance();
        period.add(Calendar.DAY_OF_YEAR, (days * -1) + 1);

        period.add(Calendar.YEAR, -1);
        assertThat.textNotPresentByTimeout(DF_YEAR_CHART.format(period.getTime()), TIMEOUT);
        period.add(Calendar.YEAR, 1);

        final Calendar current = Calendar.getInstance();
        while (period.before(current) || period.get(Calendar.YEAR) == current.get(Calendar.YEAR))
        {
            assertThat.textPresentByTimeout(DF_YEAR_CHART.format(period.getTime()), TIMEOUT);
            period.add(Calendar.YEAR, 1);
        }

        assertThat.textNotPresent(DF_YEAR_CHART.format(period.getTime()));
    }

    private void configureGadget(String gadgetName, String projectName, String projectId, String period, int days)
    {
        clickConfigButton();
        waitForGadgetConfiguration();

        selectProjectOrFilterFromAutoComplete("quickfind", projectName, projectId);
        client.select("periodName", period);
        client.type("daysprevious", "" + days);
        client.select("refresh", REFRESH_NEVER);

        submitGadgetConfig();
        waitForGadgetView("chart");

        maximizeGadget(gadgetName);
        waitForGadgetView("chart");
    }

    private int whichQuarter(Calendar date)
    {
        if (date.get(Calendar.MONTH) < Calendar.APRIL)
        {
            return 1;
        }
        else if (date.get(Calendar.MONTH) < Calendar.JULY)
        {
            return 2;
        }
        else if (date.get(Calendar.MONTH) < Calendar.OCTOBER)
        {
            return 3;
        }
        else
        {
            return 4;
        }
    }

    protected void gotoDashboard()
    {
        client.selectFrame("relative=top");
        getNavigator().currentDashboard().view();
    }

    protected void assertDaysForPeriodValid()
    {
        //assuming default is 'daily'
        setTextField("daysprevious", "301");
        submitGadgetConfig();
        final String expectedError = "Days must not exceed 300 for daily period";
        assertThat.elementPresentByTimeout("jquery=div.error:contains('" + expectedError + "')", TIMEOUT);

    }
}
