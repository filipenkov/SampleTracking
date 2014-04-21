package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.jelly.service.JellyServiceException;
import com.atlassian.jira.security.Permissions;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import electric.xml.Document;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import webwork.action.ActionContext;

public class TestExternalUserManagement extends AbstractJellyTestCase
{
    public TestExternalUserManagement(String s)
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

    public void testAddNewUser() throws Exception
    {
        toggleExternalUserManagementEnabled(true);
        final Document document = runScript("add-new-user.test.add-new-user.jelly");
        try
        {
            String errorMsg = document.getRoot().getElement("Error").getElement("ErrorMessage").getString();
            assertNotNull(errorMsg);
            assertTrue(errorMsg.indexOf("Cannot add user, as external user management is enabled, please contact your JIRA administrators.") != -1);
        } catch (Exception e)
        {
            fail("Could not find the appropriate error message in the result [" + document.toString() + "]");
        }
    }

    public void testEditUserGroups() throws JellyServiceException
    {
        User u = UtilsForTests.getTestUser("new-user");
        Group g = UtilsForTests.getTestGroup("new-group");

        toggleExternalUserManagementEnabled(true);
        final Document document = runScript("add-user-to-group.test.add-user-to-group.jelly");
        try
        {
            String errorMsg = document.getRoot().getElement("Error").getElement("ErrorMessage").getString();
            assertNotNull(errorMsg);
            assertTrue(errorMsg.indexOf("Cannot edit group memberships, as external user management is enabled, please contact your JIRA administrators.") != -1);
        } catch (Exception e)
        {
            fail("Could not find the appropriate error message in the result [" + document.toString() + "]");
        }

    }

    public void testAddGroup() throws JellyServiceException
    {
        User u = UtilsForTests.getTestUser("new-user");

        toggleExternalUserManagementEnabled(true);
        final Document document = runScript("add-user-to-group.test.add-user-to-group-from-group.jelly");
        try
        {
            String errorMsg = document.getRoot().getElement("Error").getElement("ErrorMessage").getString();
            assertNotNull(errorMsg);
            assertTrue(errorMsg.indexOf("Cannot add groups, as external user management is enabled, please contact your JIRA administrators.") != -1);
        } catch (Exception e)
        {
            fail("Could not find the appropriate error message in the result [" + document.toString() + "]");
        }
    }

    public void testDeleteGroup() throws JellyServiceException
    {
        Group g = UtilsForTests.getTestGroup("existing-group");

        toggleExternalUserManagementEnabled(true);
        final Document document = runScript("remove-group.test.remove-existing-group.jelly");
        try
        {
            String errorMsg = document.getRoot().getElement("Error").getElement("ErrorMessage").getString();
            assertNotNull(errorMsg);
            assertTrue(errorMsg.indexOf("Cannot delete group, as external user management is enabled, please contact your JIRA administrators.") != -1);
        } catch (Exception e)
        {
            fail("Could not find the appropriate error message in the result [" + document.toString() + "]");
        }
    }

    public void testDeleteUser() throws JellyServiceException
    {
        User u = UtilsForTests.getTestUser("existing-user");

        toggleExternalUserManagementEnabled(true);
        final Document document = runScript("remove-user.test.remove-existing-user.jelly");
        try
        {
            String errorMsg = document.getRoot().getElement("Error").getElement("ErrorMessage").getString();
            assertNotNull(errorMsg);
            assertTrue(errorMsg.indexOf("Cannot delete user, as external user management is enabled, please contact your JIRA administrators.") != -1);
        } catch (Exception e)
        {
            fail("Could not find the appropriate error message in the result [" + document.toString() + "]");
        }
    }

    protected void toggleExternalUserManagementEnabled(boolean enable)
    {
        ComponentAccessor.getApplicationProperties().setOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT, enable);
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "admin" + FS;
    }
}
