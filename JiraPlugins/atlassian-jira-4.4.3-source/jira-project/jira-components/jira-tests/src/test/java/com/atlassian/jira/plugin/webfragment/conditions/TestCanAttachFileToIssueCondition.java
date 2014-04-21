package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import static org.easymock.EasyMock.expect;

public class TestCanAttachFileToIssueCondition extends ListeningTestCase
{
    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final AttachmentService attachmentService = mocksControl.createMock(AttachmentService.class);

        JiraServiceContext context = new JiraServiceContextImpl(null, new SimpleErrorCollection());

        expect(attachmentService.canCreateAttachments(context, issue)).andReturn(true);

        final AbstractIssueCondition condition = new CanAttachFileToIssueCondition(attachmentService);

        mocksControl.replay();
        assertTrue(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();

    }

    @Test
    public void testFalse()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);
        final AttachmentService attachmentService = mocksControl.createMock(AttachmentService.class);

        JiraServiceContext context = new JiraServiceContextImpl(null, new SimpleErrorCollection());

        expect(attachmentService.canCreateAttachments(context, issue)).andReturn(false);

        final AbstractIssueCondition condition = new CanAttachFileToIssueCondition(attachmentService);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, issue, null));
        mocksControl.verify();

    }
}
