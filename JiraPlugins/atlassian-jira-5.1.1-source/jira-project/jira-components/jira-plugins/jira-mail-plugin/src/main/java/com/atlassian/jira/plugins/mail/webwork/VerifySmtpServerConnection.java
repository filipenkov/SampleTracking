/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.gzipfilter.org.apache.commons.lang.exception.ExceptionUtils;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.mail.MailConstants;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import java.util.Map;

import static com.atlassian.mail.MailConstants.DEFAULT_SMTP_PROTOCOL;

public class VerifySmtpServerConnection extends MailServerActionSupport
{
    private String provider;
    private final MailLoggingManager mailLoggingManager;

    public VerifySmtpServerConnection( MailLoggingManager mailLoggingManager, WebResourceManager webResourceManager)
    {
        this.mailLoggingManager = mailLoggingManager;
        webResourceManager.requireResource("com.atlassian.jira.jira-mail-plugin:verifymailserverconnection");
        webResourceManager.requireResource("com.atlassian.jira.jira-mail-plugin:mail-servers");
    }

    protected static long verifyTimeout;

    public String doDefault() throws Exception
    {
        return INPUT;
    }

    protected void validateServer(final MailServer mailServer, VerifyMailServer verifier)
    {
        verifier.verifyMailServer(mailServer);
        if (verifier.hasErrors())
        {
            setErrorMessages(verifier.getErrorMessages());
        }
    }

    @Override
    protected String doExecute() throws Exception
    {
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
        final MailServer mailServer;
        if (TextUtils.stringSet(getJndiLocation()))
        {
            mailServer = new SMTPMailServerImpl(null, getName(), getDescription(), getFrom(), getPrefix(), true, mailProtocol, getJndiLocation(), port, isTlsRequired(), getUsername(), getPassword(), verifyTimeout);
        }
        else
        {
            mailServer = new SMTPMailServerImpl(null, getName(), getDescription(), getFrom(), getPrefix(), false, mailProtocol, getServerName(), port, isTlsRequired(), getUsername(), getPassword(), verifyTimeout);
        }
        configureSocks(mailServer);
        mailLoggingManager.configureLogging(mailServer);
        validateServer(mailServer, new VerifySmptServer());
        return SUCCESS;
    }

    public String doAdd() throws Exception
    {
        // add always provides the password
        setChangePassword(true);
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
        // setup always provides the password
        setChangePassword(true);
        doVerification();
        return "setup";
    }

    public boolean isAnonymous()
    {
        return StringUtils.isBlank(getUsername()) && StringUtils.isBlank(getJndiLocation());
    }

    @SuppressWarnings("unused")
    public String getServiceProvider()
    {
        return provider;
    }

    @SuppressWarnings("unused")
    public void setServiceProvider(final String provider)
    {
        this.provider = provider;
    }

    static class VerifySmptServer extends VerifyMailServer
    {

        public void verifyMailServer(final MailServer server)
        {
            Transport transport = null;
            try
            {
                addTimeouts(server, VerifySmtpServerConnection.verifyTimeout);
                Session session = server.getSession();
                if (session != null)
                {
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
    }

    public Map<String, String> getSupportedServiceProviders()
    {
        return AddSmtpMailServer.getSupportedServiceProviders(this);
    }

    @Override
    public String getActiveTab()
    {
        return ViewMailServers.OUTGOING_MAIL_TAB;
    }

    public String getCancelURI()
    {
        return ViewMailServers.OUTGOING_MAIL_ACTION;
    }
}
