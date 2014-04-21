package com.atlassian.jira.webtest.selenium.auidialog.hints;

import com.atlassian.jira.functest.framework.log.FuncTestOut;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.Quarantine;
import com.atlassian.jira.webtest.selenium.auidialog.AbstractAuiDialogTest;
import com.atlassian.jira.webtest.selenium.framework.dialogs.ActionsDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests that JIRa usage hints are visible in particular dialogs.
 *
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
@Quarantine
public class TestHintsInDialogs extends AbstractAuiDialogTest
{
    private static final String VISIBLE_HINT_CONTAINER_LOCATOR = VISIBLE_DIALOG_CONTENT_SELECTOR + " .hint-container";

    private interface DialogToTest
    {
        void openDialog();

        String getHintText();
    }

    private List<DialogToTest> dialogsWithHints = dialogsWithHints();
    private List<DialogToTest> dialogsWithoutHints = dialogsWithoutHints();
    private ActionsDialog dotDialog;

    private List<DialogToTest> dialogsWithHints()
    {
        List<DialogToTest> dialogsWithLinks = new ArrayList<DialogToTest>();
        dialogsWithLinks.add(new LabelsDialog());
        dialogsWithLinks.add(new CommentDialog());
        dialogsWithLinks.add(new AssignIssueDialog());
        dialogsWithLinks.add(new IssueTransitionDialog());
        return dialogsWithLinks;
    }

    private List<DialogToTest> dialogsWithoutHints()
    {
        List<DialogToTest> dialogsWithoutLinks = new ArrayList<DialogToTest>();
        dialogsWithoutLinks.add(new DeleteDialog());
        dialogsWithoutLinks.add(new DeleteDashboardDialog());
        dialogsWithoutLinks.add(new AttachFileDialog());
        dialogsWithoutLinks.add(new CloneIssueDialog());
        dialogsWithoutLinks.add(new DeleteFilterDialog());
        dialogsWithoutLinks.add(new LinkIssueDialog());
        dialogsWithoutLinks.add(new LogWorkDialog());
        return dialogsWithoutLinks;
    }


    @Override
    public void onSetUp()
    {
        super.onSetUp();
        dotDialog = new ActionsDialog(context());
        restoreData("TestHintsInDialogs.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoIssue("MKY-1");
    }

    public void testDialogsWithHints()
    {
        for (DialogToTest withHint : dialogsWithHints)
        {
            FuncTestOut.log("Asserting hints in dialog '" + withHint.getClass() + "'");
            getNavigator().gotoIssue("MKY-1");
            withHint.openDialog();
            assertHintPresent();
            assertThat.textPresent(withHint.getHintText());
            closeDialogByClickingCancel();
        }
    }

    public void testDialogsWithoutHints()
    {
        for (DialogToTest withoutHint : dialogsWithoutHints)
        {
            FuncTestOut.log("Asserting no hints in dialog '" + withoutHint.getClass() + "'");
            getNavigator().gotoIssue("MKY-1");
            withoutHint.openDialog();
            assertDialogIsOpenAndReady();
            assertHintNotPresent();
            closeDialogByClickingCancel();
        }
    }

    public void testDirtyFormCheck()
    {
        // ignore for internet explorer
        if (client.getEval("window.navigator.userAgent").contains("MSIE")) {
            return;
        }

        getNavigator().gotoIssue("MKY-1");
        new AssignIssueDialog().openDialog();
        assertHintPresent();
        //mark the form as dirty by selecting an assignee
        client.selectOption("assignee", "- Automatic -");
        client.chooseOkOnNextConfirmation();
        client.click("css=.shortcut-tip-trigger");
        assertEquals("You have entered new data in this dialog. If you navigate away from this dialog without first saving your data, the changes will be lost. Click cancel to return to the dialog.", client.getConfirmation());
        assertThat.visibleByTimeout("jquery=.aui-popup .aui-popup-heading:contains(Keyboard Shortcuts)", DROP_DOWN_WAIT);
    }

    private void assertHintPresent()
    {
        assertThat.elementPresentByTimeout(VISIBLE_HINT_CONTAINER_LOCATOR, DEFAULT_TIMEOUT);
    }

    private void assertHintNotPresent()
    {
        assertThat.elementNotPresentByTimeout(VISIBLE_HINT_CONTAINER_LOCATOR, DEFAULT_TIMEOUT);
    }

    private void openFromDotDialog(String action)
    {
        dotDialog.open();
        dotDialog.queryActions(action);
        dotDialog.selectSuggestionUsingClick();
    }

    private class LabelsDialog implements DialogToTest
    {
        public void openDialog()
        {
            client.clickAndWaitForAjaxWithJquery("jquery=#issuedetails a.edit-labels", DEFAULT_TIMEOUT);
        }

        public String getHintText()
        {
            return "Pressing l also opens this dialog";
        }
    }

    private class DeleteDialog implements DialogToTest
    {
        public void openDialog()
        {
            client.clickAndWaitForAjaxWithJquery("jquery=#delete-issue", DEFAULT_TIMEOUT);
        }

        public String getHintText()
        {
            throw new UnsupportedOperationException("Dialog doesn't have a hint!");
        }
    }

    private class CommentDialog implements DialogToTest
    {
        public void openDialog()
        {
            openFromDotDialog("Comment");
        }

        public String getHintText()
        {
            return "Pressing m also adds a comment";
        }
    }

    private class AssignIssueDialog implements DialogToTest
    {
        public void openDialog()
        {
            client.clickAndWaitForAjaxWithJquery("assign-issue", DEFAULT_TIMEOUT);
        }

        public String getHintText()
        {
            return "Pressing a also opens this dialog";
        }
    }

    private class AttachFileDialog implements DialogToTest
    {
        public void openDialog()
        {
            client.clickAndWaitForAjaxWithJquery("attach-file", DEFAULT_TIMEOUT);
        }

        public String getHintText()
        {
            throw new UnsupportedOperationException("Dialog doesn't have a hint!");
        }
    }

    private class CloneIssueDialog implements DialogToTest
    {
        public void openDialog()
        {
            client.clickAndWaitForAjaxWithJquery("clone-issue", DEFAULT_TIMEOUT);
        }

        public String getHintText()
        {
            throw new UnsupportedOperationException("Dialog doesn't have a hint!");
        }
    }

    private class IssueTransitionDialog implements DialogToTest
    {
        public void openDialog()
        {
            client.clickLinkWithText("Close Issue", false);
            client.waitForAjaxWithJquery(DEFAULT_TIMEOUT);
        }

        public String getHintText()
        {
            return "Pressing full-stop (.) can also be used to open this dialog box";
        }
    }

    private class DeleteFilterDialog implements DialogToTest
    {
        public void openDialog()
        {
            getNavigator().gotoPage("secure/ManageFilters.jspa", true);
            client.click("jquery=#fav-filters-tab");
            waitFor(200);
            client.clickAndWaitForAjaxWithJquery("delete_10000", DEFAULT_TIMEOUT);
        }

        public String getHintText()
        {
            throw new UnsupportedOperationException("Dialog doesn't have a hint!");
        }
    }

    private class LinkIssueDialog implements DialogToTest
    {
        public void openDialog()
        {
            client.clickAndWaitForAjaxWithJquery("link-issue", DEFAULT_TIMEOUT);
        }

        public String getHintText()
        {
            throw new UnsupportedOperationException("Dialog doesn't have a hint!");
        }
    }

    private class LogWorkDialog implements DialogToTest
    {
        public void openDialog()
        {
            client.clickAndWaitForAjaxWithJquery("log-work", DEFAULT_TIMEOUT);
        }

        public String getHintText()
        {
            throw new UnsupportedOperationException("Dialog doesn't have a hint!");
        }
    }


    private class DeleteDashboardDialog implements DialogToTest
    {
        public void openDialog()
        {
            getNavigator().gotoPage("secure/ConfigurePortalPages!default.jspa", true);
            client.click("jquery=#favourite-dash-tab");
            waitFor(200);
            client.clickAndWaitForAjaxWithJquery("delete_0", DEFAULT_TIMEOUT);
        }

        public String getHintText()
        {
            throw new UnsupportedOperationException("Dialog doesn't have a hint!");
        }
    }

}
