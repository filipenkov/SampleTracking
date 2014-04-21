/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.setup;

import com.atlassian.crowd.password.encoder.AtlassianSecurityPasswordEncoder;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.MapBuilder;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.UserManager;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestSetup2 extends AbstractUsersTestCase
{
    private Setup2 s2;

    public TestSetup2(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        final UserService userService = ComponentManager.getComponentInstanceOfType(UserService.class);
        final PasswordEncoderFactory passwordEncoderFactory = ComponentManager.getComponentInstanceOfType(PasswordEncoderFactory.class);
        passwordEncoderFactory.addEncoder(new AtlassianSecurityPasswordEncoder());

        s2 = new Setup2(userService, null);
    }

    public void testGetSets()
    {
        assertNull(s2.getUsername());
        assertNull(s2.getFullname());
        assertNull(s2.getEmail());
        assertNull(s2.getPassword());
        assertNull(s2.getConfirm());

        s2.setUsername("bob");
        assertEquals("bob", s2.getUsername());
        s2.setFullname("bob smith");
        assertEquals("bob smith", s2.getFullname());
        s2.setEmail("bob@bob.com");
        assertEquals("bob@bob.com", s2.getEmail());
        s2.setPassword("password");
        assertEquals("password", s2.getPassword());
        s2.setConfirm("password");
        assertEquals("password", s2.getConfirm());
    }

    public void testDoDefaultSetupAlready() throws Exception
    {
        s2.getApplicationProperties().setString(APKeys.JIRA_SETUP, "true");
        assertEquals("setupalready", s2.doDefault());
    }

    public void testDoDefaultExistingAdmins() throws Exception
    {
        GenericDelegator genericDelegator = GenericDelegator.getGenericDelegator("default");
        genericDelegator.create("OSUser", MapBuilder.build("id", genericDelegator.getNextSeqId("OSUser"), "name", "admin", "passwordHash", "-"));
        genericDelegator.create("OSGroup", MapBuilder.build("id", genericDelegator.getNextSeqId("OSGroup"), "name", "admingroup"));
        genericDelegator.create("OSMembership", MapBuilder.build("id", genericDelegator.getNextSeqId("OSMembership"),
                "groupName", "admingroup", "userName", "admin"));

        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, "admingroup");

        assertEquals("existingadmins", s2.doDefault());
    }

    public void testDoValidationEmail() throws Exception
    {
        s2.getApplicationProperties().setString(APKeys.JIRA_SETUP, "true");
        assertEquals("setupalready", s2.execute());
        s2.getApplicationProperties().setString(APKeys.JIRA_SETUP, null);

        setAllValidData();

        s2.setEmail("");
        checkValidateSingleError("email", "You must specify an email address.");

        s2.setEmail("abc");
        checkValidateSingleError("email", "You must specify a valid email address.");
    }

    public void testDoValidationFullName() throws Exception
    {
        setAllValidData();
        s2.setFullname("");
        checkValidateSingleError("fullname", "You must specify a full name.");
    }

    public void testDoValidationUserName() throws Exception
    {
        setAllValidData();
        s2.setUsername("");
        checkValidateSingleError("username", "You must specify a username.");

        UserManager.getInstance().createUser("bob");
        s2.setUsername("bob");
        checkValidateSingleError("username", "A user with that username already exists.");
    }


    public void testDoValidationPassword() throws Exception
    {
        setAllValidData();

        s2.setPassword("");

        assertEquals(Action.INPUT, s2.execute());
        assertEquals(2, s2.getErrors().size());
        assertEquals("Your password and confirmation password do not match.", s2.getErrors().get("confirm"));

        setAllValidData();

        s2.setConfirm("");
        assertEquals(Action.INPUT, s2.execute());
        assertEquals(2, s2.getErrors().size());
        assertEquals("Your password and confirmation password do not match.", s2.getErrors().get("confirm"));
    }

    public void tesDoValidationPassword2() throws Exception
    {
        setAllValidData();
        s2.setPassword(null);
        s2.setConfirm(null);
        checkValidateSingleError("password", "You must specify a password and a confirmation password.");
    }

    public void testDoValidationConfirm() throws Exception
    {
        setAllValidData();
        s2.setConfirm("wrong password");
        checkValidateSingleError("confirm", "Your password and confirmation password do not match.");
    }

    public void testExecuteExistingAdmin() throws Exception
    {
        setAllValidData();

        GenericDelegator genericDelegator = GenericDelegator.getGenericDelegator("default");
        genericDelegator.create("OSUser", MapBuilder.build("id", genericDelegator.getNextSeqId("OSUser"), "name", "admin", "passwordHash", "-"));
        genericDelegator.create("OSGroup", MapBuilder.build("id", genericDelegator.getNextSeqId("OSGroup"), "name", "admingroup"));
        genericDelegator.create("OSMembership", MapBuilder.build("id", genericDelegator.getNextSeqId("OSMembership"),
                "groupName", "admingroup", "userName", "admin"));

        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, "admingroup");

        assertEquals("existingadmins", s2.execute());
    }

    public void testExecuteFine() throws Exception
    {

        setAllValidData();
        assertEquals(Action.SUCCESS, s2.execute());

        GenericDelegator genericDelegator = GenericDelegator.getGenericDelegator("default");
        List testuser = genericDelegator.findByAnd("OSUser", MapBuilder.build("name", "test username"));
        assertEquals(1, testuser.size());

        GenericValue user = (GenericValue) testuser.get(0);

        JiraPropertySetFactory jiraPropertySetFactory = ComponentManager.getComponentInstanceOfType(JiraPropertySetFactory.class);
        PropertySet ps = jiraPropertySetFactory.buildNoncachingPropertySet("OSUser", user.getLong("id"));

        assertEquals("test@atlassian.com", ps.getString("email"));
        assertEquals("test fullname", ps.getString("fullName"));
        //        assertTrue(user.authenticate("password"));

        Collection<GenericValue> memberships = genericDelegator.findByAnd("OSMembership", MapBuilder.build("userName", "test username"));
        assertEquals(3, memberships.size());

        List<String> groups = new ArrayList<String>();
        for (GenericValue membership : memberships)
        {
            groups.add(membership.getString("groupName"));
        }

        assertTrue(groups.contains(AbstractSetupAction.DEFAULT_GROUP_ADMINS));
        assertTrue(groups.contains(AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS));
        assertTrue(groups.contains(AbstractSetupAction.DEFAULT_GROUP_USERS));
    }

    private void checkValidateSingleError(String element, String errormsg) throws Exception
    {
        assertEquals(Action.INPUT, s2.execute());
        assertEquals(1, s2.getErrors().size());
        assertEquals(errormsg, s2.getErrors().get(element));
    }

    private void setAllValidData()
    {
        s2.setEmail("test@atlassian.com");
        s2.setFullname("test fullname");
        s2.setUsername("test username");
        s2.setPassword("password");
        s2.setConfirm("password");
    }
}
