package com.atlassian.jira.project;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MockIssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.dispatcher.ActionResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestDefaultProjectManagerProjectObj extends ListeningTestCase
{

    private OfBizDelegator mockDelegator;
    private EventPublisher mockEventPublisher;

    @Before
    public void setup()
    {
        mockDelegator = mock(OfBizDelegator.class);
        mockEventPublisher = mock(EventPublisher.class);
        ComponentAccessor.initialiseWorker((new MockComponentWorker().addMock(OfBizDelegator.class, mockDelegator)));
    }

    @Test
    public void testCreateProject()
    {
        final ProjectRoleManager projectRoleManager = mock(ProjectRoleManager.class);

        Map projectMap = EasyMap.build("key", "KEY", "name", "Name", "lead", "admin", "description", null, "url", null, "counter", 0L, "assigneetype", null);
        projectMap.put("avatar", 12345L);
        final GenericValue mockProjectGV = new MockGenericValue("Project", projectMap);
        mockProjectGV.set("id", 1000L);
        final Project mockProject = new ProjectImpl(mockProjectGV);

        when(mockDelegator.createValue("Project", projectMap)).thenReturn(mockProjectGV);

        DefaultProjectManager projectManager = new DefaultProjectManager(mockDelegator, null, null, projectRoleManager, null,
                getMockAvatarManager(), null, null, null, mockEventPublisher);

        Project project = projectManager.createProject("Name", "KEY", null, "admin", null, null);
        verify(projectRoleManager).applyDefaultsRolesToProject(mockProject);
        assertNotNull(project);
        assertEquals("KEY", project.getKey());
        assertEquals("Name", project.getName());

    }

    @Test
    public void testCreateProjectForNullValues() throws GenericEntityException
    {
        DefaultProjectManager projectManager = new DefaultProjectManager(mockDelegator, null, null, null,
                null, null, null, null, null, mockEventPublisher);

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
    public void testUpdateProject() throws GenericEntityException
    {
        final IssueSecurityLevelManager issueSecurityLevelManager = mock(IssueSecurityLevelManager.class);
        issueSecurityLevelManager.clearUsersLevels();

        MockProject mockProject = new MockProject(12);

        OfBizDelegator ofBizDelegator = new MockOfBizDelegator();
        ofBizDelegator.store(new MockGenericValue("Project", FieldMap.build("id", 12L).add("key", "KEY")));

        ProjectFactory proFac = new MockProjectFactory();
        DefaultProjectManager projectManager = new DefaultProjectManager(ofBizDelegator, null, proFac, null, null, null, null, null, null, null)
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
    }

    @Test
    public void testUpdateProjectForNullValues() throws GenericEntityException
    {
        DefaultProjectManager projectManager = new DefaultProjectManager(mockDelegator, null, null, null, null, null,
                null, null, null, mockEventPublisher);

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
        final ProjectRoleManager projectRoleManager = mock(ProjectRoleManager.class);
        final IssueManager issueManager = mock(IssueManager.class);

        Map projectMap = EasyMap.build("key", "KEY", "name", "Name", "lead", "admin", "description", null, "url", null, "counter", 0L, "assigneetype", null);
        final GenericValue mockProjectGV = new MockGenericValue("Project", projectMap);
        final Project mockProject = new ProjectImpl(mockProjectGV);
        when(issueManager.getIssueIdsForProject(mockProject.getId())).thenReturn(EasyList.build(1L, 2L));
        MutableIssue issue1 = MockIssueFactory.createIssue(1);
        MutableIssue issue2 = MockIssueFactory.createIssue(2);
        ActionResult actionResult = new ActionResult("success", null, EasyList.build(), null);

        when(issueManager.getIssueObject(1L)).thenReturn(issue1);
        issueManager.deleteIssueNoEvent(issue1);

        when(issueManager.getIssueObject(2L)).thenReturn(issue2);
        issueManager.deleteIssueNoEvent(issue2);


        DefaultProjectManager projectManager = new DefaultProjectManager(mockDelegator, null, null, projectRoleManager, issueManager,
                null, null, null, null, null);

        projectManager.removeProjectIssues(mockProject);


    }

    @Test
    public void testRemoveProjectIssuesnNullProject() throws Exception
    {
        DefaultProjectManager projectManager = new DefaultProjectManager(mockDelegator, null, null, null, null,
                null, null, null, null, mockEventPublisher);
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
        final ProjectRoleManager projectRoleManager = mock(ProjectRoleManager.class);
        final IssueManager issueManager = mock(IssueManager.class);

        Map projectMap = EasyMap.build("key", "KEY", "name", "Name", "lead", "admin", "description", null, "url", null, "counter", 0L, "assigneetype", null);
        final GenericValue mockProjectGV = new MockGenericValue("Project", projectMap);
        final Project mockProject = new ProjectImpl(mockProjectGV);
        when(issueManager.getIssueIdsForProject(mockProject.getId())).thenReturn(EasyList.build(1L, 2L));

        when(issueManager.getIssueObject(1L)).thenReturn(null);

        when(issueManager.getIssueObject(2L)).thenReturn(null);

        DefaultProjectManager projectManager = new DefaultProjectManager(mockDelegator, null, null, projectRoleManager, issueManager,
                null, null, null, null, mockEventPublisher);
                                                                   
        projectManager.removeProjectIssues(mockProject);

    }

    @Test
    public void testRemoveProjectNullProject() throws Exception
    {
        DefaultProjectManager projectManager = new DefaultProjectManager(mockDelegator, null, null, null, null, null, null, null, null, mockEventPublisher);
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

        final ProjectFactory projectFactory = mock(ProjectFactory.class);

        Map projectMap = EasyMap.build("id", 1000L, "key", "KEY", "name", "Name", "lead", "admin", "description", null, "url", null, "counter", 0L, "assigneetype", null);
        final GenericValue mockProjectGV = new MockGenericValue("Project", projectMap);
        final List<GenericValue> projectGVs = new ArrayList<GenericValue>();
        projectGVs.add(mockProjectGV);

        final Project mockProject = new ProjectImpl(mockProjectGV);
        final List<Project> projects = Collections.singletonList(mockProject);

        when(mockDelegator.findAll("Project", EasyList.build("name"))).thenReturn(projectGVs);

        when(projectFactory.getProjects(projectGVs)).thenReturn(projects);

        DefaultProjectManager projectManager = new DefaultProjectManager(mockDelegator, null, projectFactory, null, null, null, null, null, null, mockEventPublisher);

        assertEquals(projects, projectManager.getProjectObjects());

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
