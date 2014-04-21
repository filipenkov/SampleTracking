/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme.issuesecurity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManagerImpl;
import com.atlassian.jira.project.DefaultProjectManager;
import com.atlassian.jira.scheme.DefaultSchemeFactory;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

import java.util.HashMap;

public class TestSchemeManager extends LegacyJiraMockTestCase
{
    public TestSchemeManager(String s)
    {
        super(s);
    }

    SchemeManager issueSchemeManager;
    GenericValue issueScheme;
    GenericValue project;
    GenericValue project2;
    GenericValue issue;

    protected void setUp() throws Exception
    {
        super.setUp();

        PermissionContextFactory ctxFactory = ComponentAccessor.getPermissionContextFactory();
        DefaultSchemeFactory schemeFactory = new DefaultSchemeFactory();
        OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(CoreFactory.getGenericDelegator());
        AssociationManager associationManager = CoreFactory.getAssociationManager();
        ManagerFactory.addService(IssueSecuritySchemeManager.class, new IssueSecuritySchemeManagerImpl(new DefaultProjectManager(), new PermissionTypeManager(), ctxFactory, schemeFactory, null, associationManager, ofBizDelegator, null));

        issueSchemeManager = ManagerFactory.getIssueSecuritySchemeManager();

        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(2), "lead", "paul"));
        project2 = UtilsForTests.getTestEntity("Project", new HashMap());
    }

    protected void setupSchemes() throws CreateException, GenericEntityException
    {
        //Create a issueSecurity scheme and add to the project
        issueScheme = issueSchemeManager.createScheme("IScheme", "Test Desc");
        issueSchemeManager.addSchemeToProject(project, issueScheme);
    }

    public void testCreateScheme() throws CreateException, GenericEntityException
    {
        setupSchemes();
        assertTrue(issueSchemeManager.getSchemes().size() == 1);
        assertTrue(issueSchemeManager.getSchemes(project).size() == 1);
    }

    public void testCopyScheme() throws CreateException, GenericEntityException
    {
        setupSchemes();

        issueSchemeManager.copyScheme(issueSchemeManager.getScheme("IScheme"));
        assertTrue(issueSchemeManager.getSchemes().size() == 2);
        assertTrue(issueSchemeManager.getSchemes(project).size() == 1);
        assertNotNull(issueSchemeManager.getScheme("Copy of IScheme"));

        //copy it again and it should add the new number to it
        issueSchemeManager.copyScheme(issueSchemeManager.getScheme("IScheme"));
        assertNotNull(issueSchemeManager.getScheme("Copy 2 of IScheme"));
        assertTrue(issueSchemeManager.getSchemes().size() == 3);

        issueSchemeManager.copyScheme(issueSchemeManager.getScheme("Copy of IScheme"));
        assertTrue(issueSchemeManager.getSchemes().size() == 4);
        assertTrue(issueSchemeManager.getSchemes(project).size() == 1);
        assertNotNull(issueSchemeManager.getScheme("Copy of Copy of IScheme"));
    }

    public void testCreateEntity() throws CreateException, GenericEntityException
    {
        setupSchemes();

        issueSchemeManager.createSchemeEntity(issueScheme, new SchemeEntity("type1", "group", new Long(2)));
        issueSchemeManager.createSchemeEntity(issueScheme, new SchemeEntity("type2", "group", new Long(2)));

        //Add and invalid entity. The id is a string
        try
        {
            issueSchemeManager.createSchemeEntity(issueScheme, new SchemeEntity("type1", "group", "test"));
            fail("Should have thrown a IllegalArgumentException!");
        }
        catch (IllegalArgumentException e)
        {
        }

        assertTrue(issueSchemeManager.getEntities(issueScheme).size() == 2);
    }

    public void testGetScheme() throws CreateException, GenericEntityException
    {
        setupSchemes();

        assertTrue(issueSchemeManager.getSchemes().size() == 1);
        assertTrue(issueSchemeManager.getSchemes(project).size() == 1);

        assertNotNull(issueSchemeManager.getScheme(issueScheme.getLong("id")));
        assertNotNull(issueSchemeManager.getScheme(issueScheme.getString("name")));

        assertTrue(issueSchemeManager.getSchemes(project2).size() == 0);
    }

    public void testGetEntities() throws CreateException, GenericEntityException
    {
        setupSchemes();

        assertTrue(issueSchemeManager.getEntities(issueScheme).size() == 0);

        issueSchemeManager.createSchemeEntity(issueScheme, new SchemeEntity("type1", "group", new Long(2)));
        issueSchemeManager.createSchemeEntity(issueScheme, new SchemeEntity("type2", "group2", new Long(2)));

        assertTrue(issueSchemeManager.getEntities(issueScheme).size() == 2);

        assertTrue(issueSchemeManager.getEntities(issueScheme, new Long(2)).size() == 2);

        assertTrue(issueSchemeManager.getEntities(issueScheme, new Long(2), "group").size() == 1);

        assertTrue(issueSchemeManager.getEntities(issueScheme, "type2", new Long(2)).size() == 1);

        try
        {
            assertTrue(issueSchemeManager.getEntities(issueScheme, "xxx").size() == 1);
            fail("Should have thrown a IllegalArgumentException!");
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    public void testCreateDefaultScheme() throws CreateException, GenericEntityException
    {
        issueSchemeManager.createDefaultScheme();

        assertTrue(issueSchemeManager.getSchemes().size() == 1);
        assertNotNull(issueSchemeManager.getDefaultScheme());
    }

    public void testDeleteScheme() throws CreateException, GenericEntityException
    {
        setupSchemes();

        assertTrue(issueSchemeManager.getSchemes().size() == 1);
        issueSchemeManager.deleteScheme(issueScheme.getLong("id"));
        assertTrue(issueSchemeManager.getSchemes().size() == 0);
    }

    public void testDeleteEntity() throws CreateException, GenericEntityException
    {
        setupSchemes();

        GenericValue entity2 = issueSchemeManager.createSchemeEntity(issueScheme, new SchemeEntity("type1", "group", new Long(2)));
        assertTrue(issueSchemeManager.getEntities(issueScheme).size() == 1);
        issueSchemeManager.deleteEntity(entity2.getLong("id"));
        assertTrue(issueSchemeManager.getEntities(issueScheme).size() == 0);
    }

    public void testUpdateScheme() throws CreateException, GenericEntityException
    {
        setupSchemes();

        issueScheme.set("name", "Test Update Scheme");
        issueSchemeManager.updateScheme(issueScheme);
        assertTrue(issueSchemeManager.getSchemes().size() == 1);
        assertNotNull(issueSchemeManager.getScheme("Test Update Scheme"));
    }

    public void testGetProjects() throws CreateException, GenericEntityException
    {
        setupSchemes();

        assertTrue(issueSchemeManager.getProjects(issueScheme).size() == 1);
        assertEquals(project, issueSchemeManager.getProjects(issueScheme).get(0));
    }
}
