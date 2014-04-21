/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.jira.component.ComponentAccessor;
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

import java.util.Map;

@WebSudoRequired
public abstract class AddMailServer extends MailServerActionSupport
{
    private String provider;

    public AddMailServer()
    {
        WebResourceManager webResourceManager = ComponentAccessor.getWebResourceManager();
        webResourceManager.requireResource("com.atlassian.jira.jira-mail-plugin:verifymailserverconnection");
        webResourceManager.requireResource("com.atlassian.jira.jira-mail-plugin:mail-servers");
    }

    public String doDefault() throws Exception
    {
        setTimeout(MailConstants.DEFAULT_TIMEOUT);
        return Action.INPUT;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
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

        configureSocks(mailServer);
        MailFactory.getServerManager().create(mailServer);
        return getRedirect(getCancelURI());
    }

    private boolean canAddSmtpMailServer()
    {
        return canManageSmtpMailServers();
    }

    private boolean canAddPopMailServer()
    {
        return canManagePopMailServers();
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

    public abstract Map<String, String> getSupportedServiceProviders();

    public abstract String getCancelURI();
}
