package com.atlassian.jira.jql.permission;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.vote.VoteManager;

/**
 * Checks if votes are enabled.
 *
 * @since v4.0
 */
public class VotePermissionChecker implements ClausePermissionChecker
{
    private final VoteManager voteManager;

    public VotePermissionChecker(final VoteManager voteManager)
    {
        this.voteManager = voteManager;
    }

    public boolean hasPermissionToUseClause(final User user)
    {
        return voteManager.isVotingEnabled();
    }
}
