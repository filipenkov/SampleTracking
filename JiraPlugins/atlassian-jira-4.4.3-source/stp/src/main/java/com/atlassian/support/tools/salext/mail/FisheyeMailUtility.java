package com.atlassian.support.tools.salext.mail;

import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.atlassian.mail.MailException;
import com.cenqua.fisheye.AppConfig;
import com.cenqua.fisheye.mail.MailMessage;
import com.cenqua.fisheye.mail.Mailer;


public class FisheyeMailUtility extends AbstractMailUtility
{
	private static final Logger log = Logger.getLogger(FisheyeMailUtility.class);
	@Override
	public boolean isMailServerConfigured()
	{
		return AppConfig.getsConfig().getConfig().getSmtp() != null;
	}

	@Override
	protected void queueEmail(SupportRequest requestInfo) throws MailException, AddressException, MessagingException
	{
        AppConfig.getsConfig().getMailer().reload(AppConfig.getsConfig().getConfig().getSmtp());

        // inside the OSGi container running in JRE 1.6 there are two activation.jar files.
        // The one shipped with the JRE takes precedence but clashes with the version of the mail.jar we ship
        // to correct this - we need to run the mail sending with the class loader of the product itself in the Thread Context
        Thread.currentThread().setContextClassLoader(AppConfig.getsConfig().getClass().getClassLoader());
        
        if(!isMailServerConfigured())
        {
            Properties props = new Properties();
            props.setProperty("mail.smtp.host", "mail.atlassian.com");
            props.put("mail.smtp.starttls.enable", String.valueOf(true));
            Session session = Session.getInstance(props);
            
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(requestInfo.getFromAddress()));
            msg.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(requestInfo.getToAddress(), false));
            msg.setSubject(requestInfo.getSubject());
            for(Entry<String, String> entry: requestInfo.getHeaders())
            {
            	msg.addHeader(entry.getKey(), entry.getValue());
            }

            Multipart multiPart = SupportMailQueueItem.toMultiPart(requestInfo);
            msg.setContent(multiPart);
            
            Transport.send(msg);
        }
        else 
        {
        	Mailer mailer = AppConfig.getsConfig().getMailer();
            MailMessage msg = new MailMessage();
            msg.setFrom(requestInfo.getFromAddress());
            msg.addRecipient(requestInfo.getToAddress());
            msg.setSubject(requestInfo.getSubject());

            Multipart multiPart = SupportMailQueueItem.toMultiPart(requestInfo);
    		msg.setMultiPartContent(multiPart);

            for(Entry<String, String> entry: requestInfo.getHeaders())
            {
            	msg.addHeader(entry.getKey(), entry.getValue());
            }
            
        	if(mailer.sendMessage(msg)) 
        	{
        		log.info("Sent support Request to " + requestInfo.getToAddress() + " successfully.");
        	}
        	else 
        	{
        		log.error("Unable to send email");
        	}
        } 
	}

}
