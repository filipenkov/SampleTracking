package com.atlassian.support.tools.salext.mail;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.apache.log4j.Logger;

import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.MailQueueItem;
import com.atlassian.stash.mail.MailMessage;
import com.atlassian.stash.mail.MailService;
import com.atlassian.support.tools.salext.SupportApplicationInfo;

public class StashMailUtility extends AbstractMailUtility {

	private static final Logger log = Logger.getLogger(AbstractMailUtility.class);

	private final MailService mailService;

	public StashMailUtility(MailService mailService) {
		this.mailService = mailService;
	}

	@Override
	public boolean isMailServerConfigured() {
		return mailService.isMailHostConfigured();
	}

	@Override
	protected void queueSupportRequestEmail(SupportRequest requestInfo, SupportApplicationInfo info)
			throws MailException, AddressException, MessagingException, IOException {
		final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

		try {
			Thread.currentThread().setContextClassLoader(mailService.getClass().getClassLoader());

			if (isMailServerConfigured()) {
				MailMessage.Builder builder = new MailMessage.Builder().from(requestInfo.getFromAddress())
						.subject(requestInfo.getSubject()).text(requestInfo.getBody()).to(requestInfo.getToAddress());

				for (Map.Entry<String, String> header : requestInfo.getHeaders()) {
					builder.header(header.getKey(), header.getValue());
				}

				for (final SupportRequestAttachment attachment : requestInfo.getAttachments()) {
					Object data = attachment.getData();

					byte[] bytes;
					if (data instanceof byte[]) {
						bytes = (byte[]) data;
					} else if (data instanceof String) {
						bytes = ((String) data).getBytes();
					} else {
						log.warn("Attachment [" + attachment.getName() + "], of declared type [" + attachment.getType()
								+ "], contains unexpected data: "
								+ (data == null ? "(Null)" : data.getClass().getName())
								+ ". It will not be added to the mail message");
						continue;
					}
					builder.attachment(attachment.getName(), new ByteArrayDataSource(bytes, attachment.getType()));
				}

				mailService.sendMessage(builder.build());
				log.info("Sent support request to " + requestInfo.getToAddress() + " using configured MailService");
			} else {
				Properties props = new Properties();
				props.setProperty("mail.smtp.host", "mail.atlassian.com");
				props.put("mail.smtp.starttls.enable", String.valueOf(true));
				Session session = Session.getInstance(props);

				MimeMessage msg = new MimeMessage(session);
				msg.setContent(SupportRequestMailQueueItem.toMultiPart(requestInfo));
				msg.setFrom(new InternetAddress(requestInfo.getFromAddress()));
				msg.setRecipients(javax.mail.Message.RecipientType.TO,
						InternetAddress.parse(requestInfo.getToAddress(), false));
				msg.setSubject(requestInfo.getSubject());

				for (Map.Entry<String, String> entry : requestInfo.getHeaders()) {
					msg.addHeader(entry.getKey(), entry.getValue());
				}

				Transport.send(msg);
				log.info("Sent support request to " + requestInfo.getToAddress() + " using Atlassian mail server");
			}
		} finally {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}

	private Set<String> addressStringToSet(String addressList) {
		Set<String> addressSet = new TreeSet<String>();
		for (String address : addressList.split(",")) {
			addressSet.add(address);
		}
		return addressSet;
	}

	@Override
	public void sendMail(ProductAwareEmail email) {
		final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

		try {
			Thread.currentThread().setContextClassLoader(mailService.getClass().getClassLoader());

			if (isMailServerConfigured()) {
				MailMessage.Builder builder = new MailMessage.Builder().from(email.getFrom())
						.subject(email.getSubject()).text(email.getBody()).to(email.getTo());

				mailService.sendMessage(builder.build());
			} else {
				try {
					Properties props = new Properties();
					props.setProperty("mail.smtp.host", "mail.atlassian.com");
					props.put("mail.smtp.starttls.enable", String.valueOf(true));
					Session session = Session.getInstance(props);

					MimeMessage msg = new MimeMessage(session);
					msg.setContent(email.getMultipart());
					msg.setFrom(new InternetAddress(email.getFrom()));
					msg.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(email.getTo(), false));
					msg.setSubject(email.getSubject());

					Iterator iterator = email.getHeaders().entrySet().iterator();
					while (iterator.hasNext()) {
						Entry entry = (Entry) iterator.next();
						msg.addHeader(entry.getKey().toString(), entry.getValue().toString());
					}

					Transport.send(msg);

					MailQueueItem item = new SimpleSupportMailQueueItem(email);
					item.send();
					log.debug("Sent message '" + item.getSubject() + "' using Atlassian's mail servers.");
				} catch (Exception e) {
					log.error("Error sending mail using Atlassian's mail servers:", e);
				}
			}
		} finally {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}

	}
}
