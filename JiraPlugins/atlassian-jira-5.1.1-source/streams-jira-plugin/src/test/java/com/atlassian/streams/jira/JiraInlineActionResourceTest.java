package com.atlassian.streams.jira;

import com.atlassian.streams.jira.rest.resources.JiraInlineActionResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JiraInlineActionResourceTest
{
    @Mock JiraInlineActionHandler handler;

    String issueKey = "issue-key";
    JiraInlineActionResource resource;

    @Before
    public void setUp()
    {
        resource = new JiraInlineActionResource(handler);
    }

    @Test
    public void tryToWatchIssuePreviouslyWatched()
    {
        when(handler.hasPreviouslyWatched(issueKey)).thenReturn(true);
        assertThat(resource.watchIssue(issueKey).getStatus(), is(equalTo(CONFLICT.getStatusCode())));
    }

    @Test
    public void tryToWatchIssueNotWatched()
    {
        when(handler.hasPreviouslyWatched(issueKey)).thenReturn(false);
        when(handler.startWatching(issueKey)).thenReturn(true);
        assertThat(resource.watchIssue(issueKey).getStatus(), is(equalTo(NO_CONTENT.getStatusCode())));
    }

    @Test
    public void tryToWatchIssueAndNotSuccessful()
    {
        when(handler.hasPreviouslyWatched(issueKey)).thenReturn(false);
        when(handler.startWatching(issueKey)).thenReturn(false);
        assertThat(resource.watchIssue(issueKey).getStatus(), is(equalTo(PRECONDITION_FAILED.getStatusCode())));
    }

    @Test
    public void tryToVoteOnIssuePreviouslyVoted()
    {
        when(handler.hasPreviouslyVoted(issueKey)).thenReturn(true);
        assertThat(resource.voteIssue(issueKey).getStatus(), is(equalTo(CONFLICT.getStatusCode())));
    }

    @Test
    public void tryToVoteOnIssueNotVoted()
    {
        when(handler.hasPreviouslyVoted(issueKey)).thenReturn(false);
        when(handler.voteOnIssue(issueKey)).thenReturn(true);
        assertThat(resource.voteIssue(issueKey).getStatus(), is(equalTo(NO_CONTENT.getStatusCode())));
    }

    @Test
    public void tryToVoteOnIssueNotVotedAndNotSuccessful()
    {
        when(handler.hasPreviouslyVoted(issueKey)).thenReturn(false);
        when(handler.voteOnIssue(issueKey)).thenReturn(false);
        assertThat(resource.voteIssue(issueKey).getStatus(), is(equalTo(PRECONDITION_FAILED.getStatusCode())));
    }
}
