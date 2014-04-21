package com.atlassian.streams.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.security.JiraAuthenticationContext;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.size;

public class JiraInlineActionHandlerImpl implements JiraInlineActionHandler
{
    private final WatcherManager watcherManager;
    private final IssueManager issueManager;
    private final JiraAuthenticationContext authenticationContext;
    private final VoteManager voteManager;

    public JiraInlineActionHandlerImpl(final WatcherManager watcherManager, final IssueManager issueManager, 
            final JiraAuthenticationContext authenticationContext, final VoteManager voteManager)
    {
        this.watcherManager = checkNotNull(watcherManager, "watcherManager");
        this.issueManager = checkNotNull(issueManager, "issueManager");
        this.authenticationContext = checkNotNull(authenticationContext, "authenticationContext");
        this.voteManager = checkNotNull(voteManager, "voteManager");
    }
    
    public boolean startWatching(String issueKey)
    {
        User user = authenticationContext.getLoggedInUser();
        Issue issue = issueManager.getIssueObject(issueKey);
        
        int previousWatcherCount = size(watcherManager.getCurrentWatcherUsernames(issue.getGenericValue()));
        watcherManager.startWatching(user, issue.getGenericValue());
        int updatedWatcherCount = size(watcherManager.getCurrentWatcherUsernames(issue.getGenericValue()));
        return updatedWatcherCount == previousWatcherCount + 1;
    }

    public boolean voteOnIssue(String issueKey)
    {
        User user = authenticationContext.getLoggedInUser();
        Issue issue = issueManager.getIssueObject(issueKey);
        return voteManager.addVote(user, issue.getGenericValue());
    }
}
