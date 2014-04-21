package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.ui.Shortcuts;
import com.atlassian.jira.webtest.selenium.auidialog.AbstractAuiDialogTest;
import com.atlassian.jira.webtest.selenium.framework.dialogs.ActionsDialog;

@WebTest ({ Category.SELENIUM_TEST })
public class TestCommentToggling extends AbstractAuiDialogTest
{
    private static final String STALKER_ADD_COMMENT_SELECTOR = "jquery=#stalker form#issue-comment-add";
    private static final String FOOTER_ADD_COMMENT_SELECTOR = "jquery=#addcomment form#issue-comment-add";
    private static final String FOOTER_COMMENT_TRIGGER_SELECTOR = "jquery=#footer-comment-button";
    private static final String STALKER_COMMENT_TRIGGER_SELECTOR = "jquery=#comment-issue";
    private static final String COMMENT_CANCEL_SELECTOR = "issue-comment-add-cancel";
    private static final String STALKER_CONTEXT_SELECTOR = "jquery=#stalker";
    private static final String FOOTER_CONTEXT_SELECTOR = "jquery=#addcomment";
    private static final String COMMENT_PREVIEW_LINK_SELECTOR = "#comment-preview_link";
    private static final String PREVIEW_CLASS_VISIBLE = ".previewClass:visible";
    private static final String COMMENT_VISIBLE = "#comment:visible";
    private static final String DUMMY_TEXT_FROM_FOOTER = "test from footer comment form";
    private static final String DUMMY_TEXT_FROM_STALKER = "test from stalker comment form";
    private static final String SUBMIT_SELECTOR = "jquery=#issue-comment-add-submit";
    private static final String FOOTER_TRIGGER_ACTIVE_SELECTOR = "jquery=#addcomment.active";
    private static final String STALKER_TRIGGER_ACTIVE_SELECTOR = "jquery=#comment-issue.active";
    private static final String COMMENT_ACTION = "Comment";

    private ActionsDialog theDotDialog;

    public void onSetUp()
    {
        super.onSetUp();
        theDotDialog = new ActionsDialog(context());
        restoreData("TestOpsBar.xml");
    }

    public void testCommentToggling()
    {
        getNavigator().gotoIssue("HSP-1");

        // opening comment from key command should move form from footer into stalker and show
        context().ui().pressInBody(Shortcuts.COMMENT);
        assertStalkerCommentActive();

        // clicking add comment header in footer should remove the from the stalker and append to footer
        triggerCommentFromFooter();
        assertFooterCommentActive();

        triggerCommentFromStalkerBar();
        assertStalkerCommentActive();

        triggerCommentFromStalkerBar();
        assertNoCommentActive();

        triggerCommentFromStalkerBar();
        assertStalkerCommentActive();

        cancelComment();
        assertNoCommentActive();

        triggerCommentFromFooter();
        assertFooterCommentActive();

        cancelComment();
        assertNoCommentActive();

        _testFormsSubmit();

    }

    private void _testFormsSubmit()
    {
        triggerCommentFromFooter();
        client.typeWithFullKeyEvents("jquery=" + COMMENT_VISIBLE, DUMMY_TEXT_FROM_FOOTER);
        client.click(SUBMIT_SELECTOR, false);
        assertThat.textPresentByTimeout(DUMMY_TEXT_FROM_FOOTER, 8000);

        triggerCommentFromStalkerBar();
        client.typeWithFullKeyEvents("jquery=" + COMMENT_VISIBLE, DUMMY_TEXT_FROM_STALKER);
        client.click(SUBMIT_SELECTOR, false);
        assertThat.textPresentByTimeout(DUMMY_TEXT_FROM_STALKER, 8000);
    }

    private void assertNoCommentActive()
    {
        assertThat.elementNotPresentByTimeout(STALKER_TRIGGER_ACTIVE_SELECTOR); // should have active class
        assertThat.elementNotPresentByTimeout(FOOTER_TRIGGER_ACTIVE_SELECTOR); // should also have an active class
    }

    private void assertStalkerCommentActive()
    {
        assertThat.elementPresentByTimeout(STALKER_ADD_COMMENT_SELECTOR);
        assertThat.elementNotPresentByTimeout(FOOTER_ADD_COMMENT_SELECTOR);
        assertThat.elementPresentByTimeout(STALKER_TRIGGER_ACTIVE_SELECTOR); // should have active class
        assertWikiPreviewWorks(STALKER_CONTEXT_SELECTOR);
    }

    private void assertFooterCommentActive()
    {
        assertThat.elementPresentByTimeout(FOOTER_ADD_COMMENT_SELECTOR);
        assertThat.elementNotPresentByTimeout(STALKER_ADD_COMMENT_SELECTOR);
        assertThat.elementPresentByTimeout(FOOTER_TRIGGER_ACTIVE_SELECTOR); // should also have an active class
        assertWikiPreviewWorks(FOOTER_CONTEXT_SELECTOR);
    }

    private void assertWikiPreviewWorks(String ctx)
    {
        client.click(ctx + " " + COMMENT_PREVIEW_LINK_SELECTOR);
        assertThat.elementPresentByTimeout(ctx + " " + PREVIEW_CLASS_VISIBLE, 20000);
        client.click(ctx + " " + COMMENT_PREVIEW_LINK_SELECTOR);
        assertThat.elementNotPresentByTimeout(ctx + " " + PREVIEW_CLASS_VISIBLE);
        client.click(ctx + " " + COMMENT_VISIBLE);
    }

    public void testCommentDialogNotTriggeredByInlineCommentButton()
    {
        getNavigator().gotoIssue("HSP-1");
        openCommentActionDialog();
        closeDialogByEscape();
        triggerCommentFromFooter();
        assertEquals("1", client.getEval("dom=this.browserbot.getCurrentWindow().jQuery('#addcomment form#issue-comment-add').length"));
    }

    public void testCloseLinkNotAddedToInlineCommentByDotDialog()
    {
        getNavigator().gotoIssue("HSP-1");
        theDotDialog.open();
        theDotDialog.close();
        assertEquals("1", client.getEval("dom=this.browserbot.getCurrentWindow().jQuery('#addcomment form#issue-comment-add .cancel').length"));
    }


    private void triggerCommentFromStalkerBar()
    {
        client.click(STALKER_COMMENT_TRIGGER_SELECTOR);
    }

    private void triggerCommentFromFooter()
    {
        client.click(FOOTER_COMMENT_TRIGGER_SELECTOR);
    }

    private void openCommentActionDialog()
    {
        theDotDialog.open();
        theDotDialog.queryActions(COMMENT_ACTION);
        theDotDialog.selectSuggestionUsingClick();
        assertDialogIsOpenAndReady();
    }

    private void cancelComment()
    {
        client.click(COMMENT_CANCEL_SELECTOR);
    }
}
