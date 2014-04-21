/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.notification;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.jira.web.action.admin.notification.AddScheme;
import com.mockobjects.servlet.MockHttpServletResponse;
import webwork.action.Action;

public class TestAddScheme extends AbstractWebworkTestCase
{
    public TestAddScheme(String s)
    {
        super(s);
    }

    public void testGetsSets()
    {
        AddScheme as = new AddScheme();
        assertNull(as.getName());
        as.setName("Scheme");
        assertEquals("Scheme", as.getName());
    }

    public void testValidation() throws Exception
    {
        AddScheme as = new AddScheme();
        String result = as.execute();
        assertEquals(Action.INPUT, result);
        assertEquals(1, as.getErrors().size());
        assertEquals("Please specify a name for this Scheme.", as.getErrors().get("name"));
    }

    public void testExecute() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("EditNotifications!default.jspa?schemeId=1");

        AddScheme as = new AddScheme();
        as.setName("This scheme");

        String result = as.execute();

        assertEquals(Action.NONE, result);
        ManagerFactory.getNotificationSchemeManager().schemeExists("This scheme");
        response.verify();
    }
}
