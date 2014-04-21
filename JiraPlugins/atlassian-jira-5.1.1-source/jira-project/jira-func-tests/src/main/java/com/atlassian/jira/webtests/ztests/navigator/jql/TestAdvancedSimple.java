package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.assertions.IssueNavigatorAssertions;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Test switching from the advanced JQL view to the basic editing view
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestAdvancedSimple extends AbstractJqlFuncTest
{
    // errors with the JQL then switch to basic
    public void testJQLErrors() throws Exception
    {
        administration.restoreBlankInstance();
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);

        assertInvalidJqlAndSwitchToBasicDoesntFit("project = INVALID");
        assertInvalidJqlAndSwitchToBasicDoesntFit("issuetype = INVALID");
        assertInvalidJqlAndSwitchToBasic("summary ~ \"*INVALID\"", "Invalid start character '*'", createFilterFormParam("query", "*INVALID"));
        assertInvalidJqlAndSwitchToBasic("comment ~ \"*INVALID\"", "Invalid start character '*'", createFilterFormParam("query", "*INVALID"));
        assertInvalidJqlAndSwitchToBasic("description ~ \"*INVALID\"", "Invalid start character '*'", createFilterFormParam("query", "*INVALID"));
        assertInvalidJqlAndSwitchToBasic("environment ~ \"*INVALID\"", "Invalid start character '*'", createFilterFormParam("query", "*INVALID"));
        assertInvalidJqlWarningAndSwitchToBasic("reporter = INVALID", "Could not find username: INVALID", createFilterFormParam("reporter", "INVALID"));
        assertInvalidJqlWarningAndSwitchToBasic("assignee = INVALID", "Could not find username: INVALID", createFilterFormParam("assignee", "INVALID"));
        assertInvalidJqlAndSwitchToBasicDoesntFit("status = INVALID");
        assertInvalidJqlAndSwitchToBasicDoesntFit("resolution = INVALID");
        assertInvalidJqlAndSwitchToBasicDoesntFit("priority = INVALID");
        assertInvalidJqlAndSwitchToBasic("created >= INVALID AND created <= INVALID", "Invalid date format. Please enter the date in the format", createFilterFormParam("created:after", "INVALID"), createFilterFormParam("created:before", "INVALID"));
        assertInvalidJqlAndSwitchToBasic("updated >= INVALID AND updated <= INVALID", "Invalid date format. Please enter the date in the format", createFilterFormParam("updated:after", "INVALID"), createFilterFormParam("updated:before", "INVALID"));
        assertInvalidJqlAndSwitchToBasic("due >= INVALID AND due <= INVALID", "Invalid date format. Please enter the date in the format", createFilterFormParam("duedate:after", "INVALID"), createFilterFormParam("duedate:before", "INVALID"));
        assertInvalidJqlAndSwitchToBasic("resolved >= INVALID AND resolved <= INVALID", "Invalid date format. Please enter the date in the format", createFilterFormParam("resolutiondate:after", "INVALID"), createFilterFormParam("resolutiondate:before", "INVALID"));
        assertInvalidJqlAndSwitchToBasic("workratio >= INVALID AND workratio <= INVALID", "The min limit must be specified using an integer", createFilterFormParam("workratio:min", "INVALID"), createFilterFormParam("workratio:max", "INVALID"));
    }

    public void testSystemWithOneProject() throws Exception
    {
        administration.restoreData("TestSwitchingWithOneProject.xml");
        assertFitsFilterForm("project = HSP AND fixVersion = 10000", createFilterFormParam("fixfor", "10000"));
        assertTooComplex("fixVersion = 10000");
    }
    
    private void assertInvalidJqlAndSwitchToBasic(final String invalidJqlQuery, String errorMessage, IssueNavigatorAssertions.FilterFormParam... params)
    {
        assertInvalidJqlAndSwitchToBasic("jqlerror", invalidJqlQuery, errorMessage, params);
    }

    private void assertInvalidJqlWarningAndSwitchToBasic(final String invalidJqlQuery, String errorMessage, IssueNavigatorAssertions.FilterFormParam... params)
    {
        assertInvalidJqlAndSwitchToBasic("jqlwarning", invalidJqlQuery, errorMessage, params);
    }

    // given an invalid JQL query, execute it, verify there were errors, switch to
    // basic view, and make sure no form elements are filled in
    private void assertInvalidJqlAndSwitchToBasic(String locator, final String invalidJqlQuery, String errorMessage, IssueNavigatorAssertions.FilterFormParam... params)
    {
        navigation.issueNavigator().createSearch(invalidJqlQuery);

        // we don't care about the error messages...we just want there to be an error.
        final IdLocator jqlErrorLocator = new IdLocator(tester, locator);
        assertTrue("No JQL Errors found", jqlErrorLocator.getNodes().length > 0);

        // switch over to basic
        tester.clickLink("switchnavtype");

        for (IssueNavigatorAssertions.FilterFormParam param : params)
        {
            assertFilterFormValue(param);
        }

        text.assertTextPresent(new IdLocator(tester, "issue-filter"), errorMessage);
    }


    // given an invalid JQL query, execute it, verify there were errors, switch to
    // basic view, and make sure no form elements are filled in
    private void assertInvalidJqlAndSwitchToBasicDoesntFit(final String invalidJqlQuery)
    {
        navigation.issueNavigator().createSearch(invalidJqlQuery);

        // we don't care about the error messages...we just want there to be an error.
        final IdLocator jqlErrorLocator = new IdLocator(tester, "jqlerror");
        assertTrue("No JQL Errors found", jqlErrorLocator.getNodes().length > 0);

        assertions.getIssueNavigatorAssertions().assertJqlTooComplex();
    }
}
