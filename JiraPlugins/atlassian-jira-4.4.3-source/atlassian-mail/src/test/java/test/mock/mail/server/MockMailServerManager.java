/*
 * Created by IntelliJ IDEA.
 * User: owen
 * Date: Nov 25, 2002
 * Time: 10:45:16 AM
 * CVS Revision: $Revision: 1.11 $
 * Last CVS Commit: $Date: 2004/01/06 05:43:19 $
 * Author of last CVS Commit: $Author: dloeng $
 * To change this template use Options | File Templates.
 */
package test.mock.mail.server;

import alt.javax.mail.Session;
import com.atlassian.mail.server.PopMailServer;
import com.atlassian.mail.server.managers.AbstractMailServerManager;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.MailException;

import javax.mail.Authenticator;
import java.util.List;
import java.util.Properties;

import test.mock.mail.MockSession;

public class MockMailServerManager extends AbstractMailServerManager
{
    private MockSession mockSession = new MockSession();
    private MailServer server;

    public MailServer getMailServer(final Long id) throws MailException
    {
        return server;
    }

    public MailServer getMailServer(final String name) throws MailException
    {
        return null;
    }

    public List getServerNames() throws MailException
    {
        return null;
    }

    public List getSmtpMailServers() throws MailException
    {
        return null;
    }

    public List getPopMailServers() throws MailException
    {
        return null;
    }

    public Long create(final MailServer mailServer) throws MailException
    {
        return null;
    }

    public void update(final MailServer mailServer) throws MailException
    {
    }

    public void delete(final Long mailServerId) throws MailException
    {
    }

    public SMTPMailServer getDefaultSMTPMailServer() throws MailException
    {
        return new MockSMTPMailServer(null, "default", "", "", "", false, "mail.atlassian.com", "", "");
    }

    public PopMailServer getDefaultPopMailServer() throws MailException
    {
        return null;
    }


    public Session getSession(Properties props, Authenticator auth)
    {
        return mockSession.getInstance(props, auth);
    }
}
