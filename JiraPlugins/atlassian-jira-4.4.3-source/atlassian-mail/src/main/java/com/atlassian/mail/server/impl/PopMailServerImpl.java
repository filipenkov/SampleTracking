package com.atlassian.mail.server.impl;

import alt.javax.mail.Session;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.AbstractMailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.PopMailServer;

import javax.naming.NamingException;

import java.net.Authenticator;

import static com.atlassian.mail.MailConstants.DEFAULT_POP_PORT;
import static com.atlassian.mail.MailConstants.DEFAULT_POP_PROTOCOL;
import static com.atlassian.mail.MailConstants.DEFAULT_TIMEOUT;

public class PopMailServerImpl extends AbstractMailServer implements PopMailServer
{
    public PopMailServerImpl()
    {

    }

    public PopMailServerImpl(Long id, String name, String description, String serverName, String username, String password)
    {
        this(id, name, description, DEFAULT_POP_PROTOCOL, serverName, DEFAULT_POP_PORT, username, password, DEFAULT_TIMEOUT);
    }

    public PopMailServerImpl(Long id, String name, String description, MailProtocol popProtocol, String serverName, String popPort, String username, String password)
    {
        this(id, name, description, popProtocol, serverName,  popPort, username, password, DEFAULT_TIMEOUT);
    }

    public PopMailServerImpl(Long id, String name, String description, MailProtocol popProtocol, String serverName, String popPort, String username, String password, long timeout)
    {
        super(id, name, description, popProtocol, serverName,  popPort, username, password, timeout);
    }

    @Override
    protected javax.mail.Authenticator getAuthenticator() {
        return null;
    }

    public String getType()
    {
        return MailServerManager.SERVER_TYPES[0];
    }

    public Session getSession() throws NamingException
    {
        return null;
    }
}
