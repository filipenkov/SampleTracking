package com.atlassian.jira.bc.user.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;

import java.util.List;

/**
 * Service that retrieves a collection of {@link User} objects based on a partial query string
 */
@PublicApi
public interface UserPickerSearchService
{
    /**
     * Get Users based on a query string.
     * <p>
     * Matches on the start of username, Each word in Full Name & email.
     * <p>
     * Results are sorted according to the {@link com.atlassian.jira.issue.comparator.UserBestNameComparator}.
     *
     * @param jiraServiceContext Jira Service Context
     * @param query              String to search for.
     * @return List of {@link User} objects that match criteria.
     */
    List<User> findUsers(JiraServiceContext jiraServiceContext, String query);

    /**
     * Get Users based on a query string.
     * <p>
     * Matches on the start of username, Each word in Full Name & email. This will search even if
     * the query passed is null or empty.
     * <p>
     * Results are sorted according to the {@link com.atlassian.jira.issue.comparator.UserBestNameComparator}.
     *
     * @param jiraServiceContext Jira Service Context
     * @param query              String to search for.
     * @return List of {@link User} objects that match criteria.
     */
    List<User> findUsersAllowEmptyQuery(JiraServiceContext jiraServiceContext, String query);

    /**
     * Returns true only if UserPicker Ajax search is enabled AND the user in the context has User Browse permission.
     *
     * @return True if enabled, otherwise false
     * @param jiraServiceContext Jira Service Context
     */
    boolean canPerformAjaxSearch(JiraServiceContext jiraServiceContext);
    boolean canPerformAjaxSearch(User user);

    /**
      * Whether or not the UserPicker Ajax should search or show email addresses
      *
      * @return True if email addresses can be shown, otherwise false
      * @param jiraServiceContext Jira Service Context
      */
    boolean canShowEmailAddresses(JiraServiceContext jiraServiceContext);
}
