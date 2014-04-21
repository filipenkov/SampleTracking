package test.mock.mail;

import alt.javax.mail.Session;
import alt.javax.mail.Transport;
import com.mockobjects.ExpectationValue;
import com.mockobjects.MockObject;

import javax.mail.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MockSession extends MockObject implements Session
{
    private Map urlAuthenticators = new HashMap();
    private final ExpectationValue myDebug = new ExpectationValue("debug");
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
        return this;
    }

    public Session getInstance(Properties props)
    {
        this.props = props;
        wrapped = javax.mail.Session.getInstance(props);
        return this;
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

    public void setExpectedDebug(boolean aDebug)
    {
        myDebug.setActual(aDebug);
    }

    public void setDebug(boolean aDebug)
    {
        myDebug.setActual(aDebug);
    }

    public boolean getDebug()
    {
        notImplemented();
        return false;
    }

    public Provider getProviders()[]
    {
        notImplemented();
        return null;
    }

    public Provider getProvider(String name)
    {
        notImplemented();
        return null;
    }

    public void setProvider(Provider provider)
    {
        notImplemented();
    }

    public Transport getTransport()
    {
        if (myTransport == null)
            myTransport = new MockTransport();
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
        notImplemented();
        return null;
    }

    public Transport getTransport(Provider provider)
    {
        notImplemented();
        return null;
    }

    public Transport getTransport(URLName url)
    {
        notImplemented();
        return null;
    }

    public Store getStore()
    {
        notImplemented();
        return null;
    }

    public Store getStore(String name)
    {
        notImplemented();
        return null;
    }

    public Store getStore(URLName url)
    {
        notImplemented();
        return null;
    }

    public Store getStore(Provider provider)
    {
        notImplemented();
        return null;
    }

    public Folder getFolder()
    {
        notImplemented();
        return null;
    }

    public Folder getFolder(Store store)
    {
        notImplemented();
        return null;
    }

    public Folder getFolder(URLName url)
    {
        notImplemented();
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
        notImplemented();
        return null;
    }

    public Properties getProperties()
    {
        notImplemented();
        return null;
    }

    public String getProperty(String name)
    {
        notImplemented();
        return null;
    }

    public javax.mail.Session getWrappedSession()
    {
        return wrapped;
    }

}
