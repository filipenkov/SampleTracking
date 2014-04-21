package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.io.UnsupportedEncodingException;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestIssueNavigatorLoadFilter extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestIssueNavigatorLoadFilter.xml");
    }

    public void testIssueNavigatorRetainSimpleSearch() throws Exception
    {
        long jqlInValidDoesntFit      = 10000;//"invalidDoesntFit";     "\"New Component 5\" OR project = homosapien"
        long jqlInValidFits           = 10001;//"invalidFits";          "reporter in membersOf(\"blub\")"
        long jqlValidDoesnFit         = 10002;//"validDoesntFit";       "component = \"New Component 5\""
        long jqlValidAndFits          = 10003;//"validFits";            "component = \"New Component 2\" AND project = homosapien"

        executeNavigatorJqlQuery(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, jqlInValidDoesntFit, "The value 'New Component 5' does not exist for the field 'component'.");
        executeNavigatorJqlQuery(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, IssueNavigatorNavigation.NavigatorEditMode.SIMPLE,   jqlInValidFits, "Could not find group: blub");

        executeNavigatorJqlQuery(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, null, jqlValidDoesnFit);
        assertIssues("HSP-1");
        executeNavigatorJqlQuery(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, null, jqlValidAndFits);
        assertIssues("HSP-1");
    }

    public void testIssueNavigatorRetainAdvancedSearch() throws Exception
    {
        long jqlInValidDoesntFit      = 10000;//"invalidDoesntFit";     "\"New Component 5\" OR project = homosapien"
        long jqlInValidFits           = 10001;//"invalidFits";          "reporter in membersOf(\"blub\")"
        long jqlValidDoesnFit         = 10002;//"validDoesntFit";       "component = \"New Component 5\""
        long jqlValidAndFits          = 10003;//"validFits";            "component = \"New Component 2\" AND project = homosapien"

        executeNavigatorJqlQuery(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, jqlInValidDoesntFit, "The value 'New Component 5' does not exist for the field 'component'.");
        executeNavigatorJqlQuery(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED,   jqlInValidFits, "Function 'membersOf' can not generate a list of usernames for group 'blub'; the group does not exist.");

        executeNavigatorJqlQuery(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, null, jqlValidDoesnFit);
        assertIssues("HSP-1");
        executeNavigatorJqlQuery(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, null, jqlValidAndFits);
        assertIssues("HSP-1");
    }

    private void executeNavigatorJqlQuery(final IssueNavigatorNavigation.NavigatorEditMode startEditMode, final IssueNavigatorNavigation.NavigatorEditMode expectedEditMode, final long filter, String... expectedErrors) throws UnsupportedEncodingException
    {
        if(navigation.issueNavigator().getCurrentEditMode() != startEditMode)
        {
           navigation.issueNavigator().displayAllIssues();
           navigation.issueNavigator().gotoEditMode(startEditMode);
        }

        navigation.issueNavigator().loadFilter(filter, null);

        assertEquals(IssueNavigatorNavigation.NavigatorMode.SUMMARY, navigation.issueNavigator().getCurrentMode());

        if (expectedEditMode != null)
        {
            tester.clickLink("editfilter");
            assertEquals(IssueNavigatorNavigation.NavigatorMode.EDIT, navigation.issueNavigator().getCurrentMode());
            assertEquals(expectedEditMode, navigation.issueNavigator().getCurrentEditMode());
            if (expectedErrors != null)
            {
                if (navigation.issueNavigator().getCurrentEditMode() == IssueNavigatorNavigation.NavigatorEditMode.ADVANCED)
                {
                    assertions.getIssueNavigatorAssertions().assertJqlErrors(expectedErrors);
                }
                else
                {
                    for (String expectedError : expectedErrors)
                    {
                        text.assertTextPresent(tester.getDialog().getResponseText(), expectedError);
                    }
                }
            }
        }
    }
}
