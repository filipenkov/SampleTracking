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
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

public class TestDeleteSchemeAction extends AbstractWebworkTestCase
{
    SchemeManager permSchemeManager;
    SchemeManager notificationSchemeManager;

    public TestDeleteSchemeAction(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        notificationSchemeManager = ManagerFactory.getNotificationSchemeManager();
        permSchemeManager = ManagerFactory.getPermissionSchemeManager();
    }

    public void testDeletePermissionScheme() throws Exception
    {
        //use a mock servlet response
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewPermissionSchemes.jspa");

        com.atlassian.jira.web.action.admin.permission.DeleteScheme deleteScheme = new com.atlassian.jira.web.action.admin.permission.DeleteScheme();

        //The scheme manager should be set correctly
        assertEquals(deleteScheme.getSchemeManager(), permSchemeManager);

        assertTrue(permSchemeManager.getSchemes().size() == 0);

        //create a scheme to delete
        GenericValue scheme = permSchemeManager.createScheme("PScheme", "Test Desc");
        assertTrue(permSchemeManager.getSchemes().size() == 1);

        //delete the scheme
        deleteScheme.setSchemeId(scheme.getLong("id"));
        deleteScheme.setConfirmed(true);

        String result = deleteScheme.execute();

        //the scheme should be gone
        assertTrue(permSchemeManager.getSchemes().size() == 0);

        //there should be no errors
        assertEquals(0, deleteScheme.getErrors().size());

        assertEquals(Action.NONE, result);

        response.verify();
    }

    public void testDeleteNotificationScheme() throws Exception
    {
        //use a mock servlet response
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewNotificationSchemes.jspa");

        com.atlassian.jira.web.action.admin.notification.DeleteScheme deleteScheme =
                new com.atlassian.jira.web.action.admin.notification.DeleteScheme((NotificationSchemeManager) notificationSchemeManager);

        //The scheme manager should be set correctly
        assertEquals(deleteScheme.getSchemeManager(), notificationSchemeManager);

        assertTrue(notificationSchemeManager.getSchemes().size() == 0);

        //create a scheme to delete
        GenericValue scheme = notificationSchemeManager.createScheme("NScheme", "Test Desc");
        assertTrue(notificationSchemeManager.getSchemes().size() == 1);

        //delete the scheme
        deleteScheme.setSchemeId(scheme.getLong("id"));
        deleteScheme.setConfirmed(true);

        String result = deleteScheme.execute();

        //the scheme should be gone
        assertTrue(notificationSchemeManager.getSchemes().size() == 0);

        //there should be no errors
        assertEquals(0, deleteScheme.getErrors().size());

        assertEquals(Action.NONE, result);

        response.verify();
    }
}
