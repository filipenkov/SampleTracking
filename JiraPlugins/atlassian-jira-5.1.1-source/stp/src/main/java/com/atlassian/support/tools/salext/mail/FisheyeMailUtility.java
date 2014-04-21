package com.atlassian.support.tools.salext.mail;

import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.atlassian.fisheye.spi.data.MailMessageData;
import com.atlassian.fisheye.spi.services.MailService;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.support.tools.salext.SupportApplicationInfo;
import com.cenqua.fisheye.AppConfig;

public class FisheyeMailUtility extends AbstractMailUtility
{
	private static final Logger log = Logger.getLogger(FisheyeMailUtility.class);
	private final MailService mailService;
	
	public FisheyeMailUtility(MailService mailService) {
		this.mailService = mailService;
	}

	@Override
	public boolean isMailServerConfigured()
	{
		return AppConfig.getsConfig().getConfig().getSmtp() != null;
	}

	@Override
	protected void queueSupportRequestEmail(SupportRequest requestInfo, SupportApplicationInfo info) throws MailException, AddressException, MessagingException
	{
				
		// inside the OSGi container running in JRE 1.6 there are two
		// activation.jar files.
		// The one shipped with the JRE takes precedence but clashes with the
		// version of the mail.jar we ship
		// to correct this - we need to run the mail sending with the class
		// loader of the product itself in the Thread Context
		final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		
		try
		{
			Thread.currentThread().setContextClassLoader(AppConfig.getsConfig().getClass().getClassLoader());

			if( ! isMailServerConfigured())
			{
				Properties props = new Properties();
				props.setProperty("mail.smtp.host", "mail.atlassian.com");
				props.put("mail.smtp.starttls.enable", String.valueOf(true));
				Session session = Session.getInstance(props);

				MimeMessage msg = new MimeMessage(session);
				msg.setFrom(new InternetAddress(requestInfo.getFromAddress()));
				msg.setRecipients(javax.mail.Message.RecipientType.TO,
						InternetAddress.parse(requestInfo.getToAddress(), false));
				msg.setSubject(requestInfo.getSubject());
				for(Entry<String, String> entry: requestInfo.getHeaders())
				{
					msg.addHeader(entry.getKey(), entry.getValue());
				}

				Multipart multiPart = SupportRequestMailQueueItem.toMultiPart(requestInfo);
				msg.setContent(multiPart);

				Transport.send(msg);
				log.info("Sent support request to " + requestInfo.getToAddress() + " using Atlassian mail server.");
			}
			else
			{
				// We need to construct one of these to hand off to Fisheye's internal mailer.
				MailMessageData message = new MailMessageData(); 

				message.setFrom(requestInfo.getFromAddress());
				message.addRecipient(requestInfo.getToAddress());
				message.setSubject(requestInfo.getSubject());
				for(Entry<String, String> entry: requestInfo.getHeaders())
				{
					message.addHeader(entry.getKey(), entry.getValue());
				}

				Multipart multiPart = SupportRequestMailQueueItem.toMultiPart(requestInfo);
				message.setMultiPartContent(multiPart);

				if(mailService.sendMessage(message))
				{
					log.info("Sent support request to " + requestInfo.getToAddress() + " successfully.");
				}
				else
				{
					log.error("Unable to send support request using configured mail server.");
				}
			}
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}

	@Override
	public void sendMail(ProductAwareEmail email) {
		if (isMailServerConfigured()) {
			// We need to construct one of these to hand off to Fisheye's internal mailer.
			MailMessageData message = new MailMessageData(); 
			
			message.setFrom(email.getFrom());
			message.addRecipient(email.getTo());
			message.addRecipient(email.getCc());
			message.addRecipient(email.getBcc());
			message.setSubject(email.getSubject());
			message.setBodyText(MailMessageData.CONTENT_TYPE_HTML,email.getBody());
			
			if(mailService.sendMessage(message))
			{
				log.info("Sent scheduled email successfully.");
			}
			else
			{
				log.error("Unable to send scheduled mail using configured mail server.");
			}
		}
		else
		{
			SimpleSupportMailQueueItem item = new SimpleSupportMailQueueItem(email);
			try {
				item.send();
			} catch (MailException e) {
				log.error("Error sending mail using Atlassian mail server:", e);
			}
		}
	}

}
