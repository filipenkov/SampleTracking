/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.mockobjects.servlet.MockHttpServletResponse;
import webwork.action.Action;

public class TestAddSchemeAction extends AbstractWebworkTestCase
{
    SchemeManager permSchemeManager;
    SchemeManager notificationSchemeManager;

    public TestAddSchemeAction(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        notificationSchemeManager = ManagerFactory.getNotificationSchemeManager();
        permSchemeManager = ManagerFactory.getPermissionSchemeManager();
    }

    public void testAddPermissionScheme() throws Exception
    {
        //use a mock servlet response
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewPermissionSchemes!default.jspa?schemeId=1");

        com.atlassian.jira.web.action.admin.permission.AddScheme addScheme = new com.atlassian.jira.web.action.admin.permission.AddScheme();

        //set the scheme details
        addScheme.setName("test scheme");
        addScheme.setDescription("test scheme");

        //The scheme manager should be set correctly
        assertEquals(addScheme.getSchemeManager(), permSchemeManager);

        assertTrue(permSchemeManager.getSchemes().size() == 0);

        //add the scheme
        String result = addScheme.execute();

        //the new scheme should be there
        assertTrue(permSchemeManager.getSchemes().size() == 1);

        //there should be no errors
        assertEquals(0, addScheme.getErrors().size());

        assertEquals(Action.NONE, result);

        response.verify();
    }

    public void testAddNotificationScheme() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("EditNotifications!default.jspa?schemeId=1");

        com.atlassian.jira.web.action.admin.notification.AddScheme addScheme =
                new com.atlassian.jira.web.action.admin.notification.AddScheme((NotificationSchemeManager) notificationSchemeManager);

        //set the scheme details
        addScheme.setName("test scheme");
        addScheme.setDescription("test scheme");

        //The scheme manager should be set correctly
        assertEquals(addScheme.getSchemeManager(), notificationSchemeManager);

        assertTrue(notificationSchemeManager.getSchemes().size() == 0);

        //add the scheme
        String result = addScheme.execute();

        //the new scheme should be there
        assertTrue(notificationSchemeManager.getSchemes().size() == 1);

        //there should be no errors
        assertEquals(0, addScheme.getErrors().size());

        assertEquals(Action.NONE, result);

        response.verify();
    }
}
