package com.atlassian.support.tools.salext.mail;


import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.MailQueueItem;

public class JiraMailUtility extends AbstractMailUtility
{
	/**
	 * JIRA's custom mail header used to contain a magic fingerprint string
	 * "unique" to a JIRA instance, used for identification purposes.
	 */
	public static final String HEADER_JIRA_FINGER_PRINT = "X-JIRA-FingerPrint";

	@Override
	protected void queueEmail(SupportRequest requestInfo) throws MailException, AddressException, MessagingException
	{
		JiraApplicationContext context = ComponentManager.getComponentInstanceOfType(JiraApplicationContext.class);
		requestInfo.addHeader(HEADER_JIRA_FINGER_PRINT, context.getFingerPrint());
		MailQueueItem item = new SupportMailQueueItem(requestInfo);
		ManagerFactory.getMailQueue().addItem(item);
	}
}
