package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operator.Operator;
import com.atlassian.query.order.OrderByImpl;
import com.opensymphony.user.User;
import org.easymock.EasyMock;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @since v4.0
 */
public class TestCustomFieldContextConfigHelperImpl extends MockControllerTestCase
{
    private SearchProvider searchProvider;
    private FieldConfigSchemeManager fieldConfigSchemeManager;
    private CustomField customField;
    private User user;

    @Before
    public void setUp() throws Exception
    {
        searchProvider = mockController.getMock(SearchProvider.class);
        fieldConfigSchemeManager = mockController.getMock(FieldConfigSchemeManager.class);
        customField = mockController.getMock(CustomField.class);
        user = null;
    }

    @Test
    public void testDoesContextHaveIssuesWithContextObjects() throws Exception
    {
        JiraContextNode context1 = EasyMock.createMock(JiraContextNode.class);
        EasyMock.expect(context1.getProjectObject())
                .andReturn(null);

        JiraContextNode context2 = EasyMock.createMock(JiraContextNode.class);
        EasyMock.expect(context2.getProjectObject())
                .andReturn(new MockProject(555L)).times(2);

        GenericValue gv1 = new MockGenericValue("issuetype", MapBuilder.newBuilder().add("id", 666L).toMap());
        
        replay(context1, context2);

        final boolean contextHasIssues = true;
        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager)
        {
            @Override
            boolean _doesContextHaveIssues(final User user, final List<Long> projectIds, final List<Long> issueTypeIds)
            {
                assertEquals(Collections.singletonList(555L), projectIds);
                assertEquals(Collections.singletonList(666L), issueTypeIds);
                return contextHasIssues;
            }
        };

        boolean result = helper.doesContextHaveIssues(user, CollectionBuilder.newBuilder(context1, context2, null).asList(), CollectionBuilder.newBuilder(gv1, null).asList());
        assertEquals(contextHasIssues, result);

        verify(context1, context2);
    }

    @Test
    public void testDoesContextHaveIssuesWithContextObjectsNull() throws Exception
    {
        JiraContextNode context1 = EasyMock.createMock(JiraContextNode.class);
        EasyMock.expect(context1.getProjectObject())
                .andReturn(null);

        JiraContextNode context2 = EasyMock.createMock(JiraContextNode.class);
        EasyMock.expect(context2.getProjectObject())
                .andReturn(new MockProject(555L)).times(2);

        replay(context1, context2);

        final boolean contextHasIssues = true;
        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager)
        {
            @Override
            boolean _doesContextHaveIssues(final User user, final List<Long> projectIds, final List<Long> issueTypeIds)
            {
                assertEquals(Collections.singletonList(555L), projectIds);
                assertEquals(Collections.<Long>emptyList(), issueTypeIds);
                return contextHasIssues;
            }
        };

        boolean result = helper.doesContextHaveIssues(user, CollectionBuilder.newBuilder(context1, context2, null).asList(), null);
        assertEquals(contextHasIssues, result);

        verify(context1, context2);
    }

    @Test
    public void testDoesContextHaveIssuesWithGenericValues() throws Exception
    {
        GenericValue project1 = new MockGenericValue("project", MapBuilder.newBuilder().add("id", 555L).toMap());
        GenericValue issueType1 = new MockGenericValue("issuetype", MapBuilder.newBuilder().add("id", 666L).toMap());

        replay();

        final boolean contextHasIssues = true;
        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager)
        {
            @Override
            boolean _doesContextHaveIssues(final User user, final List<Long> projectIds, final List<Long> issueTypeIds)
            {
                assertEquals(Collections.singletonList(555L), projectIds);
                assertEquals(Collections.singletonList(666L), issueTypeIds);
                return contextHasIssues;
            }
        };

        boolean result = helper.doesContextHaveIssues(user, CollectionBuilder.newBuilder(project1, null).asList(), CollectionBuilder.newBuilder(issueType1, null).asSet());
        assertEquals(contextHasIssues, result);
    }

    @Test
    public void testDoesContextHaveIssuesWithGenericValuesNull() throws Exception
    {
        replay();

        final boolean contextHasIssues = true;
        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager)
        {
            @Override
            boolean _doesContextHaveIssues(final User user, final List<Long> projectIds, final List<Long> issueTypeIds)
            {
                assertEquals(Collections.<Long>emptyList(), projectIds);
                assertEquals(Collections.<Long>emptyList(), issueTypeIds);
                return contextHasIssues;
            }
        };

        boolean result = helper.doesContextHaveIssues(user, (List<GenericValue>) null, null);
        assertEquals(contextHasIssues, result);
    }

    @Test
    public void testDoesGlobalContextHaveIssues() throws Exception
    {
        replay();

        final boolean contextHasIssues = true;
        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager)
        {
            @Override
            boolean _doesContextHaveIssues(final User user, final List<Long> projectIds, final List<Long> issueTypeIds)
            {
                assertEquals(Collections.<Long>emptyList(), projectIds);
                assertEquals(Collections.<Long>emptyList(), issueTypeIds);
                return contextHasIssues;
            }
        };

        boolean result = helper.doesGlobalContextHaveIssues(user);
        assertEquals(contextHasIssues, result);
    }

    @Test
    public void test_DoesContextHaveIssuesEmptyLists() throws Exception
    {
        Query query = new QueryImpl(null, new OrderByImpl(), null);
        EasyMock.expect(searchProvider.searchCountOverrideSecurity(query, user))
                .andReturn(1L);

        replay();

        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager);

        boolean result = helper._doesContextHaveIssues(user, Collections.<Long>emptyList(), Collections.<Long>emptyList());
        assertTrue(result);
    }

    @Test
    public void test_DoesContextHaveIssuesEmptyIssueTypes() throws Exception
    {
        TerminalClauseImpl whereClause = new TerminalClauseImpl("project", Operator.IN, new MultiValueOperand(11L, 22L));
        Query query = new QueryImpl(whereClause, new OrderByImpl(), null);
        EasyMock.expect(searchProvider.searchCountOverrideSecurity(query, user))
                .andReturn(0L);

        replay();

        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager);

        boolean result = helper._doesContextHaveIssues(user, CollectionBuilder.newBuilder(11L, 22L).asList(), Collections.<Long>emptyList());
        assertFalse(result);
    }

    @Test
    public void test_DoesContextHaveIssuesEmptyProjects() throws Exception
    {
        TerminalClauseImpl whereClause = new TerminalClauseImpl("issuetype", Operator.IN, new MultiValueOperand(11L, 22L));
        Query query = new QueryImpl(whereClause, new OrderByImpl(), null);
        EasyMock.expect(searchProvider.searchCountOverrideSecurity(query, user))
                .andReturn(0L);

        replay();

        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager);

        boolean result = helper._doesContextHaveIssues(user, Collections.<Long>emptyList(), CollectionBuilder.newBuilder(11L, 22L).asList());
        assertFalse(result);
    }

    @Test
    public void test_DoesContextHaveIssuesProjectsAndIssueTypes() throws Exception
    {
        TerminalClauseImpl projects = new TerminalClauseImpl("project", Operator.IN, new MultiValueOperand(11L, 22L));
        TerminalClauseImpl issueTypes = new TerminalClauseImpl("issuetype", Operator.IN, new MultiValueOperand(11L, 22L));
        AndClause whereClause = new AndClause(projects, issueTypes);

        Query query = new QueryImpl(whereClause, new OrderByImpl(), null);
        EasyMock.expect(searchProvider.searchCountOverrideSecurity(query, user))
                .andReturn(0L);

        replay();

        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager);

        boolean result = helper._doesContextHaveIssues(user, CollectionBuilder.newBuilder(11L, 22L).asList(), CollectionBuilder.newBuilder(11L, 22L).asList());
        assertFalse(result);
    }

    @Test
    public void testAddNewCustomFieldContextHasIssues() throws Exception
    {
        replay();
        
        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager)
        {
            @Override
            boolean doesContextHaveIssues(final User user, final List<JiraContextNode> projectContexts, final List<GenericValue> issueTypes)
            {
                return true;
            }
        };

        boolean result = helper.doesAddingContextToCustomFieldAffectIssues(user, customField, Collections.<JiraContextNode>emptyList(), Collections.<GenericValue>emptyList(), true);
        assertTrue(result);
    }

    @Test
    public void testAddNewCustomFieldContextDoesntHaveIssues() throws Exception
    {
        replay();

        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager)
        {
            @Override
            boolean doesContextHaveIssues(final User user, final List<JiraContextNode> projectContexts, final List<GenericValue> issueTypes)
            {
                return false;
            }
        };

        boolean result = helper.doesAddingContextToCustomFieldAffectIssues(user, customField, Collections.<JiraContextNode>emptyList(), Collections.<GenericValue>emptyList(), true);
        assertFalse(result);
    }

    @Test
    public void testAddExistingFieldHasGlobalScheme() throws Exception
    {
        FieldConfigScheme scheme = mockController.getMock(FieldConfigScheme.class);
        EasyMock.expect(scheme.isGlobal())
                .andReturn(true);
        EasyMock.expect(fieldConfigSchemeManager.getConfigSchemesForField(customField))
                .andReturn(Collections.singletonList(scheme));

        replay();

        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager);

        boolean result = helper.doesAddingContextToCustomFieldAffectIssues(user, customField, Collections.<JiraContextNode>emptyList(), Collections.<GenericValue>emptyList(), false);
        assertFalse(result);
    }

    @Test
    public void testAddExistingFieldDoesntHaveGlobalScheme() throws Exception
    {
        FieldConfigScheme scheme = mockController.getMock(FieldConfigScheme.class);
        EasyMock.expect(scheme.isGlobal())
                .andReturn(false);
        EasyMock.expect(fieldConfigSchemeManager.getConfigSchemesForField(customField))
                .andReturn(Collections.singletonList(scheme));

        replay();

        final boolean contextHaveIssues = false;
        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager)
        {
            @Override
            boolean doesContextHaveIssues(final User user, final List<JiraContextNode> projectContexts, final List<GenericValue> issueTypes)
            {
                return contextHaveIssues;
            }
        };

        boolean result = helper.doesAddingContextToCustomFieldAffectIssues(user, customField, Collections.<JiraContextNode>emptyList(), Collections.<GenericValue>emptyList(), false);
        assertEquals(contextHaveIssues, result);
    }

    @Test
    public void testChangingNewIsGlobal() throws Exception
    {
        FieldConfigScheme scheme = mockController.getMock(FieldConfigScheme.class);
        EasyMock.expect(scheme.isGlobal())
                .andReturn(false);

        replay();

        final boolean contextHaveIssues = false;
        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager)
        {
            @Override
            boolean doesGlobalContextHaveIssues(final User user)
            {
                return contextHaveIssues;
            }
        };

        boolean result = helper.doesChangingContextAffectIssues(user, customField, scheme, true, Collections.<JiraContextNode>emptyList(), Collections.<GenericValue>emptyList());
        assertEquals(contextHaveIssues, result);
    }

    @Test
    public void testChangingOldIsGlobal() throws Exception
    {
        FieldConfigScheme scheme = mockController.getMock(FieldConfigScheme.class);
        EasyMock.expect(scheme.isGlobal())
                .andReturn(true);

        replay();

        final boolean contextHaveIssues = false;
        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager)
        {
            @Override
            boolean doesGlobalContextHaveIssues(final User user)
            {
                return contextHaveIssues;
            }
        };

        boolean result = helper.doesChangingContextAffectIssues(user, customField, scheme, false, Collections.<JiraContextNode>emptyList(), Collections.<GenericValue>emptyList());
        assertEquals(contextHaveIssues, result);
    }

    @Test
    public void testChangingNeitherGlobalFieldHasGlobal() throws Exception
    {
        FieldConfigScheme oldScheme = EasyMock.createMock(FieldConfigScheme.class);
        EasyMock.expect(oldScheme.isGlobal())
                .andReturn(false);

        FieldConfigScheme otherScheme = EasyMock.createMock(FieldConfigScheme.class);
        EasyMock.expect(otherScheme.isGlobal())
                .andReturn(true);

        EasyMock.expect(fieldConfigSchemeManager.getConfigSchemesForField(customField))
                .andReturn(Collections.singletonList(otherScheme));

        replay(oldScheme, otherScheme);

        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager);

        boolean result = helper.doesChangingContextAffectIssues(user, customField, oldScheme, false, Collections.<JiraContextNode>emptyList(), Collections.<GenericValue>emptyList());
        assertFalse(result);

        verify(oldScheme, otherScheme);
    }

    @Test
    public void testChangingNeitherGlobalFieldDoesntHaveGlobalNewContextHasIssues() throws Exception
    {
        FieldConfigScheme oldScheme = EasyMock.createMock(FieldConfigScheme.class);
        EasyMock.expect(oldScheme.isGlobal())
                .andReturn(false);

        EasyMock.expect(fieldConfigSchemeManager.getConfigSchemesForField(customField))
                .andReturn(Collections.<FieldConfigScheme>emptyList());

        replay(oldScheme);

        final List<JiraContextNode> inputProjectContexts = Collections.emptyList();
        final List<GenericValue> inputIssueTypes = Collections.emptyList();

        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager)
        {
            @Override
            boolean doesContextHaveIssues(final User user, final List<JiraContextNode> projectContexts, final List<GenericValue> issueTypes)
            {
                assertSame(inputProjectContexts, projectContexts);
                assertSame(inputIssueTypes, issueTypes);
                return true;
            }
        };

        boolean result = helper.doesChangingContextAffectIssues(user, customField, oldScheme, false, inputProjectContexts, inputIssueTypes);
        assertTrue(result);

        verify(oldScheme);
    }

    @Test
    public void testChangingNeitherGlobalFieldDoesntHaveGlobalNewContextDoesntHaveIssues() throws Exception
    {
        final List<GenericValue> associatedProjects = Collections.<GenericValue>singletonList(new MockGenericValue("project"));
        final Set<GenericValue> associatedIssueTypes = Collections.<GenericValue>singleton(new MockGenericValue("issuetype"));

        FieldConfigScheme oldScheme = EasyMock.createMock(FieldConfigScheme.class);
        EasyMock.expect(oldScheme.isGlobal())
                .andReturn(false);
        EasyMock.expect(oldScheme.getAssociatedProjects())
                .andReturn(associatedProjects);
        EasyMock.expect(oldScheme.getAssociatedIssueTypes())
                .andReturn(associatedIssueTypes);

        EasyMock.expect(fieldConfigSchemeManager.getConfigSchemesForField(customField))
                .andReturn(Collections.<FieldConfigScheme>emptyList());

        replay(oldScheme);

        final List<JiraContextNode> inputProjectContexts = Collections.emptyList();
        final List<GenericValue> inputIssueTypes = Collections.emptyList();

        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager)
        {
            @Override
            boolean doesContextHaveIssues(final User user, final List<JiraContextNode> projectContexts, final List<GenericValue> issueTypes)
            {
                assertSame(inputProjectContexts, projectContexts);
                assertSame(inputIssueTypes, issueTypes);
                return false;
            }

            @Override
            boolean doesContextHaveIssues(final User user, final List<GenericValue> projects, final Set<GenericValue> issueTypes)
            {
                assertEquals(associatedProjects, projects);
                assertEquals(associatedIssueTypes, issueTypes);
                return true;
            }
        };

        boolean result = helper.doesChangingContextAffectIssues(user, customField, oldScheme, false, inputProjectContexts, inputIssueTypes);
        assertTrue(result);

        verify(oldScheme);
    }

    @Test
    public void testRemoveNonGlobalSchemeFieldStillHasGlobal() throws Exception
    {
        FieldConfigScheme scheme = EasyMock.createMock(FieldConfigScheme.class);
        EasyMock.expect(scheme.isGlobal())
                .andReturn(false);
        FieldConfigScheme otherScheme = EasyMock.createMock(FieldConfigScheme.class);
        EasyMock.expect(otherScheme.isGlobal())
                .andReturn(true);
        EasyMock.expect(fieldConfigSchemeManager.getConfigSchemesForField(customField))
                .andReturn(Collections.singletonList(otherScheme));

        replay(scheme, otherScheme);

        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager);

        boolean result = helper.doesRemovingSchemeFromCustomFieldAffectIssues(user, customField, scheme);
        assertFalse(result);
        
        verify(scheme, otherScheme);
    }

    @Test
    public void testRemoveNonGlobalSchemeFieldHasNoOtherGlobal() throws Exception
    {
        final FieldConfigScheme scheme = EasyMock.createMock(FieldConfigScheme.class);
        EasyMock.expect(scheme.isGlobal())
                .andReturn(false);
        EasyMock.expect(fieldConfigSchemeManager.getConfigSchemesForField(customField))
                .andReturn(Collections.<FieldConfigScheme>emptyList());

        replay(scheme);

        final boolean contextHasIssues = true;
        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager)
        {
            @Override
            boolean doesFieldConfigSchemeHaveIssues(final User user, final FieldConfigScheme fieldConfigScheme)
            {
                assertSame(scheme, fieldConfigScheme);
                return contextHasIssues;
            }
        };

        boolean result = helper.doesRemovingSchemeFromCustomFieldAffectIssues(user, customField, scheme);
        assertEquals(contextHasIssues, result);

        verify(scheme);
    }

    @Test
    public void testRemoveGlobalSchemeFieldHasNoOtherNonGlobalSchemes() throws Exception
    {
        final FieldConfigScheme scheme = EasyMock.createMock(FieldConfigScheme.class);
        EasyMock.expect(scheme.isGlobal())
                .andReturn(true);
        EasyMock.expect(fieldConfigSchemeManager.getConfigSchemesForField(customField))
                .andReturn(Collections.<FieldConfigScheme>emptyList());

        replay(scheme);

        final boolean contextHasIssues = true;
        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager)
        {
            @Override
            boolean doesGlobalContextHaveIssues(final User user)
            {
                return contextHasIssues;
            }
        };

        boolean result = helper.doesRemovingSchemeFromCustomFieldAffectIssues(user, customField, scheme);
        assertEquals(contextHasIssues, result);

        verify(scheme);
    }

    @Test
    public void testRemoveGlobalSchemeFieldHasOneOtherNonGlobalScheme() throws Exception
    {
        final FieldConfigScheme scheme = EasyMock.createMock(FieldConfigScheme.class);
        EasyMock.expect(scheme.isGlobal())
                .andReturn(true);

        final FieldConfigScheme otherScheme = EasyMock.createMock(FieldConfigScheme.class);
        EasyMock.expect(fieldConfigSchemeManager.getConfigSchemesForField(customField))
                .andReturn(CollectionBuilder.newBuilder(otherScheme).asList());

        final List<GenericValue> associatedProjects = Collections.<GenericValue>singletonList(new MockGenericValue("project", MapBuilder.newBuilder().add("id", 555L).toMap()));
        final Set<GenericValue> associatedIssueTypes = Collections.<GenericValue>singleton(new MockGenericValue("issuetype", MapBuilder.newBuilder().add("id", 666L).toMap()));

        EasyMock.expect(otherScheme.isGlobal())
                .andReturn(false);
        EasyMock.expect(otherScheme.getAssociatedProjects())
                .andReturn(associatedProjects);
        EasyMock.expect(otherScheme.getAssociatedIssueTypes())
                .andReturn(associatedIssueTypes);

        TerminalClause projectClause = new TerminalClauseImpl("project", Operator.IN, new MultiValueOperand(555L));
        TerminalClause issueTypeClause = new TerminalClauseImpl("issuetype", Operator.IN, new MultiValueOperand(666L));
        AndClause andClause = new AndClause(projectClause, issueTypeClause);
        NotClause whereClause = new NotClause(andClause);
        Query expectedQuery = new QueryImpl(whereClause, new OrderByImpl(), null);

        EasyMock.expect(searchProvider.searchCountOverrideSecurity(expectedQuery, user))
                .andReturn(1L);

        replay(scheme, otherScheme);

        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager);

        boolean result = helper.doesRemovingSchemeFromCustomFieldAffectIssues(user, customField, scheme);
        assertTrue(result);

        verify(scheme, otherScheme);
    }

    @Test
    public void testRemoveGlobalSchemeFieldHasTwoOtherNonGlobalSchemes() throws Exception
    {
        final FieldConfigScheme scheme = EasyMock.createMock(FieldConfigScheme.class);
        EasyMock.expect(scheme.isGlobal())
                .andReturn(true);

        final FieldConfigScheme otherScheme1 = EasyMock.createMock(FieldConfigScheme.class);
        final FieldConfigScheme otherScheme2 = EasyMock.createMock(FieldConfigScheme.class);
        EasyMock.expect(fieldConfigSchemeManager.getConfigSchemesForField(customField))
                .andReturn(CollectionBuilder.newBuilder(otherScheme1, otherScheme2).asList());

        final Set<GenericValue> associatedIssueTypes1 = Collections.<GenericValue>singleton(new MockGenericValue("issuetype", MapBuilder.newBuilder().add("id", 666L).toMap()));
        final List<GenericValue> associatedProjects2 = Collections.<GenericValue>singletonList(new MockGenericValue("project", MapBuilder.newBuilder().add("id", 555L).toMap()));

        EasyMock.expect(otherScheme1.isGlobal())
                .andReturn(false);
        EasyMock.expect(otherScheme1.getAssociatedProjects())
                .andReturn(null);
        EasyMock.expect(otherScheme1.getAssociatedIssueTypes())
                .andReturn(associatedIssueTypes1);

        EasyMock.expect(otherScheme2.isGlobal())
                .andReturn(false);
        EasyMock.expect(otherScheme2.getAssociatedProjects())
                .andReturn(associatedProjects2);
        EasyMock.expect(otherScheme2.getAssociatedIssueTypes())
                .andReturn(null);

        TerminalClause issueTypeClause = new TerminalClauseImpl("issuetype", Operator.IN, new MultiValueOperand(666L));
        NotClause notClause1 = new NotClause(issueTypeClause);

        TerminalClause projectClause = new TerminalClauseImpl("project", Operator.IN, new MultiValueOperand(555L));
        NotClause notClause2 = new NotClause(projectClause);

        AndClause andClause = new AndClause(notClause1, notClause2);

        Query expectedQuery = new QueryImpl(andClause, new OrderByImpl(), null);

        EasyMock.expect(searchProvider.searchCountOverrideSecurity(expectedQuery, user))
                .andReturn(1L);

        replay(scheme, otherScheme1, otherScheme2);

        CustomFieldContextConfigHelperImpl helper = new CustomFieldContextConfigHelperImpl(searchProvider, fieldConfigSchemeManager);

        boolean result = helper.doesRemovingSchemeFromCustomFieldAffectIssues(user, customField, scheme);
        assertTrue(result);

        verify(scheme, otherScheme1, otherScheme2);
    }
}
