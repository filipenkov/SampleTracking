package com.atlassian.jira.webtest.framework.dialog;

import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.component.AutoCompleteInput;
import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.model.IssueAware;
import com.atlassian.jira.webtest.framework.model.IssueOperation;

/**
 * The one and only, the Dot Dialog (a.k.a. the Issue Actions Dialog).
 *
 * @since v4.3
 */
public interface DotDialog extends Dialog<DotDialog>, IssueAware, Localizable
{
    /* ----------------------------------------- NESTED INTERFACES -------------------------------------------------- */

    interface DDInput extends AutoCompleteInput<DotDialog>
    {
        DDDropDown dropDown();
    }

    interface DDDropDown extends AjsDropdown<DotDialog>
    {
        /**
         * Find item in the list associated with given <tt>issueOperation</tt>.
         *
         * @param issueOperation issue operation to look up
         * @return timed query for an item corresponding to the given <tt>issueOperation</tt>
         * @throws IllegalArgumentException if the position could not be found (e.g. it is not on a current list as it
         * is filtered against some input)
         */
        TimedQuery<Item<DotDialog>> findFor(IssueOperation issueOperation);
    }

    interface CloseMode
    {
        void byEnter();

        void byEscape();

        void byClickIn(AjsDropdown.Item position);
    }

    /* ---------------------------------------------- QUERIES ------------------------------------------------------- */


    /*  -------------------------------------------- COMPONENTS ----------------------------------------------------- */

    /**
     * Input of this Dot Dialog
     *
     * @return input
     */
    DDInput input();

    /**
     * Suggestions drop-down of this Dot Dialog
     *
     * @return actions drop-down
     */
    DDDropDown dropDown();

    /*  --------------------------------------------- ACTIONS ------------------------------------------------------- */

    /**
     * Close this Dot Dialog by given mode.
     *
     * @return close mode of this dialog
     */
    CloseMode close();
}
