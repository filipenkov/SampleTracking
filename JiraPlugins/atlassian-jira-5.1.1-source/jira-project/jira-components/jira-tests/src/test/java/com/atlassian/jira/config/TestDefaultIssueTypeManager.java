package com.atlassian.jira.config;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.IssueTypeImpl;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v5.0
 */
@RunWith (ListeningMockitoRunner.class)
public class TestDefaultIssueTypeManager
{
    private ConstantsManager constantsManager;
    private TranslationManager translationManager;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private IssueIndexManager issueIndexManager;
    private OfBizDelegator ofBizDelegator;
    private ProjectManager projectManager;
    private ApplicationProperties applicationProperties;
    private IssueTypeManager issueTypeManager;
    private WorkflowManager workflowManager;
    private FieldLayoutManager fieldLayoutManager;
    private IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private IssueTypeSchemeManager issueTypeSchemeManager;
    private WorkflowSchemeManager workflowSchemeManager;
    private FieldConfigSchemeManager fieldConfigSchemeManager;
    private CustomFieldManager customFieldManager;
    private EventPublisher eventPublisher;


    @Before
    public void setUp()
    {
        constantsManager = mock(ConstantsManager.class);
        translationManager = mock(TranslationManager.class);
        jiraAuthenticationContext = mock(JiraAuthenticationContext.class);
        issueIndexManager = mock(IssueIndexManager.class);
        ofBizDelegator = mock(OfBizDelegator.class);
        projectManager = mock(ProjectManager.class);
        applicationProperties = mock(ApplicationProperties.class);
        issueTypeManager = mock(IssueTypeManager.class);
        workflowManager = mock(WorkflowManager.class);
        fieldLayoutManager = mock(FieldLayoutManager.class);
        issueTypeScreenSchemeManager = mock(IssueTypeScreenSchemeManager.class);
        issueTypeSchemeManager = mock(IssueTypeSchemeManager.class);
        workflowSchemeManager = mock(WorkflowSchemeManager.class);
        fieldConfigSchemeManager = mock(FieldConfigSchemeManager.class);
        customFieldManager = mock(CustomFieldManager.class);
        eventPublisher = mock(EventPublisher.class);
        issueTypeManager = new DefaultIssueTypeManager(constantsManager, ofBizDelegator, issueIndexManager, translationManager, jiraAuthenticationContext, projectManager, workflowManager, fieldLayoutManager, issueTypeScreenSchemeManager, issueTypeSchemeManager, workflowSchemeManager, fieldConfigSchemeManager, customFieldManager, eventPublisher)
        {
            @Override
            protected String getNextStringId() throws GenericEntityException
            {
                return "10";
            }

            @Override
            protected void removePropertySet(GenericValue constantGv)
            {
            }
        };
    }


    @Test
    public void testCreateStandardIssueType() throws Exception
    {
        MockGenericValue bugIssueTypeGV = new MockGenericValue("IssueType", 1l);
        bugIssueTypeGV.set("name", "Bug");
        bugIssueTypeGV.set("description", "A bug");
        bugIssueTypeGV.set("iconurl", "http://www.toastmasters.com");
        IssueType bugIssueType = new IssueTypeImpl(bugIssueTypeGV, translationManager, jiraAuthenticationContext);

        MockGenericValue featureIssueTypeGV = new MockGenericValue("IssueType", 2l);
        featureIssueTypeGV.set("name", "Feature");
        featureIssueTypeGV.set("description", "A new feature");
        featureIssueTypeGV.set("iconurl", "http://www.atlassian.com");
        IssueType featureIssueType = new IssueTypeImpl(featureIssueTypeGV, translationManager, jiraAuthenticationContext);

        when(constantsManager.getAllIssueTypeObjects()).thenReturn(Lists.newArrayList(bugIssueType, featureIssueType));

        MockGenericValue usabilityIssueTypeGV = new MockGenericValue("IssueType", 10l);
        usabilityIssueTypeGV.set("name", "Usability Design");
        usabilityIssueTypeGV.set("description", "Usability design for a new feature");
        usabilityIssueTypeGV.set("iconurl", "http://www.usability.com");
        IssueTypeFieldsdArgumentMatcher matcher = new IssueTypeFieldsdArgumentMatcher("10", "Usability Design", "Usability design for a new feature", "http://www.usability.com", null);
        when(ofBizDelegator.createValue(eq(ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE), argThat(matcher))).thenReturn(usabilityIssueTypeGV);

        IssueType issueType = issueTypeManager.createIssueType("Usability Design", "Usability design for a new feature", "http://www.usability.com");
        assertEquals("Usability Design", issueType.getName());

        verify(issueTypeSchemeManager).addOptionToDefault("10");
        verify(constantsManager).refreshIssueTypes();
    }

    @Test
    public void testCreateStandardIssueTypeDuplicateName() throws Exception
    {
        MockGenericValue bugIssueTypeGV = new MockGenericValue("IssueType", 1l);
        bugIssueTypeGV.set("name", "Bug");
        bugIssueTypeGV.set("description", "A bug");
        bugIssueTypeGV.set("iconurl", "http://www.toastmasters.com");
        IssueType bugIssueType = new IssueTypeImpl(bugIssueTypeGV, translationManager, jiraAuthenticationContext);

        MockGenericValue featureIssueTypeGV = new MockGenericValue("IssueType", 2l);
        featureIssueTypeGV.set("name", "Feature");
        featureIssueTypeGV.set("description", "A new feature");
        featureIssueTypeGV.set("iconurl", "http://www.atlassian.com");
        IssueType featureIssueType = new IssueTypeImpl(featureIssueTypeGV, translationManager, jiraAuthenticationContext);

        when(constantsManager.getAllIssueTypeObjects()).thenReturn(Lists.newArrayList(bugIssueType, featureIssueType));

        try
        {
            issueTypeManager.createIssueType(" fEAtUre ", "Usability design for a new feature", "http://www.usability.com");
            fail("Expected failure: An issue type with the name ' fEAtUre ' exists already.");
        }
        catch (IllegalStateException ex)
        {
            assertEquals("An issue type with the name ' fEAtUre ' exists already.", ex.getMessage());
        }
    }

    @Test
    public void testEditIssueTypes() throws Exception
    {
        MockGenericValue bugIssueTypeGV = new MockGenericValue("IssueType", 1l);
        bugIssueTypeGV.set("name", "Bug");
        bugIssueTypeGV.set("description", "A bug");
        bugIssueTypeGV.set("iconurl", "http://www.toastmasters.com");
        IssueType bugIssueType = new IssueTypeImpl(bugIssueTypeGV, translationManager, jiraAuthenticationContext);

        MockGenericValue featureIssueTypeGV = new MockGenericValue("IssueType", 2l)
        {
            @Override
            public void store() throws GenericEntityException
            {
            }
        };
        featureIssueTypeGV.set("name", "Feature");
        featureIssueTypeGV.set("description", "A new feature");
        featureIssueTypeGV.set("iconurl", "http://www.atlassian.com");
        IssueType featureIssueType = new IssueTypeImpl(featureIssueTypeGV, translationManager, jiraAuthenticationContext);

        when(constantsManager.getAllIssueTypeObjects()).thenReturn(Lists.newArrayList(bugIssueType, featureIssueType));

        issueTypeManager.editIssueType(featureIssueType, "Small Feature", "new description", "http://test.de");

        assertEquals("Small Feature", featureIssueType.getName());
        assertEquals("new description", featureIssueType.getDescription());
        assertEquals("http://test.de", featureIssueType.getIconUrl());

        verify(constantsManager).refreshIssueTypes();
    }

    @Test
    public void testRemoveIssueTypes() throws Exception
    {
        final GenericValue issueGV = new MockGenericValue("Issue", 1234l);
        final BooleanHolder removedBugIssueType = new BooleanHolder();
        GenericValue bugIssueTypeGV = new MockGenericValue("IssueType", 1l)
        {
            @Override
            public List<GenericValue> getRelated(String s) throws GenericEntityException
            {
                if (s.equals("ChildIssue"))
                {
                    return Lists.newArrayList(issueGV);
                }
                throw new UnsupportedOperationException("Method call not mocked!");
            }

            @Override
            public void remove() throws GenericEntityException
            {
                removedBugIssueType.booleanValue = true;
            }
        };
        bugIssueTypeGV.set("name", "Bug");
        bugIssueTypeGV.set("description", "A bug");
        bugIssueTypeGV.set("iconurl", "http://www.toastmasters.com");
        IssueType bugIssueType = new IssueTypeImpl(bugIssueTypeGV, translationManager, jiraAuthenticationContext){};

        MockGenericValue featureIssueTypeGV = new MockGenericValue("IssueType", 2l);
        featureIssueTypeGV.set("name", "Feature");
        featureIssueTypeGV.set("description", "A new feature");
        featureIssueTypeGV.set("iconurl", "http://www.atlassian.com");
        IssueType featureIssueType = new IssueTypeImpl(featureIssueTypeGV, translationManager, jiraAuthenticationContext);

        when(constantsManager.getAllIssueTypeObjects()).thenReturn(Lists.newArrayList(bugIssueType, featureIssueType));
        when(constantsManager.getIssueTypeObject("1")).thenReturn(bugIssueType);
        when(constantsManager.getIssueTypeObject("2")).thenReturn(featureIssueType);

        GenericValue projectGV = new MockGenericValue("Project", 1234l);
        when(projectManager.getProjects()).thenReturn(Lists.newArrayList(projectGV));

        JiraWorkflow workflow = mock(JiraWorkflow.class);
        when(workflowManager.getWorkflow(1234l, "1")).thenReturn(workflow);

        when(fieldLayoutManager.getFieldLayoutSchemes()).thenReturn(Collections.<FieldLayoutScheme>emptyList());
        when(issueTypeScreenSchemeManager.getIssueTypeScreenSchemes()).thenReturn(Collections.<IssueTypeScreenScheme>emptyList());

        when(constantsManager.getIssueType("1")).thenReturn(bugIssueTypeGV);
        when(constantsManager.getSubTaskIssueTypeObjects()).thenReturn(Collections.<IssueType>emptyList());
        when(constantsManager.getIssueTypes()).thenReturn(Lists.newArrayList(bugIssueTypeGV, featureIssueTypeGV));
        when(workflowManager.getWorkflow(1234l, "2")).thenReturn(workflow);

        MockGenericValue workflowScheme = new MockGenericValue("WorkflowScheme", 10l);
        when(workflowSchemeManager.getSchemes()).thenReturn(Lists.<GenericValue>newArrayList(workflowScheme));
        MockGenericValue workflowSchemeEntity = new MockGenericValue("WorkflowSchemeEntity", 20l);
        workflowSchemeEntity.set("issuetype", 1);

        when(workflowSchemeManager.getEntities(workflowScheme)).thenReturn(Lists.<GenericValue>newArrayList(workflowSchemeEntity));


        MockGenericValue fieldLayoutSchemeGV = new MockGenericValue("FieldLayoutScheme", 10l);
        final BooleanHolder removedFieldLayoutSchemed = new BooleanHolder();

        FieldLayoutScheme fieldLayoutScheme = new FieldLayoutSchemeImpl(fieldLayoutManager, fieldLayoutSchemeGV)
        {
            @Override
            public boolean containsEntity(String issueTypeId)
            {
                return issueTypeId.equals("1");
            }

            @Override
            public void removeEntity(String issueTypeId)
            {
                if (issueTypeId.equals("1"))
                {
                    removedFieldLayoutSchemed.booleanValue = true;
                }
            }
        };
        when(fieldLayoutManager.getFieldLayoutSchemes()).thenReturn(Lists.<FieldLayoutScheme>newArrayList(fieldLayoutScheme));

        MockGenericValue issueTypeScreenSchemeGV = new MockGenericValue("IssueTypeScreenScheme", 10l);

        final BooleanHolder removedIssueTypeScreenScheme = new BooleanHolder();
        IssueTypeScreenScheme issueTypeScreenScheme = new IssueTypeScreenSchemeImpl(issueTypeScreenSchemeManager, issueTypeScreenSchemeGV)
        {
            @Override
            public boolean containsEntity(String issueTypeId)
            {
                return issueTypeId.equals("1");
            }

            @Override
            public void removeEntity(String issueTypeId)
            {
                if (issueTypeId.equals("1"))
                {
                    removedIssueTypeScreenScheme.booleanValue = true;
                }
            }
        };
        when(issueTypeScreenSchemeManager.getIssueTypeScreenSchemes()).thenReturn(Lists.newArrayList(issueTypeScreenScheme));

        issueTypeManager.removeIssueType("1", "2");

        assertTrue(removedFieldLayoutSchemed.booleanValue);
        verify(workflowSchemeManager).deleteEntity(20l);
        assertTrue(removedIssueTypeScreenScheme.booleanValue);
        verify(fieldConfigSchemeManager).removeInvalidFieldConfigSchemesForIssueType(bugIssueType);
        verify(constantsManager).refreshIssueTypes();
        verify(issueTypeSchemeManager).removeOptionFromAllSchemes("1");

        verify(issueIndexManager).reIndex(issueGV);
        verify(ofBizDelegator).storeAll(Lists.<GenericValue>newArrayList(issueGV));
        assertTrue(removedBugIssueType.booleanValue);
        verify(constantsManager).refreshIssueTypes();
    }


    class IssueTypeFieldsdArgumentMatcher extends ArgumentMatcher<Map<String, Object>>
    {
        final String id;
        private final String name;
        private final String descpription;
        private final String iconUrl;
        private final String style;

        IssueTypeFieldsdArgumentMatcher(String id, String name, String descpription, String iconUrl, String style)
        {
            this.id = id;
            this.name = name;
            this.descpription = descpription;
            this.iconUrl = iconUrl;
            this.style = style;
        }

        public boolean matches(Object o)
        {
            Map<String, Object> gv = (Map<String, Object>) o;
            return id.equals(gv.get("id")) && name.equals(gv.get("name")) && descpription.equals(gv.get("description")) && iconUrl.equals(gv.get("iconurl")) && ((style == null) ? gv.get("style") == null : style.equals(gv.get("style")));
        }
    }


}
