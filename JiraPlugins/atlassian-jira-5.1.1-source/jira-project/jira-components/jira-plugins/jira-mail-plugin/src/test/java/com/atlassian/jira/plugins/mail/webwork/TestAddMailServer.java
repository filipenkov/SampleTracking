/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.mock.MockFeatureManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServer;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import webwork.action.Action;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestAddMailServer extends AbstractMailServerTest
{
    @Test
    public void testDoValidation() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("IncomingMailServers.jspa");

        AddMailServer ams = new AddPopMailServer();
        ams.setName("Test Name");
        ams.setType(ams.getTypes()[0]);
        ams.setServerName("This Server");
        ams.setUsername("fellowso");
        ams.setPassword("pass");
        ams.setChangePassword(true);
        ams.setProtocol("pop3");

        String result = ams.execute();

        response.verify();
        assertEquals(Action.NONE, result);
        assertNotNull(serverManager.getMailServer("Test Name"));

        response = JiraTestUtil.setupExpectedRedirect("IncomingMailServers.jspa");

        ams = new AddPopMailServer();
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

    @Test
    public void testDoExecuteStoresPopServer() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("IncomingMailServers.jspa");

        AddMailServer ams = new AddPopMailServer();
        ams.setName("Test Name");
        ams.setType(ams.getTypes()[0]);
        ams.setServerName("This Server");
        ams.setUsername("fellowso");
        ams.setPassword("pass");
        ams.setChangePassword(true);
        ams.setProtocol("pop3");
        String result = ams.execute();
        assertEquals(Action.NONE, result);

        MailServer mailServer = MailFactory.getServerManager().getMailServer("Test Name");
        assertNotNull(mailServer);
        assertEquals("Test Name", mailServer.getName());
        assertEquals(ams.getTypes()[0], mailServer.getType());
        assertEquals("This Server", mailServer.getHostname());
        assertEquals("fellowso", mailServer.getUsername());
        assertEquals("pass", mailServer.getPassword());
        assertNull(mailServer.getSocksHost());
        assertNull(mailServer.getSocksPort());

        response.verify();
    }

    @Test
    public void testPopSocksConfigurationSetInOnDemand() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("IncomingMailServers.jspa");
        mockFeatureManager.enable(CoreFeatures.ON_DEMAND);

        Properties systemProperties = System.getProperties();
        systemProperties.setProperty("studio.socks.host", "socks");
        systemProperties.setProperty("studio.socks.port", "1080");

        AddMailServer ams = new AddPopMailServer();
        ams.setName("Test Name");
        ams.setType(ams.getTypes()[0]);
        ams.setServerName("This Server");
        ams.setUsername("fellowso");
        ams.setPassword("pass");
        ams.setChangePassword(true);
        ams.setProtocol("pop3");
        String result = ams.execute();
        assertEquals(Action.NONE, result);

        MailServer mailServer = MailFactory.getServerManager().getMailServer("Test Name");
        assertNotNull(mailServer);
        assertEquals("Test Name", mailServer.getName());
        assertEquals(ams.getTypes()[0], mailServer.getType());
        assertEquals("This Server", mailServer.getHostname());
        assertEquals("fellowso", mailServer.getUsername());
        assertEquals("pass", mailServer.getPassword());
        assertEquals("socks", mailServer.getSocksHost());
        assertEquals("1080", mailServer.getSocksPort());

        response.verify();
    }

    @Test
    public void testDoExecuteStoresSmtpServer() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("OutgoingMailServers.jspa");

        AddMailServer ams = new AddSmtpMailServer();
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
        assertNull(mailServer.getSocksHost());
        assertNull(mailServer.getSocksPort());

        response.verify();
    }

    @Test
    public void testSmtpSocksConfigurationSetInOnDemand() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("OutgoingMailServers.jspa");

        mockFeatureManager.enable(CoreFeatures.ON_DEMAND);
        Properties systemProperties = System.getProperties();
        systemProperties.setProperty("studio.socks.host", "socks");
        systemProperties.setProperty("studio.socks.port", "1080");

        AddMailServer ams = new AddSmtpMailServer();
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
        assertEquals("socks", mailServer.getSocksHost());
        assertEquals("1080", mailServer.getSocksPort());

        response.verify();
    }

    @Test
    public void testDoExecuteStoresJndiLocation() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("OutgoingMailServers.jspa");

        AddMailServer ams = new AddSmtpMailServer();
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
        assertNull(mailServer.getSocksHost());
        assertNull(mailServer.getSocksPort());

        response.verify();
    }
}
