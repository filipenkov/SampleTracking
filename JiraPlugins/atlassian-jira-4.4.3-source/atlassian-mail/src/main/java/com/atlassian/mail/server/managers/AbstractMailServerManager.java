package com.atlassian.mail.server.managers;

import alt.javax.mail.Session;
import alt.javax.mail.SessionImpl;
import com.atlassian.mail.MailException;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.PopMailServer;
import com.atlassian.mail.server.SMTPMailServer;

import javax.mail.Authenticator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Dec 9, 2002
 * Time: 2:30:39 PM
 * To change this template use Options | File Templates.
 */
public abstract class AbstractMailServerManager implements MailServerManager
{
    public void init(Map params)
    {
        // by default, do nothing
    }

    public abstract MailServer getMailServer(Long id) throws MailException;

    public abstract MailServer getMailServer(String name) throws MailException;

    public abstract List getServerNames() throws MailException;

    public abstract List getSmtpMailServers() throws MailException;

    public abstract List getPopMailServers() throws MailException;

    public abstract Long create(MailServer mailServer) throws MailException;

    public abstract void update(MailServer mailServer) throws MailException;

    public abstract void delete(Long mailServerId) throws MailException;

    public abstract SMTPMailServer getDefaultSMTPMailServer() throws MailException;

    public abstract PopMailServer getDefaultPopMailServer() throws MailException;

    public Session getSession(Properties props, Authenticator auth)
    {
        return new SessionImpl(javax.mail.Session.getInstance(props, auth));
    }

}
