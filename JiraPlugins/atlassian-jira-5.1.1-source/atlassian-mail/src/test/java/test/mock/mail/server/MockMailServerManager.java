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

import com.atlassian.mail.MailException;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.PopMailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.managers.AbstractMailServerManager;
import com.sun.mail.util.PropUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import javax.mail.Authenticator;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import java.util.List;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PrepareForTest(Session.class)
public class MockMailServerManager extends AbstractMailServerManager
{
    private final Session mockSession;
    private MailServer server;

    public MockMailServerManager() throws NoSuchProviderException {
        mockSession = PowerMockito.mock(Session.class);
        PowerMockito.mockStatic(PropUtil.class);
        when(PropUtil.getBooleanSessionProperty(mockSession, "mail.mime.address.strict", true)).thenReturn(false);
        final Transport mockTransport = mock(Transport.class);
        when(mockSession.getTransport("smtp")).thenReturn(mockTransport);
    }

    public MailServer getMailServer(final Long id) throws MailException
    {
        return server;
    }

    public MailServer getMailServer(final String name) throws MailException
    {
        return null;
    }

    public List<String> getServerNames() throws MailException
    {
        return null;
    }

    public List<SMTPMailServer> getSmtpMailServers()
    {
        return null;
    }

    public List<PopMailServer> getPopMailServers()
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

    public SMTPMailServer getDefaultSMTPMailServer()
    {
        return new MockSMTPMailServer(null, "default", "", "", "", false, "mail.atlassian.com", "", "", mockSession);
    }

    public PopMailServer getDefaultPopMailServer()
    {
        return null;
    }


    public Session getSession(Properties props, Authenticator auth)
    {
        return mockSession;
    }
}
