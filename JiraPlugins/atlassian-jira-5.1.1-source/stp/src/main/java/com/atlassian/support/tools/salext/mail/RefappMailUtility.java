package com.atlassian.support.tools.salext.mail;


import com.atlassian.mail.Email;
import com.atlassian.support.tools.salext.SupportApplicationInfo;

public class RefappMailUtility implements MailUtility
{
	@Override
	public boolean isMailServerConfigured()
	{
		return false;
	}

	@Override
	public void sendSupportRequestMail(SupportRequest requestInfo, SupportApplicationInfo info)
	{
	}

	@Override
	public void sendMail(ProductAwareEmail email) {
	}
}
