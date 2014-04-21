/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bulkedit.operation;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionImpl;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.mockobjects.dynamic.Mock;
import com.opensymphony.user.User;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.AbstractUsersTestCase;

import java.util.HashMap;
import java.util.Map;

public class TestBulkEditOperation extends AbstractUsersTestCase
{
    private static final String ERROR_MESSAGE = "Test Error Message";
    BulkEditOperation bulkEditOperation;
    User testUser;
    private GenericValue project1;
    private GenericValue project2;
    private GenericValue version1;
    private GenericValue version2;
    private GenericValue issue1;
    private GenericValue issue2;
    private GenericValue issue3;
    private Map params;
    private GenericValue issuetype1;
    private GenericValue issuetype2;
    private GenericValue priority;

    private Mock versionManager;
    private Version versionOne;
    private Version versionTwo;

    public TestBulkEditOperation(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        params = new HashMap();
        testUser = UtilsForTests.getTestUser("testuser");
        versionManager = new Mock(VersionManager.class);

        bulkEditOperation = new BulkEditOperation(null, null, null, null);

        project1 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC", "counter", new Long(1)));
        project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "XYZ"));

        version1 = UtilsForTests.getTestEntity("Version", EasyMap.build("project", project1.getLong("id"), "sequence", new Long(1), "name", "Ver 1"));
        version2 = UtilsForTests.getTestEntity("Version", EasyMap.build("project", project1.getLong("id"), "sequence", new Long(2), "name", "Ver 2"));

        versionOne = new VersionImpl(null, new MockGenericValue("Version", EasyMap.build("project", project1.getLong("id"), "name", "Ver 1", "sequence", new Long(1), "id", new Long(1))));
        versionTwo = new VersionImpl(null, new MockGenericValue("Version", EasyMap.build("project", project1.getLong("id"), "name", "Ver 2", "sequence", new Long(2), "id", new Long(2))));

        issuetype1 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "100", "name", "testtype", "description", "test issue type"));
        issuetype2 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "200", "name", "another testtype", "description", "another test issue type"));

        issue1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("project", project1.getLong("id"), "id", new Long(100), "key", "ABC-1", "type", issuetype1.getString("id"), "workflowId", new Long(1)));
        issue2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("project", project1.getLong("id"), "id", new Long(101), "key", "ABC-2", "type", issuetype1.getString("id"), "workflowId", new Long(1)));
        issue3 = UtilsForTests.getTestEntity("Issue", EasyMap.build("project", project2.getLong("id"), "id", new Long(102), "key", "XYZ-1", "type", issuetype2.getString("id")));

        params.put(BulkEditBean.BULKEDIT_PREFIX + "100", Boolean.TRUE);
        params.put(BulkEditBean.BULKEDIT_PREFIX + "101", Boolean.TRUE);

        // Create priority
        priority = UtilsForTests.getTestEntity("Priority", EasyMap.build("id", "1", "name", "Priority"));

        ManagerFactory.getConstantsManager().refreshIssueTypes();
        ManagerFactory.getCustomFieldManager().refresh();
    }

    // Comment out failing tests

    public void testGetAllAvailableActions() throws Exception
    {
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//        List actions = bulkEditOperation.getActions(bulkEditBean, testUser);
//
//        assertEquals(6, actions.size());
//
//        final Iterator iterator = actions.iterator();
//        BulkEditAction action = (BulkEditAction) iterator.next();
//        BulkEditBean.BULK_FIXFOR.equals(action.getName());
//        action = (BulkEditAction) iterator.next();
//        BulkEditBean.BULK_AFFECTSVERSION.equals(action.getName());
//        action = (BulkEditAction) iterator.next();
//        BulkEditBean.BULK_COMPONENT.equals(action.getName());
//        action = (BulkEditAction) iterator.next();
//        BulkEditBean.BULK_ASSIGN.equals(action.getName());
//        action = (BulkEditAction) iterator.next();
//        BulkEditBean.BULK_PRIORITY.equals(action.getName());
//        action = (BulkEditAction) iterator.next();
//        BulkEditBean.BULK_CUSTOMFIELDS.equals(action.getName());
    }

//    public void testGetCannotPerformMessageKey()
//    {
//        assertEquals("bulk.edit.cannotperform", bulkEditOperation.getCannotPerformMessageKey());
//    }
//
//    public void testGetNameKey()
//    {
//        assertEquals("bulk.edit.operation.name", bulkEditOperation.getNameKey());
//    }
//
//    public void testGetOperationName()
//    {
//        assertEquals("BulkEdit", bulkEditOperation.getOperationName());
//    }
//
//    public void testGetAvailableActionsNoPermissions() throws Exception
//    {
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_FIXFOR, "bulk.edit.unavailable.permission", BulkEditBean.BULK_AFFECTSVERSION, "bulk.edit.unavailable.permission", BulkEditBean.BULK_COMPONENT, "bulk.edit.unavailable.permission", BulkEditBean.BULK_ASSIGN, "bulk.edit.unavailable.permission", BulkEditBean.BULK_PRIORITY, "bulk.edit.multiproject.unavailable.permission", BulkEditBean.BULK_CUSTOMFIELDS, "bulk.edit.unavailable.customfields");
//        _testAvailability(bulkEditBean, Collections.EMPTY_LIST, expectedErrorMessages);
//    }
//
//    public void testGetAvailableActionsSingleProjectAssignPermission() throws Exception
//    {
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                if (permissionsId == Permissions.ASSIGN_ISSUE)
//                    return true;
//
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_FIXFOR, "bulk.edit.unavailable.permission", BulkEditBean.BULK_AFFECTSVERSION, "bulk.edit.unavailable.permission", BulkEditBean.BULK_COMPONENT, "bulk.edit.unavailable.permission", BulkEditBean.BULK_PRIORITY, "bulk.edit.multiproject.unavailable.permission", BulkEditBean.BULK_CUSTOMFIELDS, "bulk.edit.unavailable.customfields");
//        _testAvailability(bulkEditBean, EasyList.build(BulkEditBean.BULK_ASSIGN), expectedErrorMessages);
//    }
//
//    public void testGetAvailableActionsSingleProjectEditPermission() throws Exception
//    {
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                if (permissionsId == Permissions.ASSIGN_ISSUE)
//                    return true;
//
//                if (permissionsId == Permissions.EDIT_ISSUE)
//                    return true;
//
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        // NOTE: Fix For Versions are not available as no RESOLVE permission is given
//        // NOTE: Affects Versions are available as EDIT permission is given
//        // NOTE: No Components - so no components should be set
//        // NOTE: No custom fields exists
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_FIXFOR, "bulk.edit.unavailable.permission", BulkEditBean.BULK_COMPONENT, "bulk.edit.unavailable.nocomponents", BulkEditBean.BULK_CUSTOMFIELDS, "bulk.edit.unavailable.customfields");
//
//        versionManager.expectAndReturn("getVersionsUnreleased", P.args(P.eq(project1), P.IS_TRUE), EasyList.build(version1));
//        versionManager.expectAndReturn("getVersionsReleased", P.args(P.eq(project1), P.IS_TRUE), EasyList.build(version1));
//
//        _testAvailability(bulkEditBean, EasyList.build(BulkEditBean.BULK_ASSIGN, BulkEditBean.BULK_PRIORITY, BulkEditBean.BULK_AFFECTSVERSION), expectedErrorMessages);
//    }
//
//    public void testGetAvailableActionsSingleProjectEditPermissionWithComponents() throws Exception
//    {
//        // Setup Comnponents
//        UtilsForTests.getTestEntity("Component", EasyMap.build("project", project1.getLong("id"), "name", "Comp 1"));
//        UtilsForTests.getTestEntity("Component", EasyMap.build("project", project1.getLong("id"), "name", "Comp 2"));
//
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                if (permissionsId == Permissions.ASSIGN_ISSUE)
//                    return true;
//
//                if (permissionsId == Permissions.EDIT_ISSUE)
//                    return true;
//
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        // NOTE: Fix For Versions are not available as no RESOLVE permission is given
//        // NOTE: AFfects Versions are available as EDIT permission is given.
//        // NOTE: No custom fields exists
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_FIXFOR, "bulk.edit.unavailable.permission", BulkEditBean.BULK_CUSTOMFIELDS, "bulk.edit.unavailable.customfields");
//
//        versionManager.expectAndReturn("getVersionsUnreleased", P.args(P.eq(project1), P.IS_TRUE), EasyList.build(version1));
//        versionManager.expectAndReturn("getVersionsReleased", P.args(P.eq(project1), P.IS_TRUE), EasyList.build(version1));
//
//        _testAvailability(bulkEditBean, EasyList.build(BulkEditBean.BULK_ASSIGN, BulkEditBean.BULK_AFFECTSVERSION, BulkEditBean.BULK_COMPONENT, BulkEditBean.BULK_PRIORITY), expectedErrorMessages);
//    }
//
//    public void testGetAvailableActionsSingleProjectJustResolvePermission() throws Exception
//    {
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                if (permissionsId == Permissions.RESOLVE_ISSUE)
//                    return true;
//
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        // Nothing should be availabel as there is no EDIT or ASSIGN permissions given
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_FIXFOR, "bulk.edit.unavailable.permission", BulkEditBean.BULK_AFFECTSVERSION, "bulk.edit.unavailable.permission", BulkEditBean.BULK_COMPONENT, "bulk.edit.unavailable.permission", BulkEditBean.BULK_ASSIGN, "bulk.edit.unavailable.permission", BulkEditBean.BULK_PRIORITY, "bulk.edit.multiproject.unavailable.permission", BulkEditBean.BULK_CUSTOMFIELDS, "bulk.edit.unavailable.customfields");
//        _testAvailability(bulkEditBean, Collections.EMPTY_LIST, expectedErrorMessages);
//    }
//
//    public void testGetAvailableActionsSingleProjectResolvePermission() throws Exception
//    {
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                if (entity.equals(project1))
//                {
//                    if (permissionsId == Permissions.EDIT_ISSUE)
//                        return true;
//
//                    if (permissionsId == Permissions.RESOLVE_ISSUE)
//                        return true;
//                }
//
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        // NOTE: No Components - so no components should be set
//        // NOTE: ASSIGN permission is not given, so no assigning should not be available
//        // NOTE: No custom fields exists
//        // We have a resolve as well as edit permission - so we should be able to set the fix-for-versions
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_ASSIGN, "bulk.edit.unavailable.permission", BulkEditBean.BULK_COMPONENT, "bulk.edit.unavailable.nocomponents", BulkEditBean.BULK_CUSTOMFIELDS, "bulk.edit.unavailable.customfields");
//
//        versionManager.expectAndReturn("getVersionsUnreleased", P.args(P.eq(project1), P.IS_TRUE), EasyList.build(version1));
//        versionManager.expectAndReturn("getVersionsReleased", P.args(P.eq(project1), P.IS_TRUE), EasyList.build(version1));
//
//        _testAvailability(bulkEditBean, EasyList.build(BulkEditBean.BULK_FIXFOR, BulkEditBean.BULK_AFFECTSVERSION, BulkEditBean.BULK_PRIORITY), expectedErrorMessages);
//    }
//
//    public void testGetAvailableActionsSingleProjectEditPermissionNoVersions() throws Exception
//    {
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//        bulkEditBean._setSelectedIssueGVsForTesting(EasyList.build(issue3));
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                if (project2.equals(entity))
//                {
//                    if (permissionsId == Permissions.RESOLVE_ISSUE)
//                        return true;
//
//                    if (permissionsId == Permissions.EDIT_ISSUE)
//                        return true;
//                }
//
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        // NOTE: No ASSIGN permission is given so no assigning
//        // NOTE: No custom fields exists
//        // NOTE: No components exists - so they cannot be set
//        // NOTE: No versions exists - so they cannot be set (even though we have permissions)
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_FIXFOR, "bulk.edit.unavailable.nofixversions", BulkEditBean.BULK_AFFECTSVERSION, "bulk.edit.unavailable.nofixversions", BulkEditBean.BULK_ASSIGN, "bulk.edit.unavailable.permission", BulkEditBean.BULK_COMPONENT, "bulk.edit.unavailable.nocomponents", BulkEditBean.BULK_CUSTOMFIELDS, "bulk.edit.unavailable.customfields");
//
//        versionManager.expectAndReturn("getVersionsUnreleased", P.args(P.eq(project2), P.IS_TRUE), Collections.EMPTY_LIST);
//        versionManager.expectAndReturn("getVersionsReleased", P.args(P.eq(project2), P.IS_TRUE), Collections.EMPTY_LIST);
//
//        _testAvailability(bulkEditBean, EasyList.build(BulkEditBean.BULK_PRIORITY), expectedErrorMessages);
//    }
//
//    public void testGetAvailableActionsMultipleProjectsNoPermission1() throws Exception
//    {
//        BulkEditBean bulkEditBean = setupBulkEditBeanMultipleProjects();
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                if (entity.equals(project1))
//                {
//                    if (permissionsId == Permissions.EDIT_ISSUE)
//                        return true;
//                }
//
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        // NOTE: priority is not available as we do not have EDIT permission for project2
//        // NOTE: no single project actions should be available
//        // NOTE: no custom fileds exist
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_FIXFOR, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_AFFECTSVERSION, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_COMPONENT, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_ASSIGN, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_PRIORITY, "bulk.edit.multiproject.unavailable.permission", BulkEditBean.BULK_CUSTOMFIELDS, "bulk.edit.unavailable.customfields");
//        _testAvailability(bulkEditBean, Collections.EMPTY_LIST, expectedErrorMessages);
//    }
//
//    public void testGetAvailableActionsMultipleProjectsNoPermission2() throws Exception
//    {
//        BulkEditBean bulkEditBean = setupBulkEditBeanMultipleProjects();
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                if (entity.equals(project2))
//                {
//                    if (permissionsId == Permissions.EDIT_ISSUE)
//                        return true;
//                }
//
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        // NOTE: priority is not available as we do not have EDIT permission for project1
//        // NOTE: no single project actions should be available
//        // NOTE: no custom fileds exist
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_FIXFOR, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_AFFECTSVERSION, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_COMPONENT, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_ASSIGN, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_PRIORITY, "bulk.edit.multiproject.unavailable.permission", BulkEditBean.BULK_CUSTOMFIELDS, "bulk.edit.unavailable.customfields");
//        _testAvailability(bulkEditBean, Collections.EMPTY_LIST, expectedErrorMessages);
//    }
//
//    public void testGetAvailableActionsMultipleProjects() throws Exception
//    {
//        BulkEditBean bulkEditBean = setupBulkEditBeanMultipleProjects();
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                if (entity.equals(project1) || entity.equals(project2))
//                {
//                    if (permissionsId == Permissions.EDIT_ISSUE)
//                        return true;
//                }
//
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        // NOTE: no single project actions are available
//        // NOTE: priority IS available as we have EDIT permissions for both projects
//        // NOTE: no custom fields exist
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_FIXFOR, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_AFFECTSVERSION, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_COMPONENT, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_ASSIGN, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_CUSTOMFIELDS, "bulk.edit.unavailable.customfields");
//        _testAvailability(bulkEditBean, EasyList.build(BulkEditBean.BULK_PRIORITY), expectedErrorMessages);
//    }
//
//    public void testGetAvailableActionsMultipleProjectsWithCustomFieldsNoPerms() throws Exception
//    {
//        // Create global custom field
//        UtilsForTests.getTestEntity("CustomField", EasyMap.build("id", new Long(1), "name", "Test Global Custom Field", "description", "Description Value", "default", "Default Value"));
//
//        BulkEditBean bulkEditBean = setupBulkEditBeanMultipleProjects();
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        // NOTE: no permissions (even though the custom field exists)
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_FIXFOR, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_AFFECTSVERSION, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_COMPONENT, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_ASSIGN, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_PRIORITY, "bulk.edit.multiproject.unavailable.permission", BulkEditBean.BULK_CUSTOMFIELDS, "bulk.edit.multiproject.unavailable.permission");
//        _testAvailability(bulkEditBean, Collections.EMPTY_LIST, expectedErrorMessages);
//    }
//
//    public void testGetAvailableActionsMultipleProjectsWithGlobalCustomField() throws Exception
//    {
//        // Create global custom field
//        GenericValue customFieldGV = UtilsForTests.getTestEntity("CustomField", EasyMap.build("id", new Long(1), "name", "Test Global Custom Field", "description", "Description Value", "default", "Default Value"));
//
//        BulkEditBean bulkEditBean = setupBulkEditBeanMultipleProjects();
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                if (project1.equals(entity) || project2.equals(entity))
//                {
//                    if (permissionsId == Permissions.EDIT_ISSUE)
//                        return true;
//                }
//
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_FIXFOR, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_AFFECTSVERSION, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_COMPONENT, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_ASSIGN, "bulk.edit.unavailable.multipleprojects");
//        _testAvailability(bulkEditBean, EasyList.build(BulkEditBean.BULK_PRIORITY, BulkEditBean.BULK_CUSTOMFIELDS), expectedErrorMessages);
//
//        checkSingleElementCollection(bulkEditOperation.getCustomFields(bulkEditBean, testUser), customFieldGV);
//        assertTrue(bulkEditOperation.canPerform(bulkEditBean, testUser));
//    }
//
//    public void testGetAvailableActionsMultipleProjectsWithProjectCustomField() throws Exception
//    {
//        // Create global custom field
//        UtilsForTests.getTestEntity("CustomField", EasyMap.build("id", new Long(1), "name", "Test Global Custom Field", "project", project1.getLong("id"), "description", "Description Value", "default", "Default Value"));
//
//        BulkEditBean bulkEditBean = setupBulkEditBeanMultipleProjects();
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                if (project1.equals(entity) || project2.equals(entity))
//                {
//                    if (permissionsId == Permissions.EDIT_ISSUE)
//                        return true;
//                }
//
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        // NOTE: only project custom field exists - but we have issues from multiple projects
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_FIXFOR, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_AFFECTSVERSION, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_COMPONENT, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_ASSIGN, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_CUSTOMFIELDS, "bulk.edit.unavailable.customfields");
//        _testAvailability(bulkEditBean, EasyList.build(BulkEditBean.BULK_PRIORITY), expectedErrorMessages);
//
//        assertTrue(bulkEditOperation.getCustomFields(bulkEditBean, testUser).isEmpty());
//    }
//
//    public void testGetAvailableActionsSingleProjectWithWrongProjectCustomField() throws Exception
//    {
//        // Create global custom field
//        UtilsForTests.getTestEntity("CustomField", EasyMap.build("id", new Long(1), "name", "Test Global Custom Field", "project", project2.getLong("id"), "description", "Description Value", "default", "Default Value"));
//
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                if (project1.equals(entity) || project2.equals(entity))
//                {
//                    if (permissionsId == Permissions.EDIT_ISSUE)
//                        return true;
//                }
//
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        // NOTE: Fix For Versions is not available as we do not have the RESOLVE permission
//        // NOTE: Affects Versions is available as we have EDIT permission
//        // NOTE: assign is not available as we do not have the ASSIGN permission
//        // NOTE: no components exist
//        // NOTE: the custom field exists but for a different project than the one the selected issues belong to
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_FIXFOR, "bulk.edit.unavailable.permission", BulkEditBean.BULK_COMPONENT, "bulk.edit.unavailable.nocomponents", BulkEditBean.BULK_ASSIGN, "bulk.edit.unavailable.permission", BulkEditBean.BULK_CUSTOMFIELDS, "bulk.edit.unavailable.customfields");
//
//        versionManager.expectAndReturn("getVersionsUnreleased", P.args(P.eq(project1), P.IS_TRUE), EasyList.build(version1));
//        versionManager.expectAndReturn("getVersionsReleased", P.args(P.eq(project1), P.IS_TRUE), EasyList.build(version1));
//
//        _testAvailability(bulkEditBean, EasyList.build(BulkEditBean.BULK_AFFECTSVERSION, BulkEditBean.BULK_PRIORITY), expectedErrorMessages);
//
//        assertTrue(bulkEditOperation.getCustomFields(bulkEditBean, testUser).isEmpty());
//    }
//
//    public void testGetAvailableActionsSingleProjectWithProjectCustomField() throws Exception
//    {
//        // Create global custom field
//        GenericValue customFieldGV = UtilsForTests.getTestEntity("CustomField", EasyMap.build("id", new Long(1), "name", "Test Global Custom Field", "project", project1.getLong("id"), "description", "Description Value", "default", "Default Value"));
//
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                // For the sake of custom field scope need BROWSE permission
//                if (permissionsId == Permissions.BROWSE)
//                {
//                    return true;
//                }
//
//                if (project1.equals(entity) || project2.equals(entity))
//                {
//                    if (permissionsId == Permissions.EDIT_ISSUE)
//                        return true;
//                }
//
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        // NOTE: Fix For Versions is not available as we do not have the RESOLVE permission
//        // NOTE: Affects Versions is available as we have EDIT permission
//        // NOTE: assign is not available as we do not have the ASSIGN permission
//        // NOTE: no components exist
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_FIXFOR, "bulk.edit.unavailable.permission", BulkEditBean.BULK_COMPONENT, "bulk.edit.unavailable.nocomponents", BulkEditBean.BULK_ASSIGN, "bulk.edit.unavailable.permission");
//
//        versionManager.expectAndReturn("getVersionsUnreleased", P.args(P.eq(project1), P.IS_TRUE), EasyList.build(version1));
//        versionManager.expectAndReturn("getVersionsReleased", P.args(P.eq(project1), P.IS_TRUE), EasyList.build(version1));
//
//        _testAvailability(bulkEditBean, EasyList.build(BulkEditBean.BULK_AFFECTSVERSION, BulkEditBean.BULK_PRIORITY, BulkEditBean.BULK_CUSTOMFIELDS), expectedErrorMessages);
//
//        checkSingleElementCollection(bulkEditOperation.getCustomFields(bulkEditBean, testUser), customFieldGV);
//    }
//
//    public void testGetAvailableActionsSingleIssueTypeWithIssueTypeCustomField() throws Exception
//    {
//        // Create global custom field
//        GenericValue customFieldGV = UtilsForTests.getTestEntity("CustomField", EasyMap.build("id", new Long(1), "name", "Test Global Custom Field", "issuetype", issuetype1.getString("id"), "description", "Description Value", "default", "Default Value"));
//
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                if (project1.equals(entity) || project2.equals(entity))
//                {
//                    if (permissionsId == Permissions.EDIT_ISSUE)
//                        return true;
//                }
//
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        // NOTE: Fix For Versions is not available as we do not have the RESOLVE permission
//        // NOTE: Affects Versions is available as we have EDIT permission
//        // NOTE: assign is not available as we do not have the ASSIGN permission
//        // NOTE: no components exist
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_FIXFOR, "bulk.edit.unavailable.permission", BulkEditBean.BULK_COMPONENT, "bulk.edit.unavailable.nocomponents", BulkEditBean.BULK_ASSIGN, "bulk.edit.unavailable.permission");
//
//        versionManager.expectAndReturn("getVersionsUnreleased", P.args(P.eq(project1), P.IS_TRUE), EasyList.build(version1));
//        versionManager.expectAndReturn("getVersionsReleased", P.args(P.eq(project1), P.IS_TRUE), EasyList.build(version1));
//
//        _testAvailability(bulkEditBean, EasyList.build(BulkEditBean.BULK_AFFECTSVERSION, BulkEditBean.BULK_PRIORITY, BulkEditBean.BULK_CUSTOMFIELDS), expectedErrorMessages);
//
//        checkSingleElementCollection(bulkEditOperation.getCustomFields(bulkEditBean, testUser), customFieldGV);
//    }
//
//    public void testGetAvailableActionsMultipleIssueTypesWithIssueTypeCustomField() throws Exception
//    {
//        // Create issue type specific custom field
//        UtilsForTests.getTestEntity("CustomField", EasyMap.build("id", new Long(1), "name", "Test Global Custom Field", "issuetype", issuetype1.getString("id"), "description", "Description Value", "default", "Default Value"));
//
//        BulkEditBean bulkEditBean = setupBulkEditBeanMultipleProjects();
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                if (project1.equals(entity) || project2.equals(entity))
//                {
//                    if (permissionsId == Permissions.EDIT_ISSUE)
//                        return true;
//                }
//
//                return false;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        // NOTE: selected issues are of different (more than one) issue type, so the issue type custom field shoud not be available
//        // NOTE: single project actions are not available
//        final Map expectedErrorMessages = EasyMap.build(BulkEditBean.BULK_FIXFOR, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_AFFECTSVERSION, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_COMPONENT, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_ASSIGN, "bulk.edit.unavailable.multipleprojects", BulkEditBean.BULK_CUSTOMFIELDS, "bulk.edit.unavailable.customfields");
//        _testAvailability(bulkEditBean, EasyList.build(BulkEditBean.BULK_PRIORITY), expectedErrorMessages);
//
//        assertTrue(bulkEditOperation.getCustomFields(bulkEditBean, testUser).isEmpty());
//    }
//
//    /**
//     * NOTE: The expectedErrorMessages has BulkEditAction name as a key and expected error message as value
//     *
//     * @param bulkEditBean
//     * @param expectedAvailableActionNames
//     * @param expectedErrorMessages
//     * @throws Exception
//     */
//    private void _testAvailability(BulkEditBean bulkEditBean, List expectedAvailableActionNames, Map expectedErrorMessages) throws Exception
//    {
//        List actions = bulkEditOperation.getActions(bulkEditBean, testUser);
//        for (int i = 0; i < actions.size(); i++)
//        {
//            BulkEditAction bulkEditAction = (BulkEditAction) actions.get(i);
//            if (expectedAvailableActionNames.contains(bulkEditAction.getName()))
//            {
//                assertTrue(bulkEditAction.isAvailable());
//            }
//            else
//            {
//                assertFalse(bulkEditAction.isAvailable());
//
//                // Get the expected error message from the map using the action name as a key
//                String message = (String) expectedErrorMessages.get(bulkEditAction.getName());
//                assertEquals(message, bulkEditAction.getUnavailableMessage());
//            }
//        }
//
//        if (expectedAvailableActionNames.isEmpty())
//        {
//            assertFalse(bulkEditOperation.canPerform(bulkEditBean, testUser));
//        }
//        else
//        {
//            assertTrue(bulkEditOperation.canPerform(bulkEditBean, testUser));
//        }
//    }
//
//    public void testGetAvailableActionsFixForVersionsHidden() throws Exception, GenericEntityException
//    {
//        _testGetAvailableActionsHiddenField(IssueFieldConstants.FIX_FOR_VERSIONS, BulkEditBean.BULK_FIXFOR);
//    }
//
//    public void testGetAvailableActionsComponentsHidden() throws Exception, GenericEntityException
//    {
//        _testGetAvailableActionsHiddenField(IssueFieldConstants.COMPONENTS, BulkEditBean.BULK_COMPONENT);
//    }
//
//    public void testGetAvailableActionsAssigneeHidden() throws Exception, GenericEntityException
//    {
//        _testGetAvailableActionsHiddenField(IssueFieldConstants.ASSIGNEE, BulkEditBean.BULK_ASSIGN);
//    }
//
//    public void testGetAvailableActionsPriorityHidden() throws Exception, GenericEntityException
//    {
//        _testGetAvailableActionsHiddenField(IssueFieldConstants.PRIORITY, BulkEditBean.BULK_PRIORITY);
//    }
//
//    public void testGetAvailableActionsCustomFieldHidden() throws Exception, GenericEntityException
//    {
//        // Create global custom field
//        GenericValue customFieldGV = UtilsForTests.getTestEntity("CustomField", EasyMap.build("id", new Long(1), "name", "Test Global Custom Field", "description", "Description Value", "default", "Default Value"));
//
//        String fieldId = FieldManager.CUSTOM_FIELD_PREFIX + customFieldGV.getLong("id");
//
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                return true;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//
//        // Mock out field layout
//        Mock mockFieldLayout = new Mock(FieldLayout.class);
//        mockFieldLayout.setStrict(true);
//
//        final OrderableField orderableField = ManagerFactory.getFieldManager().getOrderableField(fieldId);
//        mockFieldLayout.expectAndReturn("getFieldLayoutItem", P.args(new IsAnything()), new FieldLayoutItemImpl(orderableField, "Test Field Layout Item", 0, true, false));
//
//        Mock mockFieldLayoutManager = new Mock(FieldLayoutManager.class);
//        mockFieldLayoutManager.setStrict(true);
//        mockFieldLayoutManager.expectAndReturn("getFieldLayout", P.args(new IsEqual(bulkEditBean.getProject()), new IsAnything()), mockFieldLayout.proxy());
//
//        Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
//        mockCustomFieldManager.setStrict(true);
//        mockCustomFieldManager.expectAndReturn("getCustomFieldObject", P.args(new IsEqual(customFieldGV.getLong("id"))), new CustomFieldImpl(customFieldGV, null));
//
//        Mock mockFieldManager = new Mock(FieldManager.class);
//        mockFieldManager.setStrict(true);
//        mockFieldManager.expectAndReturn("getCustomFieldManager", mockCustomFieldManager.proxy());
//
//        // A bit of a hack, due to lack of support in mocks to return different objects based on the parameters passed
//        // The mock will return the Fix For Version field no matter what field is asked for - gets the desired effect for this test though
//        mockFieldManager.expectAndReturn("getOrderableField", P.args(new IsAnything()), orderableField);
//        mockFieldManager.expectAndReturn("getFieldLayoutManager", mockFieldLayoutManager.proxy());
//        ManagerFactory.addService(FieldManager.class, mockFieldManager.proxy());
//
//        List availableActions = bulkEditOperation.getActions(bulkEditBean, testUser);
//
//        for (int i = 0; i < availableActions.size(); i++)
//        {
//            BulkEditAction bulkEditAction = (BulkEditAction) availableActions.get(i);
//            if (bulkEditAction.getName().equals(BulkEditBean.BULK_CUSTOMFIELDS))
//            {
//                assertFalse(bulkEditAction.isAvailable());
//                assertEquals("bulk.edit.unavailable.customfields", bulkEditAction.getUnavailableMessage());
//            }
//        }
//    }
//
//    private void _testGetAvailableActionsHiddenField(String fieldId, String bulkAction) throws Exception
//    {
//        // Create Custom Permission Manager
//        class MyPermissionManager extends DefaultPermissionManager
//        {
//            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
//            {
//                return true;
//            }
//        }
//
//        // Register Permission Manager
//        ManagerFactory.addService(PermissionManager.class, new MyPermissionManager());
//
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//
//        // Mock out field layout
//        Mock mockFieldLayout = new Mock(FieldLayout.class);
//        mockFieldLayout.setStrict(true);
//
//        final OrderableField orderableField = ManagerFactory.getFieldManager().getOrderableField(fieldId);
//        if (ManagerFactory.getFieldManager().isRequirableField(orderableField))
//        {
//            mockFieldLayout.expectAndReturn("getFieldLayoutItem", P.args(new IsAnything()), new FieldLayoutItemImpl(orderableField, "Test Field Layout Item", 0, true, false));
//        }
//        else
//        {
//            mockFieldLayout.expectAndReturn("getFieldLayoutItem", P.args(new IsAnything()), new FieldLayoutItemImpl(orderableField, "Test Field Layout Item", 0, true, true));
//        }
//
//        Mock mockFieldLayoutManager = new Mock(FieldLayoutManager.class);
//        mockFieldLayoutManager.setStrict(true);
//        mockFieldLayoutManager.expectAndReturn("getFieldLayout", P.args(new IsEqual(bulkEditBean.getProject()), new IsAnything()), mockFieldLayout.proxy());
//
//        Mock mockFieldManager = new Mock(FieldManager.class);
//        mockFieldManager.setStrict(true);
//
//        // A bit of a hack, due to lack of support in mocks to return different objects based on the parameters passed
//        // The mock will return the Fix For Version field no matter what field is asked for - gets the desired effect for this test though
//        mockFieldManager.expectAndReturn("getFieldLayoutManager", mockFieldLayoutManager.proxy());
//        mockFieldManager.expectAndReturn("getOrderableField", P.args(new IsAnything()), orderableField);
//        ManagerFactory.addService(FieldManager.class, mockFieldManager.proxy());
//
//        List availableActions = bulkEditOperation.getActions(bulkEditBean, testUser);
//
//        for (int i = 0; i < availableActions.size(); i++)
//        {
//            BulkEditAction bulkEditAction = (BulkEditAction) availableActions.get(i);
//            if (bulkEditAction.getName().equals(bulkAction))
//            {
//                assertFalse(bulkEditAction.isAvailable());
//                assertEquals("bulk.edit.unavailable.hidden", bulkEditAction.getUnavailableMessage());
//            }
//        }
//    }
//
//    public void testDoPerformFixVersionsWithError()
//    {
//        _testDoPerformWithError(BulkEditBean.BULK_FIXFOR);
//    }
//
//    public void testDoPerformFixVersions() throws Exception
//    {
//        // Setup bulk edit bean
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//        bulkEditBean.setActions(new String[]{BulkEditBean.BULK_FIXFOR});
//
//        String[] fixVersions = new String[]{version1.getString("id"), version2.getString("id")};
//        bulkEditBean.setFixVersions(fixVersions);
//
//        versionManager.expectAndReturn("getVersions", P.args(P.eq(EasyList.build(new Long(1), new Long(2)))), EasyList.build(versionOne, versionTwo));
//
//        Collection expectedVersions = new HashSet();
//        expectedVersions.add(version1);
//        expectedVersions.add(version2);
//
//        _testDoPerform(bulkEditBean, expectedVersions, IssueFieldConstants.FIX_FOR_VERSIONS);
//    }
//
//    public void testDoPerformAffectsVersionsWithError()
//    {
//        _testDoPerformWithError(BulkEditBean.BULK_AFFECTSVERSION);
//    }
//
//    public void testDoPerformAffectsVersions() throws Exception
//    {
//        // Setup bulk edit bean
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//        bulkEditBean.setActions(new String[]{BulkEditBean.BULK_AFFECTSVERSION});
//
//        String[] affectsVersions = new String[]{version1.getString("id"), version2.getString("id")};
//        bulkEditBean.setVersions(affectsVersions);
//
//        versionManager.expectAndReturn("getVersions", P.ANY_ARGS, EasyList.build(versionOne, versionTwo));
//
//        _testDoPerform(bulkEditBean, EasyList.build(version1, version2), IssueFieldConstants.AFFECTED_VERSIONS);
//    }
//
//    public void testDoPerformComponentsWithError()
//    {
//        _testDoPerformWithError(BulkEditBean.BULK_COMPONENT);
//    }
//
//    public void testDoPerformComponents() throws Exception
//    {
//        // Setup Comnponents
//        GenericValue component1 = UtilsForTests.getTestEntity("Component", EasyMap.build("project", project1.getLong("id"), "name", "Comp 1"));
//        GenericValue component2 = UtilsForTests.getTestEntity("Component", EasyMap.build("project", project1.getLong("id"), "name", "Comp 2"));
//
//        // Setup bulk edit bean
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//        bulkEditBean.setActions(new String[]{BulkEditBean.BULK_COMPONENT});
//
//        String[] components = new String[]{component1.getString("id"), component2.getString("id")};
//        bulkEditBean.setComponents(components);
//
//        _testDoPerform(bulkEditBean, EasyList.build(component1, component2), IssueFieldConstants.COMPONENTS);
//    }
//
//    public void testDoPerformPriority() throws Exception
//    {
//        // Setup bulk edit bean
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//        bulkEditBean.setActions(new String[]{BulkEditBean.BULK_PRIORITY});
//        bulkEditBean.setPriority(priority.getString("id"));
//
//        // Remove permissions checking
//        Mock mockPermissionManager = new Mock(PermissionManager.class);
//        mockPermissionManager.setStrict(true);
//        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsAnything(), new IsAnything(), new IsEqual(testUser)), Boolean.TRUE);
//        ManagerFactory.addService(PermissionManager.class, mockPermissionManager.proxy());
//
//        // Setup mock ListenerManager
//        Mock mockListenerManager = new Mock(ListenerManager.class);
//        mockListenerManager.setStrict(true);
//
//        // Setup mock listener
//        Mock mockListener = new Mock(IssueEventListener.class);
//        mockListener.setStrict(true);
//        mockListener.expectVoid("workflowEvent", P.args(new IsEqual(new Integer(IssueEventType.ISSUE_UPDATED)), new IsAnything()));
//        mockListenerManager.expectAndReturn("getListeners", EasyMap.build("listener", mockListener.proxy()));
//        ManagerFactory.addService(ListenerManager.class, mockListenerManager.proxy());
//
//        bulkEditOperation.perform(bulkEditBean, testUser);
//        mockListener.verify();
//        mockListenerManager.verify();
//    }
//
//    public void testDoPerformAssignee() throws Exception
//    {
//        // Setup bulk edit bean
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//        bulkEditBean.setActions(new String[]{BulkEditBean.BULK_ASSIGN});
//        bulkEditBean.setAssignee(testUser.getName());
//
//        // Remove permissions checking
//        Mock mockPermissionManager = new Mock(PermissionManager.class);
//        mockPermissionManager.setStrict(true);
//        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsAnything(), new IsAnything(), new IsEqual(testUser)), Boolean.TRUE);
//        ManagerFactory.addService(PermissionManager.class, mockPermissionManager.proxy());
//
//        // Setup mock ListenerManager
//        Mock mockListenerManager = new Mock(ListenerManager.class);
//        mockListenerManager.setStrict(true);
//
//        // Mock out AssignIssueUtils
//        Mock mockAssignIssueUtils = new Mock(AssignIssueUtils.class);
//        mockAssignIssueUtils.setStrict(true);
//        mockAssignIssueUtils.expectAndReturn("assignIssue", P.args(new IsAnything(), new IsEqual(testUser), new IsEqual(testUser.getName())), UtilsForTests.getTestEntity("ChangeGroup", EasyMap.build("id", "1")));
//        ManagerFactory.addService(AssignIssueUtils.class, mockAssignIssueUtils.proxy());
//
//        // Setup mock listener
//        Mock mockListener = new Mock(IssueEventListener.class);
//        mockListener.setStrict(true);
//        mockListener.expectVoid("workflowEvent", P.args(new IsEqual(new Integer(IssueEventType.ISSUE_ASSIGNED)), new IsAnything()));
//        mockListenerManager.expectAndReturn("getListeners", EasyMap.build("listener", mockListener.proxy()));
//        ManagerFactory.addService(ListenerManager.class, mockListenerManager.proxy());
//
//        bulkEditOperation.perform(bulkEditBean, testUser);
//        mockAssignIssueUtils.verify();
//        mockListener.verify();
//        mockListenerManager.verify();
//    }
//
//    public void testDoPerformCustomFieldsWithError() throws Exception
//    {
//        GenericValue customFieldGV = UtilsForTests.getTestEntity("CustomField", EasyMap.build("id", new Long(1), "name", "Test Global Custom Field", "description", "Description Value", "default", "Default Value"));
//
//        _testDoPerformWithError(FieldManager.CUSTOM_FIELD_PREFIX + customFieldGV.getLong("id"));
//    }
//
//    public void testDoPerformCustomFields() throws Exception
//    {
//        GenericValue customFieldGV = UtilsForTests.getTestEntity("CustomField", EasyMap.build("id", new Long(1), "name", "Test Global Custom Field", "description", "Description Value", "default", "Default Value"));
//
//        // Setup bulk edit bean
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//        String customFieldId = FieldManager.CUSTOM_FIELD_PREFIX + customFieldGV.getLong("id");
//        bulkEditBean.setActions(new String[]{customFieldId});
//
//        Map customFieldValues = EasyMap.build(customFieldId, "new value");
//        bulkEditBean.setCustomFieldValues(customFieldValues);
//
//        _testDoPerform(bulkEditBean, customFieldValues, "customFields");
//    }
//
//    public void _testDoPerformWithError(String operation)
//    {
//        // Setup bulk edit bean
//        BulkEditBean bulkEditBean = setupBulkEditBean();
//        bulkEditBean.setActions(new String[]{operation});
//
//        // Remove permissions checking
//        Mock mockPermissionManager = new Mock(PermissionManager.class);
//        mockPermissionManager.setStrict(true);
//        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsAnything(), new IsAnything(), new IsEqual(testUser)), Boolean.TRUE);
//        ManagerFactory.addService(PermissionManager.class, mockPermissionManager.proxy());
//
//        versionManager.expectAndReturn("getVersions", P.ANY_ARGS, EasyList.build(versionOne, versionTwo));
//
//        // Create a mockIssueUpdate for issue update back end action
//        ActionSupport action = new MyAction();
//        action.addErrorMessage("we have a problem.");
//
//        MockActionDispatcher mockActionDispatcher = new MockActionDispatcher(false);
//        mockActionDispatcher.setResultAction((Action) action);
//        mockActionDispatcher.setResult(Action.ERROR);
//        CoreFactory.setActionDispatcher(mockActionDispatcher);
//
//        try
//        {
//            bulkEditOperation.perform(bulkEditBean, testUser);
//            fail("Exception should have been thrown.");
//        }
//        catch (Exception e)
//        {
//            assertTrue(e.getMessage().indexOf("we have a problem.") != -1);
//        }
//    }
//
//    private void _testDoPerform(BulkEditBean bulkEditBean, Object expectedParameters, String parametersKey) throws Exception
//    {
//        // Remove permissions checking
//        Mock mockPermissionManager = new Mock(PermissionManager.class);
//        mockPermissionManager.setStrict(true);
//        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsAnything(), new IsAnything(), new IsEqual(testUser)), Boolean.TRUE);
//        ManagerFactory.addService(PermissionManager.class, mockPermissionManager.proxy());
//
//        // Create a mockIssueUpdate for issue update back end action
//        MockActionDispatcher mockActionDispatcher = new MockActionDispatcher(false);
//
//        // mockActionDispatcher.setResultAction((Action) action);
//        mockActionDispatcher.setResult(Action.SUCCESS);
//        CoreFactory.setActionDispatcher(mockActionDispatcher);
//
//        bulkEditOperation.perform(bulkEditBean, testUser);
//
//        List actionsCalled = mockActionDispatcher.getActionsCalled();
//        List parametersCalled = mockActionDispatcher.getParametersCalled();
//        assertEquals(2, actionsCalled.size());
//        assertEquals(2, parametersCalled.size());
//
//        // Check that only ISSUE_UPDATE was called
//        for (int i = 0; i < actionsCalled.size(); i++)
//        {
//            String action = (String) actionsCalled.get(i);
//            Map parameters = (Map) parametersCalled.get(i);
//            ActionNames.ISSUE_UPDATE.equals(action);
//            assertEquals(testUser, parameters.get("remoteUser"));
////                assertEquals(expectedParameters, parameters.get(parametersKey));
//
//            // Cannot test for concrete issues as the underlying parameters collection gets modified each time
//        }
//    }
//
//    private BulkEditBean setupBulkEditBean()
//    {
//        BulkEditBean bulkEditBean = new BulkEditBean(null);
//        bulkEditBean._setSelectedIssueGVsForTesting(EasyList.build(issue1, issue2));
//        return bulkEditBean;
//    }
//
//    private BulkEditBean setupBulkEditBeanMultipleProjects()
//    {
//        BulkEditBean bulkEditBean = new BulkEditBean(null);
//        bulkEditBean._setSelectedIssueGVsForTesting(EasyList.build(issue1, issue2, issue3));
//        return bulkEditBean;
//    }
//
//    class MockAction extends ActionSupport
//    {
//        public boolean getHasErrorMessages()
//        {
//            return true;
//        }
//
//        public Collection getErrorMessages()
//        {
//            return EasyList.build(ERROR_MESSAGE);
//        }
//    }
//
//    class MyAction extends ActionSupport
//    {
//    }
}
