package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.Quarantine;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Selenium test for the Resolution Time Gadget.
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
@Quarantine
public class TestResolutionTimeGadget extends GadgetTest
{
    private static final long MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;
    private static final DateFormat OFBIZ_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
    private static final String CW = "selenium.browserbot.getCurrentWindow()";
    private static final String RESOLUTION_TIME = "Resolution Time";

    public void testConfiguration()
    {
        restoreData("BaseGadgetData.xml");
        loginAsAdmin();
        addGadget(RESOLUTION_TIME);
        waitForGadgetConfiguration();

        assertThat.textPresent("Project or Saved Filter:");

        assertThat.textPresent("Period:");
        assertThat.textPresent("The length of periods represented on the graph.");
        assertThat.textPresent("Hourly");
        assertThat.textPresent("Daily");
        assertThat.textPresent("Weekly");
        assertThat.textPresent("Monthly");
        assertThat.textPresent("Quarterly");
        assertThat.textPresent("Yearly");

        assertThat.textPresent("Days Previous");
        assertThat.textPresent("Days (including today) to show in the graph.");
        assertThat.textPresent("30");

        assertThat.textPresent("How often you would like this gadget to update");
        assertThat.textPresent("Refresh Interval:");
        assertThat.textPresent("Never");
        assertThat.textPresent("Every 15 Minutes");
        assertThat.textPresent("Every 30 Minutes");
        assertThat.textPresent("Every 1 Hour");
        assertThat.textPresent("Every 2 Hours");

        String[] periodValues = { "hourly", "daily", "weekly", "monthly", "quarterly", "yearly" };
        assertFieldOptionValuesPresent("periodName", periodValues);
        String[] periodLabels = { "Hourly", "Daily", "Weekly", "Monthly", "Quarterly", "Yearly" };
        assertFieldOptionLabelsPresent("periodName", periodLabels);

        assertTextFieldError("daysprevious", "-44", "Days must be greater or equal to zero");
        assertTextFieldError("daysprevious", "fruitbat", "Days must be a number");

        //assuming default is 'daily'
        setTextField("daysprevious", "301");
        submitGadgetConfig();
        final String expectedError = "Days must not exceed 300 for daily period";
        assertThat.elementPresentByTimeout("jquery=div.error:contains('" + expectedError + "')", TIMEOUT);
    }

    public void testData() throws IOException
    {
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("@createddate@", daysAgo(15));
        replacements.put("@createddateold@", daysAgo(26));
        for (int i = 1; i <= 12; i++)
        {
            replacements.put("@resolutiondate" + i + "@", daysAgo(i));
        }
        replacements.put("@resolutiondate13@", daysAgo(3)); // make it a non uniform
        restoreDataWithReplacedTokens("ResolutionTimeGadget.xml", replacements);
        loginAsAdmin();
        addGadget(RESOLUTION_TIME);
        waitForGadgetConfiguration();
        selectProjectOrFilterFromAutoComplete("quickfind", "monkey", "project-10001");
        client.selectOption("periodName", "Daily");
        client.type("daysprevious", "15");
        submitGadgetConfig();
        waitForGadgetView("chart");

        waitForjQuerySelectorMatch("area[title*=\": 3 days to resolve\"]");
        waitForjQuerySelectorMatch("area[title*=\": 4 days to resolve\"]");
        waitForjQuerySelectorMatch("area[title*=\": 5 days to resolve\"]");
        waitForjQuerySelectorMatch("area[title*=\": 6 days to resolve\"]");
        waitForjQuerySelectorMatch("area[title*=\": 7 days to resolve\"]");
        waitForjQuerySelectorMatch("area[title*=\": 8 days to resolve\"]");
        waitForjQuerySelectorMatch("area[title*=\": 9 days to resolve\"]");
        waitForjQuerySelectorMatch("area[title*=\": 10 days to resolve\"]");
        waitForjQuerySelectorMatch("area[title*=\": 11 days to resolve\"]");
        waitForjQuerySelectorMatch("area[title*=\": 17 days to resolve\"]");
        waitForjQuerySelectorMatch("area[title*=\": 13 days to resolve\"]");
        waitForjQuerySelectorMatch("area[title*=\": 14 days to resolve\"]");

        // detailed data table should not be present for the (default) profile view
        assertThat.elementNotPresent("table[class=aui]");

        maximizeGadget(RESOLUTION_TIME);
        waitForGadgetView("chart");

        waitForjQuerySelectorMatch("table.aui");
        waitForjQuerySelectorMatch("table.aui tr:first th:nth-child(1):contains(\"Period\")"); // TODO wrong wrong
        waitForjQuerySelectorMatch("table.aui tr:first th:nth-child(2):contains(\"Issues Resolved\")");
        waitForjQuerySelectorMatch("table.aui tr:first th:nth-child(3):contains(\"Resolution Time\")");
        waitForjQuerySelectorMatch("table.aui tr:first th:nth-child(4):contains(\"Avg. Resolution Time\")");
    }

    void waitForjQuerySelectorMatch(String selector)
    {
        client.waitForCondition(CW + ".jQuery('" + selector + "').length === 1", 2000);
    }

    private String daysAgo(final int i)
    {
        return OFBIZ_DATE_FORMAT.format(new Date(System.currentTimeMillis() - MILLIS_IN_A_DAY * i));
    }
}
