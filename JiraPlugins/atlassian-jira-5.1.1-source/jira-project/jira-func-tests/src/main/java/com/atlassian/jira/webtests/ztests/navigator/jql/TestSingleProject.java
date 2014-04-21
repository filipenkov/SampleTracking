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
public class TestSingleProject extends AbstractJqlFuncTest
{
    public void testSingleProjectIsSelectedOnNewFilter() throws Exception
    {
        backdoor.restoreData("TestJqlSingleProject.xml");
        navigation.issueNavigator().gotoNewMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        assertFilterFormValue(new IssueNavigatorAssertions.FilterFormParam("pid", "10001"));
        assertTrue("Project specific custom field is present", new XPathLocator(tester, "//input[@id='searcher-customfield_10000']").exists());

        navigation.issueNavigator().createSearch("type = task");
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        assertFilterFormValue(new IssueNavigatorAssertions.FilterFormParam("pid", "-1"));
        assertFalse("Project specific custom field is NOT present", new XPathLocator(tester, "//input[@id='searcher-customfield_10000']").exists());

        tester.clickLink("new_filter");
        assertFilterFormValue(new IssueNavigatorAssertions.FilterFormParam("pid", "10001"));
        assertTrue("Project specific custom field is present", new XPathLocator(tester, "//input[@id='searcher-customfield_10000']").exists());
    }

    public void testNewFilterInsertsProject() throws Exception
    {
        backdoor.restoreData("TestJqlSingleProject.xml");
        navigation.issueNavigator().gotoNewMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);

        tester.setWorkingForm("issue-filter");
        tester.submit("show");

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        text.assertTextPresent(new XPathLocator(tester, "//textarea[@id='jqltext']"), "project = MKY");
    }

    public void testQueryFits() throws Exception
    {
        backdoor.restoreData("TestJqlSingleProject.xml");
        assertFitsFilterForm("type = task", new IssueNavigatorAssertions.FilterFormParam("type", "3"));
        assertFitsFilterForm("project = MKY and type = task", new IssueNavigatorAssertions.FilterFormParam("type", "3"), new IssueNavigatorAssertions.FilterFormParam("pid", "10001"));
    }
}
