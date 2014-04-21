/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.mail;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.mail.MailConstants;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.impl.PopMailServerImpl;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.lang.StringUtils;
import webwork.action.Action;

@WebSudoRequired
public class AddMailServer extends MailServerActionSupport
{
    final static String JNDI_TIMEOUT="60000";

    public String doDefault() throws Exception
    {
        includeResources();
        setTimeout(MailConstants.DEFAULT_TIMEOUT);
        return Action.INPUT;
    }

    private void includeResources()
    {
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:verifymailserverconnection");
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        includeResources();
        final MailServer mailServer;
        final MailProtocol protocol = MailProtocol.getMailProtocol(getProtocol());
        final String port;

        if (getTimeout() == null)
        {
            setTimeout(MailConstants.DEFAULT_TIMEOUT);
        }
        if (StringUtils.isNotBlank(getPort()))
        {
            port = getPort();
        }
        else
        {
            port = protocol.getDefaultPort();
        }

        // Check to see which type of mail server should be created
        if (getTypes()[0].equals(getType()))
        {
            if (canAddPopMailServer())
            {
                mailServer = new PopMailServerImpl(null, getName(), getDescription(), protocol, getServerName(), port,
                        getUsername(), getPassword(), getTimeout());
            }
            else
            {
                return "securitybreach";
            }
        }
        else if (getTypes()[1].equals(getType()))
        {
            if (canAddSmtpMailServer())
            {
                if (StringUtils.isNotBlank(getJndiLocation()))
                {
                    mailServer = new SMTPMailServerImpl(null, getName(), getDescription(), getFrom(), getPrefix(), true,
                            protocol, getJndiLocation(), port, isTlsRequired(), getUsername(), getPassword(), getTimeout());
                }
                else
                {
                    mailServer = new SMTPMailServerImpl(null, getName(), getDescription(), getFrom(), getPrefix(), false,
                            protocol, getServerName(), port, isTlsRequired(), getUsername(), getPassword(), getTimeout());
                }
            }
            else
            {
                return "securitybreach";
            }
        }
        else
        {
            return ERROR;
        }
        MailFactory.getServerManager().create(mailServer);
        return getRedirect("ViewMailServers.jspa");
    }

    private boolean canAddSmtpMailServer()
    {
        return canManageSmtpMailServers();
    }

    private boolean canAddPopMailServer()
    {
        return canManagePopMailServers();
    }
}
