package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.model.ActionType;
import com.atlassian.jira.webtest.selenium.framework.model.LegacyIssueOperation;

/**
 * Test utility representing the Dot Dialog.
 *
 * @since v4.2
 */
public final class AddCommentDialog extends AbstractIssueDialog<AddCommentDialog>
{
    public AddCommentDialog(SeleniumContext ctx)
    {
        super(LegacyIssueOperation.COMMENT, AddCommentDialog.class, ActionType.NEW_PAGE, ctx);
    }

    // TODO move to contents object, use in standalone add comment page

    /* ----------------------------------------------- LOCATORS ----------------------------------------------------- */

    public String commentBoxLocator()
    {
        return inDialog("textarea#comment");
    }


    @Override
    protected String dialogContentsReadyLocator()
    {
        return commentBoxLocator();
    }

    /* ------------------------------------------------ QUERIES ----------------------------------------------------- */

    public boolean isInputMode()
    {
        return client.isElementPresent(commentBoxLocator());
    }


    public boolean isPreviewMode()
    {
        return !isInputMode();
    }

    /* ------------------------------------------------ ACTIONS ----------------------------------------------------- */

    @Override
    public AddCommentDialog openFromViewIssue()
    {
        actionsDialog.openFromViewIssue().queryActions(issueOperation().name()).selectSuggestionUsingClick();
        return this;
    }

    public void insertComment(String text)
    {
        if (isPreviewMode())
        {
            throw new IllegalStateException("Cannot type in preview mode");
        }
        client.type(commentBoxLocator(), text);
    }

    // TODO method for toggle preview link locator

    public void togglePreview()
    {
        client.clickAndWaitForAjaxWithJquery(VISIBLE_DIALOG_CONTENT_SELECTOR + " #comment-preview_link", 10000);
    }

    /* ------------------------------------------------ ASSERTIONS -------------------------------------------------- */

    // TODO remove

    public void assertIsInputMode()
    {
        assertThat.elementPresentByTimeout(commentBoxLocator(), context.timeouts().dialogLoad());
    }


    public void assertIsPreviewMode()
    {
        assertThat.elementNotPresentByTimeout(commentBoxLocator(), context.timeouts().dialogLoad());
    }


}