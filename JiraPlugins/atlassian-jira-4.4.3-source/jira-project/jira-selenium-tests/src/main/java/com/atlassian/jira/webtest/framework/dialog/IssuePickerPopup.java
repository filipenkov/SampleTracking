package com.atlassian.jira.webtest.framework.dialog;

import com.atlassian.jira.webtest.framework.core.component.Option;
import com.atlassian.jira.webtest.framework.core.component.Select;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.model.IssueData;

/**
 * Old style issue picker popup, used e.g in the convert to sub-task flow.
 *
 * @since v4.3
 */
public interface IssuePickerPopup extends Dialog<IssuePickerPopup>
{

    /**
     * Mode of the current issue search. 
     *
     */
    static enum SearchMode
    {
        /**
         * 'Recent issues' mode containing two sections: recent issues and current issues.
         *
         * @see com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup.ResultSection#RECENT_ISSUES
         * @see com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup.ResultSection#CURRENT_ISSUES
         */
        RECENT_ISSUES,

        /**
         * 'Filter' mode to search within a selected issue filter. Contains only one 'Filter' section
         *
         * @see com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup.ResultSection#FILTER
         */
        FILTER
    }

    /**
     * Result sections {@link #RECENT_ISSUES} and {@link #CURRENT_ISSUES} is displayed for the search mode
     * {@link com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup.SearchMode#RECENT_ISSUES}, and
     * {@link #FILTER} for search mode {@link com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup.SearchMode#FILTER}.
     *
     */
    static enum ResultSection
    {
        RECENT_ISSUES(SearchMode.RECENT_ISSUES),
        CURRENT_ISSUES(SearchMode.RECENT_ISSUES),
        FILTER(SearchMode.FILTER);

        private final SearchMode mode;

        ResultSection(SearchMode mode)
        {
            this.mode = mode;
        }

        /**
         * Search mode if this results section.
         *
         * @return search mode
         * @see com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup.SearchMode
         */
        public SearchMode mode()
        {
            return mode;
        }
    }

    static interface CloseMode
    {
        /**
         * Close the popup by closing the popup browser window
         *
         */
        void byClosingWindow();

        /**
         * Close this popup by selecting an issue from the current results
         *
         * @param issueData issue data of the issue to select
         */
        void bySelectingIssue(IssueData issueData);
    }

    /* ---------------------------------------------- COMPONENTS ---------------------------------------------------- */

    /**
     * <p>
     * Filter select of this popup.
     *
     * <p>
     * NOTE: Changing selection of this select will cause reload of this popup into the filter search mode.
     *
     * @return HTML select containing filters to search issues in
     * @see com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup.SearchMode#FILTER
     */
    Select filterSelect();

    /* ------------------------------------------------ QUERIES ----------------------------------------------------- */

    /**
     * Checks, whether this picker is currently in given search <tt>mode</tt>.
     *
     * @param mode mode to verify
     * @return timed condition verifyng, if the picker is in given <tt>mode</tt>
     * @see com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup.SearchMode
     */
    TimedCondition isInMode(SearchMode mode);

    

    /**
     * Current search mode of this picker. If this is not accessible before the query timeout, return <code>null</code>.
     *
     * @return timed query for the search mode of this picker
     * @see com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup.SearchMode
     */
    TimedQuery<SearchMode> searchMode();

    /**
     * Checks whether this picker has any issues in a particular result section.
     *
     * @param section section to check
     * @return condition verifying if there are any issue in the given results section, which also assumes that the
     * picker is open in appropriate search mode (i.e. search mode containing given <tt>section</tt>)
     * @see com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup.ResultSection#mode()
     */
    TimedCondition hasAnyIssues(ResultSection section);

    /**
     * Checks whether this picker has a particular issue, described by <tt>issueData</tt>, in a particular result
     * <tt>section</tt>.
     *
     * @param section section to check
     * @param issueData issue to find
     * @return condition verifying if the issue is present in the given results section, which also assumes that the
     * picker is open in appropriate search mode (i.e. search mode containing given <tt>section</tt>)
     * @see com.atlassian.jira.webtest.framework.dialog.IssuePickerPopup.ResultSection#mode()
     */
    TimedCondition hasIssue(ResultSection section, IssueData issueData);

    /**
     * Checks whether this picker has a particular issue, described by <tt>issueData</tt>, in its search results
     * (in any section).
     *
     * @param issueData issue to find
     * @return condition verifying if the issue is present in any results section of this picker, which also assummes that
     * the picker is open
     */
    TimedCondition hasIssue(IssueData issueData);


    /* ------------------------------------------------ ACTIONS ----------------------------------------------------- */

    /**
     * Switch to recent issues mode. Do nothing, if the picker already is in that mode.
     *
     * @return this picker instance
     */
    IssuePickerPopup switchToRecentIssues();

    /**
     * Switch to filter represented by given <tt>filterOption</tt>. Filter options may be retrieved from this picker's
     * filter select, or created manually by clients.
     *
     * @param filterOption an option representing the filter
     * @return this picker instance
     * @see #filterSelect()
     * @see com.atlassian.jira.webtest.framework.core.component.Select#all()
     * @see com.atlassian.jira.webtest.framework.core.component.Options
     */
    IssuePickerPopup switchToFilter(Option filterOption);

    /**
     * Close this issue picker popup
     *
     * @return close mode
     */
    CloseMode close();
}
