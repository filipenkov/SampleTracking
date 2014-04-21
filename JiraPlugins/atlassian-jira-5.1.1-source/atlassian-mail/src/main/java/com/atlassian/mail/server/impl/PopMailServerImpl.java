package com.atlassian.mail.server.impl;

import com.atlassian.mail.MailException;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.AbstractMailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.PopMailServer;

import javax.mail.Session;
import java.util.Properties;

import static com.atlassian.mail.MailConstants.*;

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
        super(id, name, description, popProtocol, serverName,  popPort, username, password, timeout, null, null);
    }
    
    public PopMailServerImpl(Long id, String name, String description, MailProtocol popProtocol, String serverName, String popPort, String username, String password, long timeout, String socksHost, String socksPort)
    {
        super(id, name, description, popProtocol, serverName,  popPort, username, password, timeout, socksHost, socksPort);
    }

    @Override
    protected javax.mail.Authenticator getAuthenticator()
	{
        return null;
    }

    public String getType()
	{
		return MailServerManager.SERVER_TYPES[0];
    }

	public Session getSession() throws MailException
	{
		final Properties props = loadSystemProperties(getProperties());
		return getSessionFromServerManager(props, getAuthenticator());
	}

}
