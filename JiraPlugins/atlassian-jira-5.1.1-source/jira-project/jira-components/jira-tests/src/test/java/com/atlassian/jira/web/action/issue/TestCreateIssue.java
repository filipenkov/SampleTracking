/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.jira.project.DefaultProjectManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.mockobjects.dynamic.Mock;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import org.easymock.EasyMock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

public class TestCreateIssue extends AbstractWebworkTestCase
{
    public TestCreateIssue(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        ManagerFactory.addService(ProjectManager.class, new DefaultProjectManager());
    }

    public void testGettersAndSetters()
    {
        CreateIssue ci = new CreateIssue(null, getMockIssueCreationHelperBean());
        ci.setIssuetype("foo");
        assertEquals("foo", ci.getIssuetype());
    }

    public void testGetSetProject() throws GenericEntityException
    {
        // setup mocks
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(4)));

        // test
        CreateIssue ci = new CreateIssue(null, getMockIssueCreationHelperBean());
        ci.setPid(new Long(4));
        assertEquals(new Long(4), ci.getPid());
        assertEquals(project, ci.getProject());
    }

    public void testGetNullProject() throws GenericEntityException
    {
        CreateIssue ci = new CreateIssue(null, getMockIssueCreationHelperBean());
        assertNull(ci.getProject());
    }

    /**
     * Create two projects with a security permission for one of them. Check that the user can only see one of them in
     * his allowed projects list.
     */
    public void testGetAllowedProjects() throws Exception
    {
        GenericValue firstProject = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(4)));
        UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(5)));

        // create a permission scheme and add only first project to it.
        GenericValue scheme = UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("id", new Long(10), "name", "Test Scheme", "description", "test"));
        ManagerFactory.getPermissionSchemeManager().addSchemeToProject(firstProject, scheme);
        ManagerFactory.getPermissionManager().addPermission(Permissions.CREATE_ISSUE, scheme, null, GroupDropdown.DESC);

        // ManagerFactory.getPermissionManager().addAnonymousPermission(Permissions.CREATE_ISSUE, firstProject);
        // we're not testing the security here (that's in permissionmanager tests) - just that the call was correct
        CreateIssue ci = new CreateIssue(null, getMockIssueCreationHelperBean());
        assertEquals(1, ci.getAllowedProjects().size());
        assertTrue(ci.getAllowedProjects().contains(firstProject));
    }

    public void testDoDefaultNoSelectedProject() throws Exception
    {
        CreateIssue ci = new CreateIssue(null, getMockIssueCreationHelperBean());
        assertEquals(Action.INPUT, ci.doDefault());
        assertNull(ci.getProject());
    }

    public void testDoValidation() throws Exception
    {
        IssueFactory issueFactory = ComponentManager.getComponentInstanceOfType(IssueFactory.class);
        CreateIssue ci = new CreateIssue(issueFactory, getMockIssueCreationHelperBean());
        String result = ci.execute();
        assertEquals(2, ci.getErrors().size());
        assertEquals("No project selected.", ci.getErrors().get("pid"));
        assertEquals("No issue type selected.", ci.getErrors().get("issuetype"));
        assertEquals(Action.INPUT, result);
    }

    private IssueCreationHelperBean getMockIssueCreationHelperBean()
    {
        Mock mockUserUtil = new Mock(UserUtil.class);
        mockUserUtil.expectAndReturn("hasExceededUserLimit", Boolean.FALSE);
        MockI18nBean i18n = new MockI18nBean();
        final JiraContactHelper jiraContactHelper = createMock(JiraContactHelper.class);
        expect(jiraContactHelper.getAdministratorContactMessage(EasyMock.<I18nHelper>anyObject())).andReturn("please contact your JIRA administrators.").anyTimes();
        final JiraLicenseService jiraLicenseService = createMock(JiraLicenseService.class);
        final LicenseDetails licenseDetails = createNiceMock(LicenseDetails.class);
        expect(jiraLicenseService.getLicense()).andStubReturn(licenseDetails);
        expect(licenseDetails.isLicenseSet()).andStubReturn(true);
        replay(jiraLicenseService, licenseDetails, jiraContactHelper);

        return new IssueCreationHelperBeanImpl((UserUtil) mockUserUtil.proxy(),
                ComponentManager.getInstance().getFieldManager(), null, jiraLicenseService, jiraContactHelper);
    }
}
