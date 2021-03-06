/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeManager;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

public class TestCopySchemeAction extends AbstractWebworkTestCase
{
    SchemeManager permSchemeManager;
    SchemeManager notificationSchemeManager;

    public TestCopySchemeAction(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        notificationSchemeManager = ManagerFactory.getNotificationSchemeManager();
        permSchemeManager = ManagerFactory.getPermissionSchemeManager();
    }

    public void testCopyPermissionScheme() throws Exception
    {
        //use a mock servlet response
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewPermissionSchemes.jspa");

        com.atlassian.jira.web.action.admin.permission.CopyScheme copyScheme = new com.atlassian.jira.web.action.admin.permission.CopyScheme();

        GenericValue scheme = permSchemeManager.createScheme("PScheme", "Test Desc");
        permSchemeManager.createSchemeEntity(scheme, new SchemeEntity("type", "param", new Long(1)));

        //The scheme manager should be set correctly
        assertEquals(copyScheme.getSchemeManager(), permSchemeManager);

        assertTrue(permSchemeManager.getSchemes().size() == 1);
        assertTrue(permSchemeManager.getEntities(scheme).size() == 1);

        copyScheme.setSchemeId(scheme.getLong("id"));

        //edit the scheme
        String result = copyScheme.execute();

        //the new scheme should be there
        assertTrue(permSchemeManager.getSchemes().size() == 2);

        //The name should have a prefix of the original
        assertTrue(permSchemeManager.getScheme(new Long(2)).getString("name").equals("Copy of PScheme"));
        assertTrue(permSchemeManager.getEntities(scheme).size() == permSchemeManager.getEntities(permSchemeManager.getScheme(new Long(2))).size());

        //there should be no errors
        assertEquals(0, copyScheme.getErrors().size());

        assertEquals(Action.NONE, result);

        response.verify();
    }

    public void testCopyNotificationScheme() throws Exception
    {
    }
}
