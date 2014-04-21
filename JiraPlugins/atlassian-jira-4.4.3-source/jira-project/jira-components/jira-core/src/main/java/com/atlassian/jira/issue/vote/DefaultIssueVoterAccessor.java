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

    public <T> Iterable<T> getDetails(final Locale displayLocale, final Issue issue, final Function<com.opensymphony.user.User, T> transformer)
    {
        final Collection<com.opensymphony.user.User> voters = voteManager.getVoters(displayLocale, issue.getGenericValue());
        return collection(voters, transformer);
    }

    public Iterable<com.opensymphony.user.User> getDetails(final Locale displayLocale, final Issue issue)
    {
        return getDetails(displayLocale, issue, Functions.<com.opensymphony.user.User> identity());
    }

    @Override
    public Iterable<User> getVoters(@NotNull Locale displayLocale, @NotNull Issue issue)
    {
        return voteManager.getVoters(issue, displayLocale);
    }

    public boolean isVotingEnabled()
    {
        return voteManager.isVotingEnabled();
    }

    public Iterable<String> getVoterNames(final @NotNull Issue issue)
    {
        return voteManager.getVoterUsernames(issue.getGenericValue());
    }
}
