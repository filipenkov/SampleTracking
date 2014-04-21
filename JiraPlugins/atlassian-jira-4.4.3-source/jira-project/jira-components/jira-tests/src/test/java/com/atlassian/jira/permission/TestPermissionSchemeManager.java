/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.permission;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

/**
 * TODO: replace this test with TestDefaultPermissionSchemeManager
 */
public class TestPermissionSchemeManager extends LegacyJiraMockTestCase
{
    public TestPermissionSchemeManager(String s)
    {
        super(s);
    }

    public void testGetSchemeId() throws GenericEntityException
    {
        SchemeManager psm = ManagerFactory.getPermissionSchemeManager();
        GenericValue scheme = psm.getScheme(new Long(1));
        assertNull(scheme);

        GenericValue createdScheme = UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("name", "This Name"));
        scheme = psm.getScheme(createdScheme.getLong("id"));
        assertNotNull(scheme);
    }

    public void testGetSchemeName() throws GenericEntityException
    {
        SchemeManager psm = ManagerFactory.getPermissionSchemeManager();
        GenericValue scheme = psm.getScheme("This Name");
        assertNull(scheme);
        UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("name", "This Name"));
        scheme = psm.getScheme("This Name");
        assertNotNull(scheme);
    }

    public void testSchemeExists() throws GenericEntityException
    {
        SchemeManager psm = ManagerFactory.getPermissionSchemeManager();
        assertTrue(!psm.schemeExists("This Name"));
        UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("name", "This Name"));
        assertTrue(psm.schemeExists("This Name"));
    }

    public void testCreateScheme() throws GenericEntityException
    {
        SchemeManager psm = ManagerFactory.getPermissionSchemeManager();
        GenericValue scheme = psm.createScheme("This Name", "Description");
        assertNotNull(scheme);
        scheme = psm.getScheme("This Name");
        assertNotNull(scheme);

        boolean exceptionThrown = false;
        try
        {
            psm.createScheme("This Name", "");
        }
        catch (GenericEntityException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    public void testUpdateScheme() throws GenericEntityException
    {
        SchemeManager psm = ManagerFactory.getPermissionSchemeManager();
        GenericValue scheme = psm.createScheme("This Name", "");
        assertNotNull(scheme);
        scheme.setString("name", "That Name");
        psm.updateScheme(scheme);
        scheme = psm.getScheme("This Name");
        assertNull(scheme);
        scheme = psm.getScheme("That Name");
        assertNotNull(scheme);
    }

    public void testDeleteScheme() throws GenericEntityException
    {
        SchemeManager psm = ManagerFactory.getPermissionSchemeManager();
        GenericValue scheme = psm.createScheme("This Name", "");
        assertNotNull(scheme);
        psm.deleteScheme(scheme.getLong("id"));
        scheme = psm.getScheme("This Name");
        assertNull(scheme);
    }
}
