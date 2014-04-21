package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.expect;

import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.vote.VoteManager;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import com.opensymphony.user.User;

public class TestHasVotedForIssueCondition extends ListeningTestCase
{
    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final VoteManager voteManager = mocksControl.createMock(VoteManager.class);

        final User fred = new User("fred", new MockProviderAccessor(), new MockCrowdService());

        expect(voteManager.hasVoted(fred, issue)).andReturn(true);

        final AbstractIssueCondition condition = new HasVotedForIssueCondition(voteManager);

        mocksControl.replay();
        assertTrue(condition.shouldDisplay(fred, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testNullUser()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final VoteManager voteManager = mocksControl.createMock(VoteManager.class);

        final AbstractIssueCondition condition = new HasVotedForIssueCondition(voteManager);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();
    }

    @Test
    public void testFalseEmpty()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final VoteManager voteManager = mocksControl.createMock(VoteManager.class);

        final User fred = new User("fred", new MockProviderAccessor(), new MockCrowdService());

        expect(voteManager.hasVoted(fred, issue)).andReturn(false);

        final AbstractIssueCondition condition = new HasVotedForIssueCondition(voteManager);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(fred, issue, null));
        mocksControl.verify();
    }
}
