/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.AbstractPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.crowd.embedded.api.Group;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.User;
import org.easymock.EasyMock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestCloneIssueDetails extends LegacyJiraMockTestCase
{
    Mock mockIssueLinkTypeManager;
    Mock mockSubTaskManager;
    PermissionManager myPermissionManager;
    Mock mockIssueManager;
    GenericValue cloneLink;
    ApplicationPropertiesImpl applicationProperties;
    GenericValue originalIssue;
    Map issueParamsMap;
    User testUser;

    GenericValue componentGV;

    String[] components;

    public TestCloneIssueDetails(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        // Tests fail due to unambiguous definition of IssueIndexManager in VersionManager wihtout a refreshed container
        super.setUp();
        ManagerFactory.quickRefresh();
        mockIssueLinkTypeManager = new Mock(IssueLinkTypeManager.class);
        mockIssueLinkTypeManager.setStrict(true);

        mockSubTaskManager = new Mock(SubTaskManager.class);
        mockSubTaskManager.setStrict(true);

        myPermissionManager = new MyPermissionManager();

        mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.setStrict(true);

        applicationProperties = new MyApplicationProperties("Clones");

        cloneLink = new MockGenericValue("IssueLinkType", EasyMap.build("linkname", "Clones", "inward", "clones", "outward", "is cloned by"));

        issueParamsMap = new HashMap();
        issueParamsMap.put("project", new Long(1));
        issueParamsMap.put("type", "test-type");
        issueParamsMap.put("environment", "test-env");
        issueParamsMap.put("description", "test-desc");
        issueParamsMap.put("reporter", "test-reporter");
        issueParamsMap.put("assignee", "test-assignee");
        issueParamsMap.put("summary", "test-summary");
        issueParamsMap.put("priority", "test-priority");
        issueParamsMap.put("security", new Long(1));
        issueParamsMap.put("key", "TST-1");
        issueParamsMap.put("id", new Long(1));

        originalIssue = new MockGenericValue("Issue", issueParamsMap); //UtilsForTests.getTestEntity("Issue", issueParamsMap);

        componentGV = new MockGenericValue("Component", EasyMap.build("id", new Long(1)));
        components = new String[]{componentGV.getString("id")};
    }

    public void testDisplayWarningWithCloneName() throws GenericEntityException
    {
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkTypesByName", P.args(new IsEqual("Clones")), Collections.EMPTY_LIST);
        applicationProperties = new MyApplicationProperties("Clones");
        CloneIssueDetails cloneIssueDetails = getCloneIssueDetails(applicationProperties);
        assertTrue(cloneIssueDetails.isDisplayCloneLinkWarning());
        mockIssueLinkTypeManager.verify();
    }

    public void testDisplayWarningWithoutCloneName() throws GenericEntityException
    {
        applicationProperties = new MyApplicationProperties("");
        CloneIssueDetails cloneIssueDetails = getCloneIssueDetails(applicationProperties);
        assertFalse(cloneIssueDetails.isDisplayCloneLinkWarning());
    }

    public void testDisplayWarningWithCloneNameThatExists() throws GenericEntityException
    {
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkTypesByName", P.args(new IsEqual("Clones")), EasyList.build(new IssueLinkType(cloneLink, null)));
        applicationProperties = new MyApplicationProperties("Clones");
        CloneIssueDetails cloneIssueDetails = getCloneIssueDetails(applicationProperties);
        assertFalse(cloneIssueDetails.isDisplayCloneLinkWarning());
        mockIssueLinkTypeManager.verify();
    }

    // ---- Helper Methods & Classes----
    private CloneIssueDetails getCloneIssueDetails(ApplicationPropertiesImpl applicationProperties)
    {
        CloneIssueDetails cloneIssueDetails;
        IssueCreationHelperBean issueCreationHelperBean = getMockIssueCreationHelperBean();
        if (applicationProperties != null)
            cloneIssueDetails = new CloneIssueDetails(applicationProperties, myPermissionManager, null, (IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), (SubTaskManager) mockSubTaskManager.proxy(), null, null, issueCreationHelperBean, null, null);
        else
            cloneIssueDetails = new CloneIssueDetails(null, myPermissionManager, null, (IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), (SubTaskManager) mockSubTaskManager.proxy(), null, null, issueCreationHelperBean, null, null);

        return cloneIssueDetails;
    }

    // Mock Application Properties for tests
    private static class MyApplicationProperties extends ApplicationPropertiesImpl
    {
        private final String cloneLinkTypeName;

        public MyApplicationProperties(String cloneLinkTypeName)
        {
            super(null);
            this.cloneLinkTypeName = cloneLinkTypeName;
        }

        public String getDefaultBackedString(String name)
        {
            if (APKeys.JIRA_CLONE_LINKTYPE_NAME.equals(name))
                return cloneLinkTypeName;

            return null;
        }
    }

    private class MyPermissionManager extends AbstractPermissionManager
    {
        public void addPermission(int permissionsId, GenericValue scheme, String parameter, String securityType) throws CreateException
        {

        }

        public boolean hasPermission(int permissionsId, com.atlassian.crowd.embedded.api.User u)
        {
            return true;
        }

        public boolean hasPermission(int permissionsId, GenericValue entity, com.atlassian.crowd.embedded.api.User u)
        {
            return true;
        }

        public boolean hasPermission(int permissionsId, Issue entity, com.atlassian.crowd.embedded.api.User u)
        {
            return true;
        }

        public boolean hasPermission(int permissionsId, GenericValue project, com.atlassian.crowd.embedded.api.User u, boolean issueCreation)
        {
            return true;
        }

        public boolean hasPermission(int permissionsId, Project project, com.atlassian.crowd.embedded.api.User user)
        {
            return true;
        }

        public boolean hasPermission(int permissionsId, Project project, com.atlassian.crowd.embedded.api.User user, boolean issueCreation)
        {
            return true;
        }

        public void removeGroupPermissions(String group) throws RemoveException
        {

        }

        public void removeUserPermissions(String username) throws RemoveException
        {

        }

        public boolean hasProjects(int permissionId, com.atlassian.crowd.embedded.api.User user)
        {
            return false;
        }

        public Collection<Project> getProjectObjects(final int permissionId, final com.atlassian.crowd.embedded.api.User user)
        {
            return null;
        }

        public Collection getProjects(int permissionId, com.atlassian.crowd.embedded.api.User user, GenericValue category)
        {
            return null;
        }

        public Collection<Group> getAllGroups(int permissionId, Project project)
        {
            return null;
        }

        public Collection<com.opensymphony.user.Group> getAllGroups(int permissionId, GenericValue project)
        {
            return null;
        }
    }

    private IssueCreationHelperBean getMockIssueCreationHelperBean()
    {
        Mock mockUserUtil = new Mock(UserUtil.class);
        mockUserUtil.expectAndReturn("hasExceededUserLimit", Boolean.FALSE);
        final JiraLicenseService jiraLicenseService = EasyMock.createMock(JiraLicenseService.class);
        return new IssueCreationHelperBeanImpl((UserUtil) mockUserUtil.proxy(), null, null, jiraLicenseService, null);

    }
}
