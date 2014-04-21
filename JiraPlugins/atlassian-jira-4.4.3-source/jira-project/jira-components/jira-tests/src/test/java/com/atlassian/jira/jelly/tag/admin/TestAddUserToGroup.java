/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.security.Permissions;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import electric.xml.Document;
import electric.xml.Element;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import webwork.action.ActionContext;

import java.util.List;

public class TestAddUserToGroup extends AbstractJellyTestCase
{
    public TestAddUserToGroup(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        //Create user and place in the action context
        User u = UtilsForTests.getTestUser("logged-in-user");
        JiraTestUtil.loginUser(u);

        //Create the administer permission for all users
        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, null);
    }

    public void testAddUserToGroup() throws Exception
    {
        User u = UtilsForTests.getTestUser("new-user");
        Group g = UtilsForTests.getTestGroup("new-group");

        final Document document = runScript("add-user-to-group.test.add-user-to-group.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        final List groups = u.getGroups();
        assertEquals(1, groups.size());
        assertEquals("new-group", groups.get(0));
    }

    public void testAddUserToGroupFromUser() throws Exception
    {
        Group g = UtilsForTests.getTestGroup("new-group");

        final Document document = runScript("add-user-to-group.test.add-user-to-group-from-user.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        User u = ManagerFactory.getUserManager().getUser(root.getTextString().trim());
        final List groups = u.getGroups();
        assertEquals(1, groups.size());
        assertEquals("new-group", groups.get(0));
    }

    public void testAddUserToGroupFromGroup() throws Exception
    {
        User u = UtilsForTests.getTestUser("new-user");

        final Document document = runScript("add-user-to-group.test.add-user-to-group-from-group.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        final List groups = u.getGroups();
        assertEquals(1, groups.size());
        assertEquals("new-group", groups.get(0));
    }

    public void testAddUserToGroupFromUserAndGroup() throws Exception
    {
        final Document document = runScript("add-user-to-group.test.add-user-to-group-from-user-and-group.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        User u = ManagerFactory.getUserManager().getUser(root.getTextString().trim());
        final List groups = u.getGroups();
        assertEquals(1, groups.size());
        assertEquals("new-group", groups.get(0));
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "admin" + FS;
    }
}
