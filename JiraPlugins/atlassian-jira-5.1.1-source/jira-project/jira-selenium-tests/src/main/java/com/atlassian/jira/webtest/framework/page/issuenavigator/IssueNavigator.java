package com.atlassian.jira.webtest.framework.page.issuenavigator;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.model.IssueAware;
import com.atlassian.jira.webtest.framework.page.GlobalPage;
import com.atlassian.jira.webtest.framework.page.IssueActionsParent;

/**
 * <p>
 * Represents the Issue Navigator JIRA page.
 *
 * <p>
 * TODO more!
 *
 * @since v4.3
 */
public interface IssueNavigator extends GlobalPage<IssueNavigator>, IssueActionsParent, IssueAware
{

    /* ----------------------------------------------- QUERIES ------------------------------------------------------ */

    /**
     * Checks if simple search mode is on.
     *
     * @return timed condition querying if the simple search mode is on
     */
    TimedCondition isSimpleMode();

    /**
     * Checks if advanced search mode is on.
     *
     * @return timed condition querying if the advanced search mode is on
     */
    TimedCondition isAdvancedMode();

    /* ---------------------------------------------- COMPONENTS ---------------------------------------------------- */

    /**
     * Search results table.
     *
     * @return results
     */
    IssueTable results();

    /**
     * <p>
     * Simple search filter of this issue navigator.
     *
     * <p>
     * NOTE: any operation attempted on the simple search filter in the advances mode may result in illegal state
     * exception. Use {@link SimpleSearchFilter#isReady()}, or
     * {@link #isSimpleMode()} to detect, whether usage of this component is legal in the current test context.
     *
     * @see #isSimpleMode()
     * @see #toSimpleMode()
     * @see SimpleSearchFilter
     * @return simple search filter instance of this issue navigator
     */
    SimpleSearchFilter simpleSearch();


    /* ----------------------------------------------- ACTIONS ------------------------------------------------------ */

    /**
     * Switch to simple search mode.
     *
     * @return this issue navigator instance
     */
    IssueNavigator toSimpleMode();

    /**
     * Switch to advanced (JQL) search mode.
     *
     * @return this issue navigator instance
     */
    IssueNavigator toAdvancedMode();

}
