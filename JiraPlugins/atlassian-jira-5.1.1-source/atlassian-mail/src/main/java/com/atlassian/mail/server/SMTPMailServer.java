package com.atlassian.mail.server;

import com.atlassian.mail.Email;
import com.atlassian.mail.MailConstants;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailProtocol;

import java.io.PrintStream;

public interface SMTPMailServer extends MailServer
{
    @Deprecated
    String DEFAULT_SMTP_PORT = MailConstants.DEFAULT_SMTP_PORT;

    String getDefaultFrom();

    void setDefaultFrom(String from);

    String getPrefix();

    void setPrefix(String prefix);

    boolean isSessionServer();

    void setSessionServer(boolean sessionServer);

    String getJndiLocation();

    void setJndiLocation(String jndiLocation);

    public boolean isRemovePrecedence();

    public void setRemovePrecedence(boolean precedence);

    void send(Email email) throws MailException;

    void quietSend(Email email) throws MailException;

    /**
     * Enable or disable SMTP-level logging.
     *
     * @param debug Turn debugging on or off
     */
    void setDebug(boolean debug);

    /**
     * Whether  logging is enabled.
     */
    boolean getDebug();

    /**
     * Where debug logs currently go to.
	 * @return print stream where debug info is logged to
	 */
    PrintStream getDebugStream();

    boolean isTlsRequired();

    void setTlsRequired(boolean tlsRequired);


}