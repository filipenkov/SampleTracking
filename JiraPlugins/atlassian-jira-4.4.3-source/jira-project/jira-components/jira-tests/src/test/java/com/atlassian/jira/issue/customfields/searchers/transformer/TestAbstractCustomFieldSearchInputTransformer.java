package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.SearchContextVisibilityChecker;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.ActionParamsImpl;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.jql.resolver.VersionIndexInfoResolver;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;

import java.util.Collections;

/**
 * @since v4.0
 */
public class TestAbstractCustomFieldSearchInputTransformer extends MockControllerTestCase
{
    private SearchContextVisibilityChecker searchContextVisibilityChecker;
    private NameResolver<Version> versionResolver;
    private SearchContext searchContext;
    private CustomFieldInputHelper customFieldInputHelper;
    private VersionManager versionManager;

    @Before
    public void setUp() throws Exception
    {
        searchContextVisibilityChecker = mockController.getMock(SearchContextVisibilityChecker.class);
        versionResolver = mockController.getMock(NameResolver.class);
        searchContext = mockController.getMock(SearchContext.class);
        customFieldInputHelper = getMock(CustomFieldInputHelper.class);
        versionManager = getMock(VersionManager.class);
    }

    @Test
    public void testPopulateFromParamsDelegates() throws Exception
    {
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        final ActionParamsImpl actionParams = new ActionParamsImpl();

        CustomField field = mockController.getMock(CustomField.class);
        field.populateFromParams(valuesHolder, actionParams.getKeysAndValues());

        mockController.replay();
        AbstractCustomFieldSearchInputTransformer transformer = new MyCustomFieldSearchInputTransformer(field, "cf[1000]", customFieldInputHelper);

        transformer.populateFromParams(null, valuesHolder, actionParams);

        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestParamsAreNull() throws Exception
    {
        @SuppressWarnings ({ "unchecked" }) final VersionCustomFieldSearchInputTransformer transformer = new VersionCustomFieldSearchInputTransformer("clauseName", new ClauseNames("clauseName"), mockController.getMock(CustomField.class), mockController.getMock(VersionIndexInfoResolver.class), mockController.getMock(JqlOperandResolver.class), mockController.getMock(FieldFlagOperandRegistry.class), searchContextVisibilityChecker, versionResolver, customFieldInputHelper, versionManager)
        {
            @Override
            protected CustomFieldParams getParamsFromSearchRequest(final com.opensymphony.user.User searcher, final Query query, final SearchContext searchContext)
            {
                return null;
            }
        };

        mockController.replay();

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        transformer.populateFromQuery(null, valuesHolder, null, searchContext);
        assertTrue(valuesHolder.isEmpty());
        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestParamsAreEmpty() throws Exception
    {
        @SuppressWarnings ({ "unchecked" }) final VersionCustomFieldSearchInputTransformer transformer = new VersionCustomFieldSearchInputTransformer("clauseName", new ClauseNames("clauseName"), mockController.getMock(CustomField.class), mockController.getMock(VersionIndexInfoResolver.class), mockController.getMock(JqlOperandResolver.class), mockController.getMock(FieldFlagOperandRegistry.class), searchContextVisibilityChecker, versionResolver, customFieldInputHelper, versionManager)
        {
            @Override
            protected CustomFieldParams getParamsFromSearchRequest(final com.opensymphony.user.User searcher, final Query query, final SearchContext searchContext)
            {
                return new CustomFieldParamsImpl();
            }
        };

        mockController.replay();

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        transformer.populateFromQuery(null, valuesHolder, null, searchContext);
        assertTrue(valuesHolder.isEmpty());
        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestParamsAreNotEmpty() throws Exception
    {
        final String searcherId = "searcherId";

        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.addValue(Collections.singleton("Hi"));

        final VersionCustomFieldSearchInputTransformer transformer = new VersionCustomFieldSearchInputTransformer(searcherId, new ClauseNames("clauseName"), mockController.getMock(CustomField.class), mockController.getMock(VersionIndexInfoResolver.class), mockController.getMock(JqlOperandResolver.class), mockController.getMock(FieldFlagOperandRegistry.class), searchContextVisibilityChecker, versionResolver, customFieldInputHelper, versionManager)
        {
            @Override
            protected CustomFieldParams getParamsFromSearchRequest(final com.opensymphony.user.User searcher, final Query query, final SearchContext searchContext)
            {
                return customFieldParams;
            }
        };

        mockController.replay();

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        transformer.populateFromQuery(null, valuesHolder, null, searchContext);
        assertEquals(customFieldParams, valuesHolder.get(searcherId));
        mockController.verify();
    }

    @Test
    public void testValidateParamsValuesDoesntContain() throws Exception
    {
        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        CustomField field = mockController.getMock(CustomField.class);
        mockController.replay();
        AbstractCustomFieldSearchInputTransformer transformer = new MyCustomFieldSearchInputTransformer(field, "cf[1000]", customFieldInputHelper);

        transformer.validateParams(null, null, valuesHolder, null, null);

        mockController.verify();
    }

    @Test
    public void testValidateParamsValuesContainsSearcherNoRelevantConfig() throws Exception
    {
        final String searcherId = "searcherId";
        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.addValue(searcherId, Collections.singletonList("BLAH"));
        valuesHolder.put(searcherId, customFieldParams);
        CustomField field = mockController.getMock(CustomField.class);
        mockController.replay();
        AbstractCustomFieldSearchInputTransformer transformer = new MyCustomFieldSearchInputTransformer(field, "cf[1000]", customFieldInputHelper);

        transformer.validateParams(null, searchContext, valuesHolder, null, null);

        mockController.verify();
    }

    @Test
    public void testValidateParamsValuesContainsSearcherRelevantConfig() throws Exception
    {
        final String searcherId = "cf[1000]";
        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.addValue(searcherId, Collections.singletonList("BLAH"));
        valuesHolder.put(searcherId, customFieldParams);

        ErrorCollection errors = new SimpleErrorCollection();
        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        customFieldType.validateFromParams(customFieldParams, errors, fieldConfig);
        CustomField field = mockController.getMock(CustomField.class);
        field.getReleventConfig(searchContext);
        mockController.setReturnValue(fieldConfig);
        field.getCustomFieldType();
        mockController.setReturnValue(customFieldType);
        mockController.replay();
        AbstractCustomFieldSearchInputTransformer transformer = new MyCustomFieldSearchInputTransformer(field, "cf[1000]", customFieldInputHelper);

        transformer.validateParams(null, searchContext, valuesHolder, null, errors);

        mockController.verify();
    }

    @Test
    public void testValidateParamsValuesContainsSearcherNullConfig() throws Exception
    {
        final String searcherId = "cf[1000]";
        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl();
        customFieldParams.addValue(searcherId, Collections.singletonList("BLAH"));
        valuesHolder.put(searcherId, customFieldParams);

        ErrorCollection errors = new SimpleErrorCollection();
        CustomField field = mockController.getMock(CustomField.class);
        field.getReleventConfig(searchContext);
        mockController.setReturnValue(null);
        mockController.replay();
        AbstractCustomFieldSearchInputTransformer transformer = new MyCustomFieldSearchInputTransformer(field, "cf[1000]", customFieldInputHelper);

        transformer.validateParams(null, searchContext, valuesHolder, null, errors);

        mockController.verify();
    }

    static class MyCustomFieldSearchInputTransformer extends AbstractCustomFieldSearchInputTransformer
    {
        public MyCustomFieldSearchInputTransformer(CustomField field, String urlParameterName, final CustomFieldInputHelper customFieldInputHelper)
        {
            super(field, urlParameterName, customFieldInputHelper);
        }

        public boolean doRelevantClausesFitFilterForm(final com.opensymphony.user.User searcher, final Query query, final SearchContext searchContext)
        {
            return false;
        }

        protected Clause getClauseFromParams(final com.opensymphony.user.User searcher, final CustomFieldParams customFieldParams)
        {
            return null;
        }

        protected CustomFieldParams getParamsFromSearchRequest(final com.opensymphony.user.User searcher, final Query query, final SearchContext searchContext)
        {
            return null;
        }

    }
}
