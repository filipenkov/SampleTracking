package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.customfields.converters.UserConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.util.UserFitsNavigatorHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.atlassian.crowd.embedded.api.User;
import org.easymock.EasyMock;

import java.util.Collections;

/**
 * @since v4.0
 */
public class TestUserCustomFieldSearchInputTransformer extends MockControllerTestCase
{
    private final String id = "cf[100]";
    private UserConverter userConverter;
    private UserFitsNavigatorHelper userFitsNavigatorHelper;
    private CustomField customField;
    private SearchContext searchContext;
    private User searcher = null;
    private CustomFieldInputHelper customFieldInputHelper;
    private ClauseNames clauseNames;

    @Before
    public void setUp() throws Exception
    {
        setUpMocks();
        clauseNames = new ClauseNames(id);
    }

    private void setUpMocks()
    {
        customField = mockController.getMock(CustomField.class);
        customField.getId();
        mockController.setDefaultReturnValue(id);

        userConverter = mockController.getMock(UserConverter.class);
        userFitsNavigatorHelper = mockController.getMock(UserFitsNavigatorHelper.class);
        searchContext = mockController.getMock(SearchContext.class);
        customFieldInputHelper = getMock(CustomFieldInputHelper.class);
    }

    @Test
    public void testGetParamsFromSearchRequest() throws Exception
    {
        final String userValue = "userValue";
        final String userName = "userName";

        userFitsNavigatorHelper.checkUser(userValue);
        mockController.setReturnValue(userName);

        mockController.replay();

        final UserPickerCustomFieldSearchInputTransformer transformer = new UserPickerCustomFieldSearchInputTransformer(id, clauseNames, customField, userConverter, userFitsNavigatorHelper, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(true, new SingleValueOperand(userValue));
            }
        };

        final CustomFieldParamsImpl expectedResult = new CustomFieldParamsImpl(customField, Collections.singleton(userName));
        final CustomFieldParams result = transformer.getParamsFromSearchRequest(null, new QueryImpl(), searchContext);
        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestUserNameInvalid() throws Exception
    {
        final String userValue = "userValue";

        userFitsNavigatorHelper.checkUser(userValue);
        mockController.setReturnValue(null);

        mockController.replay();

        final UserPickerCustomFieldSearchInputTransformer transformer = new UserPickerCustomFieldSearchInputTransformer(id, clauseNames, customField, userConverter, userFitsNavigatorHelper, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(true, new SingleValueOperand(userValue));
            }
        };

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(null, new QueryImpl(), searchContext);
        assertNull(result);

        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestInvalidStructure() throws Exception
    {
        mockController.replay();

        final UserPickerCustomFieldSearchInputTransformer transformer = new UserPickerCustomFieldSearchInputTransformer(id, clauseNames, customField, userConverter, userFitsNavigatorHelper, customFieldInputHelper)
        {
            @Override
            NavigatorConversionResult convertForNavigator(final Query query)
            {
                return new NavigatorConversionResult(false, null);
            }
        };

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(null, new QueryImpl(), searchContext);
        assertNull(result);

        mockController.verify();
    }

    @Test
    public void testGetSearch() throws Exception
    {
        EasyMock.expect(customField.getName()).andStubReturn("ABC");
        EasyMock.expect(customFieldInputHelper.getUniqueClauseName((com.opensymphony.user.User) searcher, clauseNames.getPrimaryName(), "ABC")).andStubReturn(clauseNames.getPrimaryName());

        final String value = "value";
        _testGetSearchClause(new TerminalClauseImpl(id, Operator.EQUALS, value), new CustomFieldParamsImpl(customField, value));
        _testGetSearchClause(null, null);
        _testGetSearchClause(null, new CustomFieldParamsImpl(customField));
        _testGetSearchClause(null, new CustomFieldParamsImpl(customField, CollectionBuilder.newBuilder(value, value+"1").asList()));
    }

    @Test
    public void testdoRelevantClausesFitFilterForm() throws Exception
    {
        final String value = "value";
        _testDoRelevantClausesFitFilterForm(false, new QueryImpl(new TerminalClauseImpl(id, Operator.EQUALS, new MultiValueOperand(value, value))), value, value);
        _testDoRelevantClausesFitFilterForm(true, new QueryImpl(new TerminalClauseImpl(id, Operator.EQUALS, value)), value, value);
        _testDoRelevantClausesFitFilterForm(false, new QueryImpl(new TerminalClauseImpl(id, Operator.EQUALS, value)), value, null);
        _testDoRelevantClausesFitFilterForm(true, null, value, value);
        _testDoRelevantClausesFitFilterForm(true,  new QueryImpl(new TerminalClauseImpl("blarg", Operator.EQUALS, value)), value, value);
        _testDoRelevantClausesFitFilterForm(false, new QueryImpl(new OrClause(new TerminalClauseImpl(id, Operator.EQUALS, value), new TerminalClauseImpl(id, Operator.EQUALS, value))), value, value);
    }

    @SuppressWarnings ({ "ThrowableInstanceNeverThrown" })
    @Test
    public void testValidateParamsNoUser() throws Exception
    {
        final String value = "value";
        final SearchContext searchContext = mockController.getMock(SearchContext.class);

        userConverter.getUser(value);
        mockController.setThrowable(new FieldValidationException("blarg!"));

        final UserFitsNavigatorHelper userFitsNavigatorHelper = mockController.getMock(UserFitsNavigatorHelper.class);
        mockController.replay();

        final CustomFieldParamsImpl params = new CustomFieldParamsImpl(customField, CollectionBuilder.newBuilder(value).asList());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder().add(id, params).toHashMap());

        final UserPickerCustomFieldSearchInputTransformer transformer = new UserPickerCustomFieldSearchInputTransformer(id, clauseNames, customField, userConverter, userFitsNavigatorHelper, customFieldInputHelper);

        ErrorCollection errorCollection = new SimpleErrorCollection();

        transformer.validateParams(null, searchContext, holder, new MockI18nHelper(), errorCollection);

        assertTrue(errorCollection.getErrors().containsValue("admin.errors.could.not.find.username " + value));

        mockController.verify();
    }

    @Test
    public void testValidateParamsHappyPath() throws Exception
    {
        final String value = "value";

        final SearchContext searchContext = mockController.getMock(SearchContext.class);

        userConverter.getUser(value);
        mockController.setReturnValue(null);
        mockController.replay();

        final CustomFieldParamsImpl params = new CustomFieldParamsImpl(customField, CollectionBuilder.newBuilder(value).asList());
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder().add(id, params).toHashMap());

        final UserPickerCustomFieldSearchInputTransformer transformer = new UserPickerCustomFieldSearchInputTransformer(id, clauseNames, customField, userConverter, userFitsNavigatorHelper, customFieldInputHelper)
        {
        };

        ErrorCollection errorCollection = new SimpleErrorCollection();

        transformer.validateParams(null, searchContext, holder, new MockI18nHelper(), errorCollection);

        assertFalse(errorCollection.hasAnyErrors());

        mockController.verify();
    }

    @Test
    public void testValidateParamsNoParams() throws Exception
    {
        final SearchContext searchContext = mockController.getMock(SearchContext.class);
        mockController.replay();

        final CustomFieldParamsImpl params = null;
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder().add(id, params).toHashMap());

        final UserPickerCustomFieldSearchInputTransformer transformer = new UserPickerCustomFieldSearchInputTransformer(id, clauseNames, customField, userConverter, userFitsNavigatorHelper, customFieldInputHelper)
        {
        };

        ErrorCollection errorCollection = new SimpleErrorCollection();

        transformer.validateParams(null, searchContext, holder, new MockI18nHelper(), errorCollection);

        assertFalse(errorCollection.hasAnyErrors());

        mockController.verify();
    }

    private void _testGetSearchClause(Clause expected, CustomFieldParams params)
    {
        setUpMocks();
        FieldValuesHolder holder = new FieldValuesHolderImpl(MapBuilder.newBuilder().add(id, params).toHashMap());
        mockController.replay();

        UserPickerCustomFieldSearchInputTransformer transformer = new UserPickerCustomFieldSearchInputTransformer(id, clauseNames, customField, userConverter, userFitsNavigatorHelper, customFieldInputHelper);
        final Clause result = transformer.getSearchClause(null, holder);
        assertEquals(expected, result);

        mockController.verify();
        mockController.onTestEnd();
        mockController = new MockController();
    }

    private void _testDoRelevantClausesFitFilterForm(boolean expected, Query query, final String name, final String checkResult)
    {
        setUpMocks();
        userFitsNavigatorHelper.checkUser(name);
        mockController.setDefaultReturnValue(checkResult);

        mockController.replay();

        final UserPickerCustomFieldSearchInputTransformer transformer = new UserPickerCustomFieldSearchInputTransformer(id, clauseNames, customField, userConverter, userFitsNavigatorHelper, customFieldInputHelper);

        boolean result = transformer.doRelevantClausesFitFilterForm(null, query, searchContext);

        if (expected)
        {
            assertTrue(result);
        }
        else
        {
            assertFalse(result);
        }

        mockController.verify();
        mockController.onTestEnd();
        mockController = new MockController();
    }
}
