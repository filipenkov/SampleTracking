package com.atlassian.jira.webtest.framework.page.issue;

import com.atlassian.jira.webtest.framework.dialog.issueaction.IssueActionDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.page.ParentPage;
import com.atlassian.jira.webtest.framework.model.IssueAware;
import com.atlassian.jira.webtest.framework.page.IssueActionsParent;

/**
 * <p>
 * Represents the View Issue page in JIRA.
 *
 * <p>
 * TODO some more stories
 *
 * @since v4.3
 */
public interface ViewIssue extends IssueActionsParent, IssueAware, ParentPage
{
    /* ----------------------------------------- NESTED INTERFACES -------------------------------------------------- */

    interface ViewIssueDialogOpenMode<D extends IssueActionDialog<D>> extends DialogOpenMode<D>
    {
        /**
         * Open the dialog by the issue menu
         *
         * @return dialog <tt>D</tt> instance
         * @see IssueMenu
         */
        D byMenu();
    }



    /* --------------------------------------------- COMPONENTS ----------------------------------------------------- */

    /**
     * Issue menu of this View Issue page.
     *
     * @return issue menu
     */
    IssueMenu menu();

    /* ----------------------------------------------- ACTIONS ------------------------------------------------------ */

    /**
     * {@inheritDoc}
     *
     * <p>
     * Extension of the parent method to be able to open the dialog via issue menu.
     *
     * @param dialogType {@inheritDoc}
     * @param <D> {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    <D extends IssueActionDialog<D>> ViewIssueDialogOpenMode<D> openDialog(Class<D> dialogType);
    
}
