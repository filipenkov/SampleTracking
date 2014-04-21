
package com.atlassian.mail.server;

import javax.mail.Session;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailProtocol;
import org.apache.log4j.Logger;

import javax.naming.NamingException;
import java.io.PrintStream;
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
    
    String getSocksHost();

    String getSocksPort();

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
    
    void setSocksHost(String socksHost);

    void setSocksPort(String socksPort);

    void setDebug(boolean debug);

	/**
	 * If debug is enabled, output from underlying javamail will go to this stream.
	* @param debugStream An optional stream to send debug messages to. If null, System.out is used.
	*/
	void setDebugStream(PrintStream debugStream);


    void setProperties(Properties props);

    /**
     *
     * @return the set of dynamic properties applied to thsi server, may very wel be null
     * in which case the mail server properties can be obtained from the session
     */
    Properties getProperties();

	void setLogger(Logger logger);
}
