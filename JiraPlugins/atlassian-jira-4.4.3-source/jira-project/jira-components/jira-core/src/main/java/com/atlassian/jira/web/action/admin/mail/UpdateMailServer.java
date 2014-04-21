package com.atlassian.jira.web.action.admin.mail;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.mail.MailConstants;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import static com.atlassian.mail.MailConstants.DEFAULT_SMTP_PORT;

@WebSudoRequired
public class UpdateMailServer extends MailServerActionSupport
{
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:verifymailserverconnection");
        if (id == null || id <= 0)
        {
            addErrorMessage(getText("admin.errors.mail.specify.server.to.update"));
            return ERROR;
        }

        MailServer mailServer = MailFactory.getServerManager().getMailServer(id);
        if (mailServer == null)
        {
            addErrorMessage(getText("admin.errors.mail.error.occured.retrieving"));
            return ERROR;
        }
        else
        {
            if ((isPop(mailServer) && canEditPopMailServer()) || (isSmtp(mailServer) && canEditSmtpMailServer()))
            {
                String port;
                if (StringUtils.isBlank(getPort()))
                {
                    port = DEFAULT_SMTP_PORT;
                }
                else
                {
                    port = getPort();
                }
                if (getTimeout() == null)
                {
                    setTimeout(MailConstants.DEFAULT_TIMEOUT);
                }
                mailServer.setDescription(getDescription());
                mailServer.setName(getName());
                mailServer.setHostname(getServerName());
                mailServer.setUsername(getUsername());
                mailServer.setPassword(getPassword());
                mailServer.setPort(port);
                mailServer.setMailProtocol(MailProtocol.getMailProtocol(getProtocol()));
                mailServer.setTimeout(getTimeout());

                if (isSmtp(mailServer))
                {
                    SMTPMailServer smtp = (SMTPMailServer) mailServer;
                    smtp.setDefaultFrom(getFrom());
                    smtp.setPrefix(getPrefix());
                    smtp.setTlsRequired(isTlsRequired());
                    if (TextUtils.stringSet(getJndiLocation()))
                    {
                        smtp.setJndiLocation(getJndiLocation());
                        smtp.setSessionServer(true);
                    }
                    else
                    {
                        smtp.setJndiLocation(null);
                        smtp.setSessionServer(false);
                    }
                }
                MailFactory.getServerManager().update(mailServer);
                return getRedirect("ViewMailServers.jspa");
            }
            else
            {
                return "securitybreach";
            }
        }
    }

    private boolean canEditSmtpMailServer()
    {
        return canManageSmtpMailServers();
    }

    private boolean canEditPopMailServer()
    {
        return canManagePopMailServers();
    }
}
