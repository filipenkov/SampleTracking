/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.mail;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.mockobjects.servlet.MockHttpServletResponse;
import webwork.action.Action;

public class TestDeleteMailServer extends LegacyJiraMockTestCase
{
    public TestDeleteMailServer(String s)
    {
        super(s);
    }

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

    public void testDoExecute() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewMailServers.jspa");

        SMTPMailServer smtp = new SMTPMailServerImpl(null, "Test Name", "Test Description", "owen.fellows", "[OWEN]", false, "mail.atlassian.com", "fellowso", "pass");
        Long msId = MailFactory.getServerManager().create(smtp);
        assertNotNull(MailFactory.getServerManager().getMailServer(msId));

        DeleteMailServer dms = new DeleteMailServer(MailFactory.getServerManager()){
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
        dms.setId(msId);
        dms.setConfirmed(true);

        String result = dms.execute();
        assertEquals(Action.NONE, result);
        assertNull(MailFactory.getServerManager().getMailServer(msId));

        response.verify();
    }

}
