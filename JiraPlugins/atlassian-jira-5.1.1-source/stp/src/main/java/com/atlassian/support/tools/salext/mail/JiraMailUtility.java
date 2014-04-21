package com.atlassian.support.tools.salext.mail;


import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.MailQueueItem;
import com.atlassian.support.tools.salext.SupportApplicationInfo;

public class JiraMailUtility extends AbstractMailUtility
{
	private static final Logger log = Logger.getLogger(JiraMailUtility.class);
	/**
	 * JIRA's custom mail header used to contain a magic fingerprint string
	 * "unique" to a JIRA instance, used for identification purposes.
	 */
	public static final String HEADER_JIRA_FINGER_PRINT = "X-JIRA-FingerPrint";

	@Override
	protected void queueSupportRequestEmail(SupportRequest requestInfo, SupportApplicationInfo info) throws MailException, AddressException, MessagingException
	{
		JiraApplicationContext context = ComponentManager.getComponentInstanceOfType(JiraApplicationContext.class);
		requestInfo.addHeader(HEADER_JIRA_FINGER_PRINT, context.getFingerPrint());
		MailQueueItem item = new SupportRequestMailQueueItem(requestInfo, info);
		ComponentAccessor.getMailQueue().addItem(item);
	}
	
	@Override
	public void sendMail(ProductAwareEmail email) {
		MailQueueItem item = new SimpleSupportMailQueueItem(email);
		if (isMailServerConfigured()) {
			ComponentAccessor.getMailQueue().addItem(item);
			log.debug("Added message'" + item.getSubject() + "' to JIRA's mail queue...");
		}
		else {
			try {
				item.send();
				log.debug("Sent message'" + item.getSubject() + "' using Atlassian's mail servers...");
			} catch (MailException e) {
				log.error("Exception sending email using Atlassian's mail servers:", e);
			}
		}
	}

}
