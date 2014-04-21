package com.atlassian.jira.mail;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.mail.MailProtocol;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.impl.SMTPMailServerImpl;
import com.atlassian.mail.MailException;
import com.atlassian.jira.ComponentManager;

/**
 * Test helper methods in MailUtils
 */
public class TestJiraMailUtils extends LegacyJiraMockTestCase
{

    public void testIsHasMailServer() throws MailException
    {
        assertFalse(JiraMailUtils.isHasMailServer());

        MailServerManager mailServerManager = ComponentAccessor.getMailServerManager();
        MailServer mailServer = new SMTPMailServerImpl(null, "test", "test", "andreask@atlassian.com", "[JIRA]", false, MailProtocol.SMTP,"localhost", "25",false, "user", "pass");
        mailServerManager.create(mailServer);

        assertTrue(JiraMailUtils.isHasMailServer());
    }

}
