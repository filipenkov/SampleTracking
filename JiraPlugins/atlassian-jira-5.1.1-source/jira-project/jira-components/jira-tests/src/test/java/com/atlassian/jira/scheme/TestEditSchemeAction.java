/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.jira.scheme.SchemeManager;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

public class TestEditSchemeAction extends AbstractWebworkTestCase
{
    SchemeManager permSchemeManager;
    SchemeManager notificationSchemeManager;

    public TestEditSchemeAction(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        notificationSchemeManager = ManagerFactory.getNotificationSchemeManager();
        permSchemeManager = ManagerFactory.getPermissionSchemeManager();
    }

    public void testEditPermissionScheme() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewPermissionSchemes.jspa");

        com.atlassian.jira.web.action.admin.permission.EditScheme editScheme = new com.atlassian.jira.web.action.admin.permission.EditScheme();

        GenericValue scheme = permSchemeManager.createScheme("PScheme", "Test Desc");

        //set the scheme details
        editScheme.setName("New Name");
        editScheme.setDescription("New Description");

        //The scheme manager should be set correctly
        assertEquals(editScheme.getSchemeManager(), permSchemeManager);

        assertTrue(permSchemeManager.getSchemes().size() == 1);

        editScheme.setSchemeId(scheme.getLong("id"));

        //edit the scheme
        String result = editScheme.execute();

        //the new scheme should be there
        assertTrue(permSchemeManager.getScheme(scheme.getLong("id")).getString("name").equals("New Name"));
        assertTrue(permSchemeManager.getScheme(scheme.getLong("id")).getString("description").equals("New Description"));

        //there should be no errors
        assertEquals(0, editScheme.getErrors().size());

        assertEquals(Action.NONE, result);

        response.verify();
    }

    public void testEditNotificationScheme() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewNotificationSchemes.jspa");

        com.atlassian.jira.web.action.admin.notification.EditScheme editScheme = new com.atlassian.jira.web.action.admin.notification.EditScheme();

        GenericValue scheme = notificationSchemeManager.createScheme("NScheme", "Test Desc");

        //set the scheme details
        editScheme.setName("New Name");
        editScheme.setDescription("New Description");

        //The scheme manager should be set correctly
        assertEquals(editScheme.getSchemeManager(), notificationSchemeManager);

        assertTrue(notificationSchemeManager.getSchemes().size() == 1);

        editScheme.setSchemeId(scheme.getLong("id"));

        //edit the scheme
        String result = editScheme.execute();

        //the new scheme should be there
        assertTrue(notificationSchemeManager.getScheme(scheme.getLong("id")).getString("name").equals("New Name"));
        assertTrue(notificationSchemeManager.getScheme(scheme.getLong("id")).getString("description").equals("New Description"));

        //there should be no errors
        assertEquals(0, editScheme.getErrors().size());

        assertEquals(Action.NONE, result);

        response.verify();
    }
}
