/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.acceptance.search.enterprise;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.CurrentAssignee;
import com.atlassian.jira.security.type.CurrentReporter;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.security.type.ProjectLead;
import com.atlassian.jira.security.type.SingleUser;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.QueryImpl;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import org.easymock.classextension.EasyMock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class TestSearchWithIssueLevelPermissions extends AbstractUsersIndexingTestCase
{
    private GenericValue projectC;
    private GenericValue permissionScheme;
    private User j;
    private User k;
    private User l;
    private User m;
    private User n;
    private GenericValue issueJ;
    private GenericValue issueK;
    private GenericValue issueL;
    private GenericValue issueM;
    private GenericValue issueN;
    private SearchProvider defaultProvider;
    private FieldVisibilityManager origFieldVisibilityManager;


    public TestSearchWithIssueLevelPermissions(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        origFieldVisibilityManager = ComponentManager.getComponentInstanceOfType(FieldVisibilityManager.class);

        final FieldVisibilityManager visibilityBean = EasyMock.createMock(FieldVisibilityManager.class);
        EasyMock.expect(visibilityBean.isFieldHidden((String)EasyMock.anyObject(), (Issue)EasyMock.anyObject())).andReturn(false).anyTimes();
        EasyMock.replay(visibilityBean);
        ManagerFactory.addService(FieldVisibilityManager.class, visibilityBean);

        // Instantiate this after the super's setUp is called as the super class installs memory index manager
        defaultProvider = ComponentManager.getComponentInstanceOfType(SearchProvider.class);

        Group allGroup = UtilsForTests.getTestGroup("All Group");

        j = UtilsForTests.getTestUser("j");
        j.addToGroup(allGroup);
        k = UtilsForTests.getTestUser("k");

        Group lGroup = UtilsForTests.getTestGroup("L Group");
        k.addToGroup(allGroup);
        k.addToGroup(lGroup);
        l = UtilsForTests.getTestUser("l");
        l.addToGroup(allGroup);
        m = UtilsForTests.getTestUser("m");
        m.addToGroup(allGroup);
        n = UtilsForTests.getTestUser("n");
        n.addToGroup(allGroup);

        projectC = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "ProjectA", "lead", m.getName()));

        //Create a new permission scheme for this project
        permissionScheme = UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("name", "Permission scheme", "description", "Permission scheme"));

        //Associate the project with permission scheme
        PermissionSchemeManager psm = ManagerFactory.getPermissionSchemeManager();
        psm.addSchemeToProject(projectC, permissionScheme);

        //Create a Scheme Permissions to browse for all users
        GenericValue schemePermission = UtilsForTests.getTestEntity("SchemePermissions", EasyMap.build("scheme", permissionScheme.getLong("id"), "permission", new Long(Permissions.BROWSE), "type", GroupDropdown.DESC));

        //Create issue level security scheme and assign it to a project
        IssueSecuritySchemeManager issueSecuritySchemeManager = ManagerFactory.getIssueSecuritySchemeManager();
        GenericValue defaultIssueSecurityScheme = issueSecuritySchemeManager.createDefaultScheme();
        issueSecuritySchemeManager.addDefaultSchemeToProject(projectC);

        //Create an issue security level for each type of security
        GenericValue currentreporterlevel = EntityUtils.createValue("SchemeIssueSecurityLevels", EasyMap.build("scheme", defaultIssueSecurityScheme.getLong("id"), "name", "currentreporterlevel", "description", ""));
        GenericValue grouplevel = EntityUtils.createValue("SchemeIssueSecurityLevels", EasyMap.build("scheme", defaultIssueSecurityScheme.getLong("id"), "name", "grouplevel", "description", ""));
        GenericValue singleuserlevel = EntityUtils.createValue("SchemeIssueSecurityLevels", EasyMap.build("scheme", defaultIssueSecurityScheme.getLong("id"), "name", "singleuserlevel", "description", ""));
        GenericValue projectleadlevel = EntityUtils.createValue("SchemeIssueSecurityLevels", EasyMap.build("scheme", defaultIssueSecurityScheme.getLong("id"), "name", "projectleadlevel", "description", ""));
        GenericValue currentassigneelevel = EntityUtils.createValue("SchemeIssueSecurityLevels", EasyMap.build("scheme", defaultIssueSecurityScheme.getLong("id"), "name", "currentassigneelevel", "description", ""));

        //Create a Scheme Issue Security for each level
        SchemeEntity currentreporterentity = new SchemeEntity(CurrentReporter.DESC, currentreporterlevel.getLong("id"));
        ManagerFactory.getIssueSecuritySchemeManager().createSchemeEntity(defaultIssueSecurityScheme, currentreporterentity);

        SchemeEntity groupentity = new SchemeEntity(GroupDropdown.DESC, lGroup.getName(), grouplevel.getLong("id"));
        ManagerFactory.getIssueSecuritySchemeManager().createSchemeEntity(defaultIssueSecurityScheme, groupentity);

        SchemeEntity singleuserentity = new SchemeEntity(SingleUser.DESC, l.getName(), singleuserlevel.getLong("id"));
        ManagerFactory.getIssueSecuritySchemeManager().createSchemeEntity(defaultIssueSecurityScheme, singleuserentity);

        SchemeEntity projectleadentity = new SchemeEntity(ProjectLead.DESC, projectleadlevel.getLong("id"));
        ManagerFactory.getIssueSecuritySchemeManager().createSchemeEntity(defaultIssueSecurityScheme, projectleadentity);

        SchemeEntity currentassigneeentity = new SchemeEntity(CurrentAssignee.DESC, currentassigneelevel.getLong("id"));
        ManagerFactory.getIssueSecuritySchemeManager().createSchemeEntity(defaultIssueSecurityScheme, currentassigneeentity);

        //Create five issues, one for each Issue Level
        issueJ = UtilsForTests.getTestEntity("Issue", EasyMap.build("reporter", j.getName(), "assignee", j.getName(), "project", projectC.getLong("id"), "security", currentreporterlevel.getLong("id")));
        issueK = UtilsForTests.getTestEntity("Issue", EasyMap.build("reporter", k.getName(), "assignee", k.getName(), "project", projectC.getLong("id"), "security", grouplevel.getLong("id")));
        issueL = UtilsForTests.getTestEntity("Issue", EasyMap.build("reporter", l.getName(), "assignee", l.getName(), "project", projectC.getLong("id"), "security", singleuserlevel.getLong("id")));
        issueM = UtilsForTests.getTestEntity("Issue", EasyMap.build("reporter", m.getName(), "assignee", m.getName(), "project", projectC.getLong("id"), "security", projectleadlevel.getLong("id")));
        issueN = UtilsForTests.getTestEntity("Issue", EasyMap.build("reporter", n.getName(), "assignee", n.getName(), "project", projectC.getLong("id"), "security", currentassigneelevel.getLong("id")));

        //Reindex to include the issue created
        ManagerFactory.getIndexManager().reIndexAll();
    }

    @Override
    protected void tearDown() throws Exception
    {
        ManagerFactory.addService(FieldVisibilityManager.class, origFieldVisibilityManager);
        super.tearDown();
    }

    public void testCurrentReporterIssueLevelPermissionReturnsTheCorrectIssues() throws GenericEntityException, SearchException, IndexException
    {
        SearchResults results = defaultProvider.search(new QueryImpl(), j, PagerFilter.getUnlimitedFilter());

        //Only one issue should be retrieved
        assertEquals(1, results.getIssues().size());
        assertEquals(issueJ.getLong("id"), results.getIssues().iterator().next().getId());
    }

    public void testGroupIssueLevelPermissionReturnsTheCorrectIssues() throws GenericEntityException, SearchException, IndexException
    {
        SearchResults results = defaultProvider.search(new QueryImpl(), k, PagerFilter.getUnlimitedFilter());

        //Only one issue should be retrieved
        assertEquals(1, results.getIssues().size());
        assertEquals(issueK.getLong("id"), results.getIssues().iterator().next().getId());
    }

    public void testSingleUserIssueLevelPermissionReturnsTheCorrectIssues() throws GenericEntityException, SearchException, IndexException
    {
        SearchResults results = defaultProvider.search(new QueryImpl(), l, PagerFilter.getUnlimitedFilter());

        //Only one issue should be retrieved
        assertEquals(1, results.getIssues().size());
        assertEquals(issueL.getLong("id"), results.getIssues().iterator().next().getId());
    }

    public void testProjectLoadLevelPermissionReturnsTheCorrectIssues() throws GenericEntityException, SearchException, IndexException
    {
        SearchResults results = defaultProvider.search(new QueryImpl(), m, PagerFilter.getUnlimitedFilter());

        //Only one issue should be retrieved
        assertEquals(1, results.getIssues().size());
        assertEquals(issueM.getLong("id"), results.getIssues().iterator().next().getId());
    }

    public void testCurrentAssigneeLevelPermissionReturnsTheCorrectIssues() throws GenericEntityException, SearchException, IndexException
    {
        SearchResults results = defaultProvider.search(new QueryImpl(), n, PagerFilter.getUnlimitedFilter());

        //Only one issue should be retrieved
        assertEquals(1, results.getIssues().size());
        assertEquals(issueN.getLong("id"), results.getIssues().iterator().next().getId());
    }
}
