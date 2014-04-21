package com.atlassian.jira.project;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.action.ActionDispatcher;
import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.opensymphony.user.EntityNotFoundException;
import org.easymock.EasyMock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.dispatcher.ActionResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @since v4.0
 */
public class TestDefaultProjectManagerProjectObj extends MockControllerTestCase
{
    @Test
    public void testCreateProject()
    {
        final OfBizDelegator delegator = mockController.getMock(OfBizDelegator.class);
        final ProjectRoleManager projectRoleManager = mockController.getMock(ProjectRoleManager.class);

        Map projectMap = EasyMap.build("key", "KEY", "name", "Name", "lead", "admin", "description", null, "url", null, "counter", 0L, "assigneetype", null);
        projectMap.put("avatar", 12345L);
        final GenericValue mockProjectGV = new MockGenericValue("Project", projectMap);
        final Project mockProject = new ProjectImpl(mockProjectGV);

        delegator.createValue("Project", projectMap);
        mockController.setReturnValue(mockProjectGV);
        projectRoleManager.applyDefaultsRolesToProject(mockProject);
        mockController.replay();

        DefaultProjectManager projectManager = new DefaultProjectManager(delegator, null, null, projectRoleManager, null,
                null, getMockAvatarManager(), null, null, null);

        Project project = projectManager.createProject("Name", "KEY", null, "admin", null, null);
        assertNotNull(project);
        assertEquals("KEY", project.getKey());
        assertEquals("Name", project.getName());

        mockController.verify();
    }

    @Test
    public void testCreateProjectForNullValues() throws GenericEntityException, EntityNotFoundException
    {
        DefaultProjectManager projectManager = new DefaultProjectManager(null, null, null, null, null,
                null, null, null, null, null);

        try
        {
            projectManager.createProject(null, null, null, null, null, null);
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            //
        }

        try
        {
            projectManager.createProject("Name", null, null, null, null, null);
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            //
        }

        try
        {
            projectManager.createProject("Name", "ABC", null, null, null, null);
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            //
        }
    }

    @Test
    public void testUpdateProject() throws GenericEntityException, EntityNotFoundException
    {
        final OfBizDelegator delegator = mockController.getMock(OfBizDelegator.class);
        final ProjectFactory projectFactory = mockController.getMock(ProjectFactory.class);
        final IssueSecurityLevelManager issueSecurityLevelManager = mockController.getMock(IssueSecurityLevelManager.class);

        Map projectMap = EasyMap.build("key", "KEY", "name", "new Name", "lead", "newlead", "description", "description", "url", "http://someurl.com", "counter", 0L, "assigneetype", null);
        final GenericValue mockProjectGV = new MockGenericValue("Project", projectMap);
        final Project mockProject = new ProjectImpl(mockProjectGV);

        delegator.store(mockProjectGV);
        delegator.findByPrimaryKey("Project", EasyMap.build("id", null));
        mockController.setReturnValue(mockProjectGV);
        projectFactory.getProject(mockProjectGV);
        mockController.setReturnValue(mockProject);
        issueSecurityLevelManager.clearUsersLevels();
        EasyMock.expectLastCall();
        mockController.replay();


        DefaultProjectManager projectManager = new DefaultProjectManager(delegator, null, projectFactory, null, null,
                null, getMockAvatarManager(), null, null, null)
        {
            @Override
            IssueSecurityLevelManager getIssueSecurityLevelManager()
            {
                return issueSecurityLevelManager;
            }
        };

        Project project = projectManager.updateProject(mockProject, "new Name", "description", "admin", null, null);
        assertNotNull(project);
        assertEquals("KEY", project.getKey());
        assertEquals("new Name", project.getName());
        assertEquals("description", project.getDescription());


        mockController.verify();

    }

    @Test
    public void testUpdateProjectForNullValues() throws GenericEntityException, EntityNotFoundException
    {
        DefaultProjectManager projectManager = new DefaultProjectManager(null, null, null, null, null,
                null, null, null, null, null);

        try
        {
            projectManager.updateProject(null, null, null, null, null, null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //
        }

        final Project mockProject = new ProjectImpl(null);
        try
        {
            projectManager.updateProject(mockProject, null, null, null, null, null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //
        }

        try
        {
            projectManager.updateProject(mockProject, "some new name", null, null, null, null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //
        }
    }

    @Test
    public void testRemoveProjectIssues() throws Exception
    {
        final OfBizDelegator delegator = mockController.getMock(OfBizDelegator.class);
        final ProjectRoleManager projectRoleManager = mockController.getMock(ProjectRoleManager.class);
        final IssueManager issueManager = mockController.getMock(IssueManager.class);
        final ActionDispatcher actionDispatcher = mockController.getMock(ActionDispatcher.class);

        Map projectMap = EasyMap.build("key", "KEY", "name", "Name", "lead", "admin", "description", null, "url", null, "counter", 0L, "assigneetype", null);
        final GenericValue mockProjectGV = new MockGenericValue("Project", projectMap);
        final Project mockProject = new ProjectImpl(mockProjectGV);
        issueManager.getIssueIdsForProject(mockProject.getId());
        mockController.setReturnValue(EasyList.build(1L, 2L));
        MockGenericValue issue1 = new MockGenericValue("Issue", EasyMap.build());
        MockGenericValue issue2 = new MockGenericValue("Issue", EasyMap.build());
        ActionResult actionResult = new ActionResult("success", null, EasyList.build(), null);

        issueManager.getIssue(1L);
        mockController.setReturnValue(issue1);
        actionDispatcher.execute("com.atlassian.jira.action.issue.IssueDelete", EasyMap.build(
                "issue", issue1, "dispatchEvent", Boolean.FALSE, "permissionOverride", Boolean.TRUE));
        mockController.setReturnValue(actionResult);

        issueManager.getIssue(2L);
        mockController.setReturnValue(issue2);
        actionDispatcher.execute("com.atlassian.jira.action.issue.IssueDelete", EasyMap.build(
                "issue", issue2, "dispatchEvent", Boolean.FALSE, "permissionOverride", Boolean.TRUE));
        mockController.setReturnValue(actionResult);

        mockController.replay();

        DefaultProjectManager projectManager = new DefaultProjectManager(delegator, null, null, projectRoleManager, issueManager,
                actionDispatcher, null, null, null, null);

        projectManager.removeProjectIssues(mockProject);

        mockController.verify();

    }

    @Test
    public void testRemoveProjectIssuesnNullProject() throws Exception
    {
        DefaultProjectManager projectManager = new DefaultProjectManager(null, null, null, null, null,
                null, null, null, null, null);
        try
        {
            projectManager.removeProjectIssues(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //
        }
    }

    @Test
    public void testRemoveProjectIssuesnNullIssueGV() throws Exception
    {
        final OfBizDelegator delegator = mockController.getMock(OfBizDelegator.class);
        final ProjectRoleManager projectRoleManager = mockController.getMock(ProjectRoleManager.class);
        final IssueManager issueManager = mockController.getMock(IssueManager.class);
        final ActionDispatcher actionDispatcher = mockController.getMock(ActionDispatcher.class);

        Map projectMap = EasyMap.build("key", "KEY", "name", "Name", "lead", "admin", "description", null, "url", null, "counter", 0L, "assigneetype", null);
        final GenericValue mockProjectGV = new MockGenericValue("Project", projectMap);
        final Project mockProject = new ProjectImpl(mockProjectGV);
        issueManager.getIssueIdsForProject(mockProject.getId());
        mockController.setReturnValue(EasyList.build(1L, 2L));

        issueManager.getIssue(1L);
        mockController.setReturnValue(null);

        issueManager.getIssue(2L);
        mockController.setReturnValue(null);

        mockController.replay();

        DefaultProjectManager projectManager = new DefaultProjectManager(delegator, null, null, projectRoleManager, issueManager,
                actionDispatcher, null, null, null, null);

        projectManager.removeProjectIssues(mockProject);

        mockController.verify();
    }

    @Test
    public void testRemoveProjectNullProject() throws Exception
    {
        DefaultProjectManager projectManager = new DefaultProjectManager(null, null, null, null, null, null, null, null, null, null);
        try
        {
            projectManager.removeProject(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            //
        }
    }

    @Test
    public void testGetAllProjectObjects() throws Exception
    {
        final OfBizDelegator delegator = mockController.getMock(OfBizDelegator.class);
        final ProjectFactory projectFactory = mockController.getMock(ProjectFactory.class);

        Map projectMap = EasyMap.build("key", "KEY", "name", "Name", "lead", "admin", "description", null, "url", null, "counter", 0L, "assigneetype", null);
        final GenericValue mockProjectGV = new MockGenericValue("Project", projectMap);
        final List<GenericValue> projectGVs = new ArrayList<GenericValue>();
        projectGVs.add(mockProjectGV);

        final Project mockProject = new ProjectImpl(mockProjectGV);
        final List<Project> projects = Collections.singletonList(mockProject);

        delegator.findAll("Project", EasyList.build("name"));
        mockController.setReturnValue(projectGVs);

        projectFactory.getProjects(projectGVs);
        mockController.setReturnValue(projects);

        mockController.replay();

        DefaultProjectManager projectManager = new DefaultProjectManager(delegator, null, projectFactory, null, null, null, null, null, null, null);

        assertEquals(projects, projectManager.getProjectObjects());

        mockController.verify();
    }

    private AvatarManager getMockAvatarManager()
    {
        return (AvatarManager) DuckTypeProxy.getProxy(AvatarManager.class, new Object()
        {
            public Long getDefaultAvatarId(Avatar.Type ofType)
            {
                return 12345L;
            }
        });
    }
}
