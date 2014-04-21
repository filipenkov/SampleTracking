/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.bean;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.security.DefaultPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TestBulkEditBeanImpl extends LegacyJiraMockTestCase
{
    GenericValue project1;
    GenericValue project2;
    GenericValue version1;
    GenericValue version2;
    private GenericValue issue1;
    private GenericValue issue2;
    private GenericValue issue3;
    Map params = new HashMap();
    private GenericValue issuetype1;
    private GenericValue issuetype2;

    public TestBulkEditBeanImpl(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        project1 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC", "counter", new Long(1)));
        project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "XYZ"));

        version1 = UtilsForTests.getTestEntity("Version", EasyMap.build("project", project1.getLong("id"), "name", "Ver 1", "sequence", new Long(1)));
        version2 = UtilsForTests.getTestEntity("Version", EasyMap.build("project", project1.getLong("id"), "name", "Ver 2", "sequence", new Long(2)));

        issuetype1 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "100", "name", "testtype", "description", "test issue type"));
        issuetype2 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "200", "name", "another testtype", "description", "another test issue type"));

        issue1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("project", project1.getLong("id"), "id", new Long(100), "key", "ABC-1", "type", issuetype1.getString("id")));
        issue2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("project", project1.getLong("id"), "id", new Long(101), "key", "ABC-2", "type", issuetype1.getString("id")));
        issue3 = UtilsForTests.getTestEntity("Issue", EasyMap.build("project", project2.getLong("id"), "id", new Long(102), "key", "XYZ-1", "type", issuetype2.getString("id")));

        params.put(BulkEditBean.BULKEDIT_PREFIX + "100", Boolean.TRUE);
        params.put(BulkEditBean.BULKEDIT_PREFIX + "101", Boolean.TRUE);

        ManagerFactory.getProjectManager().refresh();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        params.clear();
    }

    public void testIsMultipleProjects1() throws Exception, GenericEntityException
    {
        setupPermissions(true);

        BulkEditBean be = setupBulkEditBean();
        assertFalse(be.isMultipleProjects());
    }

    private BulkEditBean setupBulkEditBean() throws GenericEntityException
    {
        BulkEditBean be = new BulkEditBeanImpl(ManagerFactory.getIssueManager());
        be.setParams(params);
        be.setIssuesInUse(be.getSelectedIssues());
        return be;
    }

    public void testIsMultipleProjects2() throws Exception, GenericEntityException
    {
        setupPermissions(true);

        BulkEditBean be = new BulkEditBeanImpl(ManagerFactory.getIssueManager());
        params.put(BulkEditBean.BULKEDIT_PREFIX + "102", Boolean.TRUE); // issue 3 which belongs to project 2
        be.setParams(params);
        be.setIssuesInUse(be.getSelectedIssues());
        assertTrue(be.isMultipleProjects());
    }

    public void testGetProjectIds1() throws Exception, GenericEntityException
    {
        BulkEditBean be = setupBulkEditBean();
        assertEquals(1, be.getProjectIds().size());
        assertTrue(be.getProjectIds().contains(new Long(1)));
    }

    public void testGetProjectIds2() throws Exception, GenericEntityException
    {
        BulkEditBean be = new BulkEditBeanImpl(ManagerFactory.getIssueManager());
        params.put(BulkEditBean.BULKEDIT_PREFIX + "102", Boolean.TRUE);
        be.setParams(params);
        be.setIssuesInUse(be.getSelectedIssues());
        assertEquals(2, be.getProjectIds().size());
        assertTrue(be.getProjectIds().contains(new Long(1)));
        assertTrue(be.getProjectIds().contains(new Long(2)));
    }

    public void testGetProjectWithIssues() throws GenericEntityException, PermissionException
    {
        BulkEditBean be = setupBulkEditBean();
        assertEquals(project1, be.getProject());
    }

    public void testGetTypes() throws GenericEntityException
    {
        BulkEditBeanImpl be = new BulkEditBeanImpl(null);
        be._setSelectedIssueGVsForTesting(EasyList.build(issue1, issue2, issue3));

        Collection types = be.getIssueTypes();
        assertEquals(2, types.size());
        
        assertTrue(types.contains(issuetype1.getString("id")));
        assertTrue(types.contains(issuetype2.getString("id")));
    }

    private void setupPermissions(final boolean hasAllPermissions)
    {
        PermissionManager pm = new DefaultPermissionManager()
        {
            public boolean hasPermission(int permissionsId, GenericValue entity, User u)
            {
                if (permissionsId == Permissions.BROWSE) // always need this to be on
                {
                    return true;
                }
                else
                {
                    return hasAllPermissions;
                }
            }
        };

        ManagerFactory.addService(PermissionManager.class, pm);
    }
}
