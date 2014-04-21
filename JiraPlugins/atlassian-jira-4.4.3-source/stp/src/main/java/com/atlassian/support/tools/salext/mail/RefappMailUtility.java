package com.atlassian.support.tools.salext.mail;


import com.atlassian.support.tools.salext.SupportApplicationInfo;

public class RefappMailUtility implements MailUtility
{
	@Override
	public boolean isMailServerConfigured()
	{
		return false;
	}

	@Override
	public void sendMail(SupportRequest requestInfo, SupportApplicationInfo info)
	{
	}

}
