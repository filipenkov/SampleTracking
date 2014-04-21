/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.plugin.report.impl;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.permission.MockFieldClausePermissionFactory;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import org.easymock.classextension.EasyMock;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.Collections;

public class TestDeveloperWorkloadReport extends AbstractUsersTestCase
{
    private User bob;
    private User bill;
    private GenericValue project1;
    private GenericValue project2;
    private DeveloperWorkloadReport wr;
    private FieldClausePermissionChecker.Factory originalFactory;
    private FieldVisibilityManager origFieldVisibilityManager;

    public TestDeveloperWorkloadReport(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        originalFactory = ComponentManager.getComponentInstanceOfType(FieldClausePermissionChecker.Factory.class);
        ManagerFactory.addService(FieldClausePermissionChecker.Factory.class, new MockFieldClausePermissionFactory());

        origFieldVisibilityManager = ComponentManager.getComponentInstanceOfType(FieldVisibilityManager.class);
        final FieldVisibilityManager visibilityBean = EasyMock.createMock(FieldVisibilityManager.class);
        EasyMock.expect(visibilityBean.isFieldHidden((String)EasyMock.anyObject(), (Issue)EasyMock.anyObject())).andReturn(false).anyTimes();
        EasyMock.replay(visibilityBean);
        ManagerFactory.addService(FieldVisibilityManager.class, visibilityBean);

        bill = createMockUser("bill");
        bob = createMockUser("bob");
        UtilsForTests.getTestEntity("Resolution", EasyMap.build("id", "1"));
        project1 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC", "counter", new Long(1)));
        project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "XYZ"));
        UtilsForTests.getTestEntity("Issue", EasyMap.build("project", project1.getLong("id"), "assignee", bill.getName(), "timeestimate", new Long(DateUtils.DAY_MILLIS), "resolution", "-1"));
        UtilsForTests.getTestEntity("Issue", EasyMap.build("project", project2.getLong("id"), "assignee", bill.getName(), "timeestimate", new Long(DateUtils.HOUR_MILLIS), "resolution", "-1"));
        UtilsForTests.getTestEntity("Issue", EasyMap.build("project", project2.getLong("id"), "assignee", bill.getName(), "timeestimate", new Long(2 * DateUtils.DAY_MILLIS), "resolution", "-1"));

        // create an issue that has already been resolved
        UtilsForTests.getTestEntity("Issue", EasyMap.build("project", project2.getLong("id"), "assignee", bill.getName(), "resolution", "1"));

        // create an issue that has no time estimate. this should be dropped
        UtilsForTests.getTestEntity("Issue", EasyMap.build("project", project2.getLong("id"), "assignee", bill.getName()));

        // create an issue that is not assigned to bill
        UtilsForTests.getTestEntity("Issue", EasyMap.build("project", project2.getLong("id"), "assignee", bob.getName()));

        // reindex the issues created, otherwise they are not searchable
        ManagerFactory.getIndexManager().reIndexAll();

        // set the remoteUser
        JiraTestUtil.loginUser(bill);
        JiraTestUtil.setupAndAssociateDefaultPermissionSchemeWithPermission(project1, Permissions.BROWSE);
        JiraTestUtil.setupAndAssociateDefaultPermissionSchemeWithPermission(project2, Permissions.BROWSE);
        wr = new DeveloperWorkloadReport(null, null, null);
    }

    protected void tearDown() throws Exception
    {
        bob = null;
        bill = null;
        project1 = null;
        project2 = null;
        wr = null;
        super.tearDown();
        ManagerFactory.addService(FieldVisibilityManager.class, origFieldVisibilityManager);
        ManagerFactory.addService(FieldClausePermissionChecker.Factory.class, originalFactory);
    }

    public void testValidation() throws Exception
    {
        ProjectActionSupport projectActionSupport = new ProjectActionSupport();
        DeveloperWorkloadReport wr = new DeveloperWorkloadReport(null, null, null);
        wr.validate(projectActionSupport, Collections.EMPTY_MAP);
        assertFalse(projectActionSupport.getErrors().isEmpty());
        assertEquals(1, projectActionSupport.getErrors().size());
        assertTrue(projectActionSupport.getErrors().containsKey("developer"));
        assertTrue(projectActionSupport.getErrors().containsKey("developer"));
    }

    public void testGetAssignedIssues() throws Exception
    {
        assertEquals(4, wr.initAssignedIssues(bill, bill).size());
    }

    public void testCountMap() throws Exception
    {
        DeveloperWorkloadReport wr = new DeveloperWorkloadReport(null, null, null);

        assertEquals(2, wr.initCountMap(wr.initAssignedIssues(bill, bill)).size());
        assertEquals(new Long(3), wr.getTotalIssuesCount(wr.initCountMap(wr.initAssignedIssues(bill, bill))));
    }

    public void testWorkloadMap() throws Exception
    {
        assertEquals(2, wr.initWorkloadMap(wr.initAssignedIssues(bill, bill)).size());
    }

    public void testTotals() throws Exception
    {
        assertEquals(new Long(3), wr.getTotalIssuesCount(wr.initCountMap(wr.initAssignedIssues(bill, bill))));
    }
}
