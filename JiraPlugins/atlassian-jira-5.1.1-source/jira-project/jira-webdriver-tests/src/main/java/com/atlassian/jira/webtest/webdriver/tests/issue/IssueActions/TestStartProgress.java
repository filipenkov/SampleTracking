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

@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
public class TestStartProgress extends BaseJiraWebTest implements TestIssueAction
{

    String key;

    @Inject
    PageBinder pageBinder;

    @Before
    public void setup()
    {
        backdoor.restoreBlankInstance();
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        key = backdoor.issues().createIssue(10001L, "Monkeys everywhere :(").key();
    }

    @Test
    @Override
    public void testFromMenuOnNavigator()
    {
        startProgressOnNavigator(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnNavigator()
    {
        startProgressOnNavigator(ActionTrigger.ACTIONS_DIALOG);
    }

    @Test
    @Override
    public void testFromMenuOnViewIssue()
    {
        startProgressOnViewIssue(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnViewIssue()
    {
        startProgressOnViewIssue(ActionTrigger.ACTIONS_DIALOG);
    }

    private void startProgressOnNavigator(ActionTrigger actionTrigger)
    {
        final AdvancedSearch advancedSearch = jira.goToIssueNavigator();
        final IssueNavigatorResults results = advancedSearch.enterQuery("").submit().getResults();
        results.getSelectedIssue().startProgress(actionTrigger);
        assertEquals(key + " has been updated.", pageBinder.bind(GlobalMessage.class).getMessage());
        assertEquals("In Progress", pageBinder.bind(IssueNavigatorResults.class).getSelectedIssue().getStatus());
    }

    private void startProgressOnViewIssue(ActionTrigger actionTrigger)
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue(key);
        viewIssuePage.startProgress(actionTrigger);
        assertEquals("In Progress", viewIssuePage.getStatus());
    }
}
