package com.atlassian.jira.web.action.admin.mail;


import com.atlassian.gzipfilter.org.apache.commons.lang.exception.ExceptionUtils;
import com.atlassian.mail.MailConstants;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.impl.PopMailServerImpl;
import org.apache.commons.lang.StringUtils;
import webwork.action.Action;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

import static com.atlassian.mail.MailConstants.DEFAULT_POP_PROTOCOL;


public class VerifyPopServerConnection extends VerifyMailServerConnection
{
    @Override
    protected String doExecute() throws Exception
    {
        MailServer mailServer = null;
        String port = getPort();
        String protocol = getProtocol();
        verifyTimeout = getTimeout() == null || getTimeout() <=0 ? MailConstants.DEFAULT_TIMEOUT : getTimeout();
        MailProtocol mailProtocol = StringUtils.isNotBlank(protocol) ? MailProtocol.getMailProtocol(protocol.trim()) : DEFAULT_POP_PROTOCOL;
        port =StringUtils.isNotBlank(port) ? port.trim() :  mailProtocol.getDefaultPort();
        mailServer = new PopMailServerImpl(null, getName(), getDescription(), mailProtocol, getServerName(), port, getUsername(), getPassword(), verifyTimeout);
        validateServer(mailServer, new VerifyPopServer());
        return Action.SUCCESS;
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

    static class VerifyPopServer extends VerifyMailServerConnection.VerifyMailServer
    {
        public void verifyMailServer(final MailServer server)
        {
            Store store = null;
            try
            {
                Properties props = getServerProperties(server);
                Session session = javax.mail.Session.getInstance(props, null);
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
