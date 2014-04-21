package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.easymock.classextension.EasyMock;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since v4.0
 */
public class TestFreeTextCustomFieldSearchInputTransformer extends MockControllerTestCase
{
    private ClauseNames names = new ClauseNames("name");
    String url = "url";
    private CustomField field;
    private String id;
    private SearchContext searchContext;
    private User searcher = null;
    private CustomFieldInputHelper customFieldInputHelper;

    @Before
    public void setUp() throws Exception
    {
        field = mockController.getMock(CustomField.class);
        field.getId();
        id = "id";
        mockController.setDefaultReturnValue(id);
        searchContext = mockController.getMock(SearchContext.class);
        customFieldInputHelper = getMock(CustomFieldInputHelper.class);
    }

    @Test
    public void testCreateSearchClause() throws Exception
    {
        EasyMock.expect(field.getName())
                .andReturn(id);

        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(searcher, names.getPrimaryName(), id))
                .andReturn(id);

        replay();

        FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, names, url, customFieldInputHelper);
        final Clause result = transformer.createSearchClause(searcher, "value");
        Clause expectedResult = new TerminalClauseImpl(id, Operator.LIKE, "value");

        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetParamsFromSearchRequestDoesntFit() throws Exception
    {
        mockController.replay();
        FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, names, url, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(false, null);
            }
        };



        assertNull(transformer.getParamsFromSearchRequest(null, new QueryImpl(), searchContext));

        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestNoValue() throws Exception
    {
        mockController.replay();
        FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, names, url, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(true, null);
            }
        };



        assertNull(transformer.getParamsFromSearchRequest(null, new QueryImpl(), searchContext));

        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestHappyPath() throws Exception
    {
        mockController.replay();
        FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, names, url, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(true, new SingleValueOperand("blah"));
            }
        };

        final CustomFieldParamsImpl expectedResult = new CustomFieldParamsImpl(field, Collections.singleton("blah"));

        assertEquals(expectedResult, transformer.getParamsFromSearchRequest(null, new QueryImpl(), searchContext));

        mockController.verify();
    }

    @Test
    public void testValidateParamsDoesNotContainKey() throws Exception
    {
        final SearchContext searchContext = mockController.getMock(SearchContext.class);

        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.<String, Object>newBuilder().toHashMap());
        I18nHelper i18n = new MockI18nHelper();
        ErrorCollection errors = new SimpleErrorCollection();

        mockController.replay();

        FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, names, url, customFieldInputHelper);
        transformer.validateParams(null, searchContext, holder, i18n, errors);

        assertFalse(errors.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testValidateParamsNoRelevantConfig() throws Exception
    {
        final SearchContext searchContext = mockController.getMock(SearchContext.class);

        field.getReleventConfig(searchContext);
        mockController.setReturnValue(null);

        CustomFieldParams params = new CustomFieldParamsImpl(field, MapBuilder.newBuilder().add("blah", Collections.singletonList("blah")).toHashMap());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.<String, Object>newBuilder().add(id, params).toHashMap());
        I18nHelper i18n = new MockI18nHelper();
        ErrorCollection errors = new SimpleErrorCollection();

        mockController.replay();

        FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, names, url, customFieldInputHelper);
        transformer.validateParams(null, searchContext, holder, i18n, errors);

        assertFalse(errors.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testValidateParamsNullParam() throws Exception
    {
        CustomFieldParams params = new CustomFieldParamsImpl(field, MapBuilder.newBuilder().add("blah", Collections.singletonList("blah")).toHashMap());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.<String, Object>newBuilder().add(id, params).toHashMap());
        I18nHelper i18n = new MockI18nHelper();
        ErrorCollection errors = new SimpleErrorCollection();

        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);

        field.getReleventConfig(searchContext);
        mockController.setReturnValue(fieldConfig);

        field.getCustomFieldType();
        mockController.setReturnValue(customFieldType);

        customFieldType.getValueFromCustomFieldParams(params);
        mockController.setReturnValue(null);

        mockController.replay();

        FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, names, url, customFieldInputHelper);
        transformer.validateParams(null, searchContext, holder, i18n, errors);

        assertFalse(errors.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testValidateParamsFieldValidationException() throws Exception
    {
        CustomFieldParams params = new CustomFieldParamsImpl(field, MapBuilder.newBuilder().add("blah", Collections.singletonList("blah")).toHashMap());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.<String, Object>newBuilder().add(id, params).toHashMap());
        I18nHelper i18n = new MockI18nHelper();
        ErrorCollection errors = new SimpleErrorCollection();

        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);

        field.getReleventConfig(searchContext);
        mockController.setReturnValue(fieldConfig);

        field.getCustomFieldType();
        mockController.setReturnValue(customFieldType);

        customFieldType.getValueFromCustomFieldParams(params);
        mockController.setThrowable(new FieldValidationException("blarg!"));

        mockController.replay();

        FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, names, url, customFieldInputHelper);
        transformer.validateParams(null, searchContext, holder, i18n, errors);

        assertTrue(errors.hasAnyErrors());
        assertEquals("blarg!", errors.getErrors().get(id));

        mockController.verify();
    }

    @Test
    public void testValidateParamsClassCastException() throws Exception
    {
        CustomFieldParams params = new CustomFieldParamsImpl(field, MapBuilder.newBuilder().add("blah", Collections.singletonList("blah")).toHashMap());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.<String, Object>newBuilder().add(id, params).toHashMap());
        I18nHelper i18n = new MockI18nHelper();
        ErrorCollection errors = new SimpleErrorCollection();

        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);

        field.getReleventConfig(searchContext);
        mockController.setReturnValue(fieldConfig);

        field.getCustomFieldType();
        mockController.setReturnValue(customFieldType);

        customFieldType.getValueFromCustomFieldParams(params);
        mockController.setThrowable(new ClassCastException());

        mockController.replay();

        FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, names, url, customFieldInputHelper);
        transformer.validateParams(null, searchContext, holder, i18n, errors);

        assertTrue(errors.hasAnyErrors());
        assertEquals("Internal error attempting to validate the search term.", errors.getErrors().get(id));

        mockController.verify();
    }

    @Test
    public void testValidateParamsInValidQuery() throws Exception
    {
        CustomFieldParams params = new CustomFieldParamsImpl(field, MapBuilder.newBuilder().add("blah", Collections.singletonList("blah")).toHashMap());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.<String, Object>newBuilder().add(id, params).toHashMap());
        I18nHelper i18n = new MockI18nHelper();
        ErrorCollection errors = new SimpleErrorCollection();

        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        final QueryParser queryParser = mockController.getMock(QueryParser.class);


        field.getReleventConfig(searchContext);
        mockController.setReturnValue(fieldConfig);

        field.getCustomFieldType();
        mockController.setReturnValue(customFieldType);

        customFieldType.getValueFromCustomFieldParams(params);
        mockController.setReturnValue("value");

        queryParser.parse("value");
        mockController.setThrowable(new ParseException());

        mockController.replay();

        FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, names, url, customFieldInputHelper)
        {
            @Override
            QueryParser createQueryParser(final CustomField customField)
            {
                return queryParser;
            }
        };
        
        transformer.validateParams(null, searchContext, holder, i18n, errors);

        assertTrue(errors.hasAnyErrors());
        assertEquals("navigator.error.parse", errors.getErrors().get(id));

        mockController.verify();
    }

    @Test
    public void testValidateParamsInvalidFirstChar() throws Exception
    {
         CustomFieldParams params = new CustomFieldParamsImpl(field, MapBuilder.newBuilder().add("blah", Collections.singletonList("blah")).toHashMap());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.<String, Object>newBuilder().add(id, params).toHashMap());
        I18nHelper i18n = new MockI18nHelper();
        ErrorCollection errors = new SimpleErrorCollection();

        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        final QueryParser queryParser = mockController.getMock(QueryParser.class);


        field.getReleventConfig(searchContext);
        mockController.setReturnValue(fieldConfig);

        field.getCustomFieldType();
        mockController.setReturnValue(customFieldType);

        customFieldType.getValueFromCustomFieldParams(params);
        mockController.setReturnValue("!value");

        mockController.replay();

        FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, names, url, customFieldInputHelper);
        transformer.validateParams(null, searchContext, holder, i18n, errors);

        assertTrue(errors.hasAnyErrors());
        assertEquals("navigator.error.query.invalid.start !", errors.getErrors().get(id));

        mockController.verify();
    }

    @Test
    public void testValidateParamsHappyPath() throws Exception
    {
        CustomFieldParams params = new CustomFieldParamsImpl(field, MapBuilder.newBuilder().add("blah", Collections.singletonList("blah")).toHashMap());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.<String, Object>newBuilder().add(id, params).toHashMap());
        I18nHelper i18n = new MockI18nHelper();
        ErrorCollection errors = new SimpleErrorCollection();

        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        final FieldConfig fieldConfig = mockController.getMock(FieldConfig.class);
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        final QueryParser queryParser = mockController.getMock(QueryParser.class);


        field.getReleventConfig(searchContext);
        mockController.setReturnValue(fieldConfig);

        field.getCustomFieldType();
        mockController.setReturnValue(customFieldType);

        customFieldType.getValueFromCustomFieldParams(params);
        mockController.setReturnValue("value");

        queryParser.parse("value");
        mockController.setReturnValue(null);

        mockController.replay();

        FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, names, url, customFieldInputHelper)
        {
            @Override
            QueryParser createQueryParser(final CustomField customField)
            {
                return queryParser;
            }
        };

        transformer.validateParams(null, searchContext, holder, i18n, errors);

        assertFalse(errors.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testFitsNavigator() throws Exception
    {
        mockController.replay();

        final AtomicInteger called = new AtomicInteger(0);

        FreeTextCustomFieldSearchInputTransformer transformer = new FreeTextCustomFieldSearchInputTransformer(field, names, url, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(called.incrementAndGet() == 1, null);
            }
        };

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), searchContext));
        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), searchContext));
        assertEquals(2, called.get());

        mockController.verify();
    }
}
