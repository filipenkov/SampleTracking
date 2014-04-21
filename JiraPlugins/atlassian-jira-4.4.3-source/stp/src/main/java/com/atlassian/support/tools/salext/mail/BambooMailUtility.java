package com.atlassian.support.tools.salext.mail;


import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.Logger;

import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;

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
			log.error("Failed to obtain a mail server.", e);
			return false;
		}
	}
	
	@Override
	protected void queueEmail(SupportRequest requestInfo) throws MailException, AddressException, MessagingException
	{
        if(MailFactory.getServerManager() != null)
        {
            // inside the OSGi container running in JRE 1.6 there are two activation.jar files.
            // The one shipped with the JRE takes precedence but clashes with the version of the mail.jar we ship
            // to correct this - we need to run the mail sending with the class loader of the product itself in the Thread Context
        	Thread.currentThread().setContextClassLoader(MailFactory.getServerManager().getClass().getClassLoader());
        }
        
        SupportMailQueueItem item = new SupportMailQueueItem(requestInfo);
		item.send();
    }
}
