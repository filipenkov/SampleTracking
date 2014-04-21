
package com.atlassian.mail.server.impl;

import alt.javax.mail.Session;
import alt.javax.mail.SessionImpl;
import alt.javax.mail.Transport;
import alt.javax.mail.internet.MimeMessage;
import alt.javax.mail.internet.MimeMessageImpl;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.AbstractMailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.util.MessageCreator;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Category;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import static com.atlassian.mail.MailConstants.DEFAULT_SMTP_PROTOCOL;
import static com.atlassian.mail.MailConstants.DEFAULT_TIMEOUT;

public class SMTPMailServerImpl extends AbstractMailServer implements SMTPMailServer
{
    private static final Category log = Category.getInstance(SMTPMailServerImpl.class);

    private boolean isSessionServer;
    private String defaultFrom;
    private String prefix;
    private String jndiLocation;
    private boolean removePrecedence;
    private transient Session session;

    public SMTPMailServerImpl()
    {
    }

    public SMTPMailServerImpl(Long id, String name, String description, String from, String prefix, boolean isSession, String location, String username, String password)
    {
        this(id, name, description, from, prefix, isSession, DEFAULT_SMTP_PROTOCOL, location, DEFAULT_SMTP_PORT, false, username, password);
    }

    public SMTPMailServerImpl(Long id, String name, String description, String from, String prefix, boolean isSession, MailProtocol protocol, String location, String smtpPort, boolean tlsRequired, String username, String password)
    {
        this(id, name, description, from, prefix, isSession, false, protocol, location, smtpPort, tlsRequired, username, password, DEFAULT_TIMEOUT);
    }

    public SMTPMailServerImpl(Long id, String name, String description, String from, String prefix, boolean isSession, MailProtocol protocol, String location, String smtpPort, boolean tlsRequired, String username, String password, long timeout)
    {
        this(id, name, description, from, prefix, isSession, false, protocol, location, smtpPort, tlsRequired, username, password, timeout);
    }

    public SMTPMailServerImpl(Long id, String name, String description, String from, String prefix, boolean isSession, boolean removePrecedence, MailProtocol protocol, String location, String smtpPort, boolean tlsRequired, String username, String password, long timeout)
    {
        super(id, name, description, protocol, location, smtpPort, username, password, timeout);
        setDefaultFrom(from);
        setPrefix(prefix);
        setSessionServer(isSession);
        setRemovePrecedence(removePrecedence);
        setTlsRequired(tlsRequired);

        if (isSession)
        {
            setJndiLocation(location);
            setHostname(null);
        }
    }

    public String getJndiLocation()
    {
        return jndiLocation;
    }

    public void setJndiLocation(String jndiLocation)
    {
        this.jndiLocation = jndiLocation;
        propertyChanged();
    }

    protected Authenticator getAuthenticator()
    {
        return new MyAuthenticator();
    }

    /**
     * get the mail session
     */
    public Session getSession() throws NamingException, MailException
    {
        if (session == null)

        {
            if (isSessionServer())
            {
                Object jndiSession = getJndiSession();
                if (jndiSession instanceof javax.mail.Session)
                {
                    session = new SessionImpl((javax.mail.Session) jndiSession);
                }
                else
                {
                    log.error("Mail server at location [" + getJndiLocation() + "] is not of required type javax.mail.Session, or is in different classloader. " +
                            "It is of type '" + (jndiSession != null ? jndiSession.getClass().getName() : null) + "' in classloader '"+jndiSession.getClass().getClassLoader()+"' instead");
                    throw new IllegalArgumentException("Mail server at location [" + getJndiLocation() + "] is not of required type javax.mail.Session. ");
                }
            }
            else
            {
                final Properties props = loadSystemProperties(getProperties());
                final Authenticator auth = isAuthenticating ? getAuthenticator() : null;
                session = MailFactory.getServerManager().getSession(props, auth);
                if (getDebugStream() != null)
                {
                    try
                    {
                    session.getWrappedSession().setDebugOut(getDebugStream());
                    } catch (NoSuchMethodError nsme)
                    {
                        // JRA-8543
                        log.error("Warning: An old (pre-1.3.2) version of the JavaMail library (javamail.jar or mail.jar) bundled with your app server, is in use. Some functions such as IMAPS/POPS/SMTPS will not work. Consider upgrading the app server's javamail jar to the version JIRA provides.");
                    }
                }
            }
        }
        return session;
    }

    protected Object getJndiSession() throws NamingException
    {
        Context ctx = new InitialContext();
        Object jndiSession = ctx.lookup(getJndiLocation());
        return jndiSession;
    }

    public void send(Email email) throws MailException
    {
        try
        {
            Session thisSession = getSession();

            MimeMessage message = new MimeMessageImpl(thisSession);
            MessageCreator messageCreator = new MessageCreator();

            messageCreator.updateMimeMessage(email, getDefaultFrom(), prefix, message);

            // Send the message using the transport configured for this mail server - JRA-24549/MAIL-61
            final Transport transport = thisSession.getTransport(getMailProtocol().getProtocol());
            try
            {
                transport.connect();
                transport.sendMessage(message, message.getAllRecipients());
            }
            finally
            {
                transport.close();
            }

            // Message-Id is set by the MTA (I think) so is only accessible after sending
            if (message.getHeader("Message-Id") != null && message.getHeader("Message-Id").length > 0)
            {
                email.setMessageId(message.getHeader("Message-Id")[0]);
            }
        }
        catch (NamingException e)
        {
            throw new MailException(e);
        }
        catch (MessagingException e)
        {
            throw new MailException(e);
        }
        catch (UnsupportedEncodingException e)
        {
            log.debug("Error setting the 'from' address with an email and the user's fullname");
            e.printStackTrace();
        }
    }


    /**
     * Send a message - but don't throw exceptions, just log the errors
     */
    public void quietSend(Email email) throws MailException
    {
        try
        {
            send(email);
        }
        catch (Exception e)
        {
            log.error("Error sending mail. to:" + email.getTo() + ", cc:" + email.getCc() + ", bcc:" + email.getBcc() + ", subject:" + email.getSubject() + ", body:" + email.getBody() + ", mimeType:" + email.getMimeType() + ", encoding:" + email.getEncoding() + ", multipart:" + email.getMultipart() + ", error:" + e, e);
        }
    }

    public String getType()
    {
        return MailServerManager.SERVER_TYPES[1];
    }

    public String getDefaultFrom()
    {
        return defaultFrom;
    }

    public void setDefaultFrom(String defaultFrom)
    {
        this.defaultFrom = defaultFrom;
        propertyChanged();
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    public boolean isRemovePrecedence()
    {
        return removePrecedence;
    }

    public void setRemovePrecedence(boolean precedence)
    {
        this.removePrecedence = precedence;
        propertyChanged();
    }

    public boolean isSessionServer()
    {
        return isSessionServer;
    }

    public void setSessionServer(boolean sessionServer)
    {
        isSessionServer = sessionServer;
        propertyChanged();
    }

    private class MyAuthenticator extends Authenticator
    {
        public PasswordAuthentication getPasswordAuthentication()
        {
            return new PasswordAuthentication(getUsername(), getPassword());
        }
    }

    ///CLOVER:OFF
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof SMTPMailServerImpl))
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        final SMTPMailServerImpl smtpMailServer = (SMTPMailServerImpl) o;

        if (isSessionServer != smtpMailServer.isSessionServer)
        {
            return false;
        }
        if (defaultFrom != null ? !defaultFrom.equals(smtpMailServer.defaultFrom) : smtpMailServer.defaultFrom != null)
        {
            return false;
        }
        if (prefix != null ? !prefix.equals(smtpMailServer.prefix) : smtpMailServer.prefix != null)
        {
            return false;
        }
        if (removePrecedence !=  smtpMailServer.removePrecedence)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 29 * result + (isSessionServer ? 1 : 0);
        result = 29 * result + (defaultFrom != null ? defaultFrom.hashCode() : 0);
        result = 29 * result + (prefix != null ? prefix.hashCode() : 0);
        result = 29 * result + (removePrecedence ? 1 : 0);
        return result;
    }

    public String toString()
    {
       return new ToStringBuilder(this).append("id", getId()).append("name", getName()).append("description", getDescription()).append("server name", getHostname()).append("username", getUsername()).append("password", getPassword()).append("isSessionServer", isSessionServer).append("defaultFrom", defaultFrom).append("prefix", prefix).append("smtpPort", getPort()).toString();
    }

    /**
     * Discard the cached session when a property of the server changes.
     */
    protected void propertyChanged()
    {
        super.propertyChanged();
        session = null;
    }
}
