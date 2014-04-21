package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.WorkRatioSearcherConfig;
import com.atlassian.jira.issue.search.searchers.util.WorkRatioSearcherInputHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.atlassian.crowd.embedded.api.User;

import java.util.Map;

/**
 * @since v4.0
 */
public class TestWorkRatioSearchInputTransformer extends MockControllerTestCase
{
    private static final SimpleFieldSearchConstants CONSTANTS = SystemSearchConstants.forWorkRatio();
    private static final WorkRatioSearcherConfig SEARCHER_CONFIG = new WorkRatioSearcherConfig(CONSTANTS.getSearcherId());
    private SearchContext searchContext;

    @Before
    public void setUp() throws Exception
    {
        searchContext = mockController.getMock(SearchContext.class);
    }


    @Test
    public void testConstructor() throws Exception
    {
        final SimpleFieldSearchConstants constants = CONSTANTS;
        final WorkRatioSearcherConfig config = SEARCHER_CONFIG;

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();

        try
        {
            new WorkRatioSearchInputTransformer(null, null, null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}

        try
        {
            new WorkRatioSearchInputTransformer(constants, null, null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}

        try
        {
            new WorkRatioSearchInputTransformer(constants, config, null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}

        new WorkRatioSearchInputTransformer(constants, config, operandResolver);
        
        mockController.verify();
    }

    @Test
    public void testValidateParamsEmptyValues() throws Exception
    {
        final WorkRatioSearchInputTransformer transformer = createTransformer();

        final FieldValuesHolder values = new FieldValuesHolderImpl();
        final I18nHelper i18n = new MockI18nHelper();
        final ErrorCollection errors = new SimpleErrorCollection();

        transformer.validateParams(null, null, values, i18n, errors);
        assertFalse(errors.hasAnyErrors());
    }

    @Test
    public void testValidateParamsHappyPath() throws Exception
    {
        final WorkRatioSearchInputTransformer transformer = createTransformer();

        final FieldValuesHolder values = new FieldValuesHolderImpl();
        final I18nHelper i18n = new MockI18nHelper();
        final ErrorCollection errors = new SimpleErrorCollection();

        // just minimum
        values.put(SEARCHER_CONFIG.getMinField(), "45");
        transformer.validateParams(null, null, values, i18n, errors);
        assertFalse(errors.hasAnyErrors());

        // minimum and maximum
        values.put(SEARCHER_CONFIG.getMaxField(), "45");
        transformer.validateParams(null, null, values, i18n, errors);
        assertFalse(errors.hasAnyErrors());

        // just maximum
        values.remove(SEARCHER_CONFIG.getMinField());
        transformer.validateParams(null, null, values, i18n, errors);
        assertFalse(errors.hasAnyErrors());
    }

    @Test
    public void testValidateParamsSadPath() throws Exception
    {
        final WorkRatioSearchInputTransformer transformer = createTransformer();

        final I18nHelper i18n = new MockI18nHelper();

        // just minimum
        FieldValuesHolder values = new FieldValuesHolderImpl();
        ErrorCollection errors = new SimpleErrorCollection();
        values.put(SEARCHER_CONFIG.getMinField(), "xx");
        transformer.validateParams(null, null, values, i18n, errors);
        assertEquals(1, errors.getErrors().size());
        assertEquals("navigator.filter.workratio.min.error", errors.getErrors().get(SEARCHER_CONFIG.getMinField()));

        // just maximum
        values = new FieldValuesHolderImpl();
        errors = new SimpleErrorCollection();
        values.put(SEARCHER_CONFIG.getMaxField(), "zzz");
        transformer.validateParams(null, null, values, i18n, errors);
        assertEquals(1, errors.getErrors().size());
        assertEquals("navigator.filter.workratio.max.error", errors.getErrors().get(SEARCHER_CONFIG.getMaxField()));

        // maximum < minimum
        values = new FieldValuesHolderImpl();
        errors = new SimpleErrorCollection();
        values.put(SEARCHER_CONFIG.getMinField(), "999");
        values.put(SEARCHER_CONFIG.getMaxField(), "1");
        transformer.validateParams(null, null, values, i18n, errors);
        assertEquals(1, errors.getErrors().size());
        assertEquals("navigator.filter.workratio.limits.error", errors.getErrors().get(SEARCHER_CONFIG.getMinField()));
    }

    @Test
    public void testPopulateFromSearchRequestNoWhereClause() throws Exception
    {
        FieldValuesHolder values = new FieldValuesHolderImpl();
        final WorkRatioSearchInputTransformer transformer = createTransformer();

        transformer.populateFromQuery(null, values, new QueryImpl(), searchContext);

        assertTrue(values.isEmpty());

        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestHelperReturnsNull() throws Exception
    {
        final Clause theWhereClause = new TerminalClauseImpl("something", Operator.EQUALS, "something");
        final User theUser = null;

        FieldValuesHolder values = new FieldValuesHolderImpl();
        Query query = mockController.getMock(Query.class);
        expect(query.getWhereClause()).andReturn(theWhereClause).anyTimes();

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);

        final WorkRatioSearcherInputHelper helper = mockController.getMock(WorkRatioSearcherInputHelper.class);
        expect(helper.convertClause(theWhereClause, theUser)).andReturn(null);

        mockController.replay();

        final WorkRatioSearchInputTransformer transformer = new WorkRatioSearchInputTransformer(CONSTANTS, SEARCHER_CONFIG, operandResolver)
        {
            @Override
            WorkRatioSearcherInputHelper createWorkRatioSearcherInputHelper()
            {
                return helper;
            }
        };
        transformer.populateFromQuery(null, values, query, searchContext);

        assertTrue(values.isEmpty());

        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestHappyPath() throws Exception
    {
        final Clause theWhereClause = new TerminalClauseImpl("something", Operator.EQUALS, "something");
        final User theUser = null;

        FieldValuesHolder values = new FieldValuesHolderImpl();
        Query query = mockController.getMock(Query.class);
        expect(query.getWhereClause()).andReturn(theWhereClause).anyTimes();

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);

        final Map<String, String> result = MapBuilder.<String, String>newBuilder().add("field", "value").toMap();
        final WorkRatioSearcherInputHelper helper = mockController.getMock(WorkRatioSearcherInputHelper.class);
        expect(helper.convertClause(theWhereClause, theUser)).andReturn(result);

        mockController.replay();

        final WorkRatioSearchInputTransformer transformer = new WorkRatioSearchInputTransformer(CONSTANTS, SEARCHER_CONFIG, operandResolver)
        {
            @Override
            WorkRatioSearcherInputHelper createWorkRatioSearcherInputHelper()
            {
                return helper;
            }
        };
        transformer.populateFromQuery(null, values, query, searchContext);

        assertFalse(values.isEmpty());
        assertEquals("value", values.get("field"));

        mockController.verify();
    }

    @Test
    public void testFitsNoWhereClause() throws Exception
    {
        final WorkRatioSearchInputTransformer transformer = createTransformer();
        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), searchContext));

        mockController.verify();
    }

    @Test
    public void testFitsHelperReturnsNull() throws Exception
    {
        final Clause theWhereClause = new TerminalClauseImpl("something", Operator.EQUALS, "something");
        final User theUser = null;

        Query query = mockController.getMock(Query.class);
        expect(query.getWhereClause()).andReturn(theWhereClause).anyTimes();

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);

        final WorkRatioSearcherInputHelper helper = mockController.getMock(WorkRatioSearcherInputHelper.class);
        expect(helper.convertClause(theWhereClause, theUser)).andReturn(null);

        mockController.replay();

        final WorkRatioSearchInputTransformer transformer = new WorkRatioSearchInputTransformer(CONSTANTS, SEARCHER_CONFIG, operandResolver)
        {
            @Override
            WorkRatioSearcherInputHelper createWorkRatioSearcherInputHelper()
            {
                return helper;
            }
        };
        assertFalse(transformer.doRelevantClausesFitFilterForm(null, query, searchContext));

        mockController.verify();
    }

    @Test
    public void testFitsHappyPath() throws Exception
    {
        final Clause theWhereClause = new TerminalClauseImpl("something", Operator.EQUALS, "something");
        final User theUser = null;

        Query query = mockController.getMock(Query.class);
        expect(query.getWhereClause()).andReturn(theWhereClause).anyTimes();

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);

        final Map<String, String> result = MapBuilder.<String, String>newBuilder().add("field", "value").toMap();
        final WorkRatioSearcherInputHelper helper = mockController.getMock(WorkRatioSearcherInputHelper.class);
        expect(helper.convertClause(theWhereClause, theUser)).andReturn(result);

        mockController.replay();

        final WorkRatioSearchInputTransformer transformer = new WorkRatioSearchInputTransformer(CONSTANTS, SEARCHER_CONFIG, operandResolver)
        {
            @Override
            WorkRatioSearcherInputHelper createWorkRatioSearcherInputHelper()
            {
                return helper;
            }
        };
        assertTrue(transformer.doRelevantClausesFitFilterForm(null, query, searchContext));

        mockController.verify();
    }

    @Test
    public void testSearchClauseEmptyValues() throws Exception
    {
        FieldValuesHolder values = new FieldValuesHolderImpl();

        final WorkRatioSearchInputTransformer transformer = createTransformer();
        assertNull(transformer.getSearchClause(null, values));
    }

    @Test
    public void testSearchClauseOnlyMinimum() throws Exception
    {
        FieldValuesHolder values = new FieldValuesHolderImpl();
        values.put(SEARCHER_CONFIG.getMinField(), "50");

        final WorkRatioSearchInputTransformer transformer = createTransformer();
        final TerminalClause clause = (TerminalClause) transformer.getSearchClause(null, values);
        assertEquals(CONSTANTS.getJqlClauseNames().getPrimaryName(), clause.getName());
        assertEquals(Operator.GREATER_THAN_EQUALS, clause.getOperator());
        final SingleValueOperand actual = (SingleValueOperand) clause.getOperand();
        assertEquals("50", actual.getStringValue());
    }

    @Test
    public void testSearchClauseOnlyMaximum() throws Exception
    {
        FieldValuesHolder values = new FieldValuesHolderImpl();
        values.put(SEARCHER_CONFIG.getMaxField(), "50");

        final WorkRatioSearchInputTransformer transformer = createTransformer();
        final TerminalClause clause = (TerminalClause) transformer.getSearchClause(null, values);
        assertEquals(CONSTANTS.getJqlClauseNames().getPrimaryName(), clause.getName());
        assertEquals(Operator.LESS_THAN_EQUALS, clause.getOperator());
        final SingleValueOperand actual = (SingleValueOperand) clause.getOperand();
        assertEquals("50", actual.getStringValue());
    }

    @Test
    public void testSearchClauseBothMinAndMax() throws Exception
    {
        FieldValuesHolder values = new FieldValuesHolderImpl();
        values.put(SEARCHER_CONFIG.getMinField(), "50");
        values.put(SEARCHER_CONFIG.getMaxField(), "50");

        final WorkRatioSearchInputTransformer transformer = createTransformer();
        final AndClause clause = (AndClause) transformer.getSearchClause(null, values);

        final TerminalClause maxClause = new TerminalClauseImpl(CONSTANTS.getJqlClauseNames().getPrimaryName(), Operator.LESS_THAN_EQUALS, "50");
        final TerminalClause minClause = new TerminalClauseImpl(CONSTANTS.getJqlClauseNames().getPrimaryName(), Operator.GREATER_THAN_EQUALS, "50");
        
        assertEquals(2, clause.getClauses().size());
        assertTrue(clause.getClauses().contains(maxClause));
        assertTrue(clause.getClauses().contains(minClause));
    }

    private WorkRatioSearchInputTransformer createTransformer()
    {
        mockController.addObjectInstance(CONSTANTS);
        mockController.addObjectInstance(SEARCHER_CONFIG);
        return mockController.instantiate(WorkRatioSearchInputTransformer.class);
    }
}
