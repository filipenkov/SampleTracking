package com.atlassian.jira.plugins.mail.webwork;


import com.atlassian.gzipfilter.org.apache.commons.lang.exception.ExceptionUtils;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.mail.MailConstants;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.impl.PopMailServerImpl;
import org.apache.commons.lang.StringUtils;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import static com.atlassian.mail.MailConstants.DEFAULT_POP_PROTOCOL;


public class VerifyPopServerConnection extends AddPopMailServer
{
    private final MailLoggingManager mailLoggingManager;

    public VerifyPopServerConnection(MailLoggingManager mailLoggingManager)
    {
        this.mailLoggingManager = mailLoggingManager;
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
        verifyTimeout = getTimeout() == null || getTimeout() <= 0 ? MailConstants.DEFAULT_TIMEOUT : getTimeout();
        MailProtocol mailProtocol = StringUtils.isNotBlank(protocol) ? MailProtocol.getMailProtocol(protocol.trim()) : DEFAULT_POP_PROTOCOL;
        port = StringUtils.isNotBlank(port) ? port.trim() : mailProtocol.getDefaultPort();
        final MailServer mailServer = new PopMailServerImpl(null, getName(), getDescription(), mailProtocol, getServerName(), port, getUsername(), getPassword(), verifyTimeout);
        configureSocks(mailServer);
        mailLoggingManager.configureLogging(mailServer);
        validateServer(mailServer, new VerifyPopServer());
        return SUCCESS;
    }

    public String doAdd() throws Exception
    {
        doValidation();
        if (!hasAnyErrors())
        {
            doExecute();
        }
        return "add";
    }

    public String doUpdate() throws Exception
    {
        doValidation();
        if (!hasAnyErrors())
        {
            doExecute();
        }
        return "update";
    }

    static class VerifyPopServer extends VerifyMailServer
    {
        public void verifyMailServer(final MailServer server)
        {
            Store store = null;
            try
            {
                addTimeouts(server, VerifyPopServerConnection.verifyTimeout);
                final Session session = server.getSession();
                store = session.getStore(server.getMailProtocol().getProtocol());
                store.connect(server.getHostname(), Integer.parseInt(server.getPort()), server.getUsername(), server.getPassword());
            }
            catch (Exception e)
            {
                Throwable rootCause = e.getCause() != null ? ExceptionUtils.getRootCause(e) : e;
                String faultMessage = String.format("%s: %s", rootCause.getClass().getSimpleName(), rootCause.getMessage());
                log.error(String.format("Unable to connect to the server at %s due to the following exception: %s",server.getHostname(), rootCause.toString()));
                errors.add(faultMessage);
            } finally {
                if (store != null)
                {
                    try
                    {
                        store.close();
                    }
                    catch (MessagingException ignore)
                    {

                    }
                }
            }
        }

    }


}
