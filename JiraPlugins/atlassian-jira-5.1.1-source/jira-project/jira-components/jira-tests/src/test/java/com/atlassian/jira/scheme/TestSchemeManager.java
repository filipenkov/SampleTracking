package com.atlassian.jira.scheme;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;

public class TestSchemeManager extends LegacyJiraMockTestCase
{
    SchemeManager permSchemeManager;
    SchemeManager notificationSchemeManager;
    GenericValue permScheme;
    GenericValue notificationScheme;
    GenericValue project;
    GenericValue project2;

    protected void setUp() throws Exception
    {
        super.setUp();

        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", 2L, "lead", "paul"));
        project2 = UtilsForTests.getTestEntity("Project", new HashMap());

        permSchemeManager = ManagerFactory.getPermissionSchemeManager();
        notificationSchemeManager = ManagerFactory.getNotificationSchemeManager();

    }

    protected void setupSchemes() throws CreateException, GenericEntityException
    {
        //Create a permission scheme and add to the project
        permScheme = permSchemeManager.createScheme("PScheme", "Test Desc");
        permSchemeManager.addSchemeToProject(project, permScheme);

        //Create a permission scheme and add to the project
        notificationScheme = notificationSchemeManager.createScheme("NScheme", "Test Desc");
        notificationSchemeManager.addSchemeToProject(project, notificationScheme);
    }

    public void testCreateScheme() throws CreateException, GenericEntityException
    {
        setupSchemes();
        assertTrue(permSchemeManager.getSchemes().size() == 1);
        assertTrue(permSchemeManager.getSchemes(project).size() == 1);
    }

    public void testCopyScheme() throws CreateException, GenericEntityException
    {
        setupSchemes();

        permSchemeManager.copyScheme(permSchemeManager.getScheme("PScheme"));
        assertTrue(permSchemeManager.getSchemes().size() == 2);
        assertTrue(permSchemeManager.getSchemes(project).size() == 1);
        assertNotNull(permSchemeManager.getScheme("Copy of PScheme"));

        //copy it again and it should add the new number to it
        permSchemeManager.copyScheme(permSchemeManager.getScheme("PScheme"));
        assertNotNull(permSchemeManager.getScheme("Copy 2 of PScheme"));
        assertTrue(permSchemeManager.getSchemes().size() == 3);

        permSchemeManager.copyScheme(permSchemeManager.getScheme("Copy of PScheme"));
        assertTrue(permSchemeManager.getSchemes().size() == 4);
        assertTrue(permSchemeManager.getSchemes(project).size() == 1);
        assertNotNull(permSchemeManager.getScheme("Copy of Copy of PScheme"));

        notificationSchemeManager.copyScheme(notificationSchemeManager.getScheme("NScheme"));
        assertTrue(notificationSchemeManager.getSchemes().size() == 2);
        assertTrue(notificationSchemeManager.getSchemes(project).size() == 1);
        assertNotNull(notificationSchemeManager.getScheme("Copy of NScheme"));

        //copy it again and it should add the new number to it
        notificationSchemeManager.copyScheme(notificationSchemeManager.getScheme("NScheme"));
        assertNotNull(notificationSchemeManager.getScheme("Copy 2 of NScheme"));
        assertTrue(notificationSchemeManager.getSchemes().size() == 3);

        notificationSchemeManager.copyScheme(notificationSchemeManager.getScheme("Copy of NScheme"));
        assertTrue(notificationSchemeManager.getSchemes().size() == 4);
        assertTrue(notificationSchemeManager.getSchemes(project).size() == 1);
        assertNotNull(notificationSchemeManager.getScheme("Copy of Copy of NScheme"));
    }

    public void testCreateEntity() throws CreateException, GenericEntityException
    {
        setupSchemes();

        permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type1", "group", 2L));
        permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type2", "group", 2L));

        //Add and invalid entity. The id is a string
        try
        {
            permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type1", "group", "test"));
            fail("Should have thrown a IllegalArgumentException!");
        }
        catch (IllegalArgumentException e)
        {
        }

        assertTrue(permSchemeManager.getEntities(permScheme).size() == 2);

        notificationSchemeManager.createSchemeEntity(notificationScheme, new SchemeEntity("type1", "group", EventType.ISSUE_CREATED_ID));
        notificationSchemeManager.createSchemeEntity(notificationScheme, new SchemeEntity("type2", "group", EventType.ISSUE_ASSIGNED_ID));

        //Add and invalid entity. The id is a string
        try
        {
            notificationSchemeManager.createSchemeEntity(notificationScheme, new SchemeEntity("type1", "group", "2"));
            fail("Should have thrown a IllegalArgumentException!");
        }
        catch (IllegalArgumentException e)
        {
        }

        assertTrue(notificationSchemeManager.getEntities(notificationScheme).size() == 2);
    }

    public void testGetScheme() throws CreateException, GenericEntityException
    {
        setupSchemes();
        assertTrue(permSchemeManager.getSchemes().size() == 1);
        assertTrue(permSchemeManager.getSchemes(project).size() == 1);

        assertNotNull(permSchemeManager.getScheme(permScheme.getLong("id")));
        assertNotNull(permSchemeManager.getScheme(permScheme.getString("name")));

        assertTrue(permSchemeManager.getSchemes(project2).size() == 0);

        assertTrue(notificationSchemeManager.getSchemes().size() == 1);
        assertTrue(notificationSchemeManager.getSchemes(project).size() == 1);

        assertNotNull(notificationSchemeManager.getScheme(notificationScheme.getLong("id")));
        assertNotNull(notificationSchemeManager.getScheme(notificationScheme.getString("name")));

        assertTrue(notificationSchemeManager.getSchemes(project2).size() == 0);

        //test that the SchemeManagers are not using the same schemes
        assertNull(permSchemeManager.getScheme(notificationScheme.getString("name")));
        assertNull(notificationSchemeManager.getScheme(permScheme.getString("name")));
    }

    public void testGetEntities() throws CreateException, GenericEntityException
    {
        setupSchemes();

        permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type1", "group", 2L));
        permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type2", "group2", 2L));

        assertTrue(permSchemeManager.getEntities(permScheme).size() == 2);

        assertTrue(permSchemeManager.getEntities(permScheme, 2L).size() == 2);

        assertTrue(permSchemeManager.getEntities(permScheme, 2L, "group").size() == 1);

        assertTrue(permSchemeManager.getEntities(permScheme, "type2", 2L).size() == 1);

        try
        {
            assertTrue(permSchemeManager.getEntities(permScheme, "xxx").size() == 1);
            fail("Should have thrown a IllegalArgumentException!");
        }
        catch (IllegalArgumentException e)
        {
        }

        notificationSchemeManager.createSchemeEntity(notificationScheme, new SchemeEntity("type1", "group", EventType.ISSUE_CREATED_ID));
        notificationSchemeManager.createSchemeEntity(notificationScheme, new SchemeEntity("type2", "group2", EventType.ISSUE_ASSIGNED_ID));

        assertTrue(notificationSchemeManager.getEntities(notificationScheme).size() == 2);
        assertTrue(notificationSchemeManager.getEntities(notificationScheme, EventType.ISSUE_CREATED_ID).size() == 1);
        assertTrue(notificationSchemeManager.getEntities(notificationScheme, "type1", EventType.ISSUE_CREATED_ID).size() == 1);

        try
        {
            assertTrue(notificationSchemeManager.getEntities(notificationScheme, "2").size() == 1);
            fail("Should have thrown a IllegalArgumentException!");
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    /**
     * Default notification scheme does not work with getDefaultScheme as id is not 0
     */
    public void testCreateDefaultScheme() throws CreateException, GenericEntityException
    {
        permSchemeManager.createDefaultScheme();
        assertTrue(permSchemeManager.getSchemes().size() == 1);
        assertNotNull(permSchemeManager.getDefaultScheme());

        notificationSchemeManager.createDefaultScheme();
        assertTrue(notificationSchemeManager.getSchemes().size() == 1);
//        assertNotNull(notificationSchemeManager.getDefaultScheme());

    }

    public void testDeleteScheme() throws CreateException, GenericEntityException
    {
        setupSchemes();
        assertTrue(permSchemeManager.getSchemes().size() == 1);
        permSchemeManager.deleteScheme(permScheme.getLong("id"));
        assertTrue(permSchemeManager.getSchemes().size() == 0);

        assertTrue(notificationSchemeManager.getSchemes().size() == 1);
        notificationSchemeManager.deleteScheme(notificationScheme.getLong("id"));
        assertTrue(notificationSchemeManager.getSchemes().size() == 0);
    }

    public void testDeleteEntity() throws CreateException, GenericEntityException
    {
        setupSchemes();

        GenericValue entity = permSchemeManager.createSchemeEntity(permScheme, new SchemeEntity("type1", "group", 2L));
        assertTrue(permSchemeManager.getEntities(permScheme).size() == 1);
        permSchemeManager.deleteEntity(entity.getLong("id"));
        assertTrue(permSchemeManager.getEntities(permScheme).size() == 0);

        GenericValue entity3 = notificationSchemeManager.createSchemeEntity(notificationScheme, new SchemeEntity("type1", "group", EventType.ISSUE_CREATED_ID));
        assertTrue(notificationSchemeManager.getEntities(notificationScheme).size() == 1);
        notificationSchemeManager.deleteEntity(entity3.getLong("id"));
        assertTrue(notificationSchemeManager.getEntities(notificationScheme).size() == 0);
    }

    public void testUpdateScheme() throws CreateException, GenericEntityException
    {
        setupSchemes();

        permScheme.set("name", "Test Update Scheme");
        permSchemeManager.updateScheme(permScheme);
        assertTrue(permSchemeManager.getSchemes().size() == 1);
        assertNotNull(permSchemeManager.getScheme("Test Update Scheme"));

        notificationScheme.set("name", "Test Update Scheme");
        notificationSchemeManager.updateScheme(notificationScheme);
        assertTrue(notificationSchemeManager.getSchemes().size() == 1);
        assertNotNull(notificationSchemeManager.getScheme("Test Update Scheme"));
    }

    public void testGetProjects() throws CreateException, GenericEntityException
    {
        setupSchemes();
        assertTrue(permSchemeManager.getProjects(permScheme).size() == 1);
        assertEquals(project, permSchemeManager.getProjects(permScheme).get(0));

        assertTrue(notificationSchemeManager.getProjects(notificationScheme).size() == 1);
        assertEquals(project, notificationSchemeManager.getProjects(notificationScheme).get(0));
    }
}
