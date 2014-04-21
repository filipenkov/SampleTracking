/*
 * Created by IntelliJ IDEA.
 * User: owen
 * Date: Nov 22, 2002
 * Time: 3:33:27 PM
 * CVS Revision: $Revision: 1.6 $
 * Last CVS Commit: $Date: 2006/09/29 02:48:01 $
 * Author of last CVS Commit: $Author: cowen $
 * To change this template use Options | File Templates.
 */
package com.atlassian.mail.server;

import com.atlassian.mail.MailProtocol;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Category;

import javax.mail.Authenticator;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class AbstractMailServer implements MailServer, Serializable
{
    protected transient Category LOG = Category.getInstance(this.getClass());
    private Long id;
    private String name;
    private String description;
    private String hostname;
    private String username = null;
    private String password = null;
    private MailProtocol mailProtocol = null;
    private String port = null;
    private long timeout;
    private boolean debug;
    private boolean tlsRequired;
    private transient PrintStream debugStream;
    private Properties props = new Properties();
    protected boolean isAuthenticating;

    public AbstractMailServer()
    {
    }

    public AbstractMailServer(Long id, String name, String description, MailProtocol protocol, String hostName, String port, String username, String password, long timeout)
    {
        setId(id);
        setName(name);
        setDescription(description);
        setHostname(hostName);
        setUsername(username);
        setPassword(password);
        setMailProtocol(protocol);
        setPort(port);
        setTimeout(timeout);
        //MAIL-60: Need to ensure that system properties get set for mail servers.
        this.props = loadSystemProperties(props);
    }

    private void setInitialProperties()
    {
        if (getMailProtocol() != null)
        {
            final String protocol = getMailProtocol().getProtocol();
            props.put("mail."+protocol+".host",""+getHostname());
            props.put("mail."+protocol+".port", ""+getPort());
            props.put("mail."+protocol+".timeout",""+getTimeout());
            props.put("mail.transport.protocol",""+ protocol);
            if (isTlsRequired())
            {
                props.put("mail."+protocol+".starttls.enable","true");
            }
            if (StringUtils.isNotBlank(getUsername()))
            {
                props.put("mail."+protocol+".auth", "true");
                isAuthenticating = true;
            }
        }
        props.put("mail.debug", ""+getDebug());
        if (Boolean.getBoolean("mail.debug"))
        {
            props.put("mail.debug", "true");
        }
    }

    protected abstract Authenticator getAuthenticator();

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
        propertyChanged();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
        propertyChanged();
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
        propertyChanged();
    }

    public String getHostname()
    {
        return hostname;
    }

    public void setHostname(String serverName)
    {
        this.hostname = serverName;
        propertyChanged();
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        if (StringUtils.isNotBlank(username))
            this.username = username;
        else
            this.username = null;
        propertyChanged();
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        if (StringUtils.isNotBlank(password))
            this.password = password;
        else
            this.password = null;
        propertyChanged();
    }

    public MailProtocol getMailProtocol()
     {
         return mailProtocol;
     }

     public void setMailProtocol(final MailProtocol protocol)
     {
         this.mailProtocol = protocol;
         propertyChanged();
     }

     public String getPort()
     {
         return port;
     }

     public void setPort(final String port)
     {
         this.port = port;
         propertyChanged();
     }

    public long getTimeout()
    {
        return timeout;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
        propertyChanged();
    }

    public boolean isTlsRequired()
    {
        return tlsRequired;
    }

    public void setTlsRequired(final boolean tlsRequired)
    {
        this.tlsRequired = tlsRequired;
        propertyChanged();
    }

    public Properties getProperties()
    {
        return props;
    }

    public void setProperties(Properties props)
    {
        this.props = props;
        propertyChanged();
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        propertyChanged();
    }

    public void setDebugStream(PrintStream debugStream) {
        this.debugStream = debugStream;
        propertyChanged();
    }


    public boolean getDebug() {
        return this.debug;
    }

    public PrintStream getDebugStream() {
        return this.debugStream;
    }

    ///CLOVER:OFF
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof AbstractMailServer)) return false;
        final AbstractMailServer abstractMailServer = (AbstractMailServer) o;
        return new EqualsBuilder()
                .append(id, abstractMailServer.id)
                .append(name, abstractMailServer.name)
                .append(description, abstractMailServer.description)
                .append(hostname, abstractMailServer.hostname)
                .append(username, abstractMailServer.username)
                .append(password, abstractMailServer.password)
                .append(mailProtocol, abstractMailServer.mailProtocol)
                .append(port, abstractMailServer.port)
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(name)
                .append(description)
                .append(hostname)
                .append(username)
                .append(password)
                .append(mailProtocol)
                .append(port)
                .toHashCode();
    }

    public String toString()
    {
        return new ToStringBuilder(this).append("id", id).append("name", name).append("description", description).append("server name", hostname).append("username", username).append("password", password).toString();
    }

    /**
     * Call this method whenever a property of the server changes.
     * Subclasses should override it to clear any cached information.
     */
    protected void propertyChanged()
    {
        setInitialProperties();
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
    {
        ois.defaultReadObject();
        LOG = Category.getInstance(this.getClass());
    }

    /**
     * This allows users of atlassian mail to add command line properties to modify session defaults
     * See JRA-11452
     *
     * The hierarchy now is - default properties, then from the command line, then properties added via setProperties
     *
     * @param p the default properties for the current mail session
     * @return  the properties with the system properties loaded
     */
    protected synchronized Properties loadSystemProperties(Properties p)
    {
        Properties props = new Properties();
        props.putAll(p);
        props.putAll(System.getProperties());
        if (this.props != null)
        {
            props.putAll(this.props);
        }
        return props;
    }

    
}
