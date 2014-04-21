package com.atlassian.support.tools.salext.mail;


import com.atlassian.support.tools.salext.SupportApplicationInfo;

public interface MailUtility
{
	/* Methods required for support emails */
	public boolean isMailServerConfigured();
	public void sendSupportRequestMail(SupportRequest requestInfo, SupportApplicationInfo info) throws Exception;

	/* Methods required for internal notification emails (scheduled jobs, etc.)*/
	public void sendMail(ProductAwareEmail email);
}
