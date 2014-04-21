/*
 * Created by IntelliJ IDEA.
 * User: owen
 * Date: Nov 25, 2002
 * Time: 9:05:42 AM
 * CVS Revision: $Revision: 1.2 $
 * Last CVS Commit: $Date: 2002/12/09 07:22:24 $
 * Author of last CVS Commit: $Author: mike $
 * To change this template use Options | File Templates.
 */
package test.mock.mail.server;

import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.AbstractMailServer;
import com.atlassian.mail.server.MailServerManager;

import javax.mail.Session;

import javax.mail.Authenticator;
import javax.naming.NamingException;

public class MockAbstractMailServer extends AbstractMailServer
{
    public MockAbstractMailServer(Long id, String name, String description, MailProtocol protocol, String serverName, String port, String username, String password, String socksHost, String socksPort)
    {
        super(id, name, description, protocol, serverName, port, username, password, 10000, socksHost, socksPort);
    }

    public String getType()
    {
        return MailServerManager.SERVER_TYPES[1];
    }

    public Session getSession() throws NamingException
    {
        return null;
    }

    @Override
    protected Authenticator getAuthenticator() {
        return null;
    }

    public void setId(Long id)
    {
        super.setId(id);
    }

    public void setName(String name)
    {
        super.setName(name);
    }

    public void setDescription(String description)
    {
        super.setDescription(description);
    }

    public void setHostname(String serverName)
    {
        super.setHostname(serverName);
    }

    public void setUsername(String username)
    {
        super.setUsername(username);
    }

    public void setPassword(String password)
    {
        super.setPassword(password);
    }
}
