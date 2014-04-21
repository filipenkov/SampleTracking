/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.mail.MailFactory;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.PopMailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.PopMailServerImpl;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.junit.Test;
import webwork.action.Action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestUpdateMailServer extends AbstractMailServerTest
{
    @Test
    public void testDoExecute1() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("OutgoingMailServers.jspa");

        SMTPMailServer smtp = new SMTPMailServerImpl(null, "Test Name", "Test Description", "owen@fellows.com",
                "[OWEN]", false, "mail.atlassian.com", "fellowso", "pass");
        Long msId = MailFactory.getServerManager().create(smtp);

        UpdateMailServer ums = new UpdateSmtpMailServer();
        ums.setId(msId);
        ums.doDefault();
        assertEquals("Test Name", ums.getName());
        assertEquals("Test Description", ums.getDescription());
        assertEquals("owen@fellows.com", ums.getFrom());
        assertEquals("[OWEN]", ums.getPrefix());
        assertEquals("mail.atlassian.com", ums.getServerName());
        assertEquals("fellowso", ums.getUsername());
        assertEquals("pass", ums.getPassword());

        ums.setName("Another Name");

        String result = ums.execute();
        assertEquals(Action.NONE, result);

        MailServer newServer = MailFactory.getServerManager().getMailServer(msId);
        assertEquals("Another Name", newServer.getName());
        response.verify();
    }

    @Test
    public void testDoExecute2() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("OutgoingMailServers.jspa");

        SMTPMailServer smtp = new SMTPMailServerImpl(null, "Test Name", "Test Description", "owen@fellows.com",
                "[OWEN]", false, "mail.atlassian.com", "fellowso", "pass");
        Long msId = MailFactory.getServerManager().create(smtp);

        UpdateMailServer updateMailServerAction = new UpdateSmtpMailServer();
        updateMailServerAction.setId(msId);
        updateMailServerAction.doDefault();
        assertEquals("Test Name", updateMailServerAction.getName());
        assertEquals("Test Description", updateMailServerAction.getDescription());
        assertEquals("owen@fellows.com", updateMailServerAction.getFrom());
        assertEquals("[OWEN]", updateMailServerAction.getPrefix());
        assertEquals("mail.atlassian.com", updateMailServerAction.getServerName());
        assertEquals("fellowso", updateMailServerAction.getUsername());
        assertEquals("pass", updateMailServerAction.getPassword());

        updateMailServerAction.setServerName(null);
        updateMailServerAction.setUsername(null);
        updateMailServerAction.setChangePassword(true);
        updateMailServerAction.setPassword(null);
        updateMailServerAction.setJndiLocation("JNDI");
        updateMailServerAction.setType(MailServerManager.SERVER_TYPES[1]);

        String result = updateMailServerAction.execute();
        assertEquals(Action.NONE, result);

        SMTPMailServer newServer = (SMTPMailServer) MailFactory.getServerManager().getMailServer(msId);
        assertNull(newServer.getHostname());
        assertNull(newServer.getUsername());
        assertNull(newServer.getPassword());
        assertEquals("JNDI", newServer.getJndiLocation());
        response.verify();
    }

    @Test
    public void testChanginngUsernameDoesNotLeakPassword() throws Exception
    {
        SMTPMailServer smtp = new SMTPMailServerImpl(null, "Test Name", "Test Description", "owen@fellows.com",
                "[OWEN]", false, "mail.atlassian.com", "fellowso", "pass");
        Long msId = MailFactory.getServerManager().create(smtp);

        UpdateMailServer updateMailServerAction = new UpdateSmtpMailServer();
        updateMailServerAction.setId(msId);
        updateMailServerAction.doDefault(); // this fills all fields

        updateMailServerAction.setUsername("changedUsername");
        // this is a forgery - UI does not allow changing username without changing password
        updateMailServerAction.setChangePassword(false);
        updateMailServerAction.setPassword(null);
        updateMailServerAction.setType(MailServerManager.SERVER_TYPES[1]);

        String result = updateMailServerAction.execute();
        assertEquals(Action.INPUT, result);
        assertNull(updateMailServerAction.getPassword());
        assertTrue(updateMailServerAction.isChangePassword());
    }

    @Test
    public void testUpdatingPopServer() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("IncomingMailServers.jspa");

        PopMailServer pop = new PopMailServerImpl(null, "Test Name", "Test Description", MailProtocol.SECURE_POP,
                "pop.server.name", "999", "popuser", "poppass");
        Long msId = MailFactory.getServerManager().create(pop);

        UpdateMailServer ums = new UpdatePopMailServer();
        ums.setId(msId);
        ums.doDefault();
        assertEquals("Test Name", ums.getName());
        assertEquals("Test Description", ums.getDescription());
        assertEquals(MailProtocol.SECURE_POP.getProtocol(), ums.getProtocol());
        assertEquals("pop.server.name", ums.getServerName());
        assertEquals("999", ums.getPort());
        assertEquals("popuser", ums.getUsername());
        assertEquals("poppass", ums.getPassword());

        ums.setName("Another Name");
        ums.setDescription("Another Description");
        ums.setProtocol(MailProtocol.POP.getProtocol());
        ums.setServerName("new.pop.server");
        ums.setPort("888");

        String result = ums.execute();
        assertEquals(Action.NONE, result);

        MailServer newServer = MailFactory.getServerManager().getMailServer(msId);
        assertEquals("Another Name", newServer.getName());
        assertEquals("Another Description", newServer.getDescription());
        assertEquals(MailProtocol.POP, newServer.getMailProtocol());
        assertEquals("new.pop.server", newServer.getHostname());
        assertEquals("888", newServer.getPort());

        response.verify();
    }
}
