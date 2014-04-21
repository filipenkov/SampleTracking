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
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
public class TestEditIssue extends BaseJiraWebTest implements TestIssueAction, KeyboardShortcutAction
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
    public void testShortcutOnNavigator()
    {
        editIssueOnIssueNavigator(ActionTrigger.KEYBOARD_SHORTCUT);
    }

    @Test
    @Override
    public void testShortcutOnViewIssue()
    {
        editIssueOnViewIssue(ActionTrigger.KEYBOARD_SHORTCUT);
    }

    @Test
    @Override
    public void testFromMenuOnNavigator()
    {
        editIssueOnIssueNavigator(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnNavigator()
    {
        editIssueOnIssueNavigator(ActionTrigger.ACTIONS_DIALOG);
    }

    @Test
    @Override
    public void testFromMenuOnViewIssue()
    {
        editIssueOnViewIssue(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnViewIssue()
    {
        editIssueOnViewIssue(ActionTrigger.ACTIONS_DIALOG);
    }

    private void editIssueOnIssueNavigator(ActionTrigger actionTrigger)
    {
        final AdvancedSearch advancedSearch = jira.goToIssueNavigator();
        final IssueNavigatorResults results = advancedSearch.enterQuery("").submit().getResults();
        Map<String, String> vals = new HashMap<String, String>();
        vals.put("summary", "Changed Summary");
        results.getSelectedIssue().editIssue(vals, actionTrigger);
        assertEquals(key + " has been updated.", pageBinder.bind(GlobalMessage.class).getMessage());
    }

    private void editIssueOnViewIssue(ActionTrigger actionTrigger)
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue(key);
        Map<String, String> vals = new HashMap<String, String>();
        vals.put("summary", "Changed Summary");
        viewIssuePage.editIssue(vals, actionTrigger);
        assertEquals("Changed Summary", viewIssuePage.getSummary());
    }
}
