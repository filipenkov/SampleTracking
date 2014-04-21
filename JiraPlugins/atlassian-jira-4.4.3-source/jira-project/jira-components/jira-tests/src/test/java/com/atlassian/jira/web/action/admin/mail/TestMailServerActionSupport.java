/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.mail;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.mockobjects.servlet.MockHttpServletResponse;
import webwork.action.Action;

public class TestMailServerActionSupport extends AbstractWebworkTestCase
{
    public TestMailServerActionSupport(String s)
    {
        super(s);
    }

    private MailServerActionSupport createSecurityUnawareActionInstance()
    {
        return new MailServerActionSupport(){
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

    public void testGetsSets()
    {
        MailServerActionSupport ams = new MailServerActionSupport();
        assertNull(ams.getName());
        ams.setName("This Server");
        assertEquals("This Server", ams.getName());
        assertNull(ams.getDescription());
        ams.setDescription("This Desc");
        assertEquals("This Desc", ams.getDescription());
        assertNull(ams.getType());
        ams.setType(ams.getTypes()[0]);
        assertEquals(ams.getTypes()[0], ams.getType());
        assertEquals(MailServerManager.SERVER_TYPES[0], ams.getTypes()[0]);
        assertEquals(MailServerManager.SERVER_TYPES[1], ams.getTypes()[1]);
        ams.setType("none");
        assertNull(ams.getType());
        assertNull(ams.getServerName());
        ams.setServerName("This Server Name");
        assertEquals("This Server Name", ams.getServerName());
        assertNull(ams.getJndiLocation());
        ams.setJndiLocation("This JNDI");
        assertEquals("This JNDI", ams.getJndiLocation());
        ams.setJndiLocation("   This JNDI with whitespace \t ");
        assertEquals("This JNDI with whitespace", ams.getJndiLocation());
        assertNull(ams.getUsername());
        ams.setUsername("This Username");
        assertEquals("This Username", ams.getUsername());
        assertNull(ams.getPassword());
        ams.setPassword("This Password");
        assertEquals(ams.getPassword(), "This Password");
    }

    public void testDoDefault1() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewMailServers.jspa");

        MailServerActionSupport ums = new MailServerActionSupport();
        String result = ums.doDefault();
        assertEquals(Action.NONE, result);

        response.verify();
    }

    public void testDoDefault2() throws Exception
    {
        SMTPMailServer smtp = new SMTPMailServerImpl(null, "Test Name", "Test Description", "owen.fellows", "[OWEN]", false, "mail.atlassian.com", "fellowso", "pass");
        Long mailServerId = MailFactory.getServerManager().create(smtp);

        MailServerActionSupport ums = createSecurityUnawareActionInstance();
        ums.setId(mailServerId);

        String result = ums.doDefault();
        assertEquals(Action.INPUT, result);

        assertEquals("Test Name", ums.getName());
        assertEquals("Test Description", ums.getDescription());
        assertEquals("owen.fellows", ums.getFrom());
        assertEquals("[OWEN]", ums.getPrefix());
        assertEquals("mail.atlassian.com", ums.getServerName());
        assertEquals("fellowso", ums.getUsername());
        assertEquals("pass", ums.getPassword());
    }

    public void testDoDefault3() throws Exception
    {
        SMTPMailServer smtp = new SMTPMailServerImpl(null, "Test Name", "Test Description", "owen.fellows", "[OWEN]", true, "smtp.atlassian.com", null, null);
        Long mailServerId = MailFactory.getServerManager().create(smtp);

        MailServerActionSupport ums = createSecurityUnawareActionInstance();
        ums.setId(mailServerId);

        String result = ums.doDefault();
        assertEquals(Action.INPUT, result);

        assertEquals("Test Name", ums.getName());
        assertEquals("Test Description", ums.getDescription());
        assertEquals("owen.fellows", ums.getFrom());
        assertEquals("[OWEN]", ums.getPrefix());
        assertNull(ums.getServerName());
        assertEquals("smtp.atlassian.com", ums.getJndiLocation());
        assertNull(ums.getUsername());
        assertNull(ums.getPassword());
    }

    public void testDoValidation1() throws Exception
    {
        MailServerActionSupport ams = new MailServerActionSupport();
        String result = ams.execute();
        assertEquals(Action.INPUT, result);
        assertEquals(1, ams.getErrors().size());
        assertEquals(1, ams.getErrorMessages().size());
        assertEquals("You must specify the name of this Mail Server.", ams.getErrors().get("name"));
        assertTrue(ams.getErrorMessages().contains("You must specify the server type."));
    }

    public void testDoValidation2() throws Exception
    {
        MailServerActionSupport ams = new MailServerActionSupport();
        ams.setName("Test Name");
        ams.setType(ams.getTypes()[0]);

        String result = ams.execute();
        assertEquals(Action.INPUT, result);
        assertEquals(3, ams.getErrors().size());
        assertEquals("You must specify the location of the server.", ams.getErrors().get("serverName"));
        assertEquals("You must specify a username.", ams.getErrors().get("username"));
        assertEquals("You must specify a password.", ams.getErrors().get("password"));
    }

    public void testDoValidation3() throws Exception
    {
        MailServerActionSupport ams = new MailServerActionSupport();
        ams.setName("Test Name");
        ams.setType(ams.getTypes()[1]);

        String result = ams.execute();
        assertEquals(Action.INPUT, result);
        assertEquals(2, ams.getErrors().size());
        assertEquals(1, ams.getErrorMessages().size());
        assertEquals("You must specify a host name or a JNDI location.", ams.getErrorMessages().iterator().next());
        assertEquals("You must specify a valid from address.", ams.getErrors().get("from"));
        assertEquals("You must specify an email prefix.", ams.getErrors().get("prefix"));
    }

    public void testDoValidation4() throws Exception
    {
        MailServerActionSupport ams = new MailServerActionSupport();
        ams.setName("Test Name");
        ams.setType(ams.getTypes()[1]);
        ams.setFrom("From Address");
        ams.setPrefix("[OWEN]");
        ams.setJndiLocation("foo");
        ams.setServerName("bar");

        String result = ams.execute();
        assertEquals(Action.INPUT, result);
        assertEquals(1, ams.getErrorMessages().size());
        assertEquals("You cannot specify both a host name and a JNDI location.", ams.getErrorMessages().iterator().next());
    }

    public void testDoValidation5() throws Exception
    {
        MailServerActionSupport ams = new MailServerActionSupport();
        ams.setName("Test Name");
        ams.setType(ams.getTypes()[1]);
        ams.setFrom("test@atlassian.com");
        ams.setPrefix("[OWEN]");
        ams.setServerName("bar");
        ams.setUsername("ausername");

        String result = ams.execute();
        assertEquals(Action.INPUT, result);
        assertEquals(1, ams.getErrors().size());
        assertEquals("You must specify a password for username entered", ams.getErrors().get("password"));
    }
}
