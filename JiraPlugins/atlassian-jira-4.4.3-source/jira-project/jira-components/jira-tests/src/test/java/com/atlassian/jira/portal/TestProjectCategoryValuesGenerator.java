/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.portal;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.security.DefaultPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TestProjectCategoryValuesGenerator extends LegacyJiraMockTestCase
{
    GenericValue project1;
    GenericValue project2;
    GenericValue project3;
    GenericValue category1;
    GenericValue category2;
    GenericValue category3;

    public TestProjectCategoryValuesGenerator(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        project1 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC"));
        project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "XYZ"));
        project3 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "DEF"));

        category1 = UtilsForTests.getTestEntity("ProjectCategory", EasyMap.build("name", "Support"));
        category2 = UtilsForTests.getTestEntity("ProjectCategory", EasyMap.build("name", "Sales"));
        category3 = UtilsForTests.getTestEntity("ProjectCategory", EasyMap.build("name", "Product"));

        ManagerFactory.getProjectManager().setProjectCategory(project1, category1);
        ManagerFactory.getProjectManager().setProjectCategory(project2, category1);
        ManagerFactory.getProjectManager().setProjectCategory(project3, category2);

        // setup user to only have browse permissions for project 1 and 3
        PermissionManager pm = new DefaultPermissionManager()
        {
            public boolean hasPermission(int permissionsId, GenericValue entity, com.atlassian.crowd.embedded.api.User u)
            {
                if (permissionsId == Permissions.BROWSE) // always need this to be on
                {
                    if (entity.equals(project1) || entity.equals(project3))
                    {
                        return true;
                    }
                }
                return false;
            }
        };
        ManagerFactory.addService(PermissionManager.class, pm);
    }

    public void testGetValues()
    {
        ProjectCategoryValuesGenerator vg = new ProjectCategoryValuesGenerator();
        Map params = new HashMap();
        params.put("User", UtilsForTests.getTestUser("Fred"));
        assertEquals(3, vg.getValues(params).size());
        assertTrue(vg.getValues(params).containsKey(null));
        assertTrue(vg.getValues(params).containsKey(category1.getLong("id").toString()));
        assertTrue(vg.getValues(params).containsKey(category2.getLong("id").toString()));

        // assert that "All Projects" option appears on top of the list
        Iterator i = vg.getValues(params).keySet().iterator();
        String firstSelectOption = (String) i.next();
        assertEquals(null, firstSelectOption);
        assertEquals("All Projects", vg.getValues(params).get(firstSelectOption));
    }
}
