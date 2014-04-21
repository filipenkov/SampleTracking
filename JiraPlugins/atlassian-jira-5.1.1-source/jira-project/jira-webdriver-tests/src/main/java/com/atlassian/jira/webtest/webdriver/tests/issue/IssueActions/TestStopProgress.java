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

@Restore ("xml/TestStopProgress.xml")
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
public class TestStopProgress extends BaseJiraWebTest implements TestIssueAction
{

    @Inject
    PageBinder pageBinder;

    @Before
    public void setup()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
    }

    @Test
    @Override
    public void testFromMenuOnNavigator()
    {
        stopProgressOnIssueNavigator(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnNavigator()
    {
        stopProgressOnIssueNavigator(ActionTrigger.ACTIONS_DIALOG);
    }

    @Test
    @Override
    public void testFromMenuOnViewIssue()
    {
        stopProgresssOnViewIsssue(ActionTrigger.MENU);
    }

    @Test
    @Override
    public void testFromActionsDialogOnViewIssue()
    {
        stopProgresssOnViewIsssue(ActionTrigger.ACTIONS_DIALOG);
    }

    private void stopProgressOnIssueNavigator(ActionTrigger actionTrigger)
    {
        final AdvancedSearch advancedSearch = jira.goToIssueNavigator();
        final IssueNavigatorResults results = advancedSearch.enterQuery("").submit().getResults();
        results.getSelectedIssue().stopProgress(actionTrigger);
        assertEquals("MKY-1 has been updated.", pageBinder.bind(GlobalMessage.class).getMessage());
        assertEquals("Open", pageBinder.bind(IssueNavigatorResults.class).getSelectedIssue().getStatus());
    }

    private void stopProgresssOnViewIsssue(ActionTrigger actionTrigger)
    {
        ViewIssuePage viewIssuePage = jira.goToViewIssue("MKY-1");
        viewIssuePage.stopProgress(actionTrigger);
        assertEquals("Open", viewIssuePage.getStatus());
    }
}
