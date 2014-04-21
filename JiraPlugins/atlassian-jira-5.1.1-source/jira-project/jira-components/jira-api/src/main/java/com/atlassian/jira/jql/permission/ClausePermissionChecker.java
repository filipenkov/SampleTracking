package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;

/**
 * Checks to see that the provided user is able to use the clause.
 *
 * @since v4.0
 */
public interface ClausePermissionChecker
{
    /**
     * Checks to see that the provided user is able to use the clause. This may be as simple as determining if the user
     * has permission to see the field that the clause represents.
     *
     * @param user to check permissions against.
     * @return true if the user can use this clause, false otherwise.
     */
    boolean hasPermissionToUseClause(User user);
}
