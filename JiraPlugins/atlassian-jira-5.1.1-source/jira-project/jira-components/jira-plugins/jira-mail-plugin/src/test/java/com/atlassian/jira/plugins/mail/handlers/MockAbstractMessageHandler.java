package com.atlassian.jira.plugins.mail.handlers;

import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageUserProcessor;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.mail.MailUtils;

import java.io.IOException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;

/**
 * Mock handler to enable testing of methods in the Abstract handler.
 */
public class MockAbstractMessageHandler extends AbstractMessageHandler
{
    public MockAbstractMessageHandler(ApplicationProperties applicationProperties, JiraApplicationContext jiraApplicationContext)
    {
        super(applicationProperties, jiraApplicationContext);
    }

    public MockAbstractMessageHandler(UserManager userManager, ApplicationProperties applicationProperties,
            JiraApplicationContext jiraApplicationContext, MailLoggingManager mailLoggingManager,
            MessageUserProcessor mailUserProcessor)
    {
        super(userManager, applicationProperties, jiraApplicationContext, mailLoggingManager, mailUserProcessor);
    }

    @Override
    public boolean handleMessage(Message message, MessageHandlerContext context)
            throws MessagingException
    {
        return false;
    }

    @Override
    protected boolean attachPlainTextParts(final Part part) throws MessagingException, IOException
    {
        return !MailUtils.isContentEmpty(part) && MailUtils.isPartAttachment(part);
    }

    @Override
    protected boolean attachHtmlParts(final Part part) throws MessagingException, IOException
    {
        return !MailUtils.isContentEmpty(part) && MailUtils.isPartAttachment(part);
    }
}
