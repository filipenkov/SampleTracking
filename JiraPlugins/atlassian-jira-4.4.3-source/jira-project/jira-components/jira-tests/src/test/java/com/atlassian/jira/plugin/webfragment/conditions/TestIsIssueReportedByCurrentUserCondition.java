package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.issue.Issue;
import com.opensymphony.user.User;
import com.atlassian.jira.local.ListeningTestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;
import org.easymock.IMocksControl;

public class TestIsIssueReportedByCurrentUserCondition extends ListeningTestCase
{
    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);

        final User fred = new User("fred", new MockProviderAccessor(), new MockCrowdService());

        expect(issue.getReporterId()).andReturn("fred");

        final AbstractIssueCondition condition = new IsIssueReportedByCurrentUserCondition();

        mocksControl.replay();
        assertTrue(condition.shouldDisplay(fred, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testNullReporter()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);

        final User fred = new User("fred", new MockProviderAccessor(), new MockCrowdService());

        expect(issue.getReporterId()).andReturn(null);

        final AbstractIssueCondition condition = new IsIssueReportedByCurrentUserCondition();

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(fred, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testNullCurrent()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);

        expect(issue.getReporterId()).andReturn("fred");

        final AbstractIssueCondition condition = new IsIssueReportedByCurrentUserCondition();

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testFalse()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final User fred = new User("fred", new MockProviderAccessor(), new MockCrowdService());

        expect(issue.getReporterId()).andReturn("admin");

        final AbstractIssueCondition condition = new IsIssueReportedByCurrentUserCondition();

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(fred, issue, null));
        mocksControl.verify();


    }

}
