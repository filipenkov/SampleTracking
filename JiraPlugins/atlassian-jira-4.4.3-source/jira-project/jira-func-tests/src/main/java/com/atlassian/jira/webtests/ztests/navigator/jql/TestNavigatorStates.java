package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import junit.framework.Assert;

/**
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestNavigatorStates extends FuncTestCase
{
    /**
     * JRA-17840: check that we stay in the advanced navigation mode instead of being forced to simple mode
     * when a filter has errors in it
     */
    public void testStayInAdvancedWhenLoadingFilterWithErrors()
    {
        administration.restoreData("TestIllegalJqlFunctionParameters.xml");
        navigation.issueNavigator().gotoNewMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);
        navigation.issueNavigator().loadFilter(10014, null);
        assertEquals(IssueNavigatorNavigation.NavigatorMode.SUMMARY, navigation.issueNavigator().getCurrentMode());
        tester.clickLink("editfilter");
        Assert.assertEquals(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, navigation.issueNavigator().getCurrentEditMode());
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@id='filter-switch']/li", "simple");
        tester.clickLink("switchnavtype");
        Assert.assertEquals(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE, navigation.issueNavigator().getCurrentEditMode());
    }

    public void testCreateNewLinkDoesntAppearOnEdit() throws Exception
    {
        administration.restoreBlankInstance();
        navigation.gotoPage("/secure/IssueNavigator.jspa");
        text.assertTextNotPresent(new IdLocator(tester, "filter-description"), "Create new");

        tester.clickLink("viewfilter");
        text.assertTextPresent(new IdLocator(tester, "filter-description"), "Create new");

        tester.clickLink("new_filter");
        text.assertTextNotPresent(new IdLocator(tester, "filter-description"), "Create new");

        tester.clickLink("switchnavtype");
        text.assertTextNotPresent(new IdLocator(tester, "filter-description"), "Create new");
    }

    public void testEmptySearchAndOrderSummary() throws Exception
    {
        administration.restoreBlankInstance();
        navigation.issueNavigator().createSearch("");
        navigation.issueNavigator().gotoViewMode();
        text.assertTextPresent(new XPathLocator(tester, "//div[@id='filter-summary']"), "All Issues");
        text.assertTextSequence(new XPathLocator(tester, "//div[@id='filter-summary']"), "Sorted by", "Key descending");

        navigation.issueNavigator().createSearch("type = bug");
        navigation.issueNavigator().gotoViewMode();
        text.assertTextSequence(new XPathLocator(tester, "//div[@id='filter-summary']"), "Issue Type", "Bug");
        text.assertTextSequence(new XPathLocator(tester, "//div[@id='filter-summary']"), "Sorted by", "Key descending");

        navigation.issueNavigator().createSearch("order by type asc");
        navigation.issueNavigator().gotoViewMode();
        text.assertTextSequence(new XPathLocator(tester, "//div[@id='filter-summary']"), "All Issues");
        text.assertTextSequence(new XPathLocator(tester, "//div[@id='filter-summary']"), "Sorted by", " Issue Type ascending");
        
        navigation.issueNavigator().createSearch("type = bug order by type asc");
        navigation.issueNavigator().gotoViewMode();
        text.assertTextSequence(new XPathLocator(tester, "//div[@id='filter-summary']"), "Issue Type", "Bug");
        text.assertTextSequence(new XPathLocator(tester, "//div[@id='filter-summary']"), "Sorted by", " Issue Type ascending");
    }

    public void testExecuteAdvancedCommandSetsNavigatorEditState() throws Exception
    {
        administration.restoreBlankInstance();
        navigation.issueNavigator().createSearch("");
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);

        navigation.gotoPage("/secure/IssueNavigator!executeAdvanced.jspa?clear=true&runQuery=true&jqlQuery=project+%3D+hsp+or+type+%3D+bug");

        assertEquals(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, navigation.issueNavigator().getCurrentEditMode());

        text.assertTextPresent(new XPathLocator(tester, "//ul[@id='filter-switch']"), "This query is too complex to display in Simple mode.");
    }
}
