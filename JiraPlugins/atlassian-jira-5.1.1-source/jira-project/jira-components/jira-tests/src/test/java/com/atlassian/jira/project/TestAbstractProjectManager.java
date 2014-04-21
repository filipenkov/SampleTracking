/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class TestAbstractProjectManager extends LegacyJiraMockTestCase
{
    public TestAbstractProjectManager(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        ManagerFactory.addService(ProjectManager.class, new DefaultProjectManager());
    }

    /*public void testGetVersions() throws GenericEntityException
    {
        GenericValue version1 = UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(1)));
        GenericValue version2 = UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(2)));

        Collection versions = ManagerFactory.getProjectManager().getVersions(EasyList.build(new Long(2), new Long(1)));

        assertEquals(2, versions.size());
        assertTrue(versions.contains(version1));
        assertTrue(versions.contains(version2));
    }*/

    public void testGetProject() throws GenericEntityException
    {
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1)));
        GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(2), "project", new Long(1)));

        assertEquals(project, ManagerFactory.getProjectManager().getProject(issue));
    }
}
