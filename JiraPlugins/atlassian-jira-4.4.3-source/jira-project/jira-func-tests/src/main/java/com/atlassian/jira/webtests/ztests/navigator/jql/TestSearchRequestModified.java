package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestSearchRequestModified extends FuncTestCase
{
    public void testSimpleViewDoesNotResetQueryString() throws Exception
    {
        administration.restoreData("TestSearchRequestModified.xml");
        _testJqlIsntModifiedAfterSearchingOnSimple("type    =    bug");
        _testJqlIsntModifiedAfterSearchingOnSimple("project    =    HSP");
        _testJqlIsntModifiedAfterSearchingOnSimple("project    =    HSP and fixVersion = 'New Version 1'");
        _testJqlIsntModifiedAfterSearchingOnSimple("project    =    HSP AND affectedVersion = 'New Version 1'");
        _testJqlIsntModifiedAfterSearchingOnSimple("project   =    HSP AND component = 'New Component 1'");
        _testJqlIsntModifiedAfterSearchingOnSimple("status = 'open'");
        _testJqlIsntModifiedAfterSearchingOnSimple("resolution   =   'fixed'");
        _testJqlIsntModifiedAfterSearchingOnSimple("priority   =   'Major'");
        _testJqlIsntModifiedAfterSearchingOnSimple("created   >=    \"2009/08/26\"");
        _testJqlIsntModifiedAfterSearchingOnSimple("updated   >=    \"2009/08/26\"");
        _testJqlIsntModifiedAfterSearchingOnSimple("resolved   >=    \"2009/08/26\"");
    }

    public void testLoadedFilterShowsModified() throws Exception
    {
        administration.restoreData("TestSearchRequestModified.xml");
        navigation.issueNavigator().loadFilter(10000l, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertNotModified();
        text.assertTextPresent(new XPathLocator(tester, "//textarea[@id='jqltext']"), "type = bug");

        tester.setWorkingForm("jqlform");
        tester.setFormElement("jqlQuery", "type =   bug");
        tester.submit();
        assertModified();

        navigation.issueNavigator().gotoViewMode();
        tester.clickLink("reload");

        assertNotModified();

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);

        tester.setWorkingForm("issue-filter");
        tester.selectOption("type", "Task");
        tester.submit("show");
        assertModified();

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        assertModified();
        text.assertTextPresent(new XPathLocator(tester, "//textarea[@id='jqltext']"), "issuetype = Task");
    }
    
    private void _testJqlIsntModifiedAfterSearchingOnSimple(String jqlString)
    {
        navigation.issueNavigator().gotoNewMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        navigation.issueNavigator().createSearch(jqlString);

        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        tester.setWorkingForm("issue-filter");
        tester.submit("show");
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        text.assertTextPresent(new XPathLocator(tester, "//textarea[@id='jqltext']"), jqlString);
    }

    private void assertModified()
    {
        text.assertTextPresent(new IdLocator(tester, "filter-description"), "Filter modified since loading");
    }

    private void assertNotModified()
    {
        text.assertTextNotPresent(new IdLocator(tester, "filter-description"), "Filter modified since loading");
    }
}
