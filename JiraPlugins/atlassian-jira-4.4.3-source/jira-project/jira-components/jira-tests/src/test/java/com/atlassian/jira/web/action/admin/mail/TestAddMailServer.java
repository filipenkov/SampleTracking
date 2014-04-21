/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.mail;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServer;
import com.mockobjects.servlet.MockHttpServletResponse;
import webwork.action.Action;

public class TestAddMailServer extends AbstractUsersIndexingTestCase
{
    public TestAddMailServer(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        MailFactory.setServerManager(null);
    }

    private AddMailServer createSecurityUnawareActionInstance()
    {
        return new AddMailServer()
        {
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

    public void testDoValidation() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewMailServers.jspa");

        AddMailServer ams = createSecurityUnawareActionInstance();
        ams.setName("Test Name");
        ams.setType(ams.getTypes()[0]);
        ams.setServerName("This Server");
        ams.setUsername("fellowso");
        ams.setPassword("pass");
        ams.setProtocol("pop3");

        String result = ams.execute();

        response.verify();
        assertEquals(Action.NONE, result);

        response = JiraTestUtil.setupExpectedRedirect("ViewMailServers.jspa");

        ams = createSecurityUnawareActionInstance();
        ams.setName("Test Name");
        ams.setType(ams.getTypes()[0]);
        ams.setServerName("This Server");
        ams.setUsername("fellowso");
        ams.setPassword("pass");
        ams.setProtocol("pop3");
        result = ams.execute();
        assertEquals(Action.INPUT, result);
        assertEquals("A Mail Server with this name already exists.", ams.getErrors().get("name"));
    }

    public void testDoExecute() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewMailServers.jspa");

        AddMailServer ams = createSecurityUnawareActionInstance();
        ams.setName("Test Name");
        ams.setType(ams.getTypes()[0]);
        ams.setServerName("This Server");
        ams.setUsername("fellowso");
        ams.setPassword("pass");
        ams.setProtocol("pop3");
        String result = ams.execute();
        assertEquals(Action.NONE, result);

        MailServer mailServer = MailFactory.getServerManager().getMailServer("Test Name");
        assertNotNull(mailServer);
        assertEquals("Test Name", mailServer.getName());
        assertEquals(ams.getTypes()[0], mailServer.getType());
        assertEquals("This Server", ams.getServerName());
        assertEquals("fellowso", ams.getUsername());
        assertEquals("pass", ams.getPassword());


        response.verify();
    }

    public void testDoExecute2() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewMailServers.jspa");

        AddMailServer ams = createSecurityUnawareActionInstance();
        ams.setName("Test Name");
        ams.setType(ams.getTypes()[1]);
        ams.setServerName("This Server");
        ams.setFrom("from@address.com");
        ams.setPrefix("[OWEN]");
        ams.setProtocol("smtp");
        String result = ams.execute();
        assertEquals(Action.NONE, result);

        MailServer mailServer = MailFactory.getServerManager().getMailServer("Test Name");
        assertNotNull(mailServer);
        assertEquals("Test Name", mailServer.getName());
        assertEquals(ams.getTypes()[1], mailServer.getType());
        assertEquals("This Server", ams.getServerName());
        assertEquals(null, ams.getUsername());
        assertEquals(null, ams.getPassword());
        assertEquals("from@address.com", ams.getFrom());
        assertEquals("[OWEN]", ams.getPrefix());

        response.verify();
    }

    public void testDoExecute3() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewMailServers.jspa");

        AddMailServer ams = createSecurityUnawareActionInstance();
        ams.setName("Test Name");
        ams.setType(ams.getTypes()[1]);
        ams.setJndiLocation("This Server");
        ams.setFrom("from@address.com");
        ams.setPrefix("[OWEN]");
        ams.setProtocol("smtp");

        String result = ams.execute();
        assertEquals(Action.NONE, result);

        MailServer mailServer = MailFactory.getServerManager().getMailServer("Test Name");
        assertNotNull(mailServer);
        assertEquals("Test Name", mailServer.getName());
        assertEquals(ams.getTypes()[1], mailServer.getType());
        assertEquals("This Server", ams.getJndiLocation());
        assertEquals("from@address.com", ams.getFrom());
        assertEquals("[OWEN]", ams.getPrefix());

        response.verify();
    }
}
