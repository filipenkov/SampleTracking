package test.atlassian.mail.server.managers;

import com.atlassian.mail.server.managers.XMLMailServerManager;
import com.atlassian.mail.server.PopMailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.atlassian.mail.MailException;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestXMLMailServerManager extends TestCase
{
    public void testMailServers() throws MailException
    {
        XMLMailServerManager sm = new XMLMailServerManager();
        sm.init(Collections.EMPTY_MAP);
        assertEquals("mail-servers.xml", sm.getConfigFile());

        assertEquals(0, sm.getPopMailServers().size());
        assertEquals(1, sm.getSmtpMailServers().size());

        SMTPMailServer smtpserver = sm.getDefaultSMTPMailServer();
        assertEquals("smtptwo", smtpserver.getName());
        assertNull(smtpserver.getHostname());
        assertEquals("java:comp/env/Mail", smtpserver.getJndiLocation());
        assertEquals(1, smtpserver.getId().longValue());
    }

    public void testTestMailServers() throws MailException
    {
        XMLMailServerManager sm = new XMLMailServerManager();
        sm.init(toMap("config-file", "test-mail-servers.xml"));
        assertEquals("test-mail-servers.xml", sm.getConfigFile());

        assertEquals(1, sm.getPopMailServers().size());
        PopMailServer popserver = sm.getDefaultPopMailServer();
        assertEquals("defpop", popserver.getName());
        assertEquals("mail.atlassian.com", popserver.getHostname());
        assertEquals("foo", popserver.getUsername());
        assertEquals("bar", popserver.getPassword());
        assertEquals("The default pop server.", popserver.getDescription());
        assertEquals(new Long(2), popserver.getId());
        assertEquals(20000, popserver.getTimeout());

        assertEquals(1, sm.getSmtpMailServers().size());
        SMTPMailServer smtpserver = sm.getDefaultSMTPMailServer();
        assertEquals("defsmtp", smtpserver.getName());
        assertEquals("mail.atlassian.com", smtpserver.getHostname());
        assertEquals("foo@atlassian.com", smtpserver.getDefaultFrom());
        assertEquals(new Long(1), smtpserver.getId());
        assertEquals(15000, smtpserver.getTimeout());
    }

    public void testDeleteMailServer() throws MailException
    {
        // load two servers into the manager
        XMLMailServerManager sm = new XMLMailServerManager();
        sm.init(toMap("config-file", "test-mail-servers.xml"));

        // delete the smtp server
        List smtpMailServers = sm.getSmtpMailServers();
        assertEquals(1, smtpMailServers.size());
        SMTPMailServer s = (SMTPMailServer) smtpMailServers.get(0);
        sm.delete(s.getId());

        assertEquals(0, sm.getSmtpMailServers().size());

        // try removing a false id
        try
        {
            sm.delete(new Long(999));
            fail();
        }
        catch (Exception e)
        {

        }
    }

    public void testUpdateMailServer() throws MailException
    {
        // load two servers into the manager
        XMLMailServerManager sm = new XMLMailServerManager();
        sm.init(toMap("config-file", "test-mail-servers.xml"));

        // get the mail server loaded from XML
        List smtpMailServers = sm.getSmtpMailServers();
        assertEquals(1, smtpMailServers.size());
        SMTPMailServer s = (SMTPMailServer) smtpMailServers.get(0);

        // update it
        s.setDefaultFrom("dave@atlassian");
        s.setHostname("testhostname");

        // update it in XMLMailServerManager
        Long id = s.getId();
        sm.update(s);

        // get it back from the XMLMailServerManager and check for changed attributes
        SMTPMailServer updatedServer = (SMTPMailServer) sm.getMailServer(id);
        assertEquals("dave@atlassian", updatedServer.getDefaultFrom());
        assertEquals("testhostname", updatedServer.getHostname());
    }

    public void testIdGenerationScheme() throws MailException
    {
        // load 2 servers from XML
        XMLMailServerManager sm = new XMLMailServerManager();
        sm.init(toMap("config-file", "test-mail-servers.xml"));

        // create a 3rd server
        SMTPMailServer server1 = createSMTPMailServer("a");
        assertEquals(3, sm.create(server1).longValue());

        // delete server 2
        sm.delete(new Long(2));

        // create a 4th server
        SMTPMailServer server4 = createSMTPMailServer("d");
        assertEquals(4, sm.create(server4).longValue());
    }

    private SMTPMailServer createSMTPMailServer(String name)
    {
        return new SMTPMailServerImpl(null, name, null, null, null, false, null, null, null);
    }

    private Map toMap(final String key, final Object value)
    {
        Map map = new HashMap();
        map.put(key,value);
        return map;
    }
}
