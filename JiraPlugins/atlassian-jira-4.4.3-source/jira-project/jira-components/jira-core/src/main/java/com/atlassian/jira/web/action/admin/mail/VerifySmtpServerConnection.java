/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.mail;

import alt.javax.mail.Session;
import alt.javax.mail.Transport;
import com.atlassian.gzipfilter.org.apache.commons.lang.exception.ExceptionUtils;
import com.atlassian.mail.MailConstants;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import webwork.action.Action;

import javax.mail.MessagingException;
import java.util.Properties;

import static com.atlassian.mail.MailConstants.DEFAULT_SMTP_PROTOCOL;


public class VerifySmtpServerConnection extends VerifyMailServerConnection
{


    @Override
    protected String doExecute() throws Exception
    {
        MailServer mailServer = null;
        String port = getPort();
        String protocol = getProtocol();
        MailProtocol mailProtocol;
        if (getTimeout() == null || getTimeout() <=0)
        {
            verifyTimeout = MailConstants.DEFAULT_TIMEOUT;
        }
        else
        {
            verifyTimeout = getTimeout();
        }
        if (StringUtils.isNotBlank(protocol))
        {
            mailProtocol = MailProtocol.getMailProtocol(protocol.trim());
        }
        else
        {
            mailProtocol = DEFAULT_SMTP_PROTOCOL;
        }
        if (StringUtils.isNotBlank(port))
        {
            port = port.trim();
        }
        else
        {
            port = mailProtocol.getDefaultPort();
        }
        if (TextUtils.stringSet(getJndiLocation()))       
        {
            mailServer = new SMTPMailServerImpl(null, getName(), getDescription(), getFrom(), getPrefix(), true, mailProtocol, getJndiLocation(), port, isTlsRequired(), getUsername(), getPassword(), verifyTimeout);
        }
        else
        {
            mailServer = new SMTPMailServerImpl(null, getName(), getDescription(), getFrom(), getPrefix(), false, mailProtocol, getServerName(), port, isTlsRequired(), getUsername(), getPassword(), verifyTimeout);
        }
        validateServer(mailServer, new VerifySmptServer());
        return Action.SUCCESS;
    }

    public String doAdd() throws Exception
    {
        doVerification();
        return "add";
    }

    private void doVerification() throws Exception
    {
        doValidation();
        if (!hasAnyErrors())
        {
            doExecute();
        }
    }

    public String doUpdate() throws Exception
    {
        doVerification();
        return "update";
    }

    public String doSetup() throws Exception
    {
        doVerification();
        return "setup";
    }

    public boolean isAnonymous()
    {
        return StringUtils.isBlank(getUsername()) && StringUtils.isBlank(getJndiLocation());
    }



    static class VerifySmptServer extends VerifySmtpServerConnection.VerifyMailServer
    {

        public void verifyMailServer(final MailServer server)
        {
            Transport transport = null;
            try
            {
                Session session = server.getSession();
                if (session != null)
                {
                    addTimeouts(session, server);
                    SMTPMailServer smtpServer = (SMTPMailServer) server;
                    transport = session.getTransport();
                    if (smtpServer.isSessionServer())
                    {
                        transport.connect();
                        if (!transport.isConnected())
                        {
                            errors.add(getI18nHelper().getText("admin.mailservers.mail.bad.jndisession"));
                            log.error(String.format("Unable to connect to %s specified in JNDI", smtpServer.getJndiLocation()));
                        }

                    }
                    else
                    {
                        transport.connect(server.getHostname(), Integer.parseInt(server.getPort()), server.getUsername(), server.getPassword());
                        if (!transport.isConnected())
                        {
                            errors.add(getI18nHelper().getText("admin.mailservers.mail.bad.authentication"));
                            log.error(String.format("Unable to authenticate to %s", smtpServer.getHostname()));
                        }
                    }
                }
                else
                {
                    errors.add(getI18nHelper().getText("admin.mailservers.mail.bad.session"));
                    log.error("Unable to retrieve a seesion from the SMTP mail server");
                }
            }
            catch (Exception e)
            {
                Throwable t = ExceptionUtils.getRootCause(e) != null ? ExceptionUtils.getRootCause(e) : e;
                errors.add(String.format("%s: %s", t.getClass().getSimpleName(),t.getMessage()));
                log.error(String.format("Unable to connect to the server at %s due to the following exception: %s",server.getHostname(), t.toString()));
            }
            finally
            {
                if (transport != null)
                {
                    try
                    {
                        transport.close();
                    }
                    catch (MessagingException ignore)
                    {
                        log.error("Exception when closing transport "+ ignore.getMessage());
                    }
                }
            }
        }

        private void addTimeouts(Session session, MailServer server)
        {
            Properties p = session.getProperties();
            String protocol = p.getProperty("mail.transport.protocol");
            String connectionTimeout=String.format("mail.%s.connectiontimeout", protocol);
            String socketTimeout = String.format("mail.%s.timeout", protocol);
            p.setProperty(connectionTimeout, ""+verifyTimeout);
            p.setProperty(socketTimeout, ""+verifyTimeout);
        }
    }
}
