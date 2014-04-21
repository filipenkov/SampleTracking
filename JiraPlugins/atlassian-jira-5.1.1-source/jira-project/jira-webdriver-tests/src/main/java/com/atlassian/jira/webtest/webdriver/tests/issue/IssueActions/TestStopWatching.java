package com.atlassian.jira.webtest.webdriver.tests.issue.IssueActions;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.elements.GlobalMessage;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.navigator.IssueNavigatorResults;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ActionTrigger;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.PageBinder;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
public class TestStopWatching extends BaseJiraWebTest implements TestIssueAction
{
    @Inject
    PageBinder pageBinder;

    String key;

    @Before
    public void setup()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        backdoor.restoreBlankInstance();
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        key = backdoor.issues().createIssue(10001L, "Monkeys everywhere :(").key();
    }

    @Test
    @Override
    public void testFromMenuOnNavigator()
    {
        stopWatchingOnIssueNavigator(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnNavigator()
    {
        stopWatchingOnIssueNavigator(ActionTrigger.ACTIONS_DIALOG);
    }

    @Test
    @Override
    public void testFromMenuOnViewIssue()
    {
        stopWatchingOnViewIssue(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnViewIssue()
    {
        stopWatchingOnViewIssue(ActionTrigger.ACTIONS_DIALOG);
    }

    private void stopWatchingOnIssueNavigator(ActionTrigger actionTrigger)
    {
        final AdvancedSearch advancedSearch = jira.goToIssueNavigator();
        final IssueNavigatorResults results = advancedSearch.enterQuery("").submit().getResults();
        results.getSelectedIssue().stopWatching(actionTrigger);
        assertEquals("MKY-1 has been updated.", pageBinder.bind(GlobalMessage.class).getMessage());
    }

    private void stopWatchingOnViewIssue(ActionTrigger actionTrigger)
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("MKY-1");
        viewIssuePage.stopWatching(actionTrigger);
        assertFalse("Administrator is still watching issue", viewIssuePage.openWatchersDialog().getWatchers().contains("Administrator"));
    }
}
