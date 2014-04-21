package com.atlassian.jira.webtest.selenium.issue.dialogs;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.ui.Shortcuts;
import com.atlassian.jira.webtest.selenium.auidialog.AbstractAuiDialogTest;
import com.atlassian.jira.webtest.selenium.framework.SeleniumClosure;
import com.atlassian.jira.webtest.selenium.framework.Window;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

import static com.atlassian.jira.webtest.selenium.framework.model.Locators.Common.BODY;

/**
 * Selenium tests for delete issue confirmation AUI dialog.
 *
 * @since v4.2
 */
@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestDeleteIssue extends AbstractAuiDialogTest
{
    private static final String TEST_XML = "TestDeleteIssue.xml";
    private static final String TEST_ISSUE = "HSP-1";
    private static final String TEST_PARENT_TASK = "HSP-5";
    private static final String TEST_SUBTASK = "HSP-6";

    private final String JQUERY_FIELD = "jquery=#issue-actions-dialog .text";
    private final String ISSUEACTIONS_SUGGESTIONS_LOCATOR = "jquery=#issueactions-suggestions";


    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreData(TEST_XML);
    }

    public void testDeleteIssueDialog() throws Exception
    {
        deleteDialogFor(TEST_ISSUE);
        assertDeleteIssueDialog();
        submitDelete();
        assertOnIssueNavigatorPage();
    }

    public void testDeleteIssueDialogCancel() throws Exception
    {
        deleteDialogFor(TEST_ISSUE);
        assertDeleteIssueDialog();
        closeDialogByClickingCancel();
        assertOnTestIssuePage();
    }


    public void testDeleteSubtask() throws Exception
    {
        deleteDialogFor(TEST_SUBTASK);
        assertDeleteIssueDialog();
        submitDelete();
//        assertOnParentTaskPage();
        // actually the existing behaviour was to move back to issue navigator
        assertOnIssueNavigatorPage();
    }


    public void testDeleteNotLoggedIn() throws Exception
    {
        getNavigator().gotoIssue(TEST_ISSUE);
//        artificialLogout();
        backgroundLogout();
        client.click("delete-issue");
        assertErrorIssueDialog();
        assertAuiErrorMessage("You do not have the permission to see the specified issue");
        closeDialogByEscape();
        assertOnTestIssuePage();
    }

    public void testDeleteNotExistingIssue() throws Exception
    {
        getNavigator().gotoIssue(TEST_ISSUE);
        deleteIssueInBackground(TEST_ISSUE);
        client.click("delete-issue");
        assertDialogIsOpenAndReady();
        assertFormIsUndecorated();
        assertAuiErrorMessage("The issue no longer exists");
    }

    public void testDeleteRedirectsCorrectly() 
    {
        restoreData("TestDeleteRedirectsCorrectly.xml");
        backdoor.plugins().disablePlugin("com.atlassian.jira.jira-issue-nav-plugin");

        //deleting a subtask from the parent issue should got back to the parent issue
        getNavigator().gotoIssue("HSP-5");
        deleteSelectedIssueViaDialog(10021L);
        assertThat.elementNotPresent("css=#issuenav");
        assertThat.textPresent("HSP-5");
        assertThat.textPresent("Task");

        //deleting a subtask from the subtask should go to the issuenav
        getNavigator().gotoIssue("HSP-8");
        client.click("css=#delete-issue");
        assertThat.elementPresentByTimeout("jquery=#delete-issue-submit", DROP_DOWN_WAIT);
        client.click("jquery=#delete-issue-submit", true);
        assertThat.elementPresent("css=#issuenav");
        assertThat.textNotPresent("HSP-8");

        //deleting a parent issue from the parent issue should go to issuenav
        getNavigator().gotoIssue("HSP-1");
        client.click("css=#delete-issue");
        assertThat.elementPresentByTimeout("jquery=#delete-issue-submit", DROP_DOWN_WAIT);
        client.click("jquery=#delete-issue-submit", true);
        assertThat.elementPresent("css=#issuenav");
        assertThat.textNotPresent("HSP-1");

        //deleting a subtask from issue nav should go back to the issuenav
        getNavigator().findAllIssues();
        assertThat.textPresent("HSP-7");
        deleteSelectedIssueViaDialog(10030L);
        assertThat.elementPresent("css=#issuenav");
        assertThat.elementDoesNotContainText("css=#issuetable", "HSP-7");

        //deleting a parent issue from the issue nav should go back to the issue nav
        getNavigator().findAllIssues();
        assertThat.textPresent("HSP-2");
        deleteSelectedIssueViaDialog(10010L);
        assertThat.elementPresent("css=#issuenav");
        assertThat.elementDoesNotContainText("css=#issuetable", "HSP-2");
    }

    private void deleteSelectedIssueViaDialog(final Long issueId)
    {
        client.click("jquery=#actions_" + issueId);
        assertThat.elementPresentByTimeout("jquery=#actions_" + issueId + "_drop .issueaction-delete-issue", DROP_DOWN_WAIT);
        client.click("jquery=#actions_" + issueId + "_drop .issueaction-delete-issue");
        assertThat.elementPresentByTimeout("jquery=#delete-issue-submit", DROP_DOWN_WAIT);
        client.click("jquery=#delete-issue-submit", true);
    }

    public void testDeleteRedirectsCorrectlyFromDotDialog()
    {
        restoreData("TestDeleteRedirectsCorrectly.xml");
        backdoor.plugins().disablePlugin("com.atlassian.jira.jira-issue-nav-plugin");


        //deleting a subtask from the subtask should go to the issuenav
        getNavigator().gotoIssue("HSP-8");
        deleteSelectedIssueViaDotDialog();
        assertThat.textPresent("Issue Navigator");
        assertThat.textNotPresent("HSP-8");

        //deleting a parent issue from the parent issue should go to issuenav
        getNavigator().gotoIssue("HSP-1");
        deleteSelectedIssueViaDotDialog();
        assertThat.textPresent("Issue Navigator");
        assertThat.textNotPresent("HSP-1");

        //deleting a subtask from issue nav should go back to the issuenav
        getNavigator().findAllIssues();
        assertThat.textPresent("HSP-7");
        client.click("jquery=#actions_" + 10030L);
        assertThat.elementPresentByTimeout("jquery=#actions_" + 10030L + "_drop .issueaction-delete-issue", DROP_DOWN_WAIT);
        client.click(BODY);
        deleteSelectedIssueViaDotDialog();
        assertThat.textPresent("Issue Navigator");
        assertThat.elementDoesNotContainText("css=#issuetable", "HSP-7");

        //deleting a parent issue from the issue nav should go back to the issue nav
        getNavigator().findAllIssues();
        assertThat.textPresent("HSP-2");
        client.click("jquery=#actions_" + 10010L);
        assertThat.elementPresentByTimeout("jquery=#actions_" + 10010L + "_drop .issueaction-delete-issue", DROP_DOWN_WAIT);
        client.click(BODY);
        deleteSelectedIssueViaDotDialog();
        assertThat.textPresent("Issue Navigator");
        assertThat.elementDoesNotContainText("css=#issuetable", "HSP-2");
    }


    private void deleteSelectedIssueViaDotDialog()
    {
        client.focus(BODY);
        context().ui().pressInBody(Shortcuts.DOT_DIALOG);
        assertThat.elementPresentByTimeout(JQUERY_FIELD, DROP_DOWN_WAIT);
        client.typeWithFullKeyEvents(JQUERY_FIELD, "Delete");
        assertThat.elementPresentByTimeout(ISSUEACTIONS_SUGGESTIONS_LOCATOR + " li.active:not(.hidden):contains(Delete)");

        client.click("css=#issueactions-suggestions" + " li.active a");

        assertThat.elementPresentByTimeout("jquery=#delete-issue-submit", DROP_DOWN_WAIT);
        client.click("jquery=#delete-issue-submit", true);
    }

    private void deleteIssueInBackground(final String testIssue) throws Exception
    {
        Window.withNewWindow(client, "", "delete-task", new SeleniumClosure() {
            public void execute() throws Exception
            {
                deleteDialogFor(testIssue);
                assertDeleteIssueDialog();
                submitDelete();
                assertOnIssueNavigatorPage();
            }
        });
    }

    private void deleteDialogFor(String issueKey)
    {
        getNavigator().gotoIssue(issueKey);
        client.click("delete-issue");
    }

    private void assertErrorIssueDialog()
    {
        assertDialogIsOpenAndReady();
        assertFormIsUndecorated();
        assertDialogContainsText("Error");
    }

    private void assertDeleteIssueDialog()
    {
        assertDialogIsOpenAndReady();
        assertFormIsUndecorated();
        assertDialogContainsText("Delete");
    }

    private void submitDelete()
    {
        client.click("delete-issue-submit", true);
    }

    private void assertOnIssueNavigatorPage()
    {
        assertThat.elementHasText("jquery=section#content > header > h1", "Issue Navigator");
    }

    private void assertOnParentTaskPage()
    {
        assertThat.elementHasText("//a[@id='key-val']", TEST_PARENT_TASK);
    }

    private void assertOnTestIssuePage()
    {
        assertThat.elementHasText("//a[@id='key-val']", TEST_ISSUE);
    }

    private void artificialLogout()
    {
        client.deleteCookie("JSESSIONID", "/");
        client.deleteCookie("JSESSIONID", getEnvironmentData().getContext());
    }
}
