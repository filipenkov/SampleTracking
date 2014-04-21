package com.atlassian.support.tools.salext.mail;

import java.util.Date;

import org.apache.log4j.Logger;

import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.queue.MailQueueItem;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;

public abstract class AbstractSupportMailQueueItem implements MailQueueItem {
	private static final Logger log = Logger.getLogger(AbstractSupportMailQueueItem.class);
	protected final Date dateQueued;
	protected volatile int sendCount = 0;
	protected boolean hasError = false;

	protected AbstractSupportMailQueueItem() {
        this.dateQueued = new Date();
	}

	@Override
	public void execute() throws Exception
	{
		send();
	}

	public void send(ProductAwareEmail email) throws MailException
	{
		sendCount++;

		SMTPMailServer smtpMailServer;
		smtpMailServer = new SMTPMailServerImpl(null, "AtlassianSMTP", "Atlassian SMTP Server", null, null, false, "mail.atlassian.com", null, null);
		try {
			SMTPMailServer configuredMailServer = MailFactory.getServerManager().getDefaultSMTPMailServer();

			if(configuredMailServer != null)
			{
				smtpMailServer = configuredMailServer;
				log.warn("Sending message via mail server " + smtpMailServer.getHostname());
			}
			else {
				log.warn("No mail server found, using Atlassian mail server.");
			}
		} catch (Exception e1) {
			log.error("Exception retrieving default mail server, using Atlassian mail server.", e1);
		}
	
		try 
		{
			smtpMailServer.send(email);
			this.hasError = false;
		} 
		catch (MailException e) 
		{
			log.error("Error sending message '" + email.getSubject() + "', see stack trace below for details.");
			this.hasError = true;
			throw e;
		}
	}

	@Override
	public Date getDateQueued()
	{
		return dateQueued;
	}

	@Override
	public int getSendCount()
	{
		return sendCount;
	}

	@Override
	public boolean hasError()
	{
		return hasError;
	}
}
