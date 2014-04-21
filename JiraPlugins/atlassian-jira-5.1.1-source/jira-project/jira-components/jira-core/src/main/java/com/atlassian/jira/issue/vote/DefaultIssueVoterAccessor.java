package com.atlassian.jira.issue.vote;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Functions;
import com.atlassian.jira.util.NotNull;

import java.util.Collection;
import java.util.Locale;

import static com.atlassian.jira.util.collect.Transformed.collection;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultIssueVoterAccessor implements IssueVoterAccessor
{
    private final VoteManager voteManager;

    public DefaultIssueVoterAccessor(final VoteManager voteManager)
    {
        this.voteManager = notNull("voteManager", voteManager);
    }

    @Override
    public Iterable<User> getVoters(@NotNull Locale displayLocale, @NotNull Issue issue)
    {
        return voteManager.getVoters(issue, displayLocale);
    }

    @Override
    public boolean isVotingEnabled()
    {
        return voteManager.isVotingEnabled();
    }

    @Override
    public Iterable<String> getVoterNames(final @NotNull Issue issue)
    {
        return voteManager.getVoterUsernames(issue.getGenericValue());
    }
}
