package com.atlassian.support.tools.salext.mail;

import java.util.Date;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.log4j.Logger;

import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.queue.MailQueueItem;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;

public class SupportMailQueueItem implements MailQueueItem
{
	private static final Logger log = Logger.getLogger(SupportMailQueueItem.class);
	
	private final SupportRequest supportRequest;
	private final Date dateQueued;
	private volatile int sendCount = 0;

	public SupportMailQueueItem(SupportRequest supportRequest)
	{
		this.supportRequest = supportRequest;
        this.dateQueued = new Date();
	}

	public SupportRequest getSupportRequest()
	{
		return supportRequest;
	}
	
	@Override
	public void send() throws MailException
	{
		sendCount++;

		SMTPMailServer smtpMailServer = MailFactory.getServerManager().getDefaultSMTPMailServer();

		if(smtpMailServer == null)
		{
			smtpMailServer = new SMTPMailServerImpl(null, "AtlassianSMTP", "Atlassian SMTP Server", null, null, false, "mail.atlassian.com", null, null);
		}
		
		Email email = new Email(supportRequest.getToAddress());
		email.setFrom(supportRequest.getFromAddress());
		email.setSubject(supportRequest.getSubject());

		for(Map.Entry<String, String> entry: supportRequest.getHeaders())
		{
			email.addHeader(entry.getKey(), entry.getValue());
		}
		
		Multipart bodyMimeMultipart = toMultiPart(supportRequest);
		email.setMultipart(bodyMimeMultipart);
		smtpMailServer.send(email);
	}

	public static Multipart toMultiPart(SupportRequest supportRequest) throws MailException
	{
		Multipart bodyMimeMultipart = new MimeMultipart();
		try
		{
			if(supportRequest.getBody() != null)
			{
				MimeBodyPart textContent = new MimeBodyPart();
				textContent.setText(supportRequest.getBody());
				bodyMimeMultipart.addBodyPart(textContent);
			}
			
			for(SupportRequestAttachment attachment: supportRequest.getAttachments())
			{
				MimeBodyPart attachmentPart = new MimeBodyPart();
				if(attachment.getData() instanceof byte[])
				{	
					DataSource fds = new ByteArrayDataSource((byte[]) attachment.getData(), attachment.getType());
					attachmentPart.setDataHandler(new DataHandler(fds));
				}
				else if(attachment.getData() instanceof String)
				{
					attachmentPart.setText((String) attachment.getData());
				}
				else
				{
					log.error("Unregognized type: "+attachment.getData().getClass().getName());
				}
				
				attachmentPart.setFileName(attachment.getName());
				bodyMimeMultipart.addBodyPart(attachmentPart);
			}
		}
		catch(MessagingException e)
		{
			throw new MailException(e.getMessage(), e);
		}
		return bodyMimeMultipart;
	}

	@Override
	public void execute() throws Exception
	{
		send();
	}

	@Override
	public String getSubject()
	{
		return supportRequest.getSubject();
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
		return false;
	}
}
