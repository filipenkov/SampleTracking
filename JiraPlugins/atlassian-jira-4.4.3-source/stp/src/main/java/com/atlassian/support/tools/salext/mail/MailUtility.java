package com.atlassian.support.tools.salext.mail;


import com.atlassian.support.tools.salext.SupportApplicationInfo;

public interface MailUtility
{
	public boolean isMailServerConfigured();
	public void sendMail(SupportRequest requestInfo, SupportApplicationInfo info) throws Exception;
}
