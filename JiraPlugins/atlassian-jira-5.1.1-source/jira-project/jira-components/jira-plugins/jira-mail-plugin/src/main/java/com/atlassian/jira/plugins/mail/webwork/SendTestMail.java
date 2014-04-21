/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.mail.Email;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

@WebSudoRequired
public class SendTestMail extends JiraWebActionSupport
{
    private String message;
    private String to;
    private String subject;
    private String messageType;
    private boolean debug;
    private String log;

    public String doDefault() throws Exception
    {
        String from = null;
        String servername = null;
        String serverdescription = null;
        String username = null;
        String smtpPort = null;
        SMTPMailServer mailserver = MailFactory.getServerManager().getDefaultSMTPMailServer();

        if (mailserver == null)
        {
            log = "You do not currently have a smtp mail server set up yet.";
        }
        else
        {
            from = mailserver.getDefaultFrom();
            servername = mailserver.getName();
            serverdescription = mailserver.getDescription();
            username = mailserver.getUsername();
            smtpPort = mailserver.getPort();
        }

        setMessage("This is a test message from JIRA. \nServer: " + servername + "\nSMTP Port: " + smtpPort + "\nDescription: " + serverdescription + "\nFrom: " + from + "\nHost User Name: " + username);
        setSubject("Test Message From JIRA");
        setMessageType("text");
        setTo(getLoggedInUser().getEmailAddress());

        return INPUT;
    }

    protected void doValidation()
    {
        if (!TextUtils.verifyEmail(to))
        {
            addError("to", getText("admin.errors.must.specify.valid.address"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        SMTPMailServer mailServer = null;
        boolean oldDebug = false;
        PrintStream oldDebugStream = null;
        ByteArrayOutputStream debugLog = new ByteArrayOutputStream();
        try
        {
            mailServer = MailFactory.getServerManager().getDefaultSMTPMailServer();
            if (mailServer == null) {
                log = "You do not currently have a smtp mail server set up yet.";
                return INPUT;
            }
            oldDebug = mailServer.getDebug();
            oldDebugStream = mailServer.getDebugStream();
            mailServer.setDebug(debug);
//            final LogPrintStream logPrintStream = new LogPrintStream(Logger.getLogger("com.atlassian.mail"));
//            final TeePrintStream teePrintStream = new TeePrintStream(debugLog, true, logPrintStream);
            if (debug) { // we don't want to change debug state only if explicitly selected here
                mailServer.setDebug(true);
            }

            if (debug) {
                mailServer.setDebugStream(new PrintStream(debugLog, true));
            }

//            mailServer.setDebugStream(teePrintStream);
//            mailServer.setDebugStream(logPrintStream);

            if (NotificationRecipient.MIMETYPE_HTML.equals(getMessageType()))
            {
                mailServer.send(new Email(to).setSubject(subject).setBody(message).setMimeType("text/html"));
            }
            else
            {
                mailServer.send(new Email(to).setSubject(subject).setBody(message));
            }
        }
        catch (Exception e)
        {
            log = "An error has occurred with sending the test email:\n" + ExceptionUtils.getStackTrace(e) + "\n\n" + debugLog;
            return INPUT;
        }
        finally
        {
            if (mailServer != null)
            {
                mailServer.setDebug(oldDebug);
                mailServer.setDebugStream(oldDebugStream);
            }
        }

        log = "Your test message has been sent successfully to " + to + ".\n\n" + debugLog;
        return INPUT;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getTo()
    {
        return to;
    }

    public void setTo(String to)
    {
        this.to = to;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public String getLog()
    {
        return log;
    }

    public String getMessageType()
    {
        return messageType;
    }

    public void setMessageType(String messageType)
    {
        this.messageType = messageType;
    }

    public boolean getDebug()
    {
        return debug;
    }

    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    public Map getMimeTypes()
    {
        return EasyMap.build(NotificationRecipient.MIMETYPE_HTML, NotificationRecipient.MIMETYPE_HTML_DISPLAY, NotificationRecipient.MIMETYPE_TEXT, NotificationRecipient.MIMETYPE_TEXT_DISPLAY);
    }

    public String getActiveTab()
    {
        return ViewMailServers.OUTGOING_MAIL_TAB;
    }

    public String getCancelURI()
    {
        return ViewMailServers.OUTGOING_MAIL_ACTION;
    }
}
