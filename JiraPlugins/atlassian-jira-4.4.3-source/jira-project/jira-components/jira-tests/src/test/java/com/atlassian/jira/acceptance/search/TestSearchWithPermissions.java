/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.acceptance.search;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.scheme.SchemeType;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.QueryImpl;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import org.easymock.classextension.EasyMock;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Acceptance tests for Searching with Permissions of the types we have.
 * This is written so we can ensure that the issue return are the same when we convert this to a lucene parameter.
 * There are currently five permission types defined in permission-types.xml:
 * Current Reporter
 * Current Assignee
 * Single User
 * Project Lead
 * Group Dropdown
 * plus Issue Level Security
 */
public class TestSearchWithPermissions extends AbstractUsersIndexingTestCase
{
    protected GenericValue projectA;
    protected GenericValue projectB;
    protected GenericValue permissionScheme;
    protected GenericValue schemePermission;
    protected User a;
    protected User b;
    protected User c;
    protected User d;
    protected User e;
    protected User f;
    protected User g;
    protected User h;
    protected User i;
    protected User j;
    protected Group group1;
    protected Group group2;
    protected GenericValue issueA;
    protected GenericValue issueB;
    protected GenericValue issueC;
    protected GenericValue issueD;
    protected PermissionTypeManager ptm;

    public TestSearchWithPermissions(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        //Setup users
        setupUsers();

        //Create two projects
        projectA = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "ProjectA", "lead", f.getName()));
        projectB = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "ProjectB", "lead", g.getName()));

        //Create a new permission scheme for this project
        permissionScheme = UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("name", "Permission scheme", "description", "Permission scheme"));

        //Associate the project with permission scheme
        PermissionSchemeManager psm = ManagerFactory.getPermissionSchemeManager();
        psm.addSchemeToProject(projectA, permissionScheme);

        ptm = ManagerFactory.getPermissionTypeManager();

        //Create two issue one that has a reporter a and one that has a reporter b
        issueA = UtilsForTests.getTestEntity("Issue", EasyMap.build("reporter", a.getName(), "assignee", c.getName(), "project", projectA.getLong("id")));
        issueB = UtilsForTests.getTestEntity("Issue", EasyMap.build("reporter", b.getName(), "assignee", d.getName(), "project", projectA.getLong("id")));
        issueC = UtilsForTests.getTestEntity("Issue", EasyMap.build("reporter", a.getName(), "assignee", c.getName(), "project", projectB.getLong("id")));
        issueD = UtilsForTests.getTestEntity("Issue", EasyMap.build("reporter", b.getName(), "assignee", d.getName(), "project", projectB.getLong("id")));

        final FieldVisibilityBean visibilityBean = EasyMock.createMock(FieldVisibilityBean.class);
        EasyMock.expect(visibilityBean.isFieldHidden((String)EasyMock.anyObject(), (Issue)EasyMock.anyObject())).andReturn(false).anyTimes();
        EasyMock.replay(visibilityBean);
        ManagerFactory.addService(FieldVisibilityManager.class, visibilityBean);
        ManagerFactory.addService(FieldVisibilityBean.class, visibilityBean);

        ManagerFactory.getIndexManager().reIndexAll();
    }

    @Override
    protected void tearDown() throws Exception
    {
        // replace mocks with working versions
        ManagerFactory.addService(FieldVisibilityManager.class, new FieldVisibilityBean());
        ManagerFactory.addService(FieldVisibilityBean.class, new FieldVisibilityBean());
        super.tearDown();
    }

    protected void setupUsers()
    {
        //Create user to use during the tests
        //Reporters
        a = UtilsForTests.getTestUser("a");
        b = UtilsForTests.getTestUser("b");

        //Assignees
        c = UtilsForTests.getTestUser("c");
        d = UtilsForTests.getTestUser("d");

        //Single User
        e = UtilsForTests.getTestUser("e");

        //Project Lead from Project A
        f = UtilsForTests.getTestUser("f");

        //Project Lead from Project B
        g = UtilsForTests.getTestUser("g");

        //User with a group
        h = UtilsForTests.getTestUser("h");
        group1 = UtilsForTests.getTestGroup("group1");
        h.addToGroup(group1);

        //Issue Level security user with reporter permission
        i = UtilsForTests.getTestUser("i");
        group2 = UtilsForTests.getTestGroup("group2");
        i.addToGroup(group2);

        j = UtilsForTests.getTestUser("j");
        j.addToGroup(group2);
    }

    public void testGroupPermissionReturnsTheCorrectIssues() throws SearchException
    {
        //Create a Scheme Permission to browse for the groupType "group1"
        SchemeType groupType = ptm.getSchemeType("group");
        schemePermission = UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("scheme", permissionScheme.getLong("id"), "permission", new Long(Permissions.BROWSE), "type", groupType.getType(), "parameter", "group1"));

        SearchProvider defaultProvider = ComponentManager.getComponentInstanceOfType(SearchProvider.class);
        SearchResults results = defaultProvider.search(new QueryImpl(), h, PagerFilter.getUnlimitedFilter());

        //Only two issue should be retrieved
        assertEquals(2, results.getIssues().size());
        assertContainsIds(results.getIssues(), issueA.getLong("id"), issueB.getLong("id"));
    }

    protected void assertContainsIds(List<Issue> issues, Long ... ids)
    {
        Collection<Long> passedIds = new ArrayList<Long>();
        for (Issue issue : issues)
        {
            passedIds.add(issue.getId());
        }
        for (Long id : ids)
        {
            assertTrue(passedIds.contains(id));
        }
    }

}
