/*
 * Created by IntelliJ IDEA.
 * User: owen
 * Date: Dec 9, 2002
 * Time: 2:45:14 PM
 * CVS Revision: $Revision: 1.17 $
 * Last CVS Commit: $Date: 2005/09/13 01:21:55 $
 * Author of last CVS Commit: $Author: jturner $
 * To change this template use Options | File Templates.
 */
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
     * If debug is enabled, output will go to this stream.
    * @param debugStream An optional stream to send debug messages to. If null, System.out is used.
    */
    void setDebugStream(PrintStream debugStream);

    /**
     * Whether  logging is enabled.
     */
    boolean getDebug();

    /**
     * Where debug logs currently go to.
     */
    PrintStream getDebugStream();

    boolean isTlsRequired();

    void setTlsRequired(boolean tlsRequired);


}