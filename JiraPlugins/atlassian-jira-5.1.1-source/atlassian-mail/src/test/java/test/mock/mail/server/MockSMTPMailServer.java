/*
 * Created by IntelliJ IDEA.
 * User: owen
 * Date: Nov 25, 2002
 * Time: 9:23:34 AM
 * CVS Revision: $Revision: 1.7 $
 * Last CVS Commit: $Date: 2004/01/06 05:43:19 $
 * Author of last CVS Commit: $Author: dloeng $
 * To change this template use Options | File Templates.
 */
package test.mock.mail.server;

import com.atlassian.mail.MailException;
import com.atlassian.mail.MailUtils;
import com.atlassian.mail.Email;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import org.mockito.MockSettings;

import javax.mail.Authenticator;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.NamingException;

import javax.mail.Session;

public class MockSMTPMailServer extends SMTPMailServerImpl
{
    private final Session mockSession;

    public MockSMTPMailServer(Long id, String name, String description, String from, String prefix, boolean isSession, String location, String username, String password, Session session)
    {
        super(id, name, description, from, prefix, isSession, location, username, password);
        this.mockSession = session;
    }

    public void send(Email email) throws MailException
    {
        super.send(email);
    }

    public InternetAddress[] parseAddresses(String addresses) throws AddressException
    {
        return MailUtils.parseAddresses(addresses);
    }

    public String getDefaultFrom()
    {
        return super.getDefaultFrom();
    }

    public void setDefaultFrom(String from)
    {
        super.setDefaultFrom(from);
    }

    public String getPrefix()
    {
        return super.getPrefix();
    }

    public void setPrefix(String prefix)
    {
        super.setPrefix(prefix);
    }

    public void setSessionServer(boolean sessionServer)
    {
        super.setSessionServer(sessionServer);
    }


    public Session getSession() throws NamingException, MailException
    {
        return mockSession;
    }
}
