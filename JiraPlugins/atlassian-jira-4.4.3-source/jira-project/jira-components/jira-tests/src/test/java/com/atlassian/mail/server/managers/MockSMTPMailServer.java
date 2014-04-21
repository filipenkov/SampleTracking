package com.atlassian.mail.server.managers;

import alt.javax.mail.Session;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailUtils;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;

import javax.mail.Authenticator;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.NamingException;

public class MockSMTPMailServer extends SMTPMailServerImpl
{
    Session mockSession;

    public MockSMTPMailServer(Long id, String name, String description, String from, String prefix, boolean isSession, String location, String username, String password)
    {
        super(id, name, description, from, prefix, isSession, location, username, password);
    }

    public void send(Email email) throws MailException
    {
        super.send(email);
    }

    public InternetAddress[] parseAddresses(String addresses) throws AddressException
    {
        return MailUtils.parseAddresses(addresses);
    }

    public String getDefaultFrom()
    {
        return super.getDefaultFrom();
    }

    public void setDefaultFrom(String from)
    {
        super.setDefaultFrom(from);
    }

    public String getPrefix()
    {
        return super.getPrefix();
    }

    public void setPrefix(String prefix)
    {
        super.setPrefix(prefix);
    }

    public void setSessionServer(boolean sessionServer)
    {
        super.setSessionServer(sessionServer);
    }

    protected Authenticator getAuthenticator()
    {
        return new MockAuthenticator(getUsername(), getPassword());
    }

    public Session getSession() throws NamingException, MailException
    {
        if (mockSession == null)
            mockSession = new MockSession();
        return mockSession;
    }



}
