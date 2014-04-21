package com.atlassian.jira.webtest.selenium.auidialog;

import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.framework.dialogs.GenericDialog;
import com.atlassian.jira.webtest.selenium.framework.model.ActionType;
import com.atlassian.jira.webtest.selenium.framework.model.CancelType;

/**
 * Abstract base class for AUI dialog testing.
 *
 * @since v4.2
 * @deprecated use page objects instead
 */
@Deprecated
public abstract class AbstractAuiDialogTest extends JiraSeleniumTest
{
    protected static final String BARE_DIALOG_CONTENT_SELECTOR = "jquery=.aui-dialog-content";
    protected static final String VISIBLE_DIALOG_CONTENT_SELECTOR = "jquery=.aui-dialog-open";
    protected static final String DIALOG_FORM_SELECTOR_FORMAT =  VISIBLE_DIALOG_CONTENT_SELECTOR + " form#%s";

    private static final String SUBMIT_SELECTOR = VISIBLE_DIALOG_CONTENT_SELECTOR + " #%s";


    private static final String ERROR_MODE_SELECTOR = VISIBLE_DIALOG_CONTENT_SELECTOR + " .jira-error";
    private static final String ERROR_MESSAGE_SELECTOR = ERROR_MODE_SELECTOR + " .aui-message.error";
    private static final String WARNING_MESSAGE_SELECTOR = ERROR_MODE_SELECTOR + " .aui-message.warning";

    protected static final int DEFAULT_TIMEOUT = 10000;

    private GenericDialog helperDialog;
    
    private GenericDialog helperDialogAjax;
    private GenericDialog helperDialogNewPage;
    private GenericDialog helperDialogJavascript;

    @Override
    public void onSetUp()
    {
        super.onSetUp();
        this.helperDialogAjax = new GenericDialog(context(), ActionType.AJAX);
        this.helperDialogNewPage = new GenericDialog(context(), ActionType.NEW_PAGE);
        this.helperDialogJavascript = new GenericDialog(context(), ActionType.JAVASCRIPT);
        this.helperDialog = helperDialogAjax;
    }

    @Override
    protected void onTearDown() throws Exception
    {
        this.helperDialog = null;
        this.helperDialogAjax = null;
        this.helperDialogNewPage = null;
        this.helperDialogJavascript = null;
        super.onTearDown();
    }

    protected static String submitSelector(String buttonId)
    {
        return String.format(SUBMIT_SELECTOR, buttonId);
    }

    protected static String defaultDialogSubmitSelector()
    {
        return VISIBLE_DIALOG_CONTENT_SELECTOR + " :submit";
    }

    private static String dialogSelectorFor(String formId)
    {
        return String.format(DIALOG_FORM_SELECTOR_FORMAT, formId);
    }

    /**
     * Assert that the dialog under test is open and its content has been fully rendered.
     *
     */
    protected final void assertDialogIsOpenAndReady()
    {
        helperDialog.assertReady();
    }

    /**
     * Assert that no dialog has been yet loaded into the page.
     *
     */
    protected final void assertDialogNotLoaded() {
        assertThat.elementNotPresentByTimeout(BARE_DIALOG_CONTENT_SELECTOR, DEFAULT_TIMEOUT);
    }

    /**
     * Assert that no dialog is currently open on the page.
     *
     */
    protected final void assertDialogNotOpen() {
        helperDialog.assertNotOpen();
    }

    protected final void assertDialogContainsText(String text) {
        assertThat.elementPresentByTimeout(VISIBLE_DIALOG_CONTENT_SELECTOR + ":contains(" + text + ")",DEFAULT_TIMEOUT);
    }

    protected final void assertDialogContainsAuiForm(String formId) {
        assertThat.elementPresentByTimeout(dialogSelectorFor(formId), DEFAULT_TIMEOUT);
    }

    protected final void assertFieldHasInlineError(String fieldName, String errorText) {
        assertThat.elementPresentByTimeout(VISIBLE_DIALOG_CONTENT_SELECTOR + " :input[name=" + fieldName
                + "] + .error:contains(" + errorText + ")", DEFAULT_TIMEOUT);
    }

    protected final void assertAuiWarningMessage(String expectedText) {
        assertThat.elementPresentByTimeout(ERROR_MODE_SELECTOR, DEFAULT_TIMEOUT);
        assertThat.elementContainsText(WARNING_MESSAGE_SELECTOR, expectedText);
    }

    protected final void assertAuiErrorMessage(String expectedText) {
        assertThat.elementPresentByTimeout(ERROR_MODE_SELECTOR, DEFAULT_TIMEOUT);
        assertThat.elementContainsText(ERROR_MESSAGE_SELECTOR, expectedText);
    }

    /**
     * Close dialog by clicking the 'Cancel' link.
     * 
     */
    protected final void closeDialogByClickingCancel() {
        helperDialog.cancel(CancelType.BY_CLICK);
    }

    protected final void closeDialogByEscape() {
        helperDialog.cancel(CancelType.BY_ESCAPE);
    }


    /**
     * Submit AUI dialog using its default submit button.
     * Use with dialogs that don't do requests on submit.
     *
     */
    protected final void submitDialog() {
        helperDialogJavascript.submit();
    }

    /**
     * Submit AUI dialog using its default submit button.
     * Use with dialogs that trigger page reload on submit.
     *
     */
    protected final void submitDialogAndWaitForReload() {
        helperDialogNewPage.submit();
    }

    /**
     * Submit AUI dialog using its default submit button.
     * Use with dialogs that trigger AJAX requests on submit.
     *
     */
    protected final void submitDialogAndWaitForAjax() {
        helperDialogAjax.submit();
    }


    /**
     * Submit the open AUI dialog using a button with specific ID.
     * Use with dialogs that don't do requests on submit.
     *
     * @param submitButtonId id of the submit button
     */
    @Deprecated
    protected final void submitDialog(String submitButtonId) {
        client.click(submitSelector(submitButtonId));
    }

    /**
     * Submit the open AUI dialog using a button with specific ID.
     * Use with dialogs that trigger page reload on submit.
     *
     * @param submitButtonId id of the submit button
     */
    protected final void submitDialogAndWaitForReload(String submitButtonId) {
        client.click(submitSelector(submitButtonId), true);
    }

    /**
     * Submit the open AUI dialog using a button with specific ID.
     * Use with dialogs that trigger AJAX requests on submit.
     *
     * @param submitButtonId id of the submit button
     */
    protected final void submitDialogAndWaitForAjax(String submitButtonId) {
        client.click(submitSelector(submitButtonId));
        client.waitForAjaxWithJquery(DEFAULT_TIMEOUT);
    }

}
