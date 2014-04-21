package com.atlassian.jira.webtest.framework.page;

import com.atlassian.jira.webtest.framework.dialog.DotDialog;
import com.atlassian.jira.webtest.framework.dialog.issueaction.IssueActionDialog;
import com.atlassian.jira.webtest.framework.model.IssueAware;

/**
 * A page that is used to invoke various issue actions. Notably this includes View Issue and
 * Issue Navigator.
 *
 * @since v4.3
 */
public interface IssueActionsParent extends Page, IssueAware
{

    /**
     * Default dialog open mode
     *
     * @param <D>
     */
    interface DialogOpenMode<D extends IssueActionDialog<D>>
    {
        /**
         * Open the dialog by its shortcut.
         *
         * @return dialog <tt>D</tt> instance
         * @throws IllegalStateException if the target dialog is associated with an issue operation that has no
         * shortcut (which may be verified by calling
         * {@link com.atlassian.jira.webtest.framework.model.IssueOperation#hasShortcut()}).
         * @see com.atlassian.jira.webtest.framework.dialog.issueaction.IssueActionDialog#action()
         * @see com.atlassian.jira.webtest.framework.model.IssueOperation#hasShortcut()
         * @see com.atlassian.jira.webtest.framework.model.IssueOperation#shortcut()
         */
        D byShortcut();

        /**
         * Open the dialog via the Issue Action Dialog (the 'Dot Dialog')
         *
         * @return dialog <tt>D</tt> instance
         */
        D byDotDialog();
    }

    /**
     * <p>
     * Get dialog of given type associated with this page. Calling this dialogs
     * {@link com.atlassian.jira.webtest.framework.dialog.Dialog#open()} method will attempt to open the dialog by the
     * default method for this page.
     *
     * <p>
     * Contrary to the {@link #openDialog(Class)}, this method will not attempt any actions (opening/closing) on
     * the returned dialog instance. The dialog instance may be used to query the state of this particular dialog
     * (e.g. whether it is currently open as a result of manipulations of the <tt>ViewIssue</tt> page object without
     * involving {@link #openDialog(Class)}.
     *
     * @param dialogType class representing type of the issue action dialog to return
     * @param <D> generic type of the dialog
     * @return dialog <tt>D</tt> instance
     */
    <D extends IssueActionDialog<D>> D dialog(Class<D> dialogType);

    /**
     * Dot Dialog of this issue actions page. This method will not attempt to perform
     * any operations on the dialog.
     *
     * @return dot dialog instance of this page
     */
    DotDialog dotDialog();


    /**
     * Open an issue operation dialog of given <tt>dialogType</tt>.
     *
     * @param dialogType class representing the dialog type
     * @param <D> type of the dialog
     * @return opening mode to open the dialog in a desired way
     */
    <D extends IssueActionDialog<D>> DialogOpenMode<D> openDialog(Class<D> dialogType);


    /**
     * Open and return the dot-dialog for the issue in current context (which may be queried by
     * {@link #issueData()}.
     *
     * @return dot dialog instance of this Issue Navigator
     * @see #issueData()
     */
    DotDialog openDotDialog();
}
