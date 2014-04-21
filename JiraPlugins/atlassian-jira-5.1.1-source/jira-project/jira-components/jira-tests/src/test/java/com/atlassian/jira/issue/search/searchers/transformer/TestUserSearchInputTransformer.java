package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.component.MockComponentWorker;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.searchers.util.UserFitsNavigatorHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.ActionParamsImpl;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.plugin.jql.function.MembersOfFunction;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operator.Operator;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since v4.0
 */
public class TestUserSearchInputTransformer extends MockControllerTestCase
{
    JiraAuthenticationContext authenticationContext = null;
    UserFitsNavigatorHelper userFitsNavigatorHelper = null;
    private User theUser = null;

    UserFieldSearchConstantsWithEmpty searchConstants = null;
    private SearchContext searchContext;

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());

        searchConstants = new UserFieldSearchConstantsWithEmpty("indexField", new ClauseNames("reporter"), "fieldUrlParameter", "selectUrlParameter", "searcherId", "emptySelectFlag", "fieldId", "currentUserSelectFlag", "specificUserSelectFlag", "specificGroupSelectFlag", "emptyIndexValue", OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);
        authenticationContext = mockController.getMock(JiraAuthenticationContext.class);
        authenticationContext.getLoggedInUser();
        mockController.setDefaultReturnValue(theUser);

        userFitsNavigatorHelper = mockController.getMock(UserFitsNavigatorHelper.class);
        searchContext = mockController.getMock(SearchContext.class);
    }

    @After
    public void tearDown() throws Exception
    {
        authenticationContext = null;
    }

    @Test
    public void testPopulateFromParamsCurrentUserNotLoggedIn() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null)
        {
            @Override
            boolean isUserLoggedIn(final User user)
            {
                return false;
            }
        };

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        final String[] values = { searchConstants.getCurrentUserSelectFlag() };
        final ActionParamsImpl actionParams = new ActionParamsImpl(EasyMap.build(searchConstants.getSelectUrlParameter(), values));
        transformer.populateFromParams(null, valuesHolder, actionParams);
        assertEquals(searchConstants.getCurrentUserSelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));

        mockController.verify();
    }

    @Test
    public void testPopulateFromParamsCurrentUserLoggedIn() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null)
        {
            @Override
            boolean isUserLoggedIn(final User user)
            {
                return true;
            }
        };

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        final String[] values = { searchConstants.getCurrentUserSelectFlag() };
        final ActionParamsImpl actionParams = new ActionParamsImpl(EasyMap.build(searchConstants.getSelectUrlParameter(), values));
        transformer.populateFromParams(null, valuesHolder, actionParams);
        assertEquals(searchConstants.getCurrentUserSelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));

        mockController.verify();
    }

    @Test
    public void testPopulateFromParamsCurrentNoUserSelectWithUserField() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null)
        {
            @Override
            boolean isUserLoggedIn(final User user)
            {
                return true;
            }
        };

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        final String[] values = { "monkey" };
        final ActionParamsImpl actionParams = new ActionParamsImpl(EasyMap.build(searchConstants.getFieldUrlParameter(), values));
        transformer.populateFromParams(null, valuesHolder, actionParams);
        assertEquals(searchConstants.getSpecificUserSelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));
        assertEquals("monkey", valuesHolder.get(searchConstants.getFieldUrlParameter()));

        mockController.verify();
    }

    @Test
    public void testPopulateFromParamsCurrentHappyPath() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null)
        {
            @Override
            boolean isUserLoggedIn(final User user)
            {
                return true;
            }
        };

        final FieldValuesHolderImpl valuesHolder = new FieldValuesHolderImpl();
        final String[] values = { "monkey" };
        final String[] select= { searchConstants.getSpecificGroupSelectFlag() };
        final ActionParamsImpl actionParams = new ActionParamsImpl(EasyMap.build(searchConstants.getSelectUrlParameter(), select, searchConstants.getFieldUrlParameter(), values));
        transformer.populateFromParams(null, valuesHolder, actionParams);
        assertEquals(searchConstants.getSpecificGroupSelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));
        assertEquals("monkey", valuesHolder.get(searchConstants.getFieldUrlParameter()));

        mockController.verify();
    }

    @Test
    public void testValidateParamBlankUser() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(EasyMap.build(searchConstants.getFieldUrlParameter(), ""));
        final I18nHelper i18n = new MockI18nHelper();
        ErrorCollection errors = new SimpleErrorCollection();
        transformer.validateParams(null, null, valuesHolder, i18n, errors);
        assertFalse(errors.hasAnyErrors());        
        mockController.verify();
    }   

    @Test
    public void testValidatSpecificUserExists() throws Exception
    {
        mockController.replay();
        final AtomicBoolean called = new AtomicBoolean(false);
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null)
        {
            @Override
            boolean userExists(final String user)
            {
                called.set(true);
                assertEquals("monkey", user);
                return true;
            }
        };

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(EasyMap.build(searchConstants.getSelectUrlParameter(), searchConstants.getSpecificUserSelectFlag(), searchConstants.getFieldUrlParameter(), "monkey"));
        final I18nHelper i18n = new MockI18nHelper();
        ErrorCollection errors = new SimpleErrorCollection();
        transformer.validateParams(null, null, valuesHolder, i18n, errors);
        assertFalse(errors.hasAnyErrors());
        assertTrue(called.get());
        mockController.verify();
    }

    @Test
    public void testValidatSpecificUserDoesntExist() throws Exception
    {
        mockController.replay();
        final AtomicBoolean called = new AtomicBoolean(false);
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null)
        {
            @Override
            boolean userExists(final String user)
            {
                called.set(true);
                assertEquals("monkey", user);
                return false;
            }
        };

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(EasyMap.build(searchConstants.getSelectUrlParameter(), searchConstants.getSpecificUserSelectFlag(), searchConstants.getFieldUrlParameter(), "monkey"));
        final I18nHelper i18n = new MockI18nHelper();
        ErrorCollection errors = new SimpleErrorCollection();
        transformer.validateParams(null, null, valuesHolder, i18n, errors);
        assertTrue(errors.hasAnyErrors());        
        assertEquals("admin.errors.could.not.find.username monkey", errors.getErrors().get(searchConstants.getFieldUrlParameter()));
        assertTrue(called.get());
        mockController.verify();
    }

    @Test
    public void testValidatSpecificGroupExists() throws Exception
    {
        mockController.replay();
        final AtomicBoolean called = new AtomicBoolean(false);
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null)
        {
            @Override
            boolean groupExists(final String user)
            {
                called.set(true);
                assertEquals("monkey", user);
                return true;
            }
        };

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(EasyMap.build(searchConstants.getSelectUrlParameter(), searchConstants.getSpecificGroupSelectFlag(), searchConstants.getFieldUrlParameter(), "monkey"));
        final I18nHelper i18n = new MockI18nHelper();
        ErrorCollection errors = new SimpleErrorCollection();
        transformer.validateParams(null, null, valuesHolder, i18n, errors);
        assertFalse(errors.hasAnyErrors());
        assertTrue(called.get());
        mockController.verify();
    }

    @Test
    public void testValidatSpecificGroupDoesntExist() throws Exception
    {
        mockController.replay();
        final AtomicBoolean called = new AtomicBoolean(false);
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null)
        {
            @Override
            boolean groupExists(final String user)
            {
                called.set(true);
                assertEquals("monkey", user);
                return false;
            }
        };

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(EasyMap.build(searchConstants.getSelectUrlParameter(), searchConstants.getSpecificGroupSelectFlag(), searchConstants.getFieldUrlParameter(), "monkey"));
        final I18nHelper i18n = new MockI18nHelper();
        ErrorCollection errors = new SimpleErrorCollection();
        transformer.validateParams(null, null, valuesHolder, i18n, errors);
        assertTrue(errors.hasAnyErrors());
        assertEquals("admin.errors.abstractusersearcher.could.not.find.group monkey", errors.getErrors().get(searchConstants.getFieldUrlParameter()));
        assertTrue(called.get());
        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestNoWhereClause() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();

        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(), searchContext);

        assertTrue(valuesHolder.isEmpty());
        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestSingleValueOperand() throws Exception
    {
        userFitsNavigatorHelper.checkUser("monkey");
        mockController.setReturnValue("monkey");
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        Clause clause = builder.where().reporterUser("monkey").buildClause();

        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);

        assertEquals(searchConstants.getSpecificUserSelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));
        assertEquals("monkey", valuesHolder.get(searchConstants.getFieldUrlParameter()));
        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestSingleValueOperandUserNotOk() throws Exception
    {
        userFitsNavigatorHelper.checkUser("monkey");
        mockController.setReturnValue(null);
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        Clause clause = builder.where().reporterUser("monkey").buildClause();

        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);

        assertTrue(valuesHolder.isEmpty());
        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestEmpty() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        Clause clause = builder.where().reporterIsEmpty().buildClause();

        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);

        assertEquals(searchConstants.getEmptySelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));
        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestSingleValueOperandLong() throws Exception
    {
        userFitsNavigatorHelper.checkUser("10");
        mockController.setReturnValue("10");
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, 10L);

        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);

        assertEquals(searchConstants.getSpecificUserSelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));
        assertEquals("10", valuesHolder.get(searchConstants.getFieldUrlParameter()));
        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestGroupFunction() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        Clause clause = builder.where().reporterInGroup("monkey").buildClause();

        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);

        assertEquals(searchConstants.getSpecificGroupSelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));
        assertEquals("monkey", valuesHolder.get(searchConstants.getFieldUrlParameter()));
        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestGroupFunctionMultiArg() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.IN, new FunctionOperand(MembersOfFunction.FUNCTION_MEMBERSOF, "monkey1", "monkey2" ));

        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);

        assertFalse(valuesHolder.containsKey(searchConstants.getSelectUrlParameter()));
        assertFalse(valuesHolder.containsKey(searchConstants.getFieldUrlParameter()));
        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestCurrentUserFunction() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        Clause clause = builder.where().reporterIsCurrentUser().buildClause();

        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);

        assertEquals(searchConstants.getCurrentUserSelectFlag(), valuesHolder.get(searchConstants.getSelectUrlParameter()));
        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestNotRelevant() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        Clause clause = new TerminalClauseImpl("blarg", Operator.EQUALS, "blarg");

        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);

        assertFalse(valuesHolder.containsKey(searchConstants.getSelectUrlParameter()));
        assertFalse(valuesHolder.containsKey(searchConstants.getFieldUrlParameter()));
        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestMultiValueOperand() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, new MultiValueOperand("blarg", "blarg"));

        transformer.populateFromQuery(null, valuesHolder, new QueryImpl(clause), searchContext);

        assertFalse(valuesHolder.containsKey(searchConstants.getSelectUrlParameter()));
        assertFalse(valuesHolder.containsKey(searchConstants.getFieldUrlParameter()));
        mockController.verify();
    }
    
    @Test
    public void testPopulateFromSearchRequestNoQuery() throws Exception
    {
        mockController.replay();
        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        transformer.populateFromQuery(null, valuesHolder, null, searchContext);

        assertFalse(valuesHolder.containsKey(searchConstants.getSelectUrlParameter()));
        assertFalse(valuesHolder.containsKey(searchConstants.getFieldUrlParameter()));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNoWhereClause() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormEmpty() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        Clause clause = builder.where().reporterIsEmpty().buildClause();

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNotEmpty() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.IS_NOT, EmptyOperand.EMPTY);

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormSingleValueOperandUserOk() throws Exception
    {
        userFitsNavigatorHelper.checkUser("monkey");
        mockController.setReturnValue("monkey");
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);
        Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, "monkey");

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormSingleValueOperandUserNotOk() throws Exception
    {
        userFitsNavigatorHelper.checkUser("monkey");
        mockController.setReturnValue(null);
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, "monkey");

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormSingleValueOperandLong() throws Exception
    {
        userFitsNavigatorHelper.checkUser("10");
        mockController.setReturnValue("10");
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, 10L);

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNotSingleValueOperandLong() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.NOT_EQUALS, 10L);

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormGroupFunction() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        Clause clause = builder.where().reporterInGroup("monkey").buildClause();

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNotGroupFunction() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.NOT_IN, new FunctionOperand("membersOf", "jira-users"));

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormGroupFunctionMultiArg() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.IN, new FunctionOperand(MembersOfFunction.FUNCTION_MEMBERSOF, "monkey1", "monkey2" ));

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormCurrentUserFunctionUserLoggedIn() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null)
        {
            @Override
            boolean isUserLoggedIn(final User user)
            {
                return true;
            }
        };

        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        Clause clause = builder.where().reporterIsCurrentUser().buildClause();

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormCurrentUserFunctionUserNotLoggedIn() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null)
        {
            @Override
            boolean isUserLoggedIn(final User user)
            {
                return false;
            }
        };

        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        Clause clause = builder.where().reporterIsCurrentUser().buildClause();

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNotCurrentUserFunction() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.NOT_EQUALS, new FunctionOperand("currentUser"));

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNotRelevant() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        Clause clause = new TerminalClauseImpl("blarg", Operator.EQUALS, "blarg");

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormToManyClauses() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, "blarg");
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
            {
                return new MockSimpleNavigatorCollectorVisitor(true, CollectionBuilder.newBuilder(clause, clause).asList());
            }
        };

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNotValid() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, "blarg");
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
            {
                return new MockSimpleNavigatorCollectorVisitor(false, CollectionBuilder.newBuilder(clause).asList());
            }
        };

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormNoQuery() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, null, searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormMultiValueOperand() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        Clause clause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, new MultiValueOperand("blarg", "blarg"));

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testGetSearchClauseEmpty() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(EasyMap.build(searchConstants.getSelectUrlParameter(), searchConstants.getEmptySelectFlag()));
        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();

        assertEquals(builder.where().reporterIsEmpty().buildClause(), transformer.getSearchClause(null, valuesHolder));
    }

    @Test
    public void testGetSearchClauseCurrentUser() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        final FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(EasyMap.build(searchConstants.getSelectUrlParameter(), searchConstants.getCurrentUserSelectFlag()));
        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();

        assertEquals(builder.where().reporterIsCurrentUser().buildClause(), transformer.getSearchClause(null, valuesHolder));
    }

    @Test
    public void testGetSearchClauseSpecificUser() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);
        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();

        FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(EasyMap.build(searchConstants.getSelectUrlParameter(), searchConstants.getSpecificUserSelectFlag(), searchConstants.getFieldUrlParameter(), "monkey"));
        assertEquals(builder.where().reporterUser("monkey").buildClause(), transformer.getSearchClause(null, valuesHolder));

        JqlQueryBuilder builder2 = JqlQueryBuilder.newBuilder();
        valuesHolder = new FieldValuesHolderImpl(EasyMap.build(searchConstants.getFieldUrlParameter(), "monkey"));
        assertEquals(builder2.where().reporterUser("monkey").buildClause(), transformer.getSearchClause(null, valuesHolder));
    }

    @Test
    public void testGetSearchClauseSpecificGroup() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);
        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();

        FieldValuesHolder valuesHolder = new FieldValuesHolderImpl(EasyMap.build(searchConstants.getSelectUrlParameter(), searchConstants.getSpecificGroupSelectFlag(), searchConstants.getFieldUrlParameter(), "monkey"));
        assertEquals(builder.where().reporterInGroup("monkey").buildClause(), transformer.getSearchClause(null, valuesHolder));
    }

    @Test
    public void testGetSearchClauseNotRelevant() throws Exception
    {
        mockController.replay();
        UserSearchInputTransformer transformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, null, null);

        FieldValuesHolder valuesHolder = new FieldValuesHolderImpl();        
        assertNull(transformer.getSearchClause(null, valuesHolder));
    }


    static class MockSimpleNavigatorCollectorVisitor extends SimpleNavigatorCollectorVisitor
    {
        private final boolean valid;
        private final List<TerminalClause> clauses;

        @Override
        public List<TerminalClause> getClauses()
        {
            return clauses;
        }

        @Override
        public boolean isValid()
        {
            return valid;
        }

        public MockSimpleNavigatorCollectorVisitor(final boolean isValid, List<TerminalClause> clauses)
        {
            super("clauseName");
            valid = isValid;
            this.clauses = clauses;
        }


        @Override
        public Void visit(final AndClause andClause)
        {
            return null;
        }

        @Override
        public Void visit(final NotClause notClause)
        {
            return null;
        }

        @Override
        public Void visit(final OrClause orClause)
        {
            return null;
        }

        @Override
        public Void visit(final TerminalClause terminalClause)
        {
            return null;
        }
    }
}
