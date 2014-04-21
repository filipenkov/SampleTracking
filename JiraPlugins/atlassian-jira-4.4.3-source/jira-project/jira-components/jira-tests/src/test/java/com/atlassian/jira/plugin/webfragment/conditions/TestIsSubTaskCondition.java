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

public class TestIsSubTaskCondition extends ListeningTestCase
{
    @Test
    public void testFalse()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);

        final User fred = new User("fred", new MockProviderAccessor(), new MockCrowdService());

        expect(issue.isSubTask()).andReturn(false);

        final AbstractIssueCondition condition = new IsSubTaskCondition();

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(fred, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);

        expect(issue.isSubTask()).andReturn(true);

        final AbstractIssueCondition condition = new IsSubTaskCondition();

        mocksControl.replay();
        assertTrue(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();
    }


}
