/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.acceptance.search.enterprise;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.scheme.SchemeType;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.QueryImpl;
import org.easymock.classextension.EasyMock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

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
public class TestSearchWithPermissions extends com.atlassian.jira.acceptance.search.TestSearchWithPermissions
{
    private GenericValue issueSecurityScheme;
    private SchemeType reporter;
    private GenericValue issueE;

    private SearchProvider defaultProvider = null;

    public TestSearchWithPermissions(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        defaultProvider = ComponentManager.getComponentInstanceOfType(SearchProvider.class);

        final FieldVisibilityBean visibilityBean = EasyMock.createMock(FieldVisibilityBean.class);
        EasyMock.expect(visibilityBean.isFieldHidden((String)EasyMock.anyObject(), (Issue)EasyMock.anyObject())).andReturn(false).anyTimes();
        EasyMock.replay(visibilityBean);
        ManagerFactory.addService(FieldVisibilityManager.class, visibilityBean);
        ManagerFactory.addService(FieldVisibilityBean.class, visibilityBean);

        reporter = ptm.getSchemeType("reporter");
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        // replace our mock with a working one for the next poor sap who uses PICO
        ManagerFactory.addService(FieldVisibilityManager.class, new FieldVisibilityBean());
        ManagerFactory.addService(FieldVisibilityBean.class, new FieldVisibilityBean());
    }

    public void testCurrentReporterPermissionReturnsTheCorrectIssues() throws SearchException
    {
        //Create a Scheme Permissions to browse for a current reporter
        schemePermission = UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("scheme", permissionScheme.getLong("id"), "permission", new Long(Permissions.BROWSE), "type", reporter.getType()));

        SearchResults results = defaultProvider.search(new QueryImpl(), a, PagerFilter.getUnlimitedFilter());

        //Only one issue should be retrieved
        assertEquals(1, results.getIssues().size());
        assertEquals(issueA.getLong("id"), results.getIssues().iterator().next().getId());
    }

    public void testCurrentAssigneePermissionReturnsTheCorrectIssues() throws SearchException
    {
        //Create a Scheme Permission to browse for a current assignee
        SchemeType assignee = ptm.getSchemeType("assignee");
        schemePermission = UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("scheme", permissionScheme.getLong("id"), "permission", new Long(Permissions.BROWSE), "type", assignee.getType()));

        SearchResults results = defaultProvider.search(new QueryImpl(), c, PagerFilter.getUnlimitedFilter());

        //Only one issue should be retrieved
        assertEquals(1, results.getIssues().size());
        assertEquals(issueA.getLong("id"), results.getIssues().iterator().next().getId());
    }

    public void testSingleUserPermissionReturnsTheCorrectIssues() throws SearchException
    {
        //Create a Scheme Permission to browse for a single user
        SchemeType singleUser = ptm.getSchemeType("user");
        schemePermission = UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("scheme", permissionScheme.getLong("id"), "permission", new Long(Permissions.BROWSE), "type", singleUser.getType(), "parameter", e.getName()));

        SearchResults results = defaultProvider.search(new QueryImpl(), e, PagerFilter.getUnlimitedFilter());

        //Only two issue should be retrieved
        assertEquals(2, results.getIssues().size());
        assertContainsIds(results.getIssues(), issueA.getLong("id"), issueB.getLong("id"));
    }

    public void testProjectLeadPermissionReturnsTheCorrectIssues() throws SearchException
    {
        //Create a Scheme Permission to browse for the project lead
        SchemeType projectLead = ptm.getSchemeType("lead");
        schemePermission = UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("scheme", permissionScheme.getLong("id"), "permission", new Long(Permissions.BROWSE), "type", projectLead.getType()));

        SearchResults results = defaultProvider.search(new QueryImpl(), f, PagerFilter.getUnlimitedFilter());

        //Only two issue should be retrieved
        assertEquals(2, results.getIssues().size());
        assertContainsIds(results.getIssues(), issueA.getLong("id"), issueB.getLong("id"));
    }

    public void testIssueLevelSecurityReturnsTheCorrectIssues() throws SearchException, GenericEntityException, IndexException, CreateException
    {
        ManagerFactory.getPermissionManager().addPermission(Permissions.BROWSE, permissionScheme, "group2", GroupDropdown.DESC);

        //Create a Issue security scheme, level and permission for a user
        IssueSecuritySchemeManager issm = ManagerFactory.getIssueSecuritySchemeManager();

        //Scheme
        issueSecurityScheme = UtilsForTests.getTestEntity("IssueSecurityScheme", EasyMap.build("name", "Issue Security Scheme", "description", "Issue security scheme"));
        issm.addSchemeToProject(projectA, issueSecurityScheme);

        //Level
        GenericValue issueSecurityLevel = UtilsForTests.getTestEntity("SchemeIssueSecurityLevels", EasyMap.build("name", "Issue Security Level", "description", "Issue security level", "scheme", issueSecurityScheme.getLong("id")));

        issueE = UtilsForTests.getTestEntity("Issue", EasyMap.build("reporter", j.getName(), "assignee", d.getName(), "project", projectA.getLong("id"), "security", issueSecurityLevel.getLong("id")));
        ManagerFactory.getIndexManager().reIndexAll();

        SearchResults results = defaultProvider.search(new QueryImpl(), i, PagerFilter.getUnlimitedFilter());

        //Only one issue should be retrieved
        assertEquals(2, results.getIssues().size());
        assertContainsIds(results.getIssues(), issueA.getLong("id"), issueB.getLong("id"));
    }

    public void testIssueLevelSecurityReturnsTheCorrectIssuesWithSecurityLevels() throws SearchException, GenericEntityException, IndexException, CreateException
    {
        ManagerFactory.getPermissionManager().addPermission(Permissions.BROWSE, permissionScheme, "group2", GroupDropdown.DESC);

        //Create a Issue security scheme, level and permission for a user
        IssueSecuritySchemeManager issm = ManagerFactory.getIssueSecuritySchemeManager();

        //Scheme
        issueSecurityScheme = UtilsForTests.getTestEntity("IssueSecurityScheme", EasyMap.build("name", "Issue Security Scheme", "description", "Issue security scheme"));
        issm.addSchemeToProject(projectA, issueSecurityScheme);

        //Level
        GenericValue issueSecurityLevel = UtilsForTests.getTestEntity("SchemeIssueSecurityLevels", EasyMap.build("name", "Issue Security Level", "description", "Issue security level", "scheme", issueSecurityScheme.getLong("id")));

        //Permission
        UtilsForTests.getTestEntity("SchemeIssueSecurities", EasyMap.build("scheme", issueSecurityScheme.getLong("id"), "security", issueSecurityLevel.getLong("id"), "type", reporter.getType()));

        issueE = UtilsForTests.getTestEntity("Issue", EasyMap.build("reporter", j.getName(), "assignee", d.getName(), "project", projectA.getLong("id"), "security", issueSecurityLevel.getLong("id")));
        ManagerFactory.getIndexManager().reIndexAll();

        SearchResults results = defaultProvider.search(new QueryImpl(), j, PagerFilter.getUnlimitedFilter());

        //Only one issue should be retrieved
        assertEquals(3, results.getIssues().size());
        assertContainsIds(results.getIssues(), issueA.getLong("id"), issueB.getLong("id"), issueE.getLong("id"));
    }
}
