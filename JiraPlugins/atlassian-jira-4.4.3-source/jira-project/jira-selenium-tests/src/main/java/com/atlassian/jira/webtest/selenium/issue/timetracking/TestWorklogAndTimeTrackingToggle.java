package com.atlassian.jira.webtest.selenium.issue.timetracking;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.auidialog.AbstractAuiDialogTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.Condition;
import com.atlassian.selenium.SkipInBrowser;
import com.thoughtworks.selenium.Selenium;

/**
 * A basic Selenium test to assert the toggling functionality between Log Work and Remaining Estimate in the
 * WorklogSystemField.
 *
 * @since v4.2
 */
@SkipInBrowser(browsers={Browser.IE}) //Pop-up Issue - Responsibility: Hamish
@WebTest({Category.SELENIUM_TEST })
public class TestWorklogAndTimeTrackingToggle extends AbstractAuiDialogTest
{
    private static final int WAIT = 5000;

    public void testToggleInMultipleDialogs() throws Exception
    {
        restoreData("TestWorklogAndTimeTrackingToggle.xml");

        getNavigator().gotoIssue("HSP-1");

        // open the Resolve Dialog
        client.clickLinkWithText("Resolve Issue", false);
        client.waitForAjaxWithJquery(WAIT);

        _testToggle(".aui-dialog-open");

        // close the Resolve Dialog
        closeDialogByEscape();

        // open the Close Dialog
        client.click("opsbar-transitions_more");
        client.waitForAjaxWithJquery(WAIT);
        client.clickLinkWithText("Close Issue", false);
        client.waitForAjaxWithJquery(WAIT);

        _testToggle(".aui-dialog-open");

        // close the Close Dialog
        closeDialogByEscape();
    }

    public void testToggleInEdit() throws Exception
    {
        restoreData("TestWorklogAndTimeTrackingToggle.xml");

        getNavigator().editIssue("HSP-1");

        _testToggle("body");
    }

    private void _testToggle(final String containerSelector)
    {
        // by default the Log Work checkbox should not be checked
        assertCheckboxInOffState(containerSelector);

        // enter a value in the Remaining Estimate
        client.typeInElementWithName("timetracking", "77h");

        // toggle the checkbox
        clickCheckbox(containerSelector);

        // now we see the Log Work form
        assertCheckboxInOnState(containerSelector);

        // enter a value in the Time Logged
        client.typeInElementWithName("worklog_timeLogged", "82h");

        // toggle back to Remaining Estimate
        clickCheckbox(containerSelector);

        // visibility flips again
        assertCheckboxInOffState(containerSelector);

        // previously entered value should be there
        assertEquals("77h", client.getValue("timetracking"));

        // flip one more time
        clickCheckbox(containerSelector);

        assertCheckboxInOnState(containerSelector);

        // previously entered value should be there
        assertEquals("82h", client.getValue("worklog_timeLogged"));
    }

    private void assertCheckboxInOnState(final String containerSelector)
    {
        assertThat.byTimeout(createCheckboxCondition(Boolean.TRUE, containerSelector), WAIT);
        assertThat.elementNotVisible(String.format("jquery=%s #worklog-timetrackingcontainer", containerSelector));
        assertThat.elementVisible(String.format("jquery=%s #worklog-logworkcontainer", containerSelector));
    }

    private void assertCheckboxInOffState(final String containerSelector)
    {
        assertThat.byTimeout(createCheckboxCondition(Boolean.FALSE, containerSelector), WAIT);
        assertThat.elementVisible(String.format("jquery=%s #worklog-timetrackingcontainer", containerSelector));
        assertThat.elementNotVisible(String.format("jquery=%s #worklog-logworkcontainer", containerSelector));
    }

    private void clickCheckbox(final String containerSelector)
    {
        client.click(String.format("jquery=%s #log-work-activate", containerSelector));
    }

    private Condition createCheckboxCondition(final Boolean state, final String containerSelector)
    {
        return new Condition()
        {
            public boolean executeTest(final Selenium selenium)
            {
                return selenium.getEval(String.format("window.jQuery('%s #log-work-activate').attr('checked')", containerSelector)).equalsIgnoreCase(state.toString());
            }

            public String errorMessage()
            {
                return "Value of checkbox was supposed to be '" + state + "'";
            }
        };
    }
}
