package com.atlassian.support.tools.salext.mail;


import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;

import com.atlassian.core.task.MultiQueueTaskManager;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.MailQueueItem;
import com.atlassian.support.tools.salext.SupportApplicationInfo;

public class ConfluenceMailUtility extends AbstractMailUtility
{
	private MultiQueueTaskManager taskManager;
	private static final Logger log = Logger.getLogger(ConfluenceMailUtility.class);
	
	public ConfluenceMailUtility(MultiQueueTaskManager taskManager)
	{
		super();
		this.taskManager = taskManager;
	}

	@Override
	protected void queueSupportRequestEmail(SupportRequest requestInfo, SupportApplicationInfo info) throws MailException, AddressException, MessagingException
	{
		MailQueueItem item = new SupportRequestMailQueueItem(requestInfo, info);
		this.taskManager.addTask("mail", item);
		
		log.debug("Added message '" + item.getSubject() + "' to the Confluence mail queue...");
	}
	
	@Override
	public void sendMail(ProductAwareEmail email) {
		MailQueueItem item = new SimpleSupportMailQueueItem(email);
		if (isMailServerConfigured()) {
			this.taskManager.addTask("mail", item);
			log.debug("Added message'" + item.getSubject() + "' to the Confluence mail queue...");
		}
		else {
			try {
				item.send();
				log.debug("Sent message '" + item.getSubject() + "' using Atlassian's mail servers.");
			} catch (MailException e) {
				log.error("Error sending mail using Atlassian's mail servers:", e);
			}
		}
	}
}
