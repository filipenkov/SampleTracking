package com.atlassian.jira.service.util.handler;

import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.mail.MailUtils;

import java.io.IOException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;

/**
 * Mock handler to enable testing of methods in the Abstract handler.
 */
class MockAbstractMessageHandler extends AbstractMessageHandler
{
    public MockAbstractMessageHandler(CommentManager commentManager, IssueFactory issueFactory, ApplicationProperties applicationProperties, JiraApplicationContext jiraApplicationContext)
    {
        super(commentManager, issueFactory, applicationProperties, jiraApplicationContext);
    }

    public boolean handleMessage(Message message) throws MessagingException
    {
        return false;
    }

    protected boolean attachPlainTextParts(final Part part) throws MessagingException, IOException
    {
        return !MailUtils.isContentEmpty(part) && MailUtils.isPartAttachment(part);
    }

    protected boolean attachHtmlParts(final Part part) throws MessagingException, IOException
    {
        return !MailUtils.isContentEmpty(part) && MailUtils.isPartAttachment(part);
    }
}
