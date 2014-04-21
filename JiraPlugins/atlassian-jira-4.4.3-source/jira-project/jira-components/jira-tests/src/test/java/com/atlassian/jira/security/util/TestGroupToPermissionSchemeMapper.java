/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.util;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.permission.DefaultPermissionSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.security.Permissions;
import com.opensymphony.user.Group;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TestGroupToPermissionSchemeMapper extends LegacyJiraMockTestCase
{
    private GenericValue schemeA;
    private GenericValue schemeB;
    private GenericValue schemeC;
    private Group groupA;
    private Group groupB;
    private Group groupC;

    public TestGroupToPermissionSchemeMapper(String s)
    {
        super(s);
    }

    public void testMapper() throws GenericEntityException
    {
        PermissionSchemeManager permissionSchemeManager = new MockPermissionSchemeManager();

        // Setup a list of schemes
        schemeA = UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("name", "test scheme A"));
        schemeB = UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("name", "test scheme B"));
        schemeC = UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("name", "test scheme C"));

        // Setup Groups
        groupA = UtilsForTests.getTestGroup("test group A");
        groupB = UtilsForTests.getTestGroup("test group B");
        groupC = UtilsForTests.getTestGroup("test group C");

        SchemePermissions schemePermissions = new SchemePermissions();
        GroupToPermissionSchemeMapper mapper = new GroupToPermissionSchemeMapper(permissionSchemeManager, schemePermissions);

        // Ensure the mapper returns the coorect results
        Collection permissionSchemes = mapper.getMappedValues(groupA.getName());
        List expectedSchemes = EasyList.build(schemeA);
        assertEquals(expectedSchemes, permissionSchemes);

        permissionSchemes = mapper.getMappedValues(groupB.getName());
        expectedSchemes = EasyList.build(schemeB, schemeC);
        assertEquals(expectedSchemes, permissionSchemes);

        permissionSchemes = mapper.getMappedValues(groupC.getName());
        expectedSchemes = EasyList.build(schemeA, schemeC);
        assertEquals(expectedSchemes, permissionSchemes);

        assertTrue(mapper.getMappedValues("non existant group").isEmpty());
    }

    // Build a mock for the manager so that we can return what we want from the methods called by the
    // GroupToPermissionSchemeMapper
    private class MockPermissionSchemeManager extends DefaultPermissionSchemeManager
    {
        public MockPermissionSchemeManager()
        {
            super(null, null, null, null, null, null, null);
        }

        public List<GenericValue> getSchemes() 
        {
            return EasyList.build(schemeA, schemeB, schemeC);
        }

        public List<GenericValue> getEntities(GenericValue scheme, Long permissionId)
        {
            if (schemeA.equals(scheme) && permissionId.intValue() == Permissions.COMMENT_ISSUE)
            {
                return EasyList.build(UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("type", "group", "parameter", groupA.getName())), UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("type", "group", "parameter", groupC.getName())));
            }
            else if (schemeB.equals(scheme) && permissionId.intValue() == Permissions.BROWSE)
            {
                return EasyList.build(UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("type", "group", "parameter", groupB.getName())));
            }
            else if (schemeC.equals(scheme) && permissionId.intValue() == Permissions.CREATE_ISSUE)
            {
                // The record of type 'user' should be ignored by the mapper.
                return EasyList.build(UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("type", "group", "parameter", groupB.getName())), UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("type", "group", "parameter", groupC.getName())), UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("type", "user", "parameter", "test user")));
            }
            else
            {
                return Collections.EMPTY_LIST;
            }
        }
    }
}
