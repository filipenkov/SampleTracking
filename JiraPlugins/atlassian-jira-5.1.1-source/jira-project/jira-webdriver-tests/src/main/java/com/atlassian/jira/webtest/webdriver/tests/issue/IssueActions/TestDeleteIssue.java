package com.atlassian.jira.webtest.webdriver.tests.issue.IssueActions;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.elements.AuiMessage;
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
import org.openqa.selenium.By;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
public class TestDeleteIssue extends BaseJiraWebTest implements TestIssueAction
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
        deleteIssueOnIssueNav(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnNavigator()
    {
        deleteIssueOnIssueNav(ActionTrigger.ACTIONS_DIALOG);
    }

    @Test
    @Override
    public void testFromMenuOnViewIssue()
    {
        deleteIssueOnViewIssue(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnViewIssue()
    {
        deleteIssueOnViewIssue(ActionTrigger.ACTIONS_DIALOG);
    }

    private void deleteIssueOnIssueNav(ActionTrigger actionTrigger)
    {
        final AdvancedSearch advancedSearch = jira.goToIssueNavigator();
        final IssueNavigatorResults results = advancedSearch.enterQuery("").submit().getResults();
        results.getSelectedIssue().deleteIssue(actionTrigger);
        assertEquals("No matching issues found.", pageBinder.bind(AuiMessage.class, By.className("jqlerror-container")).getMessage());
    }

    private void deleteIssueOnViewIssue(ActionTrigger actionTrigger)
    {

        final AdvancedSearch advancedSearch = jira.goToIssueNavigator();
        IssueNavigatorResults results = advancedSearch.enterQuery("").submit().getResults();
        ViewIssuePage viewIssuePage = jira.goToViewIssue(results.getSelectedIssue().getSelectedIssueKey());
        viewIssuePage.deleteIssue(actionTrigger);
        results = pageBinder.bind(AdvancedSearch.class).enterQuery("").submit().getResults();
        assertEquals("Expected issue to be deleted", 0, results.getTotalCount());

    }
}
