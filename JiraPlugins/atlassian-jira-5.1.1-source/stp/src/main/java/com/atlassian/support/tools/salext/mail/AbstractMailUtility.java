package com.atlassian.support.tools.salext.mail;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;

import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.support.tools.salext.SupportApplicationInfo;

public abstract class AbstractMailUtility implements MailUtility
{
	private static final Logger log = Logger.getLogger(AbstractMailUtility.class);
	private static final String PRIORITY_HEADER = "X-Support-Request-Priority";
	private static final String SUPPORT_REQUEST_PROPERTIES_SUFFIX = "-support-request.properties";
	private static final String REQUEST_HEADER_SUFFIX = "-Support-Request-Version";
	protected static final String SEPARATOR_CONSTANT = "_";
	
	@Override
	public void sendSupportRequestMail(SupportRequest supportRequest, SupportApplicationInfo info) throws AddressException, MessagingException, MailException, IOException
	{
		String propertiesFileName = info.getApplicationName() + SUPPORT_REQUEST_PROPERTIES_SUFFIX;
		String requestVersionHeader = "X-" + info.getApplicationName() + REQUEST_HEADER_SUFFIX;
		String requestVersionNumber = "4.0";
		supportRequest.addHeader(PRIORITY_HEADER, String.valueOf(supportRequest.getPriority()));
		supportRequest.addHeader(requestVersionHeader, requestVersionNumber);
		supportRequest.addAttachment(new SupportRequestAttachment(propertiesFileName, "text/xml", info.saveProperties()));
		supportRequest.addAttachment(new SupportRequestAttachment("support-request-details.properties", "text/plain", supportRequest.saveForMail(info)));
		queueSupportRequestEmail(supportRequest, info);
	}

	@Override
	public boolean isMailServerConfigured()
	{
		try
		{
			return MailFactory.getServerManager().getDefaultSMTPMailServer() != null;
		}
		catch(MailException mex)
		{
			// never actually thrown by current MailServerManager impl
			log.warn("Could not successfully check for a default mail server.", mex);
			return false;
		}
	}

	protected abstract void queueSupportRequestEmail(SupportRequest requestInfo, SupportApplicationInfo info) throws MailException, AddressException, MessagingException, IOException;
}
