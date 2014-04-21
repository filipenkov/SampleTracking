package com.atlassian.support.tools.salext.mail;

import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.log4j.Logger;

import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.MailQueueItem;
import com.atlassian.support.tools.salext.SupportApplicationInfo;

public class SupportRequestMailQueueItem extends AbstractSupportMailQueueItem
{
	private static final Logger log = Logger.getLogger(SupportRequestMailQueueItem.class);
	
	private final SupportRequest supportRequest;

	private final SupportApplicationInfo info;

	public SupportRequestMailQueueItem(SupportRequest supportRequest, SupportApplicationInfo info)
	{
		super();
		this.supportRequest = supportRequest;
		this.info = info;
	}

	public SupportRequest getSupportRequest()
	{
		return supportRequest;
	}
	
	@Override
	public void send() throws MailException
	{
		ProductAwareEmail email = new ProductAwareEmail(supportRequest.getToAddress(),info);
		email.setFrom(supportRequest.getFromAddress());
		email.setSubject(supportRequest.getSubject());

		for(Map.Entry<String, String> entry: supportRequest.getHeaders())
		{
			email.addHeader(entry.getKey(), entry.getValue());
		}
		
		Multipart bodyMimeMultipart = toMultiPart(supportRequest);
		email.setMultipart(bodyMimeMultipart);

		send(email);
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
					log.error("Unrecognized attachment type: "+attachment.getData().getClass().getName());
				}
				
				attachmentPart.setFileName(attachment.getName());

				log.debug("Adding attachment " + attachmentPart.getFileName());
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
	public String getSubject()
	{
		return supportRequest.getSubject();
	}

	public int compareTo(MailQueueItem o) {
		return 0;
	}
}
