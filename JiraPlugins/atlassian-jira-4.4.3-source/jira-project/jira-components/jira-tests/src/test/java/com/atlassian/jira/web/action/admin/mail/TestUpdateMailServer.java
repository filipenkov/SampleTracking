/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.mail;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.mockobjects.servlet.MockHttpServletResponse;
import webwork.action.Action;

public class TestUpdateMailServer extends AbstractUsersIndexingTestCase
{
    public TestUpdateMailServer(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        MailFactory.setServerManager(null);
    }

    private UpdateMailServer createSecurityUnawareActionInstance()
    {
        return new UpdateMailServer(){
            @Override
            public boolean canManageSmtpMailServers()
            {
                return true;
            }

            @Override
            public boolean canManagePopMailServers()
            {
                return true;
            }
        };
    }

    public void testDoExecute1() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewMailServers.jspa");

        SMTPMailServer smtp = new SMTPMailServerImpl(null, "Test Name", "Test Description", "owen@fellows.com",
                "[OWEN]", false, "mail.atlassian.com", "fellowso", "pass");
        Long msId = MailFactory.getServerManager().create(smtp);

        UpdateMailServer ums = createSecurityUnawareActionInstance();
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

    public void testDoExecute2() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewMailServers.jspa");

        SMTPMailServer smtp = new SMTPMailServerImpl(null, "Test Name", "Test Description", "owen@fellows.com",
                "[OWEN]", false, "mail.atlassian.com", "fellowso", "pass");
        Long msId = MailFactory.getServerManager().create(smtp);

        UpdateMailServer updateMailServerAction = createSecurityUnawareActionInstance();
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
}
