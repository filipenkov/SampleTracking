package com.atlassian.jira.webtest.webdriver.tests.issue.IssueActions;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.elements.GlobalMessage;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.navigator.IssueNavigatorResults;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ActionTrigger;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.pages.viewissue.link.activity.Comment;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.PageBinder;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
public class TestComment extends BaseJiraWebTest implements TestIssueAction, KeyboardShortcutAction
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
        createCommentOnIssueNavigator(ActionTrigger.KEYBOARD_SHORTCUT);
    }

    @Test
    @Override
    public void testShortcutOnViewIssue()
    {
        createCommentOnViewIssue(ActionTrigger.KEYBOARD_SHORTCUT);
    }

    @Test
    @Override
    public void testFromMenuOnNavigator()
    {
        createCommentOnIssueNavigator(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnNavigator()
    {
        createCommentOnIssueNavigator(ActionTrigger.ACTIONS_DIALOG);
    }

    @Test
    @Override
    public void testFromMenuOnViewIssue()
    {
        createCommentOnViewIssue(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnViewIssue()
    {
        createCommentOnViewIssue(ActionTrigger.ACTIONS_DIALOG);
    }

    private void createCommentOnIssueNavigator(ActionTrigger actionTrigger)
    {
        final AdvancedSearch advancedSearch = jira.goToIssueNavigator();
        final IssueNavigatorResults results = advancedSearch.enterQuery("").submit().getResults();
        results.getSelectedIssue().addComment("Hello Mate!", actionTrigger);
        assertEquals(key + " has been updated with your comment.", pageBinder.bind(GlobalMessage.class).getMessage());
    }

    private void createCommentOnViewIssue(ActionTrigger actionTrigger)
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue(key);
        final String myComment = "Hello Mate!";
        viewIssuePage.addComment(myComment, actionTrigger);
        assertThat(viewIssuePage.getComments(), hasItem(new Comment(myComment)));
    }
}