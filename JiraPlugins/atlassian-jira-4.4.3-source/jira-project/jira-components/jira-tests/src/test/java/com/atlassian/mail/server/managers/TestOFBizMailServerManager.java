package com.atlassian.mail.server.managers;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.PopMailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.PopMailServerImpl;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import java.util.HashMap;
import java.util.Map;

public class TestOFBizMailServerManager extends LegacyJiraMockTestCase
{
    private MockMailServerManager mockMailServerManager;

    protected void setUp() throws Exception
    {
        super.setUp();
        mockMailServerManager = new MockMailServerManager();
    }

   public void testCreateSMTPAndGet() throws Exception
    {
        MailServer localMailServer = new SMTPMailServerImpl(1L,"Name","Description","owen@atlassian.com","[OWEN]",false,"mail.atlassian.com","owen","fellows");
        Long id = mockMailServerManager.create(localMailServer);
        assertNotNull(id);
        MailServer mailServer = mockMailServerManager.getMailServer(id);
        assertEquals(localMailServer, mailServer);
    }

    public void testUpdateSMTP() throws Exception
    {
        Long id = mockMailServerManager.create(new SMTPMailServerImpl(1L,"Name","Description","owen@atlassian.com","[OWEN]",false,"mail.atlassian.com","owen","fellows"));
        MailServer mailServer = mockMailServerManager.getMailServer("Name");
        if (OFBizMailServerManager.SERVER_TYPES[1].equals(mailServer.getType()))
        {
            SMTPMailServer smtp = (SMTPMailServer)mailServer;
            smtp.setUsername(null);
            smtp.setPassword(null);
            smtp.setName("new name");
            mockMailServerManager.update(smtp);
            MailServer updatedMailServer = mockMailServerManager.getMailServer(id);
            assertNull(updatedMailServer.getUsername());
            assertNull(updatedMailServer.getPassword());
            assertEquals("new name", updatedMailServer.getName());
            assertEquals(mailServer, updatedMailServer);
        }
        else
            fail("Mail Server returned is not an SMTP server");
    }

    public void testDeleteSMTP() throws Exception
    {
        Long id = mockMailServerManager.create(new SMTPMailServerImpl(1L,"Name","Description","owen@atlassian.com","[OWEN]",false,"mail.atlassian.com","owen","fellows"));
        MailServer mailServer = mockMailServerManager.getMailServer(id);
        mockMailServerManager.delete(mailServer.getId());
        assertNull(mockMailServerManager.getMailServer(id));
    }

    public void testGetMailServerGV() throws Exception
    {
        Map params = UtilMisc.toMap("id", 1L);
        params.put("name","Name");
        params.put("description","Description");
        params.put("from","owen@atlassian.com");
        params.put("prefix","[OWEN]");
        params.put("servername","mail.atlassian.com");
        params.put("smtpPort","25");
        params.put("protocol", MailProtocol.SMTP.getProtocol());
        params.put("username","owen");
        params.put("password","fellows");
        params.put("type",OFBizMailServerManager.SERVER_TYPES[1]);
        params.put("istlsrequired","false");
        params.put("timeout",5000L);
        GenericValue gv = CoreFactory.getGenericDelegator().makeValue("MailServer", params);
        Long id = mockMailServerManager.create(new SMTPMailServerImpl(1L,"Name","Description","owen@atlassian.com","[OWEN]",false,MailProtocol.SMTP,"mail.atlassian.com","25",false,"owen","fellows",5000));
        GenericValue mailServerGV = mockMailServerManager.getMailServerGV(id);
        assertEquals(gv, mailServerGV);
    }

    public void testConstructMailServer1()
    {
        MailServer oldMailServer = new SMTPMailServerImpl(1L,"Name","Description","owen@atlassian.com","[OWEN]",false,"mail.atlassian.com","owen","fellows");
        Map params = UtilMisc.toMap("id", 1L);
        params.put("name","Name");
        params.put("description","Description");
        params.put("from","owen@atlassian.com");
        params.put("prefix","[OWEN]");
        params.put("servername","mail.atlassian.com");
        params.put("username","owen");
        params.put("password","fellows");
        params.put("smtpPort","25");
        params.put("protocol","smtp");
        params.put("istlsrequired","false");
        params.put("type",OFBizMailServerManager.SERVER_TYPES[1]);
        GenericValue gv = CoreFactory.getGenericDelegator().makeValue("MailServer", params);
        MailServer newMailServer = mockMailServerManager.constructMailServer(gv);
        assertEquals(oldMailServer, newMailServer);
    }

    public void testConstructMailServer2()
    {
        MailServer oldMailServer = new SMTPMailServerImpl(1L,"Name","Description","owen@atlassian.com","[OWEN]",true,"mail.atlassian.com",null,null);
        Map params = UtilMisc.toMap("id", 1L);
        params.put("name","Name");
        params.put("description","Description");
        params.put("from","owen@atlassian.com");
        params.put("prefix","[OWEN]");
        params.put("jndilocation","mail.atlassian.com");
        params.put("smtpPort","25");
        params.put("protocol","smtp");
        params.put("username",null);
        params.put("password",null);
        params.put("type",OFBizMailServerManager.SERVER_TYPES[1]);
        GenericValue gv = CoreFactory.getGenericDelegator().makeValue("MailServer", params);
        MailServer newMailServer = mockMailServerManager.constructMailServer(gv);
        assertEquals(oldMailServer, newMailServer);
    }

    public void testConstructMailServer3()
    {
        Map params = UtilMisc.toMap("id", 1L);
        params.put("name","Name");
        params.put("description","Description");
        params.put("servername","mail.atlassian.com");
        params.put("username","owen");
        params.put("password","fellows");
        params.put("smtpPort","110");
        params.put("protocol","pop");
        params.put("type",OFBizMailServerManager.SERVER_TYPES[0]);
        GenericValue gv = CoreFactory.getGenericDelegator().makeValue("MailServer", params);
        MailServer newMailServer = mockMailServerManager.constructMailServer(gv);
        PopMailServer expectedResult = new PopMailServerImpl(1L, "Name", "Description", "mail.atlassian.com", "owen", "fellows");
        assertEquals(expectedResult, newMailServer);
    }

    public void testConstructMailServer4()
    {
        Map params = UtilMisc.toMap("type","notype");
        GenericValue gv = CoreFactory.getGenericDelegator().makeValue("MailServer", params);
        MailServer newMailServer = mockMailServerManager.constructMailServer(gv);
        assertEquals(null, newMailServer);
    }

    public void testGetMapFromColumns1() throws Exception
    {
        MailServer oldMailServer = new SMTPMailServerImpl(1L,"Name","Description","owen@atlassian.com","[OWEN]",false,"mail.atlassian.com","owen","fellows");
        Map oldMap = mockMailServerManager.getMapFromColumns(oldMailServer);
        Map params = UtilMisc.toMap("name","Name");
        params.put("description","Description");
        params.put("from","owen@atlassian.com");
        params.put("prefix","[OWEN]");
        params.put("servername","mail.atlassian.com");
        params.put("username","owen");
        params.put("password","fellows");
        params.put("smtpPort","25");
        params.put("protocol",MailProtocol.SMTP.getProtocol());
        params.put("type",OFBizMailServerManager.SERVER_TYPES[1]);
        params.put("istlsrequired","false");
        params.put("timeout",10000L);
        assertEquals(oldMap, params);
    }

    public void testGetMapFromColumns2() throws Exception
    {
        MailServer oldMailServer = new SMTPMailServerImpl(1L,"Name","Description","owen@atlassian.com","[OWEN]",true,"mail.atlassian.com",null,null);
        Map oldMap = mockMailServerManager.getMapFromColumns(oldMailServer);
        Map params = new HashMap();
        params.put("name","Name");
        params.put("description","Description");
        params.put("from","owen@atlassian.com");
        params.put("prefix","[OWEN]");
        params.put("servername", null);
        params.put("jndilocation", "mail.atlassian.com");
        params.put("username",null);
        params.put("password",null);
        params.put("smtpPort","25");
        params.put("protocol",MailProtocol.SMTP.getProtocol());
        params.put("type",OFBizMailServerManager.SERVER_TYPES[1]);
        params.put("istlsrequired","false");
        params.put("timeout",10000L);
        assertEquals(oldMap, params);
    }

    public void testGetMapFromColumns3() throws Exception
    {
        MailServer oldMailServer = new PopMailServerImpl(1L,"Name","Description","mail.atlassian.com","owen","fellows");
        Map oldMap = mockMailServerManager.getMapFromColumns(oldMailServer);
        Map params = UtilMisc.toMap("name","Name");
        params.put("description","Description");
        params.put("username","owen");
        params.put("password","fellows");
        params.put("smtpPort","110");
        params.put("protocol", MailProtocol.POP.getProtocol());
        params.put("type",OFBizMailServerManager.SERVER_TYPES[0]);
        params.put("servername","mail.atlassian.com");
        params.put("timeout",10000L);
        assertEquals(oldMap, params);
    }

    public void testGetDefaultSMTPMailServer() throws Exception
    {
        SMTPMailServer server1 = new SMTPMailServerImpl(1L, "Smtp Server Name1", "Description1", "test1@atlassian.com", "[OWEN]", false, "mail1.atlassian.com", "owen", "fellows");
        SMTPMailServer server2 = new SMTPMailServerImpl(2L, "Smtp Server Name", "Description2", "test2@atlassian.com", "[OWEN]", false, "mail2.atlassian.com", "owen", "fellows");
        mockMailServerManager.create(server1);
        mockMailServerManager.create(server2);

        // get it from the prefereneces-default.xml file
        assertEquals(server2, mockMailServerManager.getDefaultSMTPMailServer());
        assertEquals(server1, mockMailServerManager.getSmtpMailServers().get(0));

        // now get if from the first smtp server in servers
        mockMailServerManager.delete(2L);
        assertEquals(server1, mockMailServerManager.getDefaultSMTPMailServer());
        assertEquals(server1, mockMailServerManager.getSmtpMailServers().get(0));

        // cannot get any smtp servers, return null
        mockMailServerManager.delete(1L);
        assertNull(mockMailServerManager.getDefaultSMTPMailServer());
    }

    public void testGetDefaultPopMailServer() throws Exception
    {
        PopMailServer server1 = new PopMailServerImpl(1L, "Pop Server Name1", "Description1", "mail1.atlassian.com", "ownen", "fellows");
        PopMailServer server2 = new PopMailServerImpl(2L, "Pop Server Name", "Description2", "mail2.atlassian.com", "ownen", "fellows");
        mockMailServerManager.create(server1);
        mockMailServerManager.create(server2);

        assertEquals(server2, mockMailServerManager.getDefaultPopMailServer());
        assertEquals(server1, mockMailServerManager.getPopMailServers().get(0));

        mockMailServerManager.delete(2L);
        assertEquals(server1, mockMailServerManager.getDefaultPopMailServer());
        assertEquals(server1, mockMailServerManager.getPopMailServers().get(0));

        mockMailServerManager.delete(1L);
        assertNull(mockMailServerManager.getDefaultPopMailServer());
    }
}