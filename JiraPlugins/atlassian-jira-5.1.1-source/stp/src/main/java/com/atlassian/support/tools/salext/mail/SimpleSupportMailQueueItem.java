package com.atlassian.support.tools.salext.mail;

import com.atlassian.mail.MailException;

public class SimpleSupportMailQueueItem extends AbstractSupportMailQueueItem {
	private final ProductAwareEmail email;
	
	public SimpleSupportMailQueueItem(ProductAwareEmail email) {
		super();
		this.email = email;
	}

	@Override
	public void send() throws MailException {
		send(email);
	}

	@Override
	public String getSubject() {
		return email.getSubject();
	}

}
