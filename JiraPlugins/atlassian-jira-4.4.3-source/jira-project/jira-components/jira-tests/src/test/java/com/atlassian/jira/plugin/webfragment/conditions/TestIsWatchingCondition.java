package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.opensymphony.user.User;
import com.atlassian.jira.local.ListeningTestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;
import org.easymock.IMocksControl;

public class TestIsWatchingCondition extends ListeningTestCase
{
    @Test
    public void testNullUser()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final WatcherManager watcherManager = mocksControl.createMock(WatcherManager.class);

        final Issue issue = mocksControl.createMock(Issue.class);

        final AbstractIssueCondition condition = new IsWatchingIssueCondition(watcherManager);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testFalse()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final User fred = new User("fred", new MockProviderAccessor(), new MockCrowdService());
        final WatcherManager watcherManager = mocksControl.createMock(WatcherManager.class);

        final Issue issue = mocksControl.createMock(Issue.class);

        final AbstractIssueCondition condition = new IsWatchingIssueCondition(watcherManager);

        expect(watcherManager.isWatching(fred, issue)).andReturn(false);


        mocksControl.replay();
        assertFalse(condition.shouldDisplay(fred, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final User fred = new User("fred", new MockProviderAccessor(), new MockCrowdService());
        final WatcherManager watcherManager = mocksControl.createMock(WatcherManager.class);

        final Issue issue = mocksControl.createMock(Issue.class);

        final AbstractIssueCondition condition = new IsWatchingIssueCondition(watcherManager);

        expect(watcherManager.isWatching(fred, issue)).andReturn(true);


        mocksControl.replay();
        assertTrue(condition.shouldDisplay(fred, issue, null));
        mocksControl.verify();
    }


}
