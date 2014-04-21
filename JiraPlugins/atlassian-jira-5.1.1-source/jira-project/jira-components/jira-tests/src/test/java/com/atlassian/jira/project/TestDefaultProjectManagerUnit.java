package com.atlassian.jira.project;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
public class TestDefaultProjectManagerUnit extends ListeningTestCase
{

    private OfBizDelegator mockDelegator;
    private EventPublisher eventPublisher;

    @Before
    public void setup()
    {
        mockDelegator = mock(OfBizDelegator.class);
        eventPublisher = mock(EventPublisher.class);
        ComponentAccessor.initialiseWorker((new MockComponentWorker().addMock(OfBizDelegator.class, mockDelegator)));
    }
    
    @Test
    public void testCreateProjectValidation()
    {

        final ProjectRoleManager projectRoleManager = mock(ProjectRoleManager.class);
        final GenericValue mockGenericValue = new MockGenericValue("Project", 1000L);
        final ProjectImpl project =   new ProjectImpl(mockGenericValue);

        Map<String, Object> params = MapBuilder.<String, Object>newBuilder().add("key", "key").add("name", "name").add("url", null).add("lead", "lead").add(
            "description", null).add("counter", 0L).add("assigneetype", null).add("avatar", 12345L).toMap();

        when(mockDelegator.createValue("Project", params)).thenReturn(mockGenericValue);


        final DefaultProjectManager projectManager = new DefaultProjectManager(mockDelegator, null, null, null, null, null, null, null, null ,eventPublisher);
        try
        {
            projectManager.createProject(null, "KEY", "Some description", "lead", "http://blah/", new Long(3));
            fail("Should have thrown an error about the name");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("name should not be null!", e.getMessage());
        }
        try
        {
            projectManager.createProject("name", null, "Some description", "lead", "http://blah/", new Long(3));
            fail("Should have thrown an error about the key");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("key should not be null!", e.getMessage());
        }

        try
        {
            projectManager.createProject("name", "key", "Some description", null, "http://blah/", new Long(3));
            fail("Should have thrown an error about the lead");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("lead should not be null!", e.getMessage());
        }




        final DefaultProjectManager projectManager2 = new DefaultProjectManager(mockDelegator, null, null, projectRoleManager, null, getMockAvatarManager(), null, null, null, eventPublisher);
        try
        {
            projectManager2.createProject("name", "key", null, "lead", null, null);
        }
        catch (IllegalArgumentException e)
        {
            fail("Optional arguments are being required!");
        }
        verify(projectRoleManager).applyDefaultsRolesToProject(project);
    }

    @Test
    public void testCreateProject()
    {

        final Map params = EasyMap.build(
                "key", "HSP",
                "name", "homosapien",
                "url", "http://blah/",
                "lead", "lead",
                "description", "Project about humans",
                "counter", 0L,
                "assigneetype", 3L);
        params.put("avatar", 12345L);
        final Map result = new HashMap(params);
        result.put("id", 1000L);
        when(mockDelegator.createValue("Project", params)).thenReturn(new MockGenericValue("Project", result));
        ProjectRoleManager projectRoleManager = mock(ProjectRoleManager.class);

        final DefaultProjectManager projectManager2 = new DefaultProjectManager(mockDelegator, null, null, projectRoleManager, null, getMockAvatarManager(), null, null, null, eventPublisher);
        projectManager2.createProject("homosapien", "HSP", "Project about humans", "lead", "http://blah/", new Long(3));
        verify(projectRoleManager).applyDefaultsRolesToProject(new ProjectImpl(new MockGenericValue("Project", params)));
    }

    @Test
    public void testGetProjectObjByKeyIgnoreCaseFoundByAllProjects() throws Exception
    {
        final MockProject mockProject = new MockProject(456, "DEF");
        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, null, null, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            public GenericValue getProjectByKey(final String key)
            {
                return null;
            }

            @Override
            public List<Project> getProjectObjects() throws DataAccessException
            {
                return CollectionBuilder.<Project>newBuilder(new MockProject(123, "ABC"), mockProject).asList();
            }
        };

        assertEquals(mockProject, defaultProjectManager.getProjectObjByKeyIgnoreCase("dEf"));
    }

    @Test
    public void testGetProjectObjByKeyIgnoreCaseFoundByExactMatch() throws Exception
    {
        final MockGenericValue genericValue = new MockGenericValue("Project", EasyMap.build("id", 123L, "key", "DEF"));
        final ProjectFactory projectFactory = mock(ProjectFactory.class);
        when(projectFactory.getProject(genericValue)).thenReturn(new ProjectImpl(genericValue));
        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(mockDelegator, null, projectFactory, null, null, null, null, null, null, eventPublisher)
        {
            @Override
            public GenericValue getProjectByKey(final String key)
            {
                return genericValue;
            }

            @Override
            public List<Project> getProjectObjects() throws DataAccessException
            {
                return Collections.emptyList();
            }
        };

        assertEquals("DEF", defaultProjectManager.getProjectObjByKeyIgnoreCase("DEF").getKey());
        verify(mockDelegator);
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
