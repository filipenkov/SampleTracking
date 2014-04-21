package com.atlassian.support.tools.salext.mail;


import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;

import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.support.tools.salext.SupportApplicationInfo;

public class BambooMailUtility extends AbstractMailUtility
{
	private static final Logger log = Logger.getLogger(BambooMailUtility.class);
	
	@Override
	public boolean isMailServerConfigured()
	{
        final MailServerManager serverManager = MailFactory.getServerManager();
        if(serverManager == null)
        	return false;
        
		try
		{
			SMTPMailServer mailServer = serverManager.getDefaultSMTPMailServer();
			if(mailServer == null)
				return false;
			else
				return true;
		}
		catch(MailException e)
		{
			log.warn("Failed to obtain a mail server.", e);
			return false;
		}
	}
	
	@Override
	protected void queueSupportRequestEmail(SupportRequest requestInfo, SupportApplicationInfo info) throws MailException, AddressException, MessagingException
	{
		// inside the OSGi container running in JRE 1.6 there are two
		// activation.jar files.
		// The one shipped with the JRE takes precedence but clashes with the
		// version of the mail.jar we ship
		// to correct this - we need to run the mail sending with the class
		// loader of the product itself in the Thread Context
		final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		
		try {
			if(MailFactory.getServerManager() != null)
			{
				Thread.currentThread().setContextClassLoader(MailFactory.getServerManager().getClass().getClassLoader());
			}
			
			SupportRequestMailQueueItem item = new SupportRequestMailQueueItem(requestInfo, info);
			item.send();
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}

	
	}

	@Override
	public void sendMail(ProductAwareEmail email) {
		final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		
		try {
			if(MailFactory.getServerManager() != null)
			{
				Thread.currentThread().setContextClassLoader(MailFactory.getServerManager().getClass().getClassLoader());
			}
			
			SimpleSupportMailQueueItem item = new SimpleSupportMailQueueItem(email);
			item.send();
		} catch (MailException e) {
			log.error("Error sending mail using Atlassian mail servers:",e);
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}
}
