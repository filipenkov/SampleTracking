package com.atlassian.jira.webtest.webdriver.tests.navigator;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.menu.IssueActions;
import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.navigator.IssueNavigatorResults;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.PageBinder;
import org.junit.Test;
import javax.inject.Inject;

import static org.junit.Assert.assertTrue;

/**
 * @since v5.1
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUE_NAVIGATOR })
@Restore("xml/TestsSelectedIssueOperations.xml")
public class TestSelectedIssueOperations extends BaseJiraWebTest
{
    
    @Inject
    private PageBinder binder;

    @Test // JRA-26422
    public void testWorkflowOperations()
    {
        AdvancedSearch advancedSearch = jira.gotoLoginPage().loginAsSysAdmin(AdvancedSearch.class)
                .enterQuery("")
                .submit();

        final IssueNavigatorResults results = advancedSearch.getResults();
        results.getSelectedIssue().getActionsMenu().open().clickItem(IssueActions.CLOSE_ISSUE);
        FormDialog dialog = binder.bind(FormDialog.class, "workflow-transition-2-dialog");
        assertTrue("Expected dialog title to contain MKY-2 issue key", dialog.getTitle().contains("MKY-2"));

        dialog.close();
        results.nextIssue().getSelectedIssue().getActionsMenu().open().clickItem(IssueActions.CLOSE_ISSUE);
        dialog = binder.bind(FormDialog.class, "workflow-transition-2-dialog");
        assertTrue("Expected dialog title to contain ANA-4 issue key", dialog.getTitle().contains("ANA-4"));
    }

}
