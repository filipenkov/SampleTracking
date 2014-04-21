/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.permission.DefaultPermissionSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build11;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build60;
import com.atlassian.jira.web.action.setup.AbstractSetupAction;
import com.atlassian.jira.web.action.setup.SetupOldUserHelper;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

public class TestUpgradeTask_Build60 extends AbstractUsersIndexingTestCase
{
    private UpgradeTask_Build60 upgradeTaskBuild60;

    public TestUpgradeTask_Build60(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        upgradeTaskBuild60 = new UpgradeTask_Build60();
    }

    public void testGetBuildNumber()
    {
        assertEquals("60", upgradeTaskBuild60.getBuildNumber());
    }

    public void testGetShortDescription()
    {
        assertEquals(
            "Adding colors to Priorities so the status bar appearence can be configured. Adding View Control Permission to 'jira-developers' group.",
            upgradeTaskBuild60.getShortDescription());
    }

    public void testPrioritiesAreUpdated() throws Exception
    {
        // This is the upgrade task that created the priorities to start with so run this first.
        final UpgradeTask_Build11 upgradeTask_build11 = new UpgradeTask_Build11(ManagerFactory.getConstantsManager());
        upgradeTask_build11.doUpgrade(false);

        // Add another priority to test the other
        UpgradeTask_Build11.createNewEntity("Priority", 6, "New", "New", "/images/icons/new.gif");

        upgradeTaskBuild60.doUpgrade(false);

        // Retrieve the priorities and check their color values
        final List priorities = CoreFactory.getGenericDelegator().findAll("Priority", EasyList.build("id ASC"));
        assertEquals(6, priorities.size());

        GenericValue priority = (GenericValue) priorities.get(0);
        assertEquals("#cc0000", priority.getString("statusColor"));
        priority = (GenericValue) priorities.get(1);
        assertEquals("#ff0000", priority.getString("statusColor"));
        priority = (GenericValue) priorities.get(2);
        assertEquals("#009900", priority.getString("statusColor"));
        priority = (GenericValue) priorities.get(3);
        assertEquals("#006600", priority.getString("statusColor"));
        priority = (GenericValue) priorities.get(4);
        assertEquals("#003300", priority.getString("statusColor"));
        priority = (GenericValue) priorities.get(5);
        assertEquals("#cc0000", priority.getString("statusColor"));
    }

    public void testViewVersionControlPermissionNotUpdated() throws Exception
    {
        final PermissionSchemeManager originalPermissionSchemeManager = ManagerFactory.getPermissionSchemeManager();
        final PermissionManager originalPermissionManager = ManagerFactory.getPermissionManager();

        try
        {
            final Mock mockPermissionManager = new Mock(PermissionManager.class);
            mockPermissionManager.setStrict(true);
            mockPermissionManager.expectNotCalled("addPermission");
            ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

            final Mock mockPermissionSchemeManager = new Mock(PermissionSchemeManager.class);
            mockPermissionSchemeManager.setStrict(true);
            mockPermissionSchemeManager.expectNotCalled("getDefaultScheme");
            ManagerFactory.addService(PermissionSchemeManager.class, (PermissionSchemeManager) mockPermissionSchemeManager.proxy());

            upgradeTaskBuild60.doUpgrade(false);

            mockPermissionManager.verify();
            mockPermissionSchemeManager.verify();
        }
        finally
        {
            if (originalPermissionManager != null)
            {
                ManagerFactory.addService(PermissionManager.class, originalPermissionManager);
            }
            if (originalPermissionSchemeManager != null)
            {
                ManagerFactory.addService(PermissionSchemeManager.class, originalPermissionSchemeManager);
            }
        }
    }

    public void testViewVersionControlPermissionsUpdated() throws Exception
    {
        final PermissionSchemeManager originalPermissionSchemeManager = ManagerFactory.getPermissionSchemeManager();
        final PermissionManager originalPermissionManager = ManagerFactory.getPermissionManager();

        try
        {
            // Create 'jira-developers' group
            SetupOldUserHelper.addGroup(AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS);
            final GenericValue permissionScheme = UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("name", "Permission scheme",
                "description", "Permission scheme"));

            final MyPermissionSchemeManager permissionSchemeManager = new MyPermissionSchemeManager();
            permissionSchemeManager.setScheme(permissionScheme);

            ManagerFactory.addService(PermissionSchemeManager.class, permissionSchemeManager);

            final Mock mockPermissionManager = new Mock(PermissionManager.class);
            mockPermissionManager.setStrict(true);
            mockPermissionManager.expectVoid("addPermission",
                new Constraint[] { new IsEqual(new Integer(Permissions.VIEW_VERSION_CONTROL)), new IsEqual(permissionScheme), new IsEqual(
                        AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS), new IsEqual(GroupDropdown.DESC) });
            ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

            final UpgradeTask_Build11 upgradeTask_build11 = new UpgradeTask_Build11(ManagerFactory.getConstantsManager());

            upgradeTaskBuild60.doUpgrade(false);

            mockPermissionManager.verify();
            permissionSchemeManager.verify();
        }
        finally
        {
            if (originalPermissionManager != null)
            {
                ManagerFactory.addService(PermissionManager.class, originalPermissionManager);
            }
            if (originalPermissionSchemeManager != null)
            {
                ManagerFactory.addService(PermissionSchemeManager.class, originalPermissionSchemeManager);
            }
        }
    }
}

class MyPermissionSchemeManager extends DefaultPermissionSchemeManager
{
    private GenericValue scheme;
    protected boolean called = false;

    public MyPermissionSchemeManager()
    {
        super(null, null, null, null, null, null, null, null);
    }

    @Override
    public GenericValue getDefaultScheme() throws GenericEntityException
    {
        called = true;
        return scheme;
    }

    public void setScheme(final GenericValue scheme)
    {
        this.scheme = scheme;
    }

    public void verify() throws Exception
    {
        if (!called)
        {
            throw new Exception("getDefaultScheme was not called.");
        }
    }
}
