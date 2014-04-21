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
public class TestAssignIssue extends BaseJiraWebTest implements TestIssueAction, KeyboardShortcutAction
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
        backdoor.usersAndGroups().addUserToGroup("fred", "jira-developers");
    }

    @Test
    @Override
    public void testShortcutOnNavigator()
    {
        changeAssigneeOnNavigator(ActionTrigger.KEYBOARD_SHORTCUT);
    }

    @Test
    @Override
    public void testShortcutOnViewIssue()
    {
        changeAssigneeOnViewIssue(ActionTrigger.KEYBOARD_SHORTCUT);
    }

    @Test
    @Override
    public void testFromMenuOnNavigator()
    {
        changeAssigneeOnNavigator(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnNavigator()
    {
        changeAssigneeOnNavigator(ActionTrigger.ACTIONS_DIALOG);
    }

    @Test
    @Override
    public void testFromMenuOnViewIssue()
    {
        changeAssigneeOnViewIssue(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnViewIssue()
    {
        changeAssigneeOnViewIssue(ActionTrigger.ACTIONS_DIALOG);
    }


    private void changeAssigneeOnNavigator(ActionTrigger actionTrigger)
    {
        final AdvancedSearch advancedSearch = jira.goToIssueNavigator();
        final IssueNavigatorResults results = advancedSearch.enterQuery("").submit().getResults();
        results.getSelectedIssue().assignIssue("fred", actionTrigger);
        assertEquals(key + " has been assigned.", pageBinder.bind(GlobalMessage.class).getMessage());
        assertEquals("Fred Normal", pageBinder.bind(IssueNavigatorResults.class).getSelectedIssue().getAssignee());
    }

    private void changeAssigneeOnViewIssue(ActionTrigger actionTrigger)
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue(key);
        viewIssuePage.assignIssue("fred", actionTrigger);
        assertEquals("Fred Normal", viewIssuePage.getPeopleSection().getAssignee());
    }
}
