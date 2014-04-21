package com.atlassian.jira.project;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @since v4.0
 */
public class TestDefaultProjectManagerUnit extends MockControllerTestCase
{

    @Test
    public void testCreateProjectValidation()
    {
        final DefaultProjectManager projectManager = new DefaultProjectManager(null, null, null, null, null, null, null, null, null, null);
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

        OfBizDelegator ofBizDelegator = mockController.getNiceMock(OfBizDelegator.class);
        ProjectRoleManager projectRoleManager = mockController.getNiceMock(ProjectRoleManager.class);

        mockController.replay();


        final DefaultProjectManager projectManager2 = new DefaultProjectManager(ofBizDelegator, null, null, projectRoleManager, null, null, getMockAvatarManager(), null, null, null);
        try
        {
            projectManager2.createProject("name", "key", null, "lead", null, null);
        }
        catch (IllegalArgumentException e)
        {
            fail("Optional arguments are being required!");
        }
        mockController.verify();
    }

    @Test
    public void testCreateProject()
    {
        final OfBizDelegator ofBizDelegator = mockController.getMock(OfBizDelegator.class);
        final Map params = EasyMap.build(
                "key", "HSP",
                "name", "homosapien",
                "url", "http://blah/",
                "lead", "lead",
                "description", "Project about humans",
                "counter", 0L,
                "assigneetype", 3L);
        params.put("avatar", 12345L);
        ofBizDelegator.createValue("Project", params);
        mockController.setReturnValue(null);
        ProjectRoleManager projectRoleManager = mockController.getMock(ProjectRoleManager.class);
        projectRoleManager.applyDefaultsRolesToProject(new ProjectImpl(null));
        mockController.replay();

        final DefaultProjectManager projectManager2 = new DefaultProjectManager(ofBizDelegator, null, null, projectRoleManager, null, null, getMockAvatarManager(), null, null, null);
        projectManager2.createProject("homosapien", "HSP", "Project about humans", "lead", "http://blah/", new Long(3));
        mockController.verify();
    }

    @Test
    public void testGetProjectObjByKeyIgnoreCaseFoundByAllProjects() throws Exception
    {
        final MockProject mockProject = new MockProject(456, "DEF");
        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(null, null, null, null, null, null, null, null, null, null)
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
        final ProjectFactory projectFactory = mockController.getMock(ProjectFactory.class);
        projectFactory.getProject(genericValue);
        mockController.setReturnValue(new ProjectImpl(genericValue));
        mockController.replay();
        final DefaultProjectManager defaultProjectManager = new DefaultProjectManager(null, null, projectFactory, null, null, null, null, null, null, null)
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
