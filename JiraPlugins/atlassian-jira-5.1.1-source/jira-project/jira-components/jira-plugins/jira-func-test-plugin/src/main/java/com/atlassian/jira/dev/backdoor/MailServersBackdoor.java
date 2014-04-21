package com.atlassian.jira.dev.backdoor;

import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.PopMailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.PopMailServerImpl;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Use this backdoor to manipulate Mail Servers as part of setup for tests.
 *
 * This class should only be called by the {@link com.atlassian.jira.functest.framework.backdoor.MailServersControl}.
 *
 * @since v5.0
 */
@Path ("mailServers")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class MailServersBackdoor
{
    @POST
    @Path("smtp")
    public Response addSmtpServer(MailServersBean mailServerBean) throws MailException
    {
        // 1. Premptive strike against failing tests - fall over if the SMTP server won't be able to send anything.
        if (MailFactory.isSendingDisabled())
        {
            throw new IllegalStateException("Mail sending is disabled. Please restart your server without"
                    + " -Datlassian.mail.senddisabled=true.");
        }

        // 2. Delete any existing SMTP servers
        MailServerManager mailServerManager = MailFactory.getServerManager();
        List<SMTPMailServer> smtpMailServers = mailServerManager.getSmtpMailServers();
        for (SMTPMailServer smtpMailServer : smtpMailServers)
        {
            mailServerManager.delete(smtpMailServer.getId());
        }

        // 3. Add the new server
        MailServer mailServer = new SMTPMailServerImpl(null, mailServerBean.name, mailServerBean.description,
                mailServerBean.from, mailServerBean.prefix, false, MailProtocol.SMTP, mailServerBean.serverName,
                mailServerBean.port, false, "", "", 10000L);
        mailServerManager.create(mailServer);

        return Response.ok(null).build();
    }

    @POST
    @Path("pop")
    public Response addPopServer(MailServersBean mailServerBean) throws MailException
    {
        PopMailServer mailServer = new PopMailServerImpl(null, mailServerBean.name, mailServerBean.description,
                mailServerBean.serverName, mailServerBean.username, mailServerBean.password);
        if (StringUtils.isNotBlank(mailServerBean.port))
        {
            mailServer.setPort(mailServerBean.port);
        }
        if (StringUtils.isNotBlank(mailServerBean.protocol))
        {
            mailServer.setMailProtocol(MailProtocol.getMailProtocol(mailServerBean.protocol));
        }

        MailServerManager mailServerManager = MailFactory.getServerManager();
        mailServerManager.create(mailServer);

        return Response.ok(null).build();
    }
}
