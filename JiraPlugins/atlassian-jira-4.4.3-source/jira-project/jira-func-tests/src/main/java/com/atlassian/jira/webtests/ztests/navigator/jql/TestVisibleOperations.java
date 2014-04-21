package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestVisibleOperations extends FuncTestCase
{
    public void testLoadedNotModifiedOwned() throws Exception
    {
        administration.restoreData("TestFilterOperations.xml");

        navigation.issueNavigator().loadFilter(10000l, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        assertOperationsPresent("filtereditshares", "filterviewsubscriptions", "filtersaveas");
        assertOperationsNotPresent("reload", "filtersave", "filtersavenew");

    }

    public void testInvalid()
    {
        administration.restoreData("TestFilterOperations.xml");

        navigation.issueNavigator().createSearch("type = bogus");

        assertOperationsNotPresent("filtersavenew", "filtereditshares", "filterviewsubscriptions", "filtersaveas", "reload", "filtersave", "editinvalid");
    }

    public void testLoadedInvalid()
    {
        administration.restoreData("TestFilterOperations.xml");

        navigation.issueNavigator().loadFilter(10010l, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        assertOperationsNotPresent("filtersavenew", "filtersaveas", "reload", "filtersave", "editinvalid");
        assertOperationsPresent("filtereditshares", "filterviewsubscriptions");
    }

    public void testLoadedModifiedInvalid()
    {
        administration.restoreData("TestFilterOperations.xml");

        navigation.issueNavigator().loadFilter(10000l, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        tester.setWorkingForm("jqlform");
        tester.setFormElement("jqlQuery", "type = bogus");
        tester.submit();

        assertOperationsNotPresent("filtersavenew", "filtersaveas", "filtersave", "editinvalid");
        assertOperationsPresent("filtereditshares", "filterviewsubscriptions", "reload");
    }

    public void testLoadedModifiedInvalidNotOwned()
    {
        administration.restoreData("TestFilterOperations.xml");
        navigation.login(FRED_USERNAME, FRED_PASSWORD);

        navigation.issueNavigator().loadFilter(10000l, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        tester.setWorkingForm("jqlform");
        tester.setFormElement("jqlQuery", "type = bogus");
        tester.submit();

        assertOperationsNotPresent("filtersavenew", "filtersaveas", "filtersave", "editinvalid", "filtereditshares", "filterviewsubscriptions");
        assertOperationsPresent("reload");
    }

    public void testLoadedInvalidNotOwned()
    {
        administration.restoreData("TestFilterOperations.xml");
        navigation.login(FRED_USERNAME, FRED_PASSWORD);

        navigation.issueNavigator().loadFilter(10010l, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        assertOperationsNotPresent("filtersavenew", "filtersaveas", "filtersave", "editinvalid", "filtereditshares", "filterviewsubscriptions", "reload");
    }

    public void testLoadedModifiedOwned() throws Exception
    {
        administration.restoreData("TestFilterOperations.xml");

        navigation.issueNavigator().loadFilter(10000l, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        tester.setWorkingForm("jqlform");
        tester.setFormElement("jqlQuery", "type = 2");
        tester.submit();

        assertOperationsPresent("filtereditshares", "filterviewsubscriptions", "filtersaveas", "reload", "filtersave");
        assertOperationsNotPresent("filtersavenew", "editinvalid");
    }

    public void testLoadedNotModifiedNotOwned() throws Exception
    {
        administration.restoreData("TestFilterOperations.xml");
        navigation.login(FRED_USERNAME, FRED_PASSWORD);

        navigation.issueNavigator().loadFilter(10000l, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        assertTrue("Asserting 'create new' link present", new XPathLocator(tester, "//a[@id='copyasnewfilter']").exists());
        assertOperationsNotPresent("reload", "filtersave", "filtersavenew", "filtereditshares", "filterviewsubscriptions", "filtersaveas", "editinvalid");
    }

    public void testLoadedModifiedNotOwned() throws Exception
    {
        administration.restoreData("TestFilterOperations.xml");
        navigation.login(FRED_USERNAME, FRED_PASSWORD);

        navigation.issueNavigator().loadFilter(10000l, IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        tester.setWorkingForm("jqlform");
        tester.setFormElement("jqlQuery", "type = 2");
        tester.submit();

        assertTrue("Asserting 'create new' link present", new XPathLocator(tester, "//a[@id='copyasnewfilter']").exists());
        assertOperationsPresent("reload");
        assertOperationsNotPresent("filtersave", "filtersavenew", "filtereditshares", "filterviewsubscriptions", "filtersaveas", "editinvalid");
    }

    public void testNotLoaded() throws Exception
    {
        administration.restoreData("TestFilterOperations.xml");

        navigation.issueNavigator().createSearch("bug = type");
        navigation.issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.ADVANCED);

        tester.submit();

        assertOperationsPresent("filtersavenew");
        assertOperationsNotPresent("filtereditshares", "filterviewsubscriptions", "filtersaveas", "reload", "editinvalid");
    }

    private void assertOperationsPresent(final String... operationIds)
    {
        for (String operationId : operationIds)
        {
            assertTrue("Checking operation with link ID: '" + operationId + "' is present.", new XPathLocator(tester, "//div[@id='filteroperations']//a[@id='" + operationId+"']").exists());
        }
    }

    private void assertOperationsNotPresent(final String... operationIds)
    {
        for (String operationId : operationIds)
        {
            assertFalse("Checking operation with link ID: '" + operationId + "' is NOT present.", new XPathLocator(tester, "//div[@id='filteroperations']//a[@id='" + operationId+"']").exists());
        }
    }


}
