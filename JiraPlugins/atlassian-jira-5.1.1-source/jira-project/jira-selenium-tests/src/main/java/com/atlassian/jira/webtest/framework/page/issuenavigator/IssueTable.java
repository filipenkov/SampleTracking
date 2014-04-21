package com.atlassian.jira.webtest.framework.page.issuenavigator;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.model.IssueData;
import com.atlassian.jira.webtest.framework.model.IssueOperation;
import com.atlassian.jira.webtest.framework.page.PageSection;

/**
 * Issue table part of the issue navigator page.
 *
 * @since v4.3
 */
public interface IssueTable extends PageSection<IssueNavigator>
{

    interface IssueRow
    {
        /**
         * Issue data of this row.
         *
         * @return issue data
         */
        IssueData issueData();


        /**
         * Execute given operation for the currently selected issue. Implementations are free to choose how this will
         * be implemented (e.g. the dot dialog, cog menu etc.)
         *
         * @param issueOperation issue operation to execute
         */
        void execute(IssueOperation issueOperation);
    }

    /* ----------------------------------------------- LOCATORS ----------------------------------------------------- */

    /**
     * Locator of a selected row in this table.
     *
     * @return selected row locator
     */
    Locator selectedRowLocator();

    /* ------------------------------------------------ QUERIES ----------------------------------------------------- */

    /**
     * Condition checking if there is any selected row in this issue table.
     *
     * @return condition querying for existence of a selected issue row
     */
    TimedCondition hasSelectedRow();

    /**
     * Condition verifying that issue with given ID is selected.
     *
     * @param issueId ID of the issue that should be selected
     * @return timed condition querying, whether issue with given <tt>issueId</tt> is currently selected
     */
    TimedCondition isSelected(long issueId);

    /**
     * Condition verifying that issue with given <tt>issueKey</tt> is selected.
     *
     * @param issueKey key of the issue that should be selected
     * @return timed condition querying, whether issue with given <tt>issueKey</tt> is currently selected
     */
    TimedCondition isSelected(String issueKey);

    /**
     * Currently selected issue row.
     *
     * @return selected issue row
     * @throws IllegalStateException if no row is selected before the timeout expires 
     */
    TimedQuery<IssueRow> selectedRow();


    /* ------------------------------------------------ ACTIONS ----------------------------------------------------- */

    /**
     * Move selection up by pressing the arrow up key.
     *
     * @return this issue table instance.
     */
    IssueTable up();

    /**
     * Move selection down by pressing the arrow down key.
     *
     * @return this issue table instance.
     */
    IssueTable down();

}
