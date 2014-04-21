package com.atlassian.mail.server.impl.util;

import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailUtils;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang.StringUtils;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Calendar;
import java.util.Iterator;

public class MessageCreator
{
    public void updateMimeMessage(Email email, String defaultFrom, String prefix, MimeMessage message) throws MailException, MessagingException, UnsupportedEncodingException
    {
        String from = StringUtils.trim(email.getFrom());
        String fromName = email.getFromName();
        String to = email.getTo();
        String cc = email.getCc();
        String bcc = email.getBcc();
        String replyTo = email.getReplyTo();
        String inReplyTo = email.getInReplyTo();
        String subject = email.getSubject();
        String body = email.getBody();
        String mimeType = email.getMimeType();
        String encoding = email.getEncoding();
        Map headers = email.getHeaders();
        Multipart multipart = email.getMultipart();

        if (StringUtils.isBlank(StringUtils.trim(to)) && StringUtils.isBlank(StringUtils.trim(cc)) && StringUtils.isBlank(StringUtils.trim(bcc)))
        {
            throw new MailException("Tried to send mail (" + subject + ") with no recipients.");
        }

        message.setSentDate(Calendar.getInstance().getTime());
        if (to != null)
            message.setRecipients(javax.mail.Message.RecipientType.TO, MailUtils.parseAddresses(to));

        if (cc != null)
            message.setRecipients(javax.mail.Message.RecipientType.CC, MailUtils.parseAddresses(cc));

        if (bcc != null)
            message.setRecipients(javax.mail.Message.RecipientType.BCC, MailUtils.parseAddresses(bcc));

        if (replyTo != null)
            message.setReplyTo(MailUtils.parseAddresses(replyTo));

        if (inReplyTo != null)
            message.setHeader("In-Reply-To", inReplyTo);

        if (StringUtils.isNotBlank(from))
        {
            // Checks if the email address has a personal name attached to it
            InternetAddress internetAddress = new InternetAddress(from);
            if (StringUtils.isNotBlank(fromName) && internetAddress.getPersonal() == null)
            {
                if(encoding != null)
                {
                    internetAddress.setPersonal(fromName, encoding);
                }
                else
                {
                    internetAddress.setPersonal(fromName);
                }
            }
            message.setFrom(internetAddress);
        }
        else if (StringUtils.isNotBlank(defaultFrom))
        {
            // Checks if the email address has a personal name attached to it            
            InternetAddress internetAddress = new InternetAddress(defaultFrom);
            if (StringUtils.isNotBlank(fromName) && internetAddress.getPersonal() == null)
            {
                if(encoding != null)
                {
                    internetAddress.setPersonal(fromName, encoding);
                }
                else
                {
                    internetAddress.setPersonal(fromName);
                }
            }
            message.setFrom(internetAddress);
        }
        else
        {
            throw new MailException("Tried to send mail (" + subject + ") from no one (no 'from' and 'default from' specified).");
        }

        String fullSubject = subject;
        if (StringUtils.isNotBlank(prefix))
            fullSubject = prefix + " " + fullSubject;

        if (encoding != null)
            message.setSubject(fullSubject, encoding);
        else
            message.setSubject(fullSubject);

        String mimeTypeAndEncoding = mimeType;

        if (encoding != null)
            mimeTypeAndEncoding += "; charset=" + encoding + "";

        if (multipart != null)
        {
            // Create a MimeBodyPart for the body
            if (StringUtils.isNotBlank(body))
            {
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(body, mimeTypeAndEncoding);
                messageBodyPart.setDisposition(Part.INLINE);
                // add to multipart passed in
                multipart.addBodyPart(messageBodyPart, 0);
            }

            message.setContent(multipart);
        }
        else
        {
            message.setContent(body, mimeTypeAndEncoding);
        }

        // Add the custom headers (if any)
        if (headers != null)
        {
            for (Iterator iterator = headers.entrySet().iterator(); iterator.hasNext();)
            {
                Map.Entry entry = (Map.Entry) iterator.next();
                message.addHeader((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

}
