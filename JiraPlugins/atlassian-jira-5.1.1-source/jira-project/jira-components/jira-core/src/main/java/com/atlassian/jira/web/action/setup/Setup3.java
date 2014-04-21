/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.setup;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.PortUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.mail.MailConstants;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Collection;
import java.util.Map;

/**
 * This setup step is used to setup the MailListener
 */
public class Setup3 extends AbstractSetupAction
{
    private static final long TIMEOUT = 10000L;
    String jndiLocation;
    String serverName;
    String username;
    String password;
    String port;
    String from;
    boolean noemail;
    String name;
    String desc;
    String prefix;
    boolean tlsRequired;
    private long timeout;
    private String protocol;
    private final UserUtil userUtil;

    private String mailservertype;
    private String provider;

    public Setup3(UserUtil userUtil, FileFactory fileFactory)
    {
        super(fileFactory);
        this.userUtil = userUtil;
    }

    private void includeResources()
    {
        WebResourceManager webResourceManager = ComponentAccessor.getWebResourceManager();
        webResourceManager.requireResource("com.atlassian.jira.jira-mail-plugin:verifymailserverconnection");
    }

    public String doDefault() throws Exception
    {
        includeResources();
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        username = null;
        password = null;
        port = "";

        if (name == null)
            name = "Default SMTP Server";

        if (desc == null)
            desc = "This server is used for all outgoing mail.";

        if (from == null)
        {
            Collection admins = userUtil.getAdministrators();
            if (!admins.isEmpty())
            {
                User admin = (User) admins.iterator().next();
                from = admin.getEmailAddress();
            }
        }
        if (timeout == 0)
        {
            timeout = MailConstants.DEFAULT_TIMEOUT;
        }

        noemail = true;
        mailservertype = "smtp";

        return super.doDefault();
    }

    protected String doExecute() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        if (noemail)
        {
            return SUCCESS;
        }
        else
        {
            try
            {

                // create the mail server object
                final MailProtocol protocol = MailProtocol.getMailProtocol(getProtocol());
                String port = null;
                if (StringUtils.isNotBlank(getPort()))
                {
                    port = getPort();
                }
                else
                {
                    port = protocol.getDefaultPort();
                }
                if (StringUtils.isBlank("" + getTimeout()))
                {
                    timeout = TIMEOUT;
                }

                MailServer ms;
                if (isJndi())
                {
                    ms = new SMTPMailServerImpl(null, name, desc, from, prefix, true, protocol, jndiLocation, port, tlsRequired, username, password, timeout);
                }
                else
                {
                    ms = new SMTPMailServerImpl(null, name, desc, from, prefix, false, protocol, serverName, port, tlsRequired, username, password, timeout);
                }
                MailFactory.getServerManager().create(ms);
                // create the mail listener
                if (ComponentAccessor.getOfBizDelegator().findByAnd("ListenerConfig", EasyMap.build("clazz", "com.atlassian.jira.event.listeners.mail.MailListener")).isEmpty())
                {
                    ComponentAccessor.getListenerManager().createListener("Email Listener", com.atlassian.jira.event.listeners.mail.MailListener.class);
                    
                }
            }
            catch (Exception e)
            {
                log.error("Could not setup MailListener: " + e, e);
                addErrorMessage(getText("admin.errors.setup.could.not.setup.mail.listener", e));
            }
        }
        return getResult();
    }

    protected void doValidation()
    {
        includeResources();
        // return with no error messages, doExecute() will return the already setup view
        if (setupAlready())
        {
            return;
        }

        if (!noemail)
        {
            if (jndiLocation != null && serverName != null)
            {
                addErrorMessage(getText("setup3.error.onlysetup"));
            }
            else if (jndiLocation != null)
            {
                try
                {
                    Context ctx = new InitialContext();
                    Session sessionObject = (Session) ctx.lookup(jndiLocation);
                }
                catch (NamingException e)
                {
                    addError("jndiLocation", getText("setup3.error.notfound"));
                }
                catch (ClassCastException e)
                {
                    addError("jndiLocation", getText("setup3.error.notvalid"));
                }
            }
            else if (serverName != null)
            {
                if (serverName.length() < 3)
                {
                    addError("serverName", getText("setup3.error.tooshort"));
                }

                if (StringUtils.isNotBlank(getPort()) && !PortUtil.isValidPort(getPort()))
                {
                    addError("port", getText("setup3.error.server.invalidport"));
                }
            }
            else
            {
                addErrorMessage(getText("setup3.error.required"));
            }

            if (!TextUtils.verifyEmail(from))
            {
                addError("from", getText("setup3.error.validemail"));
            }

            if (!StringUtils.isNotBlank(name))
            {
                addError("name", getText("setup3.error.servername.required"));
            }
        }
    }

    public String getJndiLocation()
    {
        return jndiLocation;
    }

    public void setJndiLocation(String jndiLocation)
    {
        if (StringUtils.isNotBlank(jndiLocation))
        {
            this.jndiLocation = jndiLocation;
        }
        else
        {
            this.jndiLocation = null;
        }
    }

    public String getServerName()
    {
        return serverName;
    }

    public void setServerName(String serverName)
    {
        if (StringUtils.isNotBlank(serverName))
        {
            this.serverName = serverName;
        }
        else
        {
            this.serverName = null;
        }
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    // No longer needed
    public String getPrefix()
    {
        if (prefix == null)
        {
            prefix = "[JIRA]";
        }

        return prefix;
    }

    // no longer needed
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    public boolean getNoemail()
    {
        return noemail;
    }

    public void setNoemail(boolean noemail)
    {
        this.noemail = noemail;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDesc()
    {
        return desc;
    }

    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPort()
    {
        return port;
    }

    public void setPort(String port)
    {
        this.port = port;
    }

       public boolean isTlsRequired()
    {
        return tlsRequired;
    }

    public void setTlsRequired(final boolean tlsRequired)
    {
        this.tlsRequired = tlsRequired;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public String[] getTypes()
    {
        return MailServerManager.SERVER_TYPES;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    public Map<String, String> getSupportedServiceProviders()
    {
        return MapBuilder.<String, String>newBuilder()
            .add("custom", getText("admin.mailservers.custom"))
            .add("gmail-smtp", "Google Apps Mail / Gmail")
            .add("yahooplus-smtp", "Yahoo! Mail Plus")
            .toLinkedHashMap();
    }

    public MailProtocol[] getSupportedClientProtocols(String type)
    {
        if (StringUtils.isNotBlank(type))
        {
            return MailProtocol.getMailProtocolsForServerType(type);
        }
        else
        {
            return new MailProtocol[0];
        }
    }

    public String getMailservertype()
    {
        return mailservertype;
    }

    public void setMailservertype(String mailservertype)
    {
        this.mailservertype = mailservertype;
    }
    public boolean isJndi() {
        return "jndi".equals(mailservertype);
    }
    public boolean isSmtp() {
        return "smtp".equals(mailservertype);
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

}
