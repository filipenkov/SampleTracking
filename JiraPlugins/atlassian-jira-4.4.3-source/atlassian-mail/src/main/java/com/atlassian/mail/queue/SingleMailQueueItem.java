package com.atlassian.mail.queue;

import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.SMTPMailServer;
import org.apache.log4j.Logger;

public class SingleMailQueueItem extends AbstractMailQueueItem
{
    private static final Logger LOG = Logger.getLogger(SingleMailQueueItem.class);

    private final Email email;

    public SingleMailQueueItem(Email email)
    {
        super(email.getSubject());
        this.email = email;
    }

    public void send() throws MailException
    {
        incrementSendCount();

        SMTPMailServer smtpMailServer = MailFactory.getServerManager().getDefaultSMTPMailServer();

        if (smtpMailServer == null)
        {
            LOG.debug("Not sending message as the default SMTP Mail Server is not defined.");
            return;
        }

        // Check if mailing is disabled && if SMTPMailServer has been set
        if (!MailFactory.isSendingDisabled())
        {
            // If not, send the message
            if (mailThreader != null) mailThreader.threadEmail(email);
            smtpMailServer.send(email);
            if (mailThreader != null) mailThreader.storeSentEmail(email);
        }
		else
		{
			LOG.debug("Not sending message as sending is turned off.");
        }
    }

    public Email getEmail()
    {
        return email;
    }

    public String toString()
    {
        return (email != null ? email.toString() : "null");
    }
}
