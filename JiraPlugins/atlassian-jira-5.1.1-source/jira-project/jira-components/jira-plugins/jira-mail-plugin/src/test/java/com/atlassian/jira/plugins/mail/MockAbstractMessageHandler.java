package com.atlassian.jira.plugins.mail;

import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.jira.plugins.mail.handlers.AbstractMessageHandler;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageUserProcessor;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.mail.MailUtils;
import org.mockito.Mockito;

import java.io.IOException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;

/**
 * Mock handler to enable testing of methods in the Abstract handler.
 */
public class MockAbstractMessageHandler extends AbstractMessageHandler
{
    public MockAbstractMessageHandler()
    {
        super(Mockito.mock(UserManager.class), null, null, Mockito.mock(MailLoggingManager.class, Mockito.RETURNS_DEEP_STUBS),
                Mockito.mock(MessageUserProcessor.class));
    }

    @Override
    public boolean handleMessage(Message message, MessageHandlerContext context)
            throws MessagingException
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
