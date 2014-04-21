package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.framework.dialogs.ActionsDialog;
import com.atlassian.jira.webtest.selenium.framework.model.Mouse;
import junit.framework.Test;

import java.util.Locale;

@WebTest({Category.SELENIUM_TEST })
public class TestCommentSecurityLevel extends JiraSeleniumTest
{
    @Override
    public void onSetUp()
    {
        super.onSetUp();
        backdoor.dataImport().restoreData("TestCommentSecurityLevel.xml");
        getNavigator().login(ADMIN_USERNAME);
    }

    public static Test suite()
    {
        return suiteFor(TestCommentSecurityLevel.class);
    }

    public void testCommentSecurityLevel()
    {
        //test changing security level changes inline
        getNavigator().gotoIssue("HSP-1");
        //client.waitForPageToLoad();
        client.click("comment-issue");
        waitForContentUpdate();

        assertThat.visibleByTimeout("css=.security-level", DROP_DOWN_WAIT);
        assertUpdatingSecurityLevelChangesInline(null);

        //also try the comment box at the bottom of the page
        getNavigator().gotoIssue("HSP-1");
        client.click("footer-comment-button", false);
        assertThat.visibleByTimeout("css=.security-level", DROP_DOWN_WAIT);
        assertUpdatingSecurityLevelChangesInline(null);

        //and finally check the dot dialog
        getNavigator().gotoIssue("HSP-1");
        ActionsDialog actionsDialog = new ActionsDialog(context());
        actionsDialog.open();
        actionsDialog.queryActions("comment");
        actionsDialog.selectSuggestionUsingClick();
        assertThat.visibleByTimeout("css=#comment-add-dialog .security-level", PAGE_LOAD_WAIT_TIME);
        assertUpdatingSecurityLevelChangesInline("#comment-add-dialog");


        //test editing a comment with security level to make sure it's prepopulated
        getNavigator().gotoPage("/browse/HSP-1?page=com.atlassian.jira.plugin.system.issuetabpanels%3Aworklog-tabpanel", true);
        assertThat.textPresent("Restricted to jira-developers");
        // worklog edits are full page load
        client.click("edit_worklog_10000", true);
        assertThat.textPresent("Edit Work Log");
        assertThat.elementContainsText("css=.security-level .current-level", "Restricted to jira-developers");
        client.click("css=.select-menu a", false);
        assertThat.elementPresentByTimeout("css=.select-menu .aui-list", DROP_DOWN_WAIT);
        clickSecurityLevel("Administrators");
        client.click("log-work-submit", true);
        assertThat.textPresent("Restricted to Administrators");
    }

    // This is currently provided by a different mechanism than the comment system field.
    public void testLogWorkSecurityLevel()
    {
        //test security level on log work screen
        getNavigator().gotoIssue("HSP-1");
        ActionsDialog actionsDialog = new ActionsDialog(context());
        actionsDialog.open();
        actionsDialog.queryActions("log");
        actionsDialog.selectSuggestionUsingClick();
        assertThat.visibleByTimeout("css=#log-work-dialog .security-level", PAGE_LOAD_WAIT_TIME);
        assertUpdatingSecurityLevelChangesInline("#log-work-dialog");
    }

    private void clickSecurityLevel(String levelName)
    {
        final String classLevelName = levelName.toLowerCase(Locale.ENGLISH).replace(" ", "-");
        final String levelLocator = "css=li.aui-list-item-li-" + classLevelName;
        assertThat.elementPresentByTimeout(levelLocator, DROP_DOWN_WAIT);
        //This test seems to need this to be called twice.
        Mouse.mouseover(client, levelLocator);
        Mouse.mouseover(client, levelLocator);
        assertThat.elementPresentByTimeout(levelLocator + ".active", DROP_DOWN_WAIT);
        client.click(levelLocator);
        waitFor(500);
    }


    // in dialogs max height should be limited to available screen realestate, otherwise not.
    public void testMaxHeight()
    {
        getNavigator().gotoIssue(HSP_1);
        // bottom one (none dialog)
        client.click("footer-comment-button");
        client.click("css=#commentLevel-multi-select .drop");
        assertThat.elementPresentByTimeout("css=.select-menu.active");
        String nonDialogMaxHeight = client.getEval("window.jQuery('.select-menu.active').css('maxHeight')");

        if (!nonDialogMaxHeight.equals("none"))
        {
            throw new AssertionError("Expected no max height to be set when not in dialog");
        }

        client.getEval("window.resizeTo(600, 550)");
        try
        {
            client.click("assign-issue");
            assertThat.elementPresentByTimeout("css=#assign-dialog.aui-dialog-content-ready", 30000);
            client.click("css=#assign-dialog #commentLevel-multi-select .drop");
            String dialogMaxHeight = client.getEval("window.jQuery('.select-menu.active').css('maxHeight')");

            if (dialogMaxHeight.equals("none"))
            {
                throw new AssertionError("Expected max height to be set when in dialog with small resolution");
            }
        }
        finally
        {
            client.windowMaximize();
        }
    }

    //JRADEV-2473
    public void testPressDown()
    {
        getNavigator().gotoIssue(HSP_1);
        // bottom one (none dialog)
        client.click("footer-comment-button");
        client.keyPress("css=#commentLevel-multi-select .drop", VK_DOWN);
        assertThat.elementPresentByTimeout("css=.select-menu.active");
        assertThat.elementContainsText("css=.aui-list-item.aui-list-item-li-all-users.active", "All Users");

        //close the dialog and reopen it. Should still have the first one selected!
        client.simulateKeyPressForSpecialKey("css=body", 27);
        assertThat.elementNotPresentByTimeout("css=.select-menu.active");
        client.keyPress("css=#commentLevel-multi-select .drop", VK_DOWN);
        assertThat.elementPresentByTimeout("css=.select-menu.active");
        assertThat.elementContainsText("css=.aui-list-item.aui-list-item-li-all-users.active", "All Users");
    }

    private void assertUpdatingSecurityLevelChangesInline(String context)
    {
        String prefix = "";
        if (context != null)
        {
            prefix = context;
        }
        assertThat.elementContainsText("css=" + prefix + " .security-level .current-level", "Viewable by All Users");
        client.click("css=" + prefix + " .select-menu a", false);
        assertThat.elementPresentByTimeout("css=.select-menu .aui-list", DROP_DOWN_WAIT);
        clickSecurityLevel("Administrators");
        assertThat.elementContainsText("css=" + prefix + " .security-level .current-level", "Restricted to Administrators");

        client.click("css=" + prefix + " .select-menu a", false);
        assertThat.elementPresentByTimeout("css=.select-menu .aui-list", DROP_DOWN_WAIT);
        clickSecurityLevel("jira-developers");
        assertThat.elementContainsText("css=" + prefix + " .security-level .current-level", "Restricted to jira-developers");

        client.click("css=" + prefix + " .select-menu a", false);
        assertThat.elementPresentByTimeout("css=.select-menu .aui-list", DROP_DOWN_WAIT);
        clickSecurityLevel("All Users");
        assertThat.elementContainsText("css=" + prefix + " .security-level .current-level", "Viewable by All Users");
    }

}
