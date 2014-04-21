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
import java.util.Arrays;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
public class TestLabels extends BaseJiraWebTest implements TestIssueAction, KeyboardShortcutAction
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
        changeLabelsOnNavigator(ActionTrigger.KEYBOARD_SHORTCUT);
    }

    @Test
    @Override
    public void testShortcutOnViewIssue()
    {
        changeLabelsOnViewIssue(ActionTrigger.KEYBOARD_SHORTCUT);
    }

    @Test
    @Override
    public void testFromMenuOnNavigator()
    {
        changeLabelsOnNavigator(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnNavigator()
    {
        changeLabelsOnNavigator(ActionTrigger.ACTIONS_DIALOG);
    }

    @Test
    @Override
    public void testFromMenuOnViewIssue()
    {
        changeLabelsOnViewIssue(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnViewIssue()
    {
        changeLabelsOnViewIssue(ActionTrigger.ACTIONS_DIALOG);
    }

    private void changeLabelsOnNavigator(ActionTrigger actionTrigger)
    {
        final AdvancedSearch advancedSearch = jira.goToIssueNavigator();
        final IssueNavigatorResults results = advancedSearch.enterQuery("").submit().getResults();
        results.getSelectedIssue().addLabels(Arrays.asList("one", "two"), actionTrigger);
        assertEquals("The labels on " + key + " have been updated.", pageBinder.bind(GlobalMessage.class).getMessage());
    }

    private void changeLabelsOnViewIssue(ActionTrigger actionTrigger)
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue(key);
        viewIssuePage.addLabels(Arrays.asList("one", "two"), actionTrigger);
        assertEquals(asList("one", "two"), viewIssuePage.getDetailsSection().getLabels());
    }
}
