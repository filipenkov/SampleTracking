package com.atlassian.jira.jql.values;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.opensymphony.user.User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @since v4.0
 */
public class TestCustomFieldOptionsClauseValuesGenerator extends MockControllerTestCase
{

    private CustomFieldManager customFieldManager;
    private SearchHandlerManager searchHandlerManager;
    private FieldConfigSchemeManager fieldConfigSchemeManager;
    private PermissionManager permissionManager;
    private CustomFieldOptionsClauseValuesGenerator valuesGenerator;

    @Before
    public void setUp() throws Exception
    {
        this.customFieldManager = mockController.getMock(CustomFieldManager.class);
        this.searchHandlerManager = mockController.getMock(SearchHandlerManager.class);
        this.fieldConfigSchemeManager = mockController.getMock(FieldConfigSchemeManager.class);
        this.permissionManager = mockController.getMock(PermissionManager.class);

        this.valuesGenerator = new CustomFieldOptionsClauseValuesGenerator(customFieldManager, searchHandlerManager, fieldConfigSchemeManager, permissionManager)
        {
            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }
        };
    }

    @Test
    public void testGetPossibleValuesNoFieldForClauseName() throws Exception
    {
        searchHandlerManager.getFieldIds(null, "clauseName");
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "clauseName", "b", 10);

        assertEquals(0, possibleValues.getResults().size());
        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesNoCustomFieldForFieldName() throws Exception
    {
        searchHandlerManager.getFieldIds(null, "clauseName");
        mockController.setReturnValue(Collections.singletonList("fieldId"));

        permissionManager.getProjectObjects(Permissions.BROWSE, null);
        mockController.setReturnValue(Collections.emptyList());

        customFieldManager.getCustomFieldObject("fieldId");
        mockController.setReturnValue(null);

        mockController.replay();

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "clauseName", "b", 10);

        assertEquals(0, possibleValues.getResults().size());
        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesNoContextForVisibleProjects() throws Exception
    {
        final JiraContextNode contextNode = mockController.getMock(JiraContextNode.class);
        contextNode.getProjectObject();
        mockController.setReturnValue(new MockProject(2L, "ANA", "Another"));

        searchHandlerManager.getFieldIds(null, "clauseName");
        mockController.setReturnValue(Collections.singletonList("fieldId"));

        permissionManager.getProjectObjects(Permissions.BROWSE, null);
        mockController.setReturnValue(Collections.singletonList(new MockProject(1L, "TST", "Test")));

        final CustomField customField = mockController.getMock(CustomField.class);

        customFieldManager.getCustomFieldObject("fieldId");
        mockController.setReturnValue(customField);

        mockController.replay();

        valuesGenerator = new CustomFieldOptionsClauseValuesGenerator(customFieldManager, searchHandlerManager, fieldConfigSchemeManager, permissionManager)
        {
            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }

            @Override
            List<JiraContextNode> getContextsForCustomField(final CustomField customField)
            {
                return Collections.singletonList(contextNode);
            }
        };

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "clauseName", "b", 10);

        assertEquals(0, possibleValues.getResults().size());
        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesHappyPathWithNoPrefix() throws Exception
    {
        final MockProject project = new MockProject(1L, "TST", "Test");
        final JiraContextNode contextNode = mockController.getMock(JiraContextNode.class);
        contextNode.getProjectObject();
        mockController.setReturnValue(project);

        searchHandlerManager.getFieldIds(null, "clauseName");
        mockController.setReturnValue(Collections.singletonList("fieldId"));

        permissionManager.getProjectObjects(Permissions.BROWSE, null);
        mockController.setReturnValue(Collections.singletonList(project));

        final Collection<String> opts = CollectionBuilder.newBuilder("Opt 2", "Opt 1", "Not match").asCollection();
        final Options options = mockController.getMock(Options.class);
        options.iterator();
        mockController.setReturnValue(opts.iterator());

        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getOptions(null, contextNode);
        mockController.setReturnValue(options);

        customFieldManager.getCustomFieldObject("fieldId");
        mockController.setReturnValue(customField);

        mockController.replay();

        valuesGenerator = new CustomFieldOptionsClauseValuesGenerator(customFieldManager, searchHandlerManager, fieldConfigSchemeManager, permissionManager)
        {
            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }

            @Override
            List<JiraContextNode> getContextsForCustomField(final CustomField customField)
            {
                return Collections.singletonList(contextNode);
            }
        };

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "clauseName", "", 10);

        assertEquals(3, possibleValues.getResults().size());
        assertTrue(possibleValues.getResults().get(0).equals(new ClauseValuesGenerator.Result("Not match")));
        assertTrue(possibleValues.getResults().get(1).equals(new ClauseValuesGenerator.Result("Opt 1")));
        assertTrue(possibleValues.getResults().get(2).equals(new ClauseValuesGenerator.Result("Opt 2")));
        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesHappyPathWithNoPrefixHitsLimit() throws Exception
    {
        final MockProject project = new MockProject(1L, "TST", "Test");
        final JiraContextNode contextNode = mockController.getMock(JiraContextNode.class);
        contextNode.getProjectObject();
        mockController.setReturnValue(project);

        searchHandlerManager.getFieldIds(null, "clauseName");
        mockController.setReturnValue(Collections.singletonList("fieldId"));

        permissionManager.getProjectObjects(Permissions.BROWSE, null);
        mockController.setReturnValue(Collections.singletonList(project));

        final Collection<String> opts = CollectionBuilder.newBuilder("Opt 2", "Opt 1", "Opt 3", "Opt 4", "Opt 5").asCollection();
        final Options options = mockController.getMock(Options.class);
        options.iterator();
        mockController.setReturnValue(opts.iterator());

        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getOptions(null, contextNode);
        mockController.setReturnValue(options);

        customFieldManager.getCustomFieldObject("fieldId");
        mockController.setReturnValue(customField);

        mockController.replay();

        valuesGenerator = new CustomFieldOptionsClauseValuesGenerator(customFieldManager, searchHandlerManager, fieldConfigSchemeManager, permissionManager)
        {
            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }

            @Override
            List<JiraContextNode> getContextsForCustomField(final CustomField customField)
            {
                return Collections.singletonList(contextNode);
            }
        };

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "clauseName", "", 4);

        assertEquals(4, possibleValues.getResults().size());
        assertTrue(possibleValues.getResults().get(0).equals(new ClauseValuesGenerator.Result("Opt 1")));
        assertTrue(possibleValues.getResults().get(1).equals(new ClauseValuesGenerator.Result("Opt 2")));
        assertTrue(possibleValues.getResults().get(2).equals(new ClauseValuesGenerator.Result("Opt 3")));
        assertTrue(possibleValues.getResults().get(3).equals(new ClauseValuesGenerator.Result("Opt 4")));
        mockController.verify();
    }
    
    @Test
    public void testGetPossibleValuesHappyPathWithPrefix() throws Exception
    {
        final MockProject project = new MockProject(1L, "TST", "Test");
        final JiraContextNode contextNode = mockController.getMock(JiraContextNode.class);
        contextNode.getProjectObject();
        mockController.setReturnValue(project);

        searchHandlerManager.getFieldIds(null, "clauseName");
        mockController.setReturnValue(Collections.singletonList("fieldId"));

        permissionManager.getProjectObjects(Permissions.BROWSE, null);
        mockController.setReturnValue(Collections.singletonList(project));

        final Collection<String> opts = CollectionBuilder.newBuilder("Opt 2", "Opt 1", "Not match").asCollection();
        final Options options = mockController.getMock(Options.class);
        options.iterator();
        mockController.setReturnValue(opts.iterator());

        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getOptions(null, contextNode);
        mockController.setReturnValue(options);

        customFieldManager.getCustomFieldObject("fieldId");
        mockController.setReturnValue(customField);

        mockController.replay();

        valuesGenerator = new CustomFieldOptionsClauseValuesGenerator(customFieldManager, searchHandlerManager, fieldConfigSchemeManager, permissionManager)
        {
            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }

            @Override
            List<JiraContextNode> getContextsForCustomField(final CustomField customField)
            {
                return Collections.singletonList(contextNode);
            }
        };

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "clauseName", "o", 10);

        assertEquals(2, possibleValues.getResults().size());
        assertTrue(possibleValues.getResults().get(0).equals(new ClauseValuesGenerator.Result("Opt 1")));
        assertTrue(possibleValues.getResults().get(1).equals(new ClauseValuesGenerator.Result("Opt 2")));
        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesHappyPathWithPrefixRootContext() throws Exception
    {
        final MockProject project = new MockProject(1L, "TST", "Test");
        final JiraContextNode contextNode = mockController.getMock(JiraContextNode.class);
        contextNode.getProjectObject();
        mockController.setReturnValue(null);

        searchHandlerManager.getFieldIds(null, "clauseName");
        mockController.setReturnValue(Collections.singletonList("fieldId"));

        permissionManager.getProjectObjects(Permissions.BROWSE, null);
        mockController.setReturnValue(Collections.singletonList(project));

        final Collection<String> opts = CollectionBuilder.newBuilder("Opt 2", "Opt 1", "Not match").asCollection();
        final Options options = mockController.getMock(Options.class);
        options.iterator();
        mockController.setReturnValue(opts.iterator());

        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getOptions(null, contextNode);
        mockController.setReturnValue(options);

        customFieldManager.getCustomFieldObject("fieldId");
        mockController.setReturnValue(customField);

        mockController.replay();

        valuesGenerator = new CustomFieldOptionsClauseValuesGenerator(customFieldManager, searchHandlerManager, fieldConfigSchemeManager, permissionManager)
        {
            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }

            @Override
            List<JiraContextNode> getContextsForCustomField(final CustomField customField)
            {
                return Collections.singletonList(contextNode);
            }
        };

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "clauseName", "o", 10);

        assertEquals(2, possibleValues.getResults().size());
        assertTrue(possibleValues.getResults().get(0).equals(new ClauseValuesGenerator.Result("Opt 1")));
        assertTrue(possibleValues.getResults().get(1).equals(new ClauseValuesGenerator.Result("Opt 2")));
        mockController.verify();
    }

    @Test
    public void testGetContextsForCustomFieldOnlyRootContext() throws Exception
    {
        final JiraContextNode rootContextNode = mockController.getMock(JiraContextNode.class);

        final CustomField customField = mockController.getMock(CustomField.class);

        fieldConfigSchemeManager.getConfigSchemesForField(customField);
        mockController.setReturnValue(Collections.emptyList());

        mockController.replay();

        valuesGenerator = new CustomFieldOptionsClauseValuesGenerator(customFieldManager, searchHandlerManager, fieldConfigSchemeManager, permissionManager)
        {
            @Override
            JiraContextNode getRootContext()
            {
                return rootContextNode;
            }
        };

        final List<JiraContextNode> contexts = valuesGenerator.getContextsForCustomField(customField);
        assertEquals(1, contexts.size());
        assertTrue(contexts.contains(rootContextNode));

        mockController.verify();
    }

    @Test
    public void testGetContextsForCustomFieldOtherContexts() throws Exception
    {
        final JiraContextNode contextNode = mockController.getMock(JiraContextNode.class);

        final FieldConfigScheme scheme = mockController.getMock(FieldConfigScheme.class);
        scheme.getContexts();
        mockController.setReturnValue(Collections.singletonList(contextNode));

        final CustomField customField = mockController.getMock(CustomField.class);

        fieldConfigSchemeManager.getConfigSchemesForField(customField);
        mockController.setReturnValue(Collections.singletonList(scheme));

        mockController.replay();

        final List<JiraContextNode> contexts = valuesGenerator.getContextsForCustomField(customField);
        assertEquals(1, contexts.size());
        assertTrue(contexts.contains(contextNode));

        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesMatchFullValue() throws Exception
    {
        final MockProject project = new MockProject(1L, "TST", "Test");
        final JiraContextNode contextNode = mockController.getMock(JiraContextNode.class);
        contextNode.getProjectObject();
        mockController.setReturnValue(project);

        searchHandlerManager.getFieldIds(null, "clauseName");
        mockController.setReturnValue(Collections.singletonList("fieldId"));

        permissionManager.getProjectObjects(Permissions.BROWSE, null);
        mockController.setReturnValue(Collections.singletonList(project));

        final Collection<String> opts = CollectionBuilder.newBuilder("Opt 2", "Opt 1", "Not match").asCollection();
        final Options options = mockController.getMock(Options.class);
        options.iterator();
        mockController.setReturnValue(opts.iterator());

        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getOptions(null, contextNode);
        mockController.setReturnValue(options);

        customFieldManager.getCustomFieldObject("fieldId");
        mockController.setReturnValue(customField);

        mockController.replay();

        valuesGenerator = new CustomFieldOptionsClauseValuesGenerator(customFieldManager, searchHandlerManager, fieldConfigSchemeManager, permissionManager)
        {
            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }

            @Override
            List<JiraContextNode> getContextsForCustomField(final CustomField customField)
            {
                return Collections.singletonList(contextNode);
            }
        };

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "clauseName", "opt 1", 10);

        assertEquals(1, possibleValues.getResults().size());
        assertTrue(possibleValues.getResults().get(0).equals(new ClauseValuesGenerator.Result("Opt 1")));
        mockController.verify();
    }

    @Test
    public void testGetPossibleValuesExactMatchWithOthers() throws Exception
    {
        final MockProject project = new MockProject(1L, "TST", "Test");
        final JiraContextNode contextNode = mockController.getMock(JiraContextNode.class);
        contextNode.getProjectObject();
        mockController.setReturnValue(project);

        searchHandlerManager.getFieldIds(null, "clauseName");
        mockController.setReturnValue(Collections.singletonList("fieldId"));

        permissionManager.getProjectObjects(Permissions.BROWSE, null);
        mockController.setReturnValue(Collections.singletonList(project));

        final Collection<String> opts = CollectionBuilder.newBuilder("Opt 2", "Opt 1", "Opt 1.0").asCollection();
        final Options options = mockController.getMock(Options.class);
        options.iterator();
        mockController.setReturnValue(opts.iterator());

        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getOptions(null, contextNode);
        mockController.setReturnValue(options);

        customFieldManager.getCustomFieldObject("fieldId");
        mockController.setReturnValue(customField);

        mockController.replay();

        valuesGenerator = new CustomFieldOptionsClauseValuesGenerator(customFieldManager, searchHandlerManager, fieldConfigSchemeManager, permissionManager)
        {
            @Override
            Locale getLocale(final User searcher)
            {
                return Locale.ENGLISH;
            }

            @Override
            List<JiraContextNode> getContextsForCustomField(final CustomField customField)
            {
                return Collections.singletonList(contextNode);
            }
        };

        final ClauseValuesGenerator.Results possibleValues = valuesGenerator.getPossibleValues(null, "clauseName", "opt 1", 10);

        assertEquals(2, possibleValues.getResults().size());
        assertTrue(possibleValues.getResults().get(0).equals(new ClauseValuesGenerator.Result("Opt 1")));
        assertTrue(possibleValues.getResults().get(1).equals(new ClauseValuesGenerator.Result("Opt 1.0")));
        mockController.verify();
    }

}
