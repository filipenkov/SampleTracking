/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.setup;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.mock.MockActionDispatcher;
import com.atlassian.core.user.GroupUtils;
import com.atlassian.core.user.UserUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.action.ActionNames;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServer;
import com.atlassian.mail.server.SMTPMailServer;
import com.opensymphony.user.User;
import webwork.action.Action;

public class TestSetup3 extends AbstractUsersIndexingTestCase
{
    private Setup3 s3;

    public TestSetup3(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        final UserUtil userUtil = (UserUtil) ComponentAccessor.getUserUtil();
        s3 = new Setup3(userUtil, null);
    }

    public void testGetSets()
    {
        assertNull(s3.getName());
        assertNull(s3.getDesc());
        assertNull(s3.getFrom());
        assertNull(s3.getJndiLocation());
        assertNull(s3.getServerName());
        assertNull(s3.getUsername());
        assertNull(s3.getPassword());
        assertNull(s3.getProtocol());

        s3.setName("Server Name");
        assertEquals("Server Name", s3.getName());

        s3.setDesc("Server Description");
        assertEquals("Server Description", s3.getDesc());

        s3.setFrom("test@atlassian.com");
        assertEquals("test@atlassian.com", s3.getFrom());

        s3.setServerName("Server");
        assertEquals("Server", s3.getServerName());

        s3.setJndiLocation("Session");
        assertEquals("Session", s3.getJndiLocation());

        s3.setUsername("Username");
        assertEquals("Username", s3.getUsername());

        s3.setPassword("Password");
        assertEquals("Password", s3.getPassword());

        s3.setProtocol("smtp");
        assertEquals("smtp", s3.getProtocol());
    }

    public void testDoDefault() throws Exception
    {
        s3.getApplicationProperties().setString(APKeys.JIRA_SETUP, "true");
        assertEquals("setupalready", s3.doDefault());

        User user = UserUtils.createUser("test user", "password", "test@atlassian.com", "test user fullname");
        user.addToGroup(GroupUtils.getGroupSafely(AbstractSetupAction.DEFAULT_GROUP_ADMINS));
        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, GroupUtils.getGroupSafely(AbstractSetupAction.DEFAULT_GROUP_ADMINS).getName());

        s3.getApplicationProperties().setString(APKeys.JIRA_SETUP, null);
        assertEquals(Action.INPUT, s3.doDefault());
        assertEquals("Default SMTP Server", s3.getName());
        assertEquals("This server is used for all outgoing mail.", s3.getDesc());
        assertEquals("test@atlassian.com", s3.getFrom());
    }

    public void testDoValidationName() throws Exception
    {
        setAllValidData();
        s3.setName(null);
        checkSingleError("name", "You must specify a server name.");
    }

    public void testDoValidationFrom() throws Exception
    {
        setAllValidData();
        s3.setFrom(null);
        checkSingleError("from", "You must specify a valid email address to send notifications from");

        s3.setFrom("abc");
        checkSingleError("from", "You must specify a valid email address to send notifications from");
    }

    public void testDoValidationNoServer() throws Exception
    {
        setAllValidData();
        s3.setServerName(null);
        s3.setJndiLocation(null);

        assertEquals(Action.INPUT, s3.execute());
        checkSingleElementCollection(s3.getErrorMessages(), "You must specify a mail session or SMTP server");
    }

    public void testDoValidationBothServerSession() throws Exception
    {
        setAllValidData();
        s3.setJndiLocation("test session");
        assertEquals(Action.INPUT, s3.execute());
        checkSingleElementCollection(s3.getErrorMessages(), "You can only specify a mail session OR a SMTP server");
    }

    public void testDoValidationServer() throws Exception
    {
        setAllValidData();
        s3.setServerName("a");

        assertEquals(Action.INPUT, s3.execute());
        checkSingleError("serverName", "The length of this server is too short to be valid");
    }

    public void testDoExecuteSuccessNoEmail() throws Exception
    {
        s3.setNoemail(true);
        assertEquals(Action.SUCCESS, s3.execute());
    }

    public void testDoExecuteSuccessServer() throws Exception
    {
        MailFactory.setServerManager(null);

        MockActionDispatcher mad = new MockActionDispatcher(false);
        CoreFactory.setActionDispatcher(mad);

        setAllValidData();
        assertEquals(Action.SUCCESS, s3.execute());

        MailServer ms = MailFactory.getServerManager().getMailServer("Server Name");
        assertNotNull(ms);
        assertEquals("Server Description", ms.getDescription());
        assertEquals("defaultfrom@atlassian.com", ((SMTPMailServer) ms).getDefaultFrom());
        assertEquals("mail.atlassian.com", ms.getHostname());
        assertEquals("Username", ms.getUsername());
        assertEquals("Password", ms.getPassword());

        assertTrue(mad.getActionsCalled().contains(ActionNames.LISTENER_CREATE));
    }

    public void testDoExecuteFail() throws Exception
    {
        MockActionDispatcher mad = new MockActionDispatcher(false);
        mad.setResult(Action.ERROR);
        CoreFactory.setActionDispatcher(mad);

        setAllValidData();
        assertEquals(Action.ERROR, s3.execute());
        checkSingleElementCollection(s3.getErrorMessages(), "Could not setup MailListener: com.atlassian.core.AtlassianCoreException: Error in action: null, result: error");
    }

    public void testDoExecuteFailSetupAlready() throws Exception
    {
        s3.getApplicationProperties().setString(APKeys.JIRA_SETUP, "true");
        assertEquals("setupalready", s3.execute());
    }

    private void checkSingleError(String element, String msg) throws Exception
    {
        assertEquals(Action.INPUT, s3.execute());
        assertEquals(msg, s3.getErrors().get(element));
        assertEquals(1, s3.getErrors().size());
    }

    private void setAllValidData()
    {
        s3.setName("Server Name");
        s3.setDesc("Server Description");
        s3.setFrom("defaultfrom@atlassian.com");
        s3.setServerName("mail.atlassian.com");
        s3.setUsername("Username");
        s3.setPassword("Password");
        s3.setProtocol("smtp");
    }
}
