
package com.atlassian.mail.server;

import alt.javax.mail.Session;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailProtocol;

import javax.naming.NamingException;
import java.util.Properties;

public interface MailServer
{
    Long getId();

    String getName();

    String getDescription();

    String getType();

    String getPort();

    MailProtocol getMailProtocol();

    String getHostname();

    String getUsername();

    String getPassword();

    long getTimeout();

    boolean getDebug();

    Session getSession() throws NamingException, MailException;

    void setName(String name);

    void setDescription(String description);

    void setHostname(String hostname);

    void setUsername(String username);

    void setPassword(String password);

    void setId(Long id);

    void setPort(String port);

    void setMailProtocol(MailProtocol protocol);

    void setTimeout(long timeout);

    void setDebug(boolean debug);

    void setProperties(Properties props);

    /**
     *
     * @return the set of dynamic properties applied to thsi server, may very wel be null
     * in which case the mail server properties can be obtained from the session
     */
    Properties getProperties();
}
