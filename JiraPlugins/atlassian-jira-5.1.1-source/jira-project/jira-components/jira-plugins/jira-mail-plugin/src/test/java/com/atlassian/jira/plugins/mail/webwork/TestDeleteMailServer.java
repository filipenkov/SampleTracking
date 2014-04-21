/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.PopMailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.PopMailServerImpl;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import webwork.action.Action;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestDeleteMailServer extends AbstractMailServerTest
{
    @Test
    public void testDoValidation1() throws Exception
    {
        SMTPMailServer smtp = new SMTPMailServerImpl(null, "Test Name", "Test Description", "owen.fellows", "[OWEN]", false, "mail.atlassian.com", "fellowso", "pass");
        final MailServerManager serverManager = MailFactory.getServerManager();
        Long msId = serverManager.create(smtp);

        assertNotNull(MailFactory.getServerManager().getMailServer(msId));

        DeleteMailServer dms = new DeleteMailServer(MailFactory.getServerManager());
        dms.setId(msId);

        String result = dms.execute();
        assertEquals(Action.INPUT, result);
        assertNotNull(MailFactory.getServerManager().getMailServer(msId));
    }

    @Test
    public void testDeleteSmtpServer() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("OutgoingMailServers.jspa");

        SMTPMailServer smtp = new SMTPMailServerImpl(null, "Test Name", "Test Description", "owen.fellows", "[OWEN]", false, "mail.atlassian.com", "fellowso", "pass");
        Long msId = MailFactory.getServerManager().create(smtp);
        assertNotNull(MailFactory.getServerManager().getMailServer(msId));

        DeleteMailServer dms = new DeleteMailServer(MailFactory.getServerManager());
        dms.setId(msId);
        dms.setConfirmed(true);

        String result = dms.execute();
        assertEquals(Action.NONE, result);
        assertNull(MailFactory.getServerManager().getMailServer(msId));

        response.verify();
    }

    @Test
    public void testDeletePopServer() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("IncomingMailServers.jspa");

        PopMailServer pop = new PopMailServerImpl(null, "Test Name", "Test Description", MailProtocol.SECURE_POP,
                "pop.server.name", "999", "popuser", "poppass");
        Long msId = MailFactory.getServerManager().create(pop);

        assertNotNull(MailFactory.getServerManager().getMailServer(msId));

        DeleteMailServer dms = new DeleteMailServer(MailFactory.getServerManager());
        dms.setId(msId);
        dms.setConfirmed(true);

        String result = dms.execute();
        assertEquals(Action.NONE, result);
        assertNull(MailFactory.getServerManager().getMailServer(msId));

        response.verify();
    }

}
