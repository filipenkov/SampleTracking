package com.atlassian.support.tools.salext.mail;


import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import com.atlassian.core.task.MultiQueueTaskManager;
import com.atlassian.mail.MailException;
import com.atlassian.mail.queue.MailQueueItem;

public class ConfluenceMailUtility extends AbstractMailUtility
{
	private MultiQueueTaskManager taskManager;

	public ConfluenceMailUtility(MultiQueueTaskManager taskManager)
	{
		super();
		this.taskManager = taskManager;
	}

	@Override
	protected void queueEmail(SupportRequest requestInfo) throws MailException, AddressException, MessagingException
	{
		MailQueueItem item = new SupportMailQueueItem(requestInfo);
		this.taskManager.addTask("mail", item);
	}
}
