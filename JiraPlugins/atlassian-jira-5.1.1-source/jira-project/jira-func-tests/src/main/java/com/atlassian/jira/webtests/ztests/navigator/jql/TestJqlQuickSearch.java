package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.assertions.IssueNavigatorAssertions;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestJqlQuickSearch extends AbstractJqlFuncTest
{
    public void testQuickSearchDoesntFit() throws Exception
    {
        administration.restoreData("TestJqlQuickSearch.xml");
        // start from advanced

        navigation.issueNavigator().createSearch("");
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        _performQuickSearch("c:comp");

        assertEquals(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, navigation.issueNavigator().getCurrentEditMode());
        text.assertTextPresent(new XPathLocator(tester, "//textarea[@id='jqltext']"), "component = comp");

        // start from simple

        navigation.issueNavigator().createSearch("");
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);

        _performQuickSearch("c:comp");

        assertEquals(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, navigation.issueNavigator().getCurrentEditMode());
        text.assertTextPresent(new XPathLocator(tester, "//textarea[@id='jqltext']"), "component = comp");
        
    }

    public void testQuickSearchDoesFit() throws Exception
    {
        administration.restoreData("TestJqlQuickSearch.xml");
        // start from advanced

        navigation.issueNavigator().createSearch("");
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        _performQuickSearch("MKY");

        assertEquals(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, navigation.issueNavigator().getCurrentEditMode());
        text.assertTextPresent(new XPathLocator(tester, "//textarea[@id='jqltext']"), "project = MKY");

        // start from simple

        navigation.issueNavigator().createSearch("");
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);

        _performQuickSearch("MKY");

        assertEquals(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, navigation.issueNavigator().getCurrentEditMode());
        assertFilterFormValue(new IssueNavigatorAssertions.FilterFormParam("pid", "10001"));

    }

    private void _performQuickSearch(final String searchString)
    {
        tester.setWorkingForm("quicksearch");
        tester.setFormElement("searchString", searchString);
        tester.submit();
    }
}
