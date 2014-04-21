/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.dao.application.ApplicationDAO;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.ApplicationImpl;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.Permissions;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.opensymphony.user.User;
import electric.xml.Document;
import electric.xml.Element;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import webwork.action.ActionContext;

public class TestAddNewUser extends AbstractJellyTestCase
{
    public TestAddNewUser(String s)
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
        final Document document = runScript("add-new-user.test.add-new-user.jelly");
        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        assertEquals("new-user", root.getTextString().trim());
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "admin" + FS;
    }
}
