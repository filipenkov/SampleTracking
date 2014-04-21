/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuetypes.enterprise;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.MockEventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.DefaultIssueTypeManager;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManagerImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.web.action.admin.issuetypes.DeleteIssueType;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowActionsBean;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.Iterator;

public class TestEnterpriseDeleteIssueType extends LegacyJiraMockTestCase
{
    private GenericValue issueType;
    private GenericValue newIssueType;

    GenericValue project1;
    GenericValue project2;
    GenericValue fieldLayoutScheme1;
    GenericValue fieldLayoutScheme2;
    GenericValue association;
    private DeleteIssueType deleteIssueType;
    private GenericValue issueTypeScreenScheme1;
    private GenericValue fieldLayout1;
    private GenericValue fieldLayout2;
    private GenericValue fieldScreenScheme1;
    private GenericValue fieldScreenScheme2;

    private IssueTypeSchemeManager mockIssueTypeSchemeManager;
    private MockControl ctrlIssueTypeSchemeManager;
    private Mock mockFieldConfigSchemeManager;
    private EventPublisher mockEventPublisher;
    private User testUser;

    public TestEnterpriseDeleteIssueType(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        issueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "Issue Type", "sequence", new Long(1)));
        newIssueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "2", "name", "New Issue Type", "sequence", new Long(2)));

        UtilsForTests.getTestEntity("FieldScreen", EasyMap.build("id", WorkflowActionsBean.VIEW_COMMENTASSIGN_ID, "name", "Test Screen 1"));
        UtilsForTests.getTestEntity("FieldScreen", EasyMap.build("id", WorkflowActionsBean.VIEW_RESOLVE_ID, "name", "Test Screen 2"));

        deleteIssueType = newInstance();

        ctrlIssueTypeSchemeManager = MockClassControl.createControl(IssueTypeSchemeManagerImpl.class);
        mockIssueTypeSchemeManager = (IssueTypeSchemeManager) ctrlIssueTypeSchemeManager.getMock();
        mockEventPublisher = new MockEventPublisher();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        // Free reference to free up memory
        deleteIssueType = null;
    }

    private void setUpEnterpriseTests()
    {
        ManagerFactory.quickRefresh();

        // Project
        project1 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC"));
        project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "CDE"));

        // Create a copy of project scheme
        fieldLayoutScheme1 = UtilsForTests.getTestEntity("FieldLayoutScheme", EasyMap.build("id", new Long(1), "name", "Scheme 1", "description", "Desc 1"));
        fieldLayoutScheme2 = UtilsForTests.getTestEntity("FieldLayoutScheme", EasyMap.build("id", new Long(2), "name", "Scheme 2", "description", "Desc 2"));

        fieldLayout1 = UtilsForTests.getTestEntity("FieldLayout", EasyMap.build("name", "Test Field Layout 1"));
        fieldLayout2 = UtilsForTests.getTestEntity("FieldLayout", EasyMap.build("name", "Test Field Layout 2"));

        fieldLayoutScheme2 = UtilsForTests.getTestEntity("FieldLayoutSchemeEntity", EasyMap.build("scheme", fieldLayoutScheme1.getLong("id"), "issuetype", null));

        fieldScreenScheme1 = UtilsForTests.getTestEntity("FieldScreenScheme", EasyMap.build("name", "Test Scheme 1"));
        fieldScreenScheme2 = UtilsForTests.getTestEntity("FieldScreenScheme", EasyMap.build("name", "Test Scheme 2"));

        issueTypeScreenScheme1 = UtilsForTests.getTestEntity("IssueTypeScreenScheme", EasyMap.build("id", new Long(1), "name", "Test Scheme"));
        UtilsForTests.getTestEntity("IssueTypeScreenSchemeEntity", EasyMap.build("scheme", issueTypeScreenScheme1.getLong("id"), "issuetype", null, "fieldscreenscheme", fieldScreenScheme1.getLong("id")));

        // Instantiate the action again to pick up enterprise managers
        deleteIssueType = newInstance();

        mockIssueTypeSchemeManager.removeOptionFromAllSchemes(issueType.getString("id"));
        ctrlIssueTypeSchemeManager.replay();

        testUser = new MockUser("test");

    }

    private MockControl ctrlCustomFieldManager = MockControl.createControl(CustomFieldManager.class);
    private CustomFieldManager mockCustomFieldManager = (CustomFieldManager) ctrlCustomFieldManager.getMock();


    private DeleteIssueType newInstance()
    {

        mockFieldConfigSchemeManager = new Mock(FieldConfigSchemeManager.class);
        mockFieldConfigSchemeManager.expectVoid("removeInvalidFieldConfigSchemesForIssueType", P.ANY_ARGS);

        IssueTypeManager issueTypeManager = new DefaultIssueTypeManager(ComponentAccessor.getConstantsManager(), ComponentAccessor.getOfBizDelegator(), null, null, null, ComponentAccessor.getProjectManager(), ComponentAccessor.getWorkflowManager(), ComponentAccessor.getFieldLayoutManager(), ComponentAccessor.getIssueTypeScreenSchemeManager(), mockIssueTypeSchemeManager, ComponentAccessor.getWorkflowSchemeManager(), (FieldConfigSchemeManager) mockFieldConfigSchemeManager.proxy(), mockCustomFieldManager, mockEventPublisher);

        return new DeleteIssueType(issueTypeManager);
    }

    // Test with issue type to be deleted associated with different field layout schemes
    public void testEnterpriseNewTypeDifferentLayoutScheme() throws Exception
    {
        setUpEnterpriseTests();

        // Different field layout scheme associations
        FieldLayoutManager fieldLayoutManager = ComponentAccessor.getFieldLayoutManager();
        FieldLayoutScheme fieldLayoutScheme = fieldLayoutManager.getMutableFieldLayoutScheme(fieldLayoutScheme1.getLong("id"));
        fieldLayoutManager.addSchemeAssociation(project1, fieldLayoutScheme.getId());

        FieldLayoutSchemeEntity fieldLayoutSchemeEntity = new FieldLayoutSchemeEntityImpl(fieldLayoutManager, null, null);
        fieldLayoutSchemeEntity.setFieldLayoutId(fieldLayout1.getLong("id"));
        fieldLayoutSchemeEntity.setIssueTypeId(issueType.getString("id"));
        fieldLayoutScheme.addEntity(fieldLayoutSchemeEntity);

        fieldLayoutSchemeEntity = new FieldLayoutSchemeEntityImpl(fieldLayoutManager, null, null);
        fieldLayoutSchemeEntity.setFieldLayoutId(fieldLayout2.getLong("id"));
        fieldLayoutSchemeEntity.setIssueTypeId(newIssueType.getString("id"));
        fieldLayoutScheme.addEntity(fieldLayoutSchemeEntity);

        IssueTypeScreenSchemeManager issueTypeScreenSchemeManager = ComponentAccessor.getIssueTypeScreenSchemeManager();
        IssueTypeScreenScheme issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(new Long(1));
        issueTypeScreenSchemeManager.addSchemeAssociation(project1, issueTypeScreenScheme);

        deleteIssueType.setId(issueType.getString("id"));
        deleteIssueType.setNewId(newIssueType.getString("id"));

        assertTrue(deleteIssueType.getAvailableIssueTypes().isEmpty());
    }

    // Test with issue type to be deleted associated with different worfklows
    public void testEnterpriseGetAvailableTypesWithDifferentWorkflows() throws Exception
    {
        setUpEnterpriseTests();

        // now give one of the projects a different workflow and assign it to the scheme
        JiraWorkflow workflow = new ConfigurableJiraWorkflow("test workflow", ManagerFactory.getWorkflowManager());
        WorkflowManager workflowManager = ManagerFactory.getWorkflowManager();
        workflowManager.createWorkflow(testUser.getName(), workflow);

        GenericValue scheme = ManagerFactory.getWorkflowSchemeManager().createScheme("Test WF Scheme", null);
        ManagerFactory.getWorkflowSchemeManager().addWorkflowToScheme(scheme, workflow.getName(), newIssueType.getString("id"));
        ManagerFactory.getWorkflowSchemeManager().addSchemeToProject(project2, scheme);

        deleteIssueType.setId(issueType.getString("id"));

        assertTrue(deleteIssueType.getAvailableIssueTypes().isEmpty());
    }

    public void testEnterpriseAvailableTypeExists() throws WorkflowException, GenericEntityException
    {
        setUpEnterpriseTests();

        deleteIssueType.setId(issueType.getString("id"));
        deleteIssueType.setNewId(newIssueType.getString("id"));

        Collection<IssueType> availableIssueTypes = deleteIssueType.getAvailableIssueTypes();
        assertFalse(availableIssueTypes.isEmpty());
        assertEquals(newIssueType.getString("id"), availableIssueTypes.iterator().next().getId());
    }

    public void testEnterpriseNewTypeDifferentIssueTypeFieldScreenScheme() throws WorkflowException, GenericEntityException
    {
        setUpEnterpriseTests();

        // Different issue type screen schemeassociations
        IssueTypeScreenSchemeManager issueTypeScreenSchemeManager = ComponentAccessor.getIssueTypeScreenSchemeManager();
        FieldScreenSchemeManager fieldScreenSchemeManager = ComponentManager.getInstance().getFieldScreenSchemeManager();

        IssueTypeScreenScheme issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(issueTypeScreenScheme1.getLong("id"));

        IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = new IssueTypeScreenSchemeEntityImpl(issueTypeScreenSchemeManager, (GenericValue) null, fieldScreenSchemeManager, null);
        issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenSchemeManager.getFieldScreenScheme(fieldScreenScheme1.getLong("id")));
        issueTypeScreenSchemeEntity.setIssueTypeId(issueType.getString("id"));
        issueTypeScreenScheme.addEntity(issueTypeScreenSchemeEntity);

        issueTypeScreenSchemeEntity = new IssueTypeScreenSchemeEntityImpl(issueTypeScreenSchemeManager, (GenericValue) null, fieldScreenSchemeManager, null);
        issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenSchemeManager.getFieldScreenScheme(fieldScreenScheme2.getLong("id")));
        issueTypeScreenSchemeEntity.setIssueTypeId(newIssueType.getString("id"));
        issueTypeScreenScheme.addEntity(issueTypeScreenSchemeEntity);

        issueTypeScreenSchemeManager.addSchemeAssociation(project1, issueTypeScreenScheme);

        deleteIssueType.setId(issueType.getString("id"));
        deleteIssueType.setNewId(newIssueType.getString("id"));

        assertTrue(deleteIssueType.getAvailableIssueTypes().isEmpty());
    }

    public void testEnterpriseNewTypeDifferentWorkflow() throws WorkflowException, GenericEntityException
    {
        setUpEnterpriseTests();

        // now give one of the projects and new issuetype a different workflow and assign it to the scheme
        JiraWorkflow workflow = new ConfigurableJiraWorkflow("test workflow", ManagerFactory.getWorkflowManager());
        WorkflowManager workflowManager = ManagerFactory.getWorkflowManager();
        workflowManager.createWorkflow(testUser.getName(), workflow);

        GenericValue scheme = ManagerFactory.getWorkflowSchemeManager().createScheme("Test WF Scheme", null);
        ManagerFactory.getWorkflowSchemeManager().addWorkflowToScheme(scheme, workflow.getName(), newIssueType.getString("id"));
        // Now add the new association
        ManagerFactory.getWorkflowSchemeManager().addSchemeToProject(project2, scheme);

        deleteIssueType.setId(issueType.getString("id"));

        assertTrue("Issue types found when there should be none", deleteIssueType.getAvailableIssueTypes().isEmpty());
    }

    public void testEnterpriseRemoveWorkflowSchemeEntries() throws Exception
    {
        setUpEnterpriseTests();

        // Set expected response
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        mockHttpServletResponse.setExpectedRedirect("ViewIssueTypes.jspa");
        ActionContext.setResponse(mockHttpServletResponse);

        JiraWorkflow workflow = new ConfigurableJiraWorkflow("test workflow", ManagerFactory.getWorkflowManager());
        WorkflowManager workflowManager = ManagerFactory.getWorkflowManager();
        workflowManager.createWorkflow(testUser.getName(), workflow);

        WorkflowSchemeManager workflowSchemeManager = ManagerFactory.getWorkflowSchemeManager();
        GenericValue scheme = workflowSchemeManager.createScheme("Test WF Scheme", null);
        workflowSchemeManager.addWorkflowToScheme(scheme, workflow.getName(), issueType.getString("id"));
        workflowSchemeManager.addSchemeToProject(project1, scheme);
        workflowSchemeManager.addSchemeToProject(project2, scheme);

        deleteIssueType.setId(issueType.getString("id"));
        deleteIssueType.setNewId(newIssueType.getString("id"));

        deleteIssueType.execute();

        Collection entities = workflowSchemeManager.getEntities(scheme);
        for (Iterator iterator1 = entities.iterator(); iterator1.hasNext();)
        {
            GenericValue entity = (GenericValue) iterator1.next();
            if (issueType.getString("id").equals(entity.getString("issuetype")))
            {
                fail("Workflow Scheme entry should have been deleted.");
            }
        }
        mockFieldConfigSchemeManager.verify();
    }

    public void testEnterpriseRemoveFieldLayoutSchemeEntries() throws Exception
    {
        setUpEnterpriseTests();

        // Set expected response
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        mockHttpServletResponse.setExpectedRedirect("ViewIssueTypes.jspa");
        ActionContext.setResponse(mockHttpServletResponse);

        JiraWorkflow workflow = new ConfigurableJiraWorkflow("test workflow", ManagerFactory.getWorkflowManager());
        WorkflowManager workflowManager = ManagerFactory.getWorkflowManager();
        workflowManager.createWorkflow(testUser.getName(), workflow);

        // Different field layout scheme associations
        FieldLayoutManager fieldLayoutManager = ComponentAccessor.getFieldLayoutManager();
        FieldLayoutScheme fieldLayoutScheme = fieldLayoutManager.getMutableFieldLayoutScheme(fieldLayoutScheme1.getLong("id"));
        fieldLayoutManager.addSchemeAssociation(project1, fieldLayoutScheme.getId());

        FieldLayoutSchemeEntity fieldLayoutSchemeEntity = new FieldLayoutSchemeEntityImpl(fieldLayoutManager, null, null);
        fieldLayoutSchemeEntity.setFieldLayoutId(fieldLayout1.getLong("id"));
        fieldLayoutSchemeEntity.setIssueTypeId(issueType.getString("id"));
        fieldLayoutScheme.addEntity(fieldLayoutSchemeEntity);

        fieldLayoutScheme = fieldLayoutManager.getMutableFieldLayoutScheme(fieldLayoutScheme1.getLong("id"));
        if (fieldLayoutScheme.getEntity(issueType.getString("id")) == null)
        {
            fail("Field Layout Scheme entry should exist.");
        }

        deleteIssueType.setId(issueType.getString("id"));
        deleteIssueType.setNewId(newIssueType.getString("id"));

        deleteIssueType.execute();

        fieldLayoutScheme = fieldLayoutManager.getMutableFieldLayoutScheme(fieldLayoutScheme1.getLong("id"));
        if (fieldLayoutScheme.getEntity(issueType.getString("id")) != null)
        {
            fail("Field Layout Scheme entry should have been deleted.");
        }
        mockFieldConfigSchemeManager.verify();
    }

    public void testEnterpriseRemoveIssueTypeScreenSchemeEntries() throws Exception
    {
        setUpEnterpriseTests();

        // Set expected response
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        mockHttpServletResponse.setExpectedRedirect("ViewIssueTypes.jspa");
        ActionContext.setResponse(mockHttpServletResponse);

        JiraWorkflow workflow = new ConfigurableJiraWorkflow("test workflow", ManagerFactory.getWorkflowManager());
        WorkflowManager workflowManager = ManagerFactory.getWorkflowManager();
        workflowManager.createWorkflow(testUser.getName(), workflow);

        // Different field layout scheme associations
        IssueTypeScreenSchemeManager issueTypeScreenSchemeManager = ComponentAccessor.getIssueTypeScreenSchemeManager();
        FieldScreenSchemeManager fieldScreenSchemeManager = ComponentManager.getInstance().getFieldScreenSchemeManager();

        IssueTypeScreenScheme issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(issueTypeScreenScheme1.getLong("id"));

        IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = new IssueTypeScreenSchemeEntityImpl(issueTypeScreenSchemeManager, (GenericValue) null, fieldScreenSchemeManager, null);
        issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenSchemeManager.getFieldScreenScheme(fieldScreenScheme1.getLong("id")));
        issueTypeScreenSchemeEntity.setIssueTypeId(issueType.getString("id"));
        issueTypeScreenScheme.addEntity(issueTypeScreenSchemeEntity);

        issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(issueTypeScreenScheme1.getLong("id"));
        if (issueTypeScreenScheme.getEntity(issueType.getString("id")) == null)
        {
            fail("Issue Type Screen Scheme entry should exist.");
        }

        issueTypeScreenSchemeManager.addSchemeAssociation(project1, issueTypeScreenScheme);

        deleteIssueType.setId(issueType.getString("id"));
        deleteIssueType.setNewId(newIssueType.getString("id"));

        deleteIssueType.execute();

        issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(issueTypeScreenScheme1.getLong("id"));
        if (issueTypeScreenScheme.getEntity(issueType.getString("id")) != null)
        {
            fail("Issue Type Screen Scheme entry should have been deleted.");
        }

        mockFieldConfigSchemeManager.verify();

    }
}
