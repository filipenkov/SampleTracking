package com.atlassian.jira.project;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.user.UserUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.project.version.VersionManager;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.mockobjects.dynamic.Mock;
import com.opensymphony.user.DuplicateEntityException;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.ImmutableException;
import com.opensymphony.user.User;

import java.util.Map;

/**
 * Simple test to verify that the project factory generates valid project objects
 * from a GenericValue.
 */
public class TestProjectFactory extends LegacyJiraMockTestCase
{
    private static final Long PROJECT_ID = new Long(1);
    private static final String PROJECT_NAME = "Test Project";
    private static final String PROJECT_URL = "http://jira.atlassian.com";
    private static final String PROJECT_LEAD = "test";
    private static final String PROJECT_DESC = "this is a desc";
    private static final String PROJECT_KEY = "TST";
    private static final Long PROJECT_COUNT = new Long(12);
    private static final Long PROJECT_ASS_TYPE = new Long(2);

    public TestProjectFactory(final String string)
    {
        super(string);
    }

    public void testCreateFromNull() throws ImmutableException, DuplicateEntityException, GenericEntityException, EntityNotFoundException
    {
        assertNull(new DefaultProjectFactory().getProject(null));
    }

    public void testCreateFromGenericValue() throws ImmutableException, DuplicateEntityException, GenericEntityException, EntityNotFoundException
    {
        // Its ok to pass in null managers since we do not exercise them in this test
        final DefaultProjectFactory defaultProjectFactory = new DefaultProjectFactory();
        final Map params = EasyMap.build("id", PROJECT_ID, "name", PROJECT_NAME, "url", PROJECT_URL, "lead", PROJECT_LEAD, "description",
            PROJECT_DESC, "key", PROJECT_KEY, "counter", PROJECT_COUNT);
        params.put("assigneetype", PROJECT_ASS_TYPE);

        // Just create a mock of the version manager to stop it getting a real instance, since we do not need one for this test
        final Mock versionManager = new Mock(VersionManager.class);
        ManagerFactory.addService(VersionManager.class, (VersionManager) versionManager.proxy());

        final User projectLead = UserUtils.createUser("test", "test@test.com");
        final GenericValue projectGV = EntityUtils.createValue("Project", params);
        final Project project = defaultProjectFactory.getProject(projectGV);

        assertNotNull(project);
        assertEquals(PROJECT_ID, project.getId());
        assertEquals(PROJECT_NAME, project.getName());
        assertEquals(PROJECT_URL, project.getUrl());
        assertEquals(PROJECT_DESC, project.getDescription());
        assertEquals(PROJECT_KEY, project.getKey());
        assertEquals(PROJECT_COUNT, project.getCounter());
        assertEquals(PROJECT_ASS_TYPE, project.getAssigneeType());
        assertEquals(projectLead, project.getLead());
    }

}
