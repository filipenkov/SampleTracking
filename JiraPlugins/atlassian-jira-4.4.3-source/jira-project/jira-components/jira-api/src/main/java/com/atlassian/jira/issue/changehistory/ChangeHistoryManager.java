package com.atlassian.jira.issue.changehistory;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Manages the change history of issues. TODO: expand this interface to include the functionality from ChangeLogUtils
 */
public interface ChangeHistoryManager
{
    /**
     * Returns a List of  ChangeHistory entities
     *
     * @param issue the issue.
     * @return a List of ChangeHistory entries.
     */
    List<ChangeHistory> getChangeHistories(Issue issue);

    /**
     * Returns a List of  ChangeHistory entities for the given issue.
     *
     * @param issue the issue.
     * @param remoteUser the user who is asking.
     * @return a List of ChangeHistory entries.
     */
    List<ChangeHistory> getChangeHistoriesForUser(Issue issue, User remoteUser);

    /**
     * Returns a List of ChangeHistory entities for the given issue and user.
     *
     * @param issue the issue.
     * @param remoteUser the user who is asking.
     * @return a List of ChangeHistory entries.
     *
     * @deprecated Please use {@link #getChangeHistoriesForUser(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)} instead. Since v4.3
     */
    List<ChangeHistory> getChangeHistoriesForUser(Issue issue, com.opensymphony.user.User remoteUser);

    /**
     * Returns a List of ChangeItemBean's for the given issue which also are for the provided changeItemFieldName (i.e.
     * Link, Fix Version/s, etc). The order of the list will from oldest to newest.
     *
     * @param issue the issue the change items are associated with, not null.
     * @param changeItemFieldName the field name the change item is stored under, not null or empty.
     * @return a List of ChangeItemBean's for the given issue.
     */
    List<ChangeItemBean> getChangeItemsForField(Issue issue, String changeItemFieldName);

    /**
     * Returns a List of {@link com.atlassian.jira.issue.changehistory.ChangeHistoryItem}'s for the given issue
     *
     * @param issue the issue
     * @return  A list containing all of the change items for a specific Issue
     */
    List<ChangeHistoryItem> getAllChangeItems(Issue issue);

    /**
     * Returns an issue that has been moved by searching the change history of the original issue key for an updated
     * issue key.
     *
     * @param originalKey the original key of an issue that has since been moved (moving between projects assigns a new
     * key to an issue)
     * @return the moved {@link Issue} object
     * @throws org.ofbiz.core.entity.GenericEntityException if an unexpected error occurs
     */
    Issue findMovedIssue(String originalKey) throws GenericEntityException;

    /**
     * Given an issue key, this method returns a list of previous issue keys this issue was moved from.  This may be
     * useful for source control plugins for example, where a given changeset should be displayed even after an issue
     * has been moved and it's issue key has changed.
     * <p/>
     * The list of previous issue keys is returned in chronological order, with the most recent issue key first.
     *
     * @param issueKey The current issue key.
     * @return A collection of previous issue keys or an empty list if none exist.
     */
    Collection<String> getPreviousIssueKeys(String issueKey);

    /**
     * Returns the same as {@link #getPreviousIssueKeys(String)} but is slightly more efficient since no lookup of the
     * issue id needs to be performed.  If you have an issue object available with the issue's id use this method.
     *
     * @param issueId The id of the issue being looked up.
     * @return A collection of previous issue keys or an empty list if none exist.
     */
    Collection<String> getPreviousIssueKeys(Long issueId);

    /**
     * Find a list of issues that the given users have acted on.
     *
     * @param remoteUser The user executing this request.
     * @param usernames The users to find the history for
     * @param maxResults The maxmimum number of issues to return
     * @return An immutable collection of issue objects sorted by creation date in descending order
     * @since v4.0
     */
    Collection<Issue> findUserHistory(User remoteUser, Collection<String> usernames, int maxResults);

    /**
     * Find a list of issues that the given users have acted on.
     *
     * @param remoteUser The user executing this request.
     * @param usernames The users to find the history for
     * @param maxResults The maxmimum number of issues to return
     * @return An immutable collection of issue objects sorted by creation date in descending order
     * @since v4.3
     *
     * @deprecated Please use {@link #findUserHistory(com.atlassian.crowd.embedded.api.User, java.util.Collection, int)} instead. Since v4.3
     */
    Collection<Issue> findUserHistory(com.opensymphony.user.User remoteUser, Collection<String> usernames, int maxResults);

    /**
     * Find a list of issues that the given users have acted on with the option to limit the projects included
     * in the search.
     *
     * @param remoteUser The user executing this request.
     * @param usernames The users to find the history for
     * @param projects The projects to include issues from
     * @param maxResults The maxmimum number of issues to return
     * @return An immutable collection of issue objects sorted by creation date in descending order
     * @since v4.3
     */
    Collection<Issue> findUserHistory(User remoteUser, Collection<String> usernames, Collection<Project> projects, int maxResults);

    /**
     * Find a list of issues that the given users have acted on with the option to limit the projects included
     * in the search.
     *
     * @param remoteUser The user executing this request.
     * @param usernames The users to find the history for
     * @param projects The projects to include issues from
     * @param maxResults The maxmimum number of issues to return
     * @return An immutable collection of issue objects sorted by creation date in descending order
     * @since v4.0
     *
     * @deprecated Please use {@link #findUserHistory(com.atlassian.crowd.embedded.api.User, java.util.Collection, java.util.Collection, int)} instead. Since v4.3
     */
    Collection<Issue> findUserHistory(com.opensymphony.user.User remoteUser, Collection<String> usernames, Collection<Project> projects, int maxResults);

     /**
     * Find a map of all names ever used in the change history.
     *
     * @param field The field name.
     * @return An immutable map of issue objects sorted by creation date in descending order
     * @since v4.3
     */
    Map<String, String> findAllPossibleValues(String field);

     /**
     * Remove all change items associated with an issue.
     *
     * @param issue affected issue
     */
    void removeAllChangeItems(final Issue issue);
}
