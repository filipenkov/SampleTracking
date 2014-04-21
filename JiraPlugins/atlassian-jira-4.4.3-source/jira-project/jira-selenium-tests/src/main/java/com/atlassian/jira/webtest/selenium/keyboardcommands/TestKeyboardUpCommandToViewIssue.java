package com.atlassian.jira.webtest.selenium.keyboardcommands;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.ui.Shortcuts;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import junit.framework.Test;

/**
 * Tests the 'u' keyboard shortcut.
 *
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
public class TestKeyboardUpCommandToViewIssue extends JiraSeleniumTest
{
    private static final String VIEW_ISSUE_CHECKER = "jquery=#stalker #key-val";

     public static Test suite()
    {
        return suiteFor(TestKeyboardUpCommandToViewIssue.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestKeyboardUpCommand.xml");
    }

    public void testAssignIssue()
    {
        _testUpToViewIssueFromURL("secure/AssignIssue!default.jspa?id=10000");
    }

    public void testCommentIssue()
    {
        _testUpToViewIssueFromURL("secure/AddComment!default.jspa?id=10000");
    }

    public void testAttachFile()
    {
        _testUpToViewIssueFromURL("secure/AttachFile!default.jspa?id=10000");
    }

    public void testCloneIssue()
    {
        _testUpToViewIssueFromURL("secure/CloneIssueDetails!default.jspa?id=10000");
    }

    public void testWorkflowScreen()
    {
        _testUpToViewIssueFromURL("secure/CommentAssignIssue!default.jspa?action=5&id=10000&atl_token="+getXsrfToken());
    }

    public void testIssueToSubtask()
    {
        _testUpToViewIssueFromClick("issue-to-subtask"); // not sure why we need this but navigating directly to the url was causing a randomly failing test
    }

    public void testSubtaskToIssue()
    {
        _testUpToViewIssueFromURL("secure/ConvertSubTask.jspa?id=10020");
    }

    public void testCreateSubTask()
    {
        _testUpToViewIssueFromURL("secure/CreateSubTaskIssue!default.jspa?parentIssueId=10000");
        getNavigator().gotoPage("secure/CreateSubTaskIssue!default.jspa?parentIssueId=10000",true);
        client.click("jquery=#subtask-create-start-submit", true);
        _runUpCommand();
        assertWeAreOnViewIssue();
    }

    public void testManageAttachments()
    {
        _testUpToViewIssueFromURL("secure/ManageAttachments.jspa?id=10000");
    }

    public void testManageWatchers()
    {
        _testUpToViewIssueFromURL("secure/ManageWatchers!default.jspa?id=10000");
    }

    public void testMoveIssue()
    {
        _testUpToViewIssueFromURL("secure/MoveIssue!default.jspa?id=10000");
        getNavigator().gotoPage("secure/MoveIssue!default.jspa?id=10000",true);
        client.selectOption("project","monkey");
        client.click("next_submit", true);
        assertThat.elementContainsText("css=.formtitle","Move Issue");
        _runUpCommand();
        assertWeAreOnViewIssue();
        getNavigator().gotoPage("secure/MoveIssue!default.jspa?id=10000",true);
        client.selectOption("project","monkey");
        client.click("next_submit", true);
        assertThat.elementContainsText("css=.formtitle","Move Issue");
        assertThat.elementPresent("next_submit");
        client.click("next_submit", true);
        _runUpCommand();
        assertWeAreOnViewIssue();
    }

    public void testViewVoters()
    {
        _testUpToViewIssueFromURL("secure/ViewVoters!default.jspa?id=10000");
    }

    public void testDeleteIssue()
    {
        _testUpToViewIssueFromURL("secure/DeleteIssue!default.jspa?id=10000");
    }

    public void testEditIssue()
    {
        _testUpToViewIssueFromURL("secure/EditIssue!default.jspa?id=10000");
    }

    public void testLabelsForm()
    {
        _testUpToViewIssueFromURL("secure/EditLabels!default.jspa?id=10000");
    }

    private void _runUpCommand() {

        context().ui().pressInBody(Shortcuts.UP);
    }

    private void _testUpToViewIssueFromURL(String urlString)
    {
        getNavigator().gotoIssue("HSP-1");
        getNavigator().gotoPage(urlString, true);
        _runUpCommand();
        assertWeAreOnViewIssue();
    }

    private void _testUpToViewIssueFromClick(String id)
    {
        getNavigator().gotoIssue("HSP-1");
        client.click(id, true);
        _runUpCommand();
        assertWeAreOnViewIssue();
    }

    private void assertWeAreOnViewIssue()
    {
        client.waitForPageToLoad();
 	 	assertThat.elementPresentByTimeout(VIEW_ISSUE_CHECKER, 5000);
    }

}
