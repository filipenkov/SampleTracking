package com.atlassian.jira.webtest.selenium.gadgets;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

import static com.atlassian.jira.webtests.JIRAWebTest.ISSUE_TYPE_BUG;
import static com.atlassian.jira.webtests.JIRAWebTest.PROJECT_HOMOSAP;

/**
 * Selenium test for the Time Since Gadget.
 *
 * @since v4.0
 */
@SkipInBrowser(browsers={Browser.IE}) //JS Error XSS Protection - Responsibility: Hamish
@WebTest({Category.SELENIUM_TEST })
public class TestTimeSinceGadget extends GadgetTest
{
    @Override
    public void onSetUp()
    {
        super.onSetUp();
        getNavigator().createIssue(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, "Need more men");
        getNavigator().gotoHome();
        addGadget("Time Since Chart");
    }

    public void testConfigAndView()
    {
        _testConfiguration();
        _testPeriodField();
        _testDateField();
        _testDaysPreviously();
    }

    private void _testConfiguration()
    {
        waitForGadgetConfiguration();

        assertThat.textPresent("Project or Saved Filter");

        assertThat.textPresent("Date Field");
        assertThat.textPresent("Created");
        assertThat.textPresent("Due Date");
        assertThat.textPresent("Updated");
        assertThat.textPresent("Resolved");

        assertThat.textPresent("Period");
        assertThat.textPresent("Hourly");
        assertThat.textPresent("Daily");
        assertThat.textPresent("Weekly");
        assertThat.textPresent("Monthly");
        assertThat.textPresent("Quarterly");
        assertThat.textPresent("Yearly");
        // assert default value
        assertThat.attributeContainsValue("daysprevious", "value", "30");

        assertTextFieldError("daysprevious", "-1", "Days must be greater or equal to zero.");
        assertTextFieldError("daysprevious", "NaN", "Days must be a number");

        //reset to default
        setTextField("daysprevious", "30");
    }

    private void _testPeriodField()
    {
        selectProjectOrFilterFromAutoComplete("quickfind", "homo", "project-10000");
        submitGadgetConfig();
        waitForGadgetView("chart");
        assertGadgetTitle("Time Since Chart: homosapien");
        assertThat.textPresent("Daily");

        //hourly can only be set for 10 days or less
        changeTextConfig("daysprevious", "10");
        changeConfig("periodName", "Hourly");
        assertThat.textPresent("Hourly");

        //go back to 30, but we have to set the period to something bigger than hourly first
        changeConfig("periodName", "Weekly");
        changeTextConfig("daysprevious", "30");
        assertThat.textPresent("Weekly");

        changeConfig("periodName", "Monthly");
        assertThat.textPresent("Monthly");

        changeConfig("periodName", "Quarterly");
        assertThat.textPresent("Quarterly");

        changeConfig("periodName", "Yearly");
        assertThat.textPresent("Yearly");
    }

    private void _testDateField()
    {

        changeConfig("dateField", "Created");
        assertThat.textPresent("Created");
        assertThat.textNotPresent("Updated");

        changeConfig("dateField", "Updated");
        assertThat.textPresent("Updated");
        assertThat.textNotPresent("Created");

        changeConfig("dateField", "Resolved");
        assertThat.textPresent("Resolved");
    }

    private void _testDaysPreviously()
    {
        gotoConfig();

        //assuming default is 'yearly'
        setTextField("daysprevious", "999888");
        submitGadgetConfig();
        final String expectedError = "Days must not exceed 36500 for yearly period";
        assertThat.elementPresentByTimeout("jquery=div.error:contains('" + expectedError + "')", 10000);
//        client.waitForCondition("selenium.browserbot.getCurrentWindow().jQuery(\"span:contains('"
//                + expectedError + "')\").length === 1", TIMEOUT);

        setTextField("daysprevious", "21");
        submitChanges();
        assertThat.elementPresentByTimeout("jquery=p:contains('Period: last 21 days')");
//        client.waitForCondition("selenium.browserbot.getCurrentWindow().jQuery(\"p:contains('Period: last 21 days')\").length === 1", 10000);
    }

    private void changeTextConfig(String field, String value)
    {
        gotoConfig();
        setTextField(field, value);
        submitChanges();
    }

    private void changeConfig(String field, String value)
    {
        gotoConfig();
        getSeleniumClient().select(field, value);
        submitChanges();
    }

    private void submitChanges()
    {
        submitGadgetConfig();
        waitForGadgetView("chart");
        assertGadgetTitle("Time Since Chart: homosapien");
    }

    private void gotoConfig()
    {
        clickConfigButton();
        waitForGadgetConfiguration();
    }
}
