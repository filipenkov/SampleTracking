package com.atlassian.mail.server.managers;

import javax.mail.Session;
import javax.mail.Transport;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.PasswordAuthentication;
import javax.mail.Provider;
import javax.mail.Store;
import javax.mail.URLName;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MockSession
{
    private Map urlAuthenticators = new HashMap();
    private Transport myTransport;
    private Properties props;
    private javax.mail.Session wrapped;

    public Session getInstance(Properties props, Authenticator authenticator)
    {
        getInstance(props);
        if (authenticator == null)
            setPasswordAuthentication(new URLName((String) props.get("mail.smtp.host")), null);
        else
            setPasswordAuthentication(new URLName((String) props.get("mail.smtp.host")), ((MockAuthenticator) authenticator).getPasswordAuthentication());
        wrapped = javax.mail.Session.getInstance(props, authenticator);
        return wrapped;
    }

    public Session getInstance(Properties props)
    {
        this.props = props;
        wrapped = javax.mail.Session.getInstance(props);
        return wrapped;
    }

    public Session getDefaultInstance(Properties props, Authenticator authenticator)
    {
        wrapped = javax.mail.Session.getDefaultInstance(props, authenticator);
        return getInstance(props, authenticator);
    }

    public Session getDefaultInstance(Properties props)
    {
        wrapped = javax.mail.Session.getDefaultInstance(props);
        return getInstance(props);
    }


    public boolean getDebug()
    {
       return false;
    }

    public Provider getProviders()[]
    {
       return null;
    }

    public Provider getProvider(String name)
    {
        return null;
    }

    public void setProvider(Provider provider)
    {
    }

    public Transport getTransport()
    {
        if (myTransport == null)
            myTransport = new MockTransport(wrapped, new URLName((String) props.get("mail.smtp.host")));
        return myTransport;
    }

    public void setupGetTransport(Transport aTransport)
    {
        myTransport = aTransport;
    }

    public Transport getTransport(String aTransportName)
    {
        if ("smtp".equals(aTransportName))
            return getTransport();
        else
            return null;
    }

    public Transport getTransport(Address address)
    {
        return null;
    }

    public Transport getTransport(Provider provider)
    {
        return null;
    }

    public Transport getTransport(URLName url)
    {
        return null;
    }

    public Store getStore()
    {
        return null;
    }

    public Store getStore(String name)
    {
        return null;
    }

    public Store getStore(URLName url)
    {
        return null;
    }

    public Store getStore(Provider provider)
    {
        return null;
    }

    public Folder getFolder()
    {
        return null;
    }

    public Folder getFolder(Store store)
    {
        return null;
    }

    public Folder getFolder(URLName url)
    {
        return null;
    }

    public void setPasswordAuthentication(URLName url, PasswordAuthentication
            passwordAuthentication)
    {
        urlAuthenticators.put(url.getHost(), passwordAuthentication);
    }

    public PasswordAuthentication getPasswordAuthentication(URLName url)
    {
        return (PasswordAuthentication) urlAuthenticators.get(url.getHost());
    }

    public PasswordAuthentication requestPasswordAuthentication(java.net.InetAddress address, int port, String protocol, String prompt, String defaultUserName)
    {
        return null;
    }

    public Properties getProperties()
    {
        return null;
    }

    public String getProperty(String name)
    {
        return null;
    }

    public javax.mail.Session getWrappedSession()
    {
        return wrapped;
    }

}
