package com.atlassian.mail.server.managers;

import javax.mail.Session;
import com.atlassian.mail.MailException;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.impl.PopMailServerImpl;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;

import javax.mail.Authenticator;
import java.util.Map;
import java.util.Properties;

public class MockMailServerManager extends OFBizMailServerManager
{
    private MockSession mockSession = new MockSession();

    public Map getMapFromColumns(MailServer mailServer) throws MailException
    {
        return super.getMapFromColumns(mailServer);
    }

    public GenericValue getMailServerGV(Long id) throws MailException
    {
        return super.getMailServerGV(id);
    }

    public MailServer constructMailServer(GenericValue gv)
    {
        if (SERVER_TYPES[0].equals(gv.getString("type")))
            return new PopMailServerImpl(gv.getLong("id"), gv.getString("name"), gv.getString("description"), gv.getString("servername"), gv.getString("username"), gv.getString("password"));
        else if (SERVER_TYPES[1].equals(gv.getString("type")))
        {
            if (TextUtils.stringSet(gv.getString("servername")))
            {
                return new MockSMTPMailServer(gv.getLong("id"), gv.getString("name"), gv.getString("description"), gv.getString("from"), gv.getString("prefix"), false, gv.getString("servername"), gv.getString("username"), gv.getString("password"));
            }
            else
            {
                return new MockSMTPMailServer(gv.getLong("id"), gv.getString("name"), gv.getString("description"), gv.getString("from"), gv.getString("prefix"), true, gv.getString("jndilocation"), gv.getString("username"), gv.getString("password"));
            }
        }
        else
            return null;
    }

    public Session getSession(Properties props, Authenticator auth)
    {
        return mockSession.getInstance(props, auth);
    }
}
