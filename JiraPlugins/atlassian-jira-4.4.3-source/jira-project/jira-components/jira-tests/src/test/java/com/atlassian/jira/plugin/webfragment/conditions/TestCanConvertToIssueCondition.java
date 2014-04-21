package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.subtask.conversion.SubTaskToIssueConversionService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;
import org.easymock.IMocksControl;

public class TestCanConvertToIssueCondition extends ListeningTestCase
{
    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final SubTaskToIssueConversionService conversionService = mocksControl.createMock(SubTaskToIssueConversionService.class);

        JiraServiceContext context = new JiraServiceContextImpl(null, new SimpleErrorCollection());

        expect(conversionService.canConvertIssue(context, issue)).andReturn(true);

        final AbstractIssueCondition condition = new CanConvertToIssueCondition(conversionService);

        mocksControl.replay();
        assertTrue(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testFalse()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final SubTaskToIssueConversionService conversionService = mocksControl.createMock(SubTaskToIssueConversionService.class);

        JiraServiceContext context = new JiraServiceContextImpl(null, new SimpleErrorCollection());

        expect(conversionService.canConvertIssue(context, issue)).andReturn(false);

        final AbstractIssueCondition condition = new CanConvertToIssueCondition(conversionService);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();

    }
}
