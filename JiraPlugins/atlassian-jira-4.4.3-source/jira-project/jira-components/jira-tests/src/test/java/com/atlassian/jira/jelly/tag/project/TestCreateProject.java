/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.project;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarImpl;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import electric.xml.Document;
import electric.xml.Element;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.List;

public class TestCreateProject extends AbstractJellyTestCase
{
    private User testUser;

    public TestCreateProject(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        // create a user and log them in
        testUser = UtilsForTests.getTestUser("logged-in-user");
        //Create the administer permission for all users.  Only admins are allowed to create projects.  The new
        // project service now actually enforces this.
        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, null);
        JiraTestUtil.loginUser(testUser);
        final AvatarManager avatarManager = ComponentManager.getComponentInstanceOfType(AvatarManager.class);
        final Avatar avatar = avatarManager.create(AvatarImpl.createSystemAvatar("dummy.jpg", "image/jpeg", Avatar.Type.PROJECT));
        final String avatarId = avatar.getId().toString();
        ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_DEFAULT_AVATAR_ID, avatarId);
        UtilsForTests.getTestEntity("IssueTypeScreenScheme", EasyMap.build("name", "Test Scheme"));
    }

    public void testCreateProject() throws Exception
    {
        final String scriptFilename = "create-project.test.create-project.jelly";
        Document document = runScript(scriptFilename);

        //Check to see if project was created.
        final Collection projects = CoreFactory.getGenericDelegator().findAll("Project");
        assertFalse(projects.isEmpty());
        assertEquals(1, projects.size());

        GenericValue project = (GenericValue) projects.iterator().next();
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());
        assertEquals(project.getLong("id").toString() + ":" + project.getString("key"), root.getTextString());

        //check the permission scheme has been set to the default
        List schemes = ManagerFactory.getPermissionSchemeManager().getSchemes(project);
        GenericValue deafultScheme = ManagerFactory.getPermissionSchemeManager().getDefaultScheme();
        assertEquals(1, schemes.size());
        assertEquals(deafultScheme.get("name"), ((GenericValue) schemes.get(0)).get("name"));
    }

    public void testCreateProjectWithAvatar() throws Exception
    {
        Group g = UtilsForTests.getTestGroup("jira-user");
        testUser.addToGroup(g);
        final GenericValue scheme = ManagerFactory.getPermissionSchemeManager().createDefaultScheme();
        final PermissionManager pm = ComponentAccessor.getPermissionManager();
        //pm.addPermission(Permissions.BROWSE, scheme, "logged-in-user", "user");
        pm.addPermission(Permissions.CREATE_ISSUE, scheme, "jira-user", "group");
        pm.addPermission(Permissions.BROWSE, scheme, "jira-user", "group");
        final String scriptFilename = "create-project.test.create-project-with-avatar.jelly";
        Document document = runScript(scriptFilename);

        final Collection projects = CoreFactory.getGenericDelegator().findAll("Project");
        assertFalse(projects.isEmpty());
        assertEquals(1, projects.size());

        final GenericValue project = (GenericValue) projects.iterator().next();
        final Element root = document.getRoot();
        assertEquals(root.toString(), 0, root.getElements().size());
        assertEquals("1", project.getString("avatar"));
        assertEquals(project.getLong("id").toString() + ":" + project.getString("key"), root.getTextString());
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "project" + FS;
    }
}
