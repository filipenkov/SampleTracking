package com.atlassian.mail.server.managers;

import com.atlassian.mail.MailException;
import com.atlassian.mail.server.*;

import javax.annotation.Nullable;
import javax.mail.Authenticator;
import javax.mail.Session;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Date: Dec 9, 2002
 */
public abstract class AbstractMailServerManager implements MailServerManager
{
	private MailServerConfigurationHandler mailServerConfigurationHandler;

    public void init(Map params)
    {
        // by default, do nothing
    }

	@Nullable
    public abstract MailServer getMailServer(Long id) throws MailException;

	@Nullable
    public abstract MailServer getMailServer(String name) throws MailException;

    public abstract List<String> getServerNames() throws MailException;

    public abstract List<SMTPMailServer> getSmtpMailServers();

    public abstract List<PopMailServer> getPopMailServers();

    public abstract Long create(MailServer mailServer) throws MailException;

    public abstract void update(MailServer mailServer) throws MailException;

    public abstract void delete(Long mailServerId) throws MailException;

    @Nullable
    public abstract SMTPMailServer getDefaultSMTPMailServer();

    public boolean isDefaultSMTPMailServerDefined() {
        return getDefaultSMTPMailServer() != null;
    }

    @Nullable
    public abstract PopMailServer getDefaultPopMailServer();

    public Session getSession(Properties props, Authenticator auth)
    {
        return Session.getInstance(props, auth);
    }

	public synchronized void setMailServerConfigurationHandler(@Nullable MailServerConfigurationHandler mailServerConfigurationHandler) {
		this.mailServerConfigurationHandler = mailServerConfigurationHandler;
	}

	protected synchronized MailServerConfigurationHandler getMailServerConfigurationHandler() {
		return mailServerConfigurationHandler;
	}
}
