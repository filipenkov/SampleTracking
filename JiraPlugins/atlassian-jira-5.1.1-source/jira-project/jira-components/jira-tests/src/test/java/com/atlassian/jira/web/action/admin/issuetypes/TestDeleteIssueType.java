/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuetypes;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.DefaultIssueTypeManager;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomFieldImpl;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManagerImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.IssueTypeImpl;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

public class TestDeleteIssueType extends LegacyJiraMockTestCase
{
    private GenericValue issueType;
    private GenericValue newIssueType;

    GenericValue project1;
    GenericValue project2;
    GenericValue fieldLayoutScheme1;
    GenericValue fieldLayoutScheme2;
    GenericValue association;

    private CustomFieldManager mockCustomFieldManager;
    private MockControl ctrlCustomFieldManager;

    private IssueTypeSchemeManager mockIssueTypeSchemeManager;
    private MockControl ctrlIssueTypeSchemeManager;

    public TestDeleteIssueType(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        ctrlCustomFieldManager = MockControl.createControl(CustomFieldManager.class);
        mockCustomFieldManager = (CustomFieldManager) ctrlCustomFieldManager.getMock();

        issueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "Issue Type", "sequence", new Long(1)));
        newIssueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "2", "name", "New Issue Type", "sequence", new Long(2)));
        UtilsForTests.getTestEntity("CustomField", EasyMap.build("name", "Test Custom Field", "issuetype", issueType.getString("id"), "fieldtype",
            new Long(1), CustomFieldImpl.ENTITY_CF_TYPE_KEY, "com.atlassian.jira.plugin.system.customfieldtypes:textfield"));

        ctrlIssueTypeSchemeManager = MockClassControl.createControl(IssueTypeSchemeManagerImpl.class);
        mockIssueTypeSchemeManager = (IssueTypeSchemeManager) ctrlIssueTypeSchemeManager.getMock();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testDoExecuteUpdatesCustomFields() throws Exception
    {
        final MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        mockHttpServletResponse.setExpectedRedirect("ViewIssueTypes.jspa");
        ActionContext.setResponse(mockHttpServletResponse);

        final Mock issueFieldManager = new Mock(FieldManager.class);
        issueFieldManager.setStrict(true);
        issueFieldManager.expectVoid("refresh");
        ManagerFactory.addService(FieldManager.class, (FieldManager) issueFieldManager.proxy());

        final Mock mockFieldConfigSchemeManager = new Mock(FieldConfigSchemeManager.class);
        mockFieldConfigSchemeManager.expectVoid("removeInvalidFieldConfigSchemesForIssueType", P.ANY_ARGS);

        final WorkflowSchemeManager workflowSchemeManager = createMock(WorkflowSchemeManager.class);
        expect(workflowSchemeManager.getSchemes()).andReturn(Collections.<GenericValue>emptyList());

        final FieldLayoutManager fieldLayoutManager = createMock(FieldLayoutManager.class);
        expect(fieldLayoutManager.getFieldLayoutSchemes()).andReturn(Collections.<FieldLayoutScheme>emptyList()).anyTimes();

        final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager = createMock(IssueTypeScreenSchemeManager.class);
        expect(issueTypeScreenSchemeManager.getIssueTypeScreenSchemes()).andReturn(Collections.<IssueTypeScreenScheme>emptyList()).anyTimes();

        replay(workflowSchemeManager, fieldLayoutManager, issueTypeScreenSchemeManager);

        final EventPublisher eventPublisher = createMock(EventPublisher.class);

        final IssueTypeManager issueTypeManager = new DefaultIssueTypeManager(ComponentAccessor.getConstantsManager(), ComponentAccessor.getOfBizDelegator(), null, null, null, ComponentAccessor.getProjectManager(), null, fieldLayoutManager, issueTypeScreenSchemeManager, mockIssueTypeSchemeManager, workflowSchemeManager, (FieldConfigSchemeManager) mockFieldConfigSchemeManager.proxy(), mockCustomFieldManager, eventPublisher);

        final DeleteIssueType deleteIssueType = new DeleteIssueType(issueTypeManager);
        deleteIssueType.setId(issueType.getString("id"));
        deleteIssueType.setNewId(newIssueType.getString("id"));
        deleteIssueType.setConfirm(true);

        mockCustomFieldManager.refresh();

        ctrlCustomFieldManager.replay();

        final String result = deleteIssueType.execute();
        assertEquals(Action.NONE, result);

        final GenericDelegator delegator = CoreFactory.getGenericDelegator();
        final List issueTypes = delegator.findAll("IssueType");
        assertEquals(1, issueTypes.size());

        mockHttpServletResponse.verify();
        mockFieldConfigSchemeManager.verify();
    }

    public void testGetAvailableIssueTypes() throws Exception
    {
        final Collection allIssueTypes = EasyList.build(issueType, newIssueType);

        final Mock constantsManager = new Mock(ConstantsManager.class);
        constantsManager.setStrict(true);
        final Mock translationManager = new Mock(TranslationManager.class);
        final Mock jiraAuthenticationContext = new Mock(JiraAuthenticationContext.class);
        constantsManager.expectAndReturn("getIssueTypeObject", (issueType.getString("id")), new IssueTypeImpl(issueType, (TranslationManager)translationManager.proxy(), (JiraAuthenticationContext)jiraAuthenticationContext.proxy()));
        constantsManager.expectAndReturn("getIssueType", (issueType.getString("id")), issueType);
        constantsManager.expectAndReturn("getSubTaskIssueTypes", Collections.EMPTY_LIST);
        constantsManager.expectAndReturn("getIssueTypes", allIssueTypes);
        ManagerFactory.addService(ConstantsManager.class, (ConstantsManager) constantsManager.proxy());

        final ProjectManager projectManager = createMock(ProjectManager.class);
        expect(projectManager.getProjects()).andReturn(Collections.<GenericValue>emptyList());
        
        final FieldLayoutManager fieldLayoutManager = createMock(FieldLayoutManager.class);
        expect(fieldLayoutManager.getFieldLayoutSchemes()).andReturn(Collections.<FieldLayoutScheme>emptyList()).atLeastOnce();

        final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager = createMock(IssueTypeScreenSchemeManager.class);
        expect(issueTypeScreenSchemeManager.getIssueTypeScreenSchemes()).andReturn(Collections.<IssueTypeScreenScheme>emptyList()).atLeastOnce();

        replay(projectManager, fieldLayoutManager, issueTypeScreenSchemeManager);

        final DeleteIssueType deleteIssueType = new DeleteIssueType(ComponentAccessor.getComponent(IssueTypeManager.class));
        deleteIssueType.setId(issueType.getString("id"));
        final Collection<IssueType> availableIssueTypes = deleteIssueType.getAvailableIssueTypes();
        assertEquals(1, availableIssueTypes.size());
        assertTrue("getAvailableIssueTypes should contain " + newIssueType, availableIssueTypes.iterator().next().getId().equals(newIssueType.getString("id")));
        assertFalse("getAvailableIssueTypes should not contain " + issueType, availableIssueTypes.iterator().next().getId().equals(issueType.getString("id")));
    }
}
