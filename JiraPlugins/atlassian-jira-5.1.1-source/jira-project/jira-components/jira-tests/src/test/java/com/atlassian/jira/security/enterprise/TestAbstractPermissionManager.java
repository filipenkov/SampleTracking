/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.enterprise;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.mock.security.MockAbstractPermissionsManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.type.CurrentAssignee;
import com.atlassian.jira.security.type.CurrentReporter;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.security.type.ProjectLead;
import com.atlassian.jira.security.type.SingleUser;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

//This class has been updated to reflect the fact that permisisons are now done on a scheme basis rather than a project basis
public class TestAbstractPermissionManager extends com.atlassian.jira.security.TestAbstractPermissionManager
{
    private MockAbstractPermissionsManager apm;

    public TestAbstractPermissionManager(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        apm = new MockAbstractPermissionsManager();
        //Only add one project to the permission scheme
        ManagerFactory.getPermissionSchemeManager().addSchemeToProject(project, scheme);
    }

    public void testHasUserGroupPermission()
            throws CreateException, RemoveException, GenericEntityException, OperationNotPermittedException, InvalidGroupException
    {
        UtilsForTests.getTestEntity("Component", EasyMap.build("id", new Long(1)));

        Group group1 = createMockGroup("group1");
        addUserToGroup(bob, group1);
        Group group2 = createMockGroup("group2");
        addUserToGroup(joe, group2);

        //Anonymous Permission Per Scheme - Anyone
        apm.addPermission(2, scheme, null, GroupDropdown.DESC);
        hasUserGroupAndUserGroup(2, project, bob);
        hasUserGroupAndUserGroup(2, project, joe);
        hasUserGroupAndUserGroup(2, project, null);
        hasUserGroupAndUserGroup(2, project, paul);

        ManagerFactory.getPermissionSchemeManager().removeEntities(scheme, new Long(2));
        hasntUserGroupAndUserGroup(2, project, bob);
        hasntUserGroupAndUserGroup(2, project, joe);
        hasntUserGroupAndUserGroup(2, project, null);
        hasntUserGroupAndUserGroup(2, project, paul);

        //These projects are not assocaiated with scheme so should not have permission
        hasntUserGroupAndUserGroup(2, project2, bob);
        hasntUserGroupAndUserGroup(2, project2, joe);

        // specific permission - only these groups and schemes should have permission
        apm.addPermission(5, scheme, "group1", GroupDropdown.DESC);
        hasUserGroupAndUserGroup(5, project, bob);
        hasntUserGroupAndUserGroup(5, project2, bob);
        hasntUserGroupAndUserGroup(5, project, joe);

        PermissionSchemeManager permissionSchemeManager = ManagerFactory.getPermissionSchemeManager();

        //Test Project Lead Permissions
        SchemeEntity schemeEntity = new SchemeEntity(ProjectLead.DESC, null, new Long(9));
        permissionSchemeManager.createSchemeEntity(scheme, schemeEntity);
        hasUserAndUserGroup(9, project, paul);
        hasntUserAndUserGroup(9, project, bob);
        hasntUserAndUserGroup(9, project, joe);

        //Test SingleUser permissions
        schemeEntity = new SchemeEntity(SingleUser.DESC, "joe", new Long(10));
        permissionSchemeManager.createSchemeEntity(scheme, schemeEntity);
        hasntUserAndUserGroup(10, project, paul);
        hasntUserAndUserGroup(10, project, bob);
        hasUserAndUserGroup(10, project, joe);

        //Test CurrentReporter permissions
        schemeEntity = new SchemeEntity(CurrentAssignee.DESC, new Long(11));
        permissionSchemeManager.createSchemeEntity(scheme, schemeEntity);
        hasUserAndUserGroup(11, issue, bob);
        hasntUserAndUserGroup(11, issue, paul);
        hasntUserAndUserGroup(11, issue, joe);
        hasntUserAndUserGroup(11, issue, null);
        hasUserAndUserGroup(11, project, bob);
        hasUserAndUserGroup(11, project, paul);
        hasUserAndUserGroup(11, project, joe);
        hasntUserAndUserGroup(11, project, null);

        //Test CurrentReporter permissions
        schemeEntity = new SchemeEntity(CurrentReporter.DESC, new Long(12));
        permissionSchemeManager.createSchemeEntity(scheme, schemeEntity);
        hasUserAndUserGroup(12, issue, bob);
        hasntUserAndUserGroup(12, issue, paul);
        hasntUserAndUserGroup(12, issue, joe);
        hasntUserAndUserGroup(12, issue, null);
        hasUserAndUserGroup(12, project, bob);
        hasUserAndUserGroup(12, project, paul);
        hasUserAndUserGroup(12, project, joe);
        hasntUserAndUserGroup(12, project, null);
    }

    private void hasUserAndUserGroup(int permtype, GenericValue project, User user)
    {
        userAndUserGroup(permtype, project, user, true);
    }

    private void hasntUserAndUserGroup(int permtype, GenericValue project, User user)
    {
        userAndUserGroup(permtype, project, user, false);
    }
}
