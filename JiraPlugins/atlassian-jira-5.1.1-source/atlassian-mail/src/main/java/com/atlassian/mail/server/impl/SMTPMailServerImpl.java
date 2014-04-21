
package com.atlassian.mail.server.impl;

import com.atlassian.mail.Email;
import com.atlassian.mail.MailConstants;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.AbstractMailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.util.MessageCreator;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import static com.atlassian.mail.MailConstants.DEFAULT_SMTP_PROTOCOL;
import static com.atlassian.mail.MailConstants.DEFAULT_TIMEOUT;

public class SMTPMailServerImpl extends AbstractMailServer implements SMTPMailServer
{
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
        this(id, name, description, from, prefix, isSession, DEFAULT_SMTP_PROTOCOL, location, MailConstants.DEFAULT_SMTP_PORT, false, username, password);
    }

    public SMTPMailServerImpl(Long id, String name, String description, String from, String prefix, boolean isSession, MailProtocol protocol, String location, String smtpPort, boolean tlsRequired, String username, String password)
    {
        this(id, name, description, from, prefix, isSession, false, protocol, location, smtpPort, tlsRequired, username, password, DEFAULT_TIMEOUT);
    }

    public SMTPMailServerImpl(Long id, String name, String description, String from, String prefix, boolean isSession, MailProtocol protocol, String location, String smtpPort, boolean tlsRequired, String username, String password, long timeout)
    {
        this(id, name, description, from, prefix, isSession, false, protocol, location, smtpPort, tlsRequired, username, password, timeout);
    }

    public SMTPMailServerImpl(Long id, String name, String description, String from, String prefix, boolean isSession, MailProtocol protocol, String location, String smtpPort, boolean tlsRequired, String username, String password, long timeout, String socksHost, String socksPort)
    {
        this(id, name, description, from, prefix, isSession, false, protocol, location, smtpPort, tlsRequired, username, password, timeout, socksHost, socksPort);
    }

    public SMTPMailServerImpl(Long id, String name, String description, String from, String prefix, boolean isSession, boolean removePrecedence, MailProtocol protocol, String location, String smtpPort, boolean tlsRequired, String username, String password, long timeout)
    {
        this(id, name, description, from, prefix, isSession, removePrecedence, protocol, location, smtpPort, tlsRequired, username, password, timeout, null, null);
    }

    public SMTPMailServerImpl(Long id, String name, String description, String from, String prefix, boolean isSession, boolean removePrecedence, MailProtocol protocol, String location, String smtpPort, boolean tlsRequired, String username, String password, long timeout, String socksHost, String socksPort)
    {
        super(id, name, description, protocol, location, smtpPort, username, password, timeout, socksHost, socksPort);
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
                log.debug("Getting session from JNDI");
                Object jndiSession = getJndiSession();
                if (jndiSession instanceof javax.mail.Session)
                {
                    session =  (Session) jndiSession;
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
                session = getSessionFromServerManager(props, auth);
                if  (auth != null)
                {
                    session.setPasswordAuthentication(new URLName(getMailProtocol().getProtocol(), getHostname(), Integer.parseInt(getPort()),null, null, null), new PasswordAuthentication(getUsername(),getPassword()));
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

            MimeMessage message = new MimeMessage(thisSession);
            MessageCreator messageCreator = new MessageCreator();

            messageCreator.updateMimeMessage(email, getDefaultFrom(), prefix, message);

			log.debug("Getting transport for protocol [" + getMailProtocol().getProtocol() + "]");
            // Send the message using the transport configured for this mail server - JRA-24549/MAIL-61
            final Transport transport = thisSession.getTransport(getMailProtocol().getProtocol());
            try {
				if (log.isDebugEnabled())
				{
					log.debug("Got transport: [" + transport + "]. Connecting");
				}
				transport.connect();
				log.debug("Sending message");
				transport.sendMessage(message, message.getAllRecipients());
			}
            finally
            {
                if (transport != null)
                {
                    transport.close();
                }
            }

            // Message-Id is set by the MTA (I think) so is only accessible after sending
            if (message.getHeader("Message-Id") != null && message.getHeader("Message-Id").length > 0)
            {
				final String messageId = message.getHeader("Message-Id")[0];
				if (log.isDebugEnabled())
				{
					log.debug("Setting message id to [" + messageId + "]");
				}
				email.setMessageId(messageId);
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
            log.error("Error setting the 'from' address with an email and the user's fullname", e);
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
