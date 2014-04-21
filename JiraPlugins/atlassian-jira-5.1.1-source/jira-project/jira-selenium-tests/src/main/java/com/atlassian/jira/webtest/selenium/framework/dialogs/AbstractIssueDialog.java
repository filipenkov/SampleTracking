package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.page.issue.SeleniumViewIssue;
import com.atlassian.jira.webtest.framework.impl.selenium.page.issuenavigator.SeleniumIssueNav;
import com.atlassian.jira.webtest.framework.page.issue.ViewIssue;
import com.atlassian.jira.webtest.framework.page.issuenavigator.IssueNavigator;
import com.atlassian.jira.webtest.selenium.framework.model.ActionType;
import com.atlassian.jira.webtest.selenium.framework.model.LegacyIssueOperation;
import com.atlassian.jira.webtest.selenium.framework.model.SubmitType;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.or;
import static com.atlassian.jira.webtest.selenium.framework.model.Locators.JQUERY;

/**
 * Base class for utility classes representing JIRA issue action dialogs.
 *
 * @since v4.2
 */
public abstract class AbstractIssueDialog<T extends AbstractIssueDialog<T>> extends AbstractSubmittableDialog<T> implements IssueActionDialog
{
    // TODO parameterize with target type for actions
    // TODO should derive from AbstractSubmittableDialog

    protected static final String VISIBLE_DIALOG_CONTENT_SELECTOR = JQUERY.create(".aui-dialog-open");
    protected static final String DIALOG_CONTENT_READY_SELECTOR = JQUERY.create(".aui-dialog-content-ready");

    private static final String GENERIC_SUBMIT_SELECTOR = VISIBLE_DIALOG_CONTENT_SELECTOR + " :submit";
    private static final String GENERIC_CANCEL_SELECTOR = VISIBLE_DIALOG_CONTENT_SELECTOR + " .cancel";

    protected final IssueNavigator issueNavigator;
    protected final ViewIssue viewIssue;
    protected final ActionsDialog actionsDialog;

    private final LegacyIssueOperation issueOperation;

    public AbstractIssueDialog(LegacyIssueOperation issueOperation, Class<T> targetType, ActionType afterSubmit, SeleniumContext ctx)
    {
        super(targetType, afterSubmit, ctx);
        this.issueOperation = notNull("issueOperation", issueOperation);
        this.issueNavigator = new SeleniumIssueNav(ctx);
        this.viewIssue =  new SeleniumViewIssue(ctx);
        // TODO remove this and us view issue/issue nav to open the dialog, first make proper API for different opening types
        actionsDialog = new ActionsDialog(context);
    }

    /* ----------------------------------------------- LOCATORS ----------------------------------------------------- */

    public String submitTriggerLocator()
    {
        return GENERIC_SUBMIT_SELECTOR;
    }

    public String cancelTriggerLocator()
    {
        return GENERIC_CANCEL_SELECTOR;
    }

    @Override
    protected String visibleDialogContentsLocator()
    {
        return DIALOG_CONTENT_READY_SELECTOR;
    }

    /* ---------------------------------------------- QUERIES ------------------------------------------------------- */

    public final LegacyIssueOperation issueOperation()
    {
        return issueOperation;
    }

    public final String actionName()
    {
        return issueOperation.name();
    }

    public final boolean isOpenable()
    {
        return or(viewIssue.isAt(), issueNavigator.isAt()).byDefaultTimeout();
    }


    /* --------------------------------------------- ASSERTIONS ----------------------------------------------------- */

    public final void assertReady()
    {
        assertReady(context.timeouts().timeoutFor(Timeouts.DIALOG_LOAD));
    }

    // TODO pull this up to AuiDialog

    /**
     * Assert that no dialog is currently open on the page.
     *
     */
    public final void assertNotOpen() {
        assertThat.elementNotPresentByTimeout(VISIBLE_DIALOG_CONTENT_SELECTOR, context.timeouts().dialogLoad());
    }

    /* ----------------------------------------------- ACTIONS ------------------------------------------------------ */

    // TODO temp default implementations, they will change as API for opening changes

    public T openFromViewIssue()
    {
        viewIssue.menu().invoke(issueOperation);
        assertReady();
        return asTargetType();
    }

    public T openFromIssueNav(final int issueId)
    {
        actionsDialog.openFromIssueNav(issueId);
        actionsDialog.queryActions(actionName());
        actionsDialog.selectSuggestionUsingClick();
        assertReady();
        return asTargetType();
    }

    /**
     * Submit AUI dialog using its default submit button. Wait for the
     * web action triggered by the submit (page load, AJAX etc.)
     *
     * @return this dialog instance
     */
    public final T submit() {
        submit(SubmitType.BY_CLICK);
        return asTargetType();
    }

    /**
     * {@inheritDoc}
     *
     */
    public final Dialog open()
    {
        return openFromViewIssue();
    }

}
