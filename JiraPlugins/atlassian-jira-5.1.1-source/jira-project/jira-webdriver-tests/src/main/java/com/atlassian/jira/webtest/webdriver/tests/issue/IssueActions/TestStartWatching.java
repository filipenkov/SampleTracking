package com.atlassian.jira.webtest.webdriver.tests.issue.IssueActions;

import com.atlassian.integrationtesting.runner.restore.Restore;
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
import static org.junit.Assert.assertTrue;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.0
 */
@Restore ("xml/TestStopProgress.xml")
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
public class TestStartWatching extends BaseJiraWebTest implements TestIssueAction
{
    @Inject
    PageBinder pageBinder;

    String key;

    @Before
    public void setup()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        backdoor.restoreBlankInstance();
        jira.gotoLoginPage().login("jiradev", "jiradev", DashboardPage.class);
        key = backdoor.issues().createIssue(10001L, "Monkeys everywhere :(").key();
    }

    @Test
    @Override
    public void testFromMenuOnNavigator()
    {
        startWatchingOnIssueNavigator(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnNavigator()
    {
        startWatchingOnIssueNavigator(ActionTrigger.ACTIONS_DIALOG);
    }

    @Test
    @Override
    public void testFromMenuOnViewIssue()
    {
        startWatchingOnViewIssue(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnViewIssue()
    {
        startWatchingOnViewIssue(ActionTrigger.ACTIONS_DIALOG);
    }

    private void startWatchingOnIssueNavigator(ActionTrigger actionTrigger)
    {
        final AdvancedSearch advancedSearch = jira.goToIssueNavigator();
        final IssueNavigatorResults results = advancedSearch.enterQuery("").submit().getResults();
        results.getSelectedIssue().startWatching(actionTrigger);
        assertEquals("MKY-1 has been updated.", pageBinder.bind(GlobalMessage.class).getMessage());
    }

    private void startWatchingOnViewIssue(ActionTrigger actionTrigger)
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("MKY-1");
        viewIssuePage.startWatching(actionTrigger);
    }
}
