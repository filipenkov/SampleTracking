package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.opensymphony.user.User;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;
import org.easymock.IMocksControl;

public class TestIsIssueEditableCondition extends ListeningTestCase
{
    @Test
    public void testFalse()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final IssueManager issueManager = mocksControl.createMock(IssueManager.class);

        final User fred = new User("fred", new MockProviderAccessor(), new MockCrowdService());

        expect(issueManager.isEditable(issue)).andReturn(false);

        final AbstractIssueCondition condition = new IsIssueEditableCondition(issueManager);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(fred, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final IssueManager issueManager = mocksControl.createMock(IssueManager.class);

        expect(issueManager.isEditable(issue)).andReturn(false);

        final AbstractIssueCondition condition = new IsIssueEditableCondition(issueManager);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();

    }


}
