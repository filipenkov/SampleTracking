package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.easymock.EasyMock;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since v4.0
 */
public class TestNavigatorStructureChecker extends MockControllerTestCase
{
    private static final String FIELD_NAME = "fieldName";
    private JqlOperandResolver jqlOperandResolver;
    private FieldFlagOperandRegistry fieldFlagOperandRegistry;
    private IndexInfoResolver<Version> indexInfoResolver;
    private SearchContextVisibilityChecker searchContextVisibilityChecker;
    private SearchContext searchContext;

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        searchContextVisibilityChecker = mockController.getMock(SearchContextVisibilityChecker.class);
        searchContext = mockController.getMock(SearchContext.class);
    }

    @Test
    public void testValidForNavigatorNullQuery() throws Exception
    {
        NavigatorStructureChecker transformer = createNavigatorStructureChecker(true);
        mockController.replay();
        assertTrue(transformer.checkSearchRequest(null, searchContext));
    }

    @Test
    public void testValidForNavigatorNullWhereClause() throws Exception
    {
        NavigatorStructureChecker transformer = createNavigatorStructureChecker(true);
        mockController.replay();
        assertTrue(transformer.checkSearchRequest(new QueryImpl(), searchContext));
    }

    @Test
    public void testCheckOperandSupportedFunction() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand("function");
        Set<String> flags = new HashSet<String>();
        flags.add("flag");

        fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, functionOperand);
        mockController.setReturnValue(flags);
        mockController.replay();

        NavigatorStructureChecker checker = createNavigatorStructureChecker();
        assertTrue(checker.checkOperand(functionOperand, true, searchContext));

        mockController.verify();
    }

    @Test
    public void testCheckOperandFunctionsNotAccepted() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand("function");

        mockController.replay();

        NavigatorStructureChecker checker = createNavigatorStructureChecker();
        assertFalse(checker.checkOperand(functionOperand, false, searchContext));

        mockController.verify();
    }

    @Test
    public void testCheckOperandUnsupportedFunction() throws Exception
    {
        FunctionOperand functionOperand = new FunctionOperand("function");

        fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, functionOperand);
        mockController.setReturnValue(null);
        mockController.replay();

        NavigatorStructureChecker checker = createNavigatorStructureChecker();
        assertFalse(checker.checkOperand(functionOperand, true, searchContext));

        mockController.verify();
    }

    @Test
    public void testCheckOperandEmptyHasFlagForThisField() throws Exception
    {
        Operand operand = EmptyOperand.EMPTY;

        fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, operand);
        mockController.setReturnValue(Collections.singleton("-1"));
        mockController.replay();

        NavigatorStructureChecker checker = createNavigatorStructureChecker();
        assertTrue(checker.checkOperand(operand, false, searchContext));
    }

    @Test
    public void testCheckOperandEmptyDoesntHaveFlagForThisField() throws Exception
    {
        Operand operand = EmptyOperand.EMPTY;

        fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, operand);
        mockController.setReturnValue(null);
        mockController.replay();

        NavigatorStructureChecker checker = createNavigatorStructureChecker();
        assertFalse(checker.checkOperand(operand, false, searchContext));
    }

    @Test
    public void testCheckOperandSingleValueInFieldFlagOperandRegistry() throws Exception
    {
        SingleValueOperand operand = new SingleValueOperand(5L);

        EasyMock.expect(fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, operand))
                .andReturn(Collections.singleton("X"));

        mockController.replay();

        final AtomicBoolean called = new AtomicBoolean(false);
        NavigatorStructureChecker checker = new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), true, fieldFlagOperandRegistry, MockJqlOperandResolver.createSimpleSupport(), indexInfoResolver, searchContextVisibilityChecker)
        {
            @Override
            boolean checkValue(final SingleValueOperand operand, final SearchContext searchContext)
            {
                called.set(true);
                return true;
            }
        };
        assertTrue(checker.checkOperand(operand, false, searchContext));
        assertFalse(called.get());
    }

    @Test
    public void testCheckOperandSingleValueLiteralDoesntFit() throws Exception
    {
        SingleValueOperand operand = new SingleValueOperand(5L);

        EasyMock.expect(fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, operand))
                .andReturn(null);

        mockController.replay();

        final AtomicBoolean called = new AtomicBoolean(false);
        NavigatorStructureChecker checker = new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), true, fieldFlagOperandRegistry, MockJqlOperandResolver.createSimpleSupport(), indexInfoResolver, searchContextVisibilityChecker)
        {
            @Override
            boolean checkValue(final SingleValueOperand operand, final SearchContext searchContext)
            {
                called.set(true);
                return false;
            }
        };
        assertFalse(checker.checkOperand(operand, false, searchContext));
        assertTrue(called.get());
    }

    @Test
    public void testCheckOperandMultiValueWithNoFunctionValuesOkay() throws Exception
    {
        final SingleValueOperand o1 = new SingleValueOperand(5L);
        final SingleValueOperand o2 = new SingleValueOperand("test");
        MultiValueOperand multiValueOperand = new MultiValueOperand(CollectionBuilder.newBuilder(o1, o2).asList());

        EasyMock.expect(fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, o1))
                .andReturn(null);
        EasyMock.expect(fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, o2))
                .andReturn(null);

        mockController.replay();

        final AtomicBoolean called = new AtomicBoolean(false);
        NavigatorStructureChecker checker = new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), true, fieldFlagOperandRegistry, MockJqlOperandResolver.createSimpleSupport(), indexInfoResolver, searchContextVisibilityChecker)
        {
            @Override
            boolean checkValue(final SingleValueOperand operand, final SearchContext searchContext)
            {
                called.set(true);
                return true;
            }
        };
        assertTrue(checker.checkOperand(multiValueOperand, true, searchContext));
        assertTrue(called.get());
    }

    @Test
    public void testCheckOperandMultiValueWithNoFunctionValuesInRegistry() throws Exception
    {
        final SingleValueOperand o1 = new SingleValueOperand(5L);
        final SingleValueOperand o2 = new SingleValueOperand("test");
        MultiValueOperand multiValueOperand = new MultiValueOperand(CollectionBuilder.newBuilder(o1, o2).asList());

        EasyMock.expect(fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, o1))
                .andReturn(Collections.singleton("5"));
        EasyMock.expect(fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, o2))
                .andReturn(Collections.singleton("test"));

        mockController.replay();

        final AtomicBoolean called = new AtomicBoolean(false);
        NavigatorStructureChecker checker = new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), true, fieldFlagOperandRegistry, MockJqlOperandResolver.createSimpleSupport(), indexInfoResolver, searchContextVisibilityChecker)
        {
            @Override
            boolean checkValue(final SingleValueOperand operand, final SearchContext searchContext)
            {
                called.set(true);
                return true;
            }
        };
        assertTrue(checker.checkOperand(multiValueOperand, true, searchContext));
        assertFalse(called.get());
    }

    @Test
    public void testCheckOperandMultiValueWithNoFunctionValuesNotAllOkay() throws Exception
    {
        final SingleValueOperand o1 = new SingleValueOperand(5L);
        final SingleValueOperand o2 = new SingleValueOperand("test");
        MultiValueOperand multiValueOperand = new MultiValueOperand(CollectionBuilder.newBuilder(o1, o2).asList());

        EasyMock.expect(fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, o1))
                .andReturn(Collections.singleton("5"));
        EasyMock.expect(fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, o2))
                .andReturn(null);

        mockController.replay();

        final AtomicBoolean called = new AtomicBoolean(false);
        NavigatorStructureChecker checker = new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), true, fieldFlagOperandRegistry, MockJqlOperandResolver.createSimpleSupport(), indexInfoResolver, searchContextVisibilityChecker)
        {
            @Override
            boolean checkValue(final SingleValueOperand operand, final SearchContext searchContext)
            {
                called.set(true);
                return false;
            }
        };
        assertFalse(checker.checkOperand(multiValueOperand, true, searchContext));
        assertTrue(called.get());
    }

    @Test
    public void testCheckOperandMultiValueWithFunctionNotAccepted() throws Exception
    {
        final SingleValueOperand o1 = new SingleValueOperand(5L);
        final FunctionOperand o2 = new FunctionOperand("test");
        MultiValueOperand multiValueOperand = new MultiValueOperand(CollectionBuilder.newBuilder(o1, o2).asList());

        EasyMock.expect(fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, o1))
                .andReturn(null);
        mockController.replay();

        final AtomicBoolean called = new AtomicBoolean(false);
        NavigatorStructureChecker checker = new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), false, fieldFlagOperandRegistry, MockJqlOperandResolver.createSimpleSupport(), indexInfoResolver, searchContextVisibilityChecker)
        {
            @Override
            boolean checkValue(final SingleValueOperand operand, final SearchContext searchContext)
            {
                called.set(true);
                return true;
            }
        };
        assertFalse(checker.checkOperand(multiValueOperand, true, searchContext));
        assertTrue(called.get());
    }
    
    @Test
    public void testCheckOperandMultiValueWithFunctionIsAccepted() throws Exception
    {
        final SingleValueOperand o1 = new SingleValueOperand(5L);
        final FunctionOperand functionOperand = new FunctionOperand("test");
        MultiValueOperand multiValueOperand = new MultiValueOperand(CollectionBuilder.newBuilder(o1, functionOperand).asList());

        EasyMock.expect(fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, o1))
                .andReturn(Collections.singleton("5"));
        EasyMock.expect(fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, functionOperand))
                .andReturn(Collections.singleton("2"));
        mockController.replay();

        final AtomicBoolean called = new AtomicBoolean(false);
        NavigatorStructureChecker checker = new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), true, fieldFlagOperandRegistry, MockJqlOperandResolver.createSimpleSupport(), indexInfoResolver, searchContextVisibilityChecker)
        {
            @Override
            boolean checkValue(final SingleValueOperand operand, final SearchContext searchContext)
            {
                called.set(true);
                return true;
            }
        };
        assertTrue(checker.checkOperand(multiValueOperand, true, searchContext));
        assertFalse(called.get());
    }

    @Test
    public void testCheckOperandMultiValueWithEmptyHasFlag() throws Exception
    {
        final SingleValueOperand o1 = new SingleValueOperand(5L);
        final EmptyOperand o2 = EmptyOperand.EMPTY;
        MultiValueOperand multiValueOperand = new MultiValueOperand(CollectionBuilder.newBuilder(o1, o2).asList());

        EasyMock.expect(fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, o1))
                .andReturn(Collections.singleton("5"));
        EasyMock.expect(fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, o2))
                .andReturn(Collections.singleton("-1"));
        mockController.replay();

        NavigatorStructureChecker checker = new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), true, fieldFlagOperandRegistry, MockJqlOperandResolver.createSimpleSupport(), indexInfoResolver, searchContextVisibilityChecker)
        {
            @Override
            boolean checkValue(final SingleValueOperand operand, final SearchContext searchContext)
            {
                fail("Shouldn't need to be called");
                return false;
            }
        };
        assertTrue(checker.checkOperand(multiValueOperand, false, searchContext));
    }

    @Test
    public void testCheckOperandMultiValueWithEmptyDoesntHaveFlag() throws Exception
    {
        final SingleValueOperand o1 = new SingleValueOperand(5L);
        final EmptyOperand o2 = EmptyOperand.EMPTY;
        MultiValueOperand multiValueOperand = new MultiValueOperand(CollectionBuilder.newBuilder(o1, o2).asList());

        EasyMock.expect(fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, o1))
                .andReturn(Collections.singleton("HA"));
        EasyMock.expect(fieldFlagOperandRegistry.getFlagForOperand(FIELD_NAME, o2))
                .andReturn(null);
        mockController.replay();

        NavigatorStructureChecker checker = new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), true, fieldFlagOperandRegistry, MockJqlOperandResolver.createSimpleSupport(), indexInfoResolver, searchContextVisibilityChecker)
        {
            @Override
            boolean checkValue(final SingleValueOperand operand, final SearchContext searchContext)
            {
                fail("Shouldn't need to be called");
                return true;
            }
        };
        assertFalse(checker.checkOperand(multiValueOperand, false, searchContext));
    }

    @Test
    public void testCheckOperator() throws Exception
    {
        mockController.replay();

        NavigatorStructureChecker checker = createNavigatorStructureChecker();
        List<Operator> supportedOperators = CollectionBuilder.newBuilder(Operator.EQUALS, Operator.IN, Operator.IS).asList();

        for (Operator operator : Operator.values())
        {
            if (!supportedOperators.contains(operator))
            {
                assertFalse(checker.checkOperator(operator));
            }
            else
            {
                assertTrue(checker.checkOperator(operator));
            }
        }
    }

    @Test
    public void testCheckSearchRequestHappyPath() throws Exception
    {
        AndClause andClause = new AndClause(new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "value"),
                new TerminalClauseImpl("other", Operator.EQUALS, "valueother"));

        mockController.replay();

        NavigatorStructureChecker checker = new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), true, fieldFlagOperandRegistry, MockJqlOperandResolver.createSimpleSupport(), indexInfoResolver, searchContextVisibilityChecker)
        {
            @Override
            boolean checkOperator(final Operator operator)
            {
                return true;
            }

            @Override
            boolean checkOperand(final Operand operand, final boolean acceptFunctions, final SearchContext searchContext)
            {
                return true;
            }
        };

        assertValidate(andClause, checker, true);
    }

    @Test
    public void testCheckSearchRequestClausesNotValid() throws Exception
    {
        AndClause andClause = new AndClause(new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "value"),
                new TerminalClauseImpl("other", Operator.EQUALS, "valueother"));

        final SimpleNavigatorCollectorVisitor mockVisitor = new SimpleNavigatorCollectorVisitor(FIELD_NAME)
        {
            @Override
            public boolean isValid()
            {
                return false;
            }

            @Override
            public Void visit(final AndClause andClause)
            {
                // NOOP
                return null;
            }
        };

        mockController.replay();

        NavigatorStructureChecker checker = new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), true, fieldFlagOperandRegistry, MockJqlOperandResolver.createSimpleSupport(), indexInfoResolver, searchContextVisibilityChecker)
        {
            @Override
            boolean checkOperator(final Operator operator)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            boolean checkOperand(final Operand operand, final boolean acceptFunctions, final SearchContext searchContext)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectorVisitor()
            {
                return mockVisitor;
            }
        };

        assertValidate(andClause, checker, false);
    }

    @Test
    public void testCheckSearchRequestTooManyClauses() throws Exception
    {
        AndClause andClause = new AndClause(new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "value"),
                new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "valueother"));

        mockController.replay();

        NavigatorStructureChecker checker = new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), true, fieldFlagOperandRegistry, MockJqlOperandResolver.createSimpleSupport(), indexInfoResolver, searchContextVisibilityChecker)
        {
            @Override
            boolean checkOperator(final Operator operator)
            {
                throw new UnsupportedOperationException();
            }

            @Override
            boolean checkOperand(final Operand operand, final boolean acceptFunctions, SearchContext searchContext)
            {
                throw new UnsupportedOperationException();
            }

        };

        assertValidate(andClause, checker, false);
    }

    @Test
    public void testCheckSearchRequestOperatorNotValid() throws Exception
    {
        AndClause andClause = new AndClause(new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "value"),
                new TerminalClauseImpl("other", Operator.EQUALS, "valueother"));

        mockController.replay();

        NavigatorStructureChecker checker = new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), true, fieldFlagOperandRegistry, MockJqlOperandResolver.createSimpleSupport(), indexInfoResolver, searchContextVisibilityChecker)
        {
            @Override
            boolean checkOperator(final Operator operator)
            {
                return false;
            }

            @Override
            boolean checkOperand(final Operand operand, final boolean acceptFunctions, final SearchContext searchContext)
            {
                throw new UnsupportedOperationException();
            }

        };

        assertValidate(andClause, checker, false);
    }

    @Test
    public void testCheckSearchRequestOperandNotValid() throws Exception
    {
        AndClause andClause = new AndClause(new TerminalClauseImpl(FIELD_NAME, Operator.EQUALS, "value"),
                new TerminalClauseImpl("other", Operator.EQUALS, "valueother"));

        mockController.replay();

        NavigatorStructureChecker checker = new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), true, fieldFlagOperandRegistry, MockJqlOperandResolver.createSimpleSupport(), indexInfoResolver, searchContextVisibilityChecker)
        {
            @Override
            boolean checkOperator(final Operator operator)
            {
                return true;
            }

            @Override
            boolean checkOperand(final Operand operand, final boolean acceptFunctions, SearchContext searchContext)
            {
                return false;
            }

        };

        assertValidate(andClause, checker, false);
    }

    @Test
    public void testCheckValueHappyPath() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("10");
        final List<String> ids = CollectionBuilder.newBuilder("10", "25").asList();

        indexInfoResolver.getIndexedValues(operand.getStringValue());
        mockController.setReturnValue(ids);

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, ids);
        mockController.setReturnValue(CollectionBuilder.newBuilder("10").asSet());

        mockController.replay();
        final NavigatorStructureChecker<Version> checker = createNavigatorStructureChecker(false);
        assertTrue(checker.checkValue(operand, searchContext));
        mockController.verify();
    }

    @Test
    public void testCheckValueFail() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand(10L);
        final List<String> ids = CollectionBuilder.newBuilder("10").asList();

        indexInfoResolver.getIndexedValues(operand.getLongValue());
        mockController.setReturnValue(ids);

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, ids);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asSet());

        mockController.replay();
        final NavigatorStructureChecker<Version> checker = createNavigatorStructureChecker(false);
        assertFalse(checker.checkValue(operand, searchContext));
        mockController.verify();
    }

    private NavigatorStructureChecker<Version> createNavigatorStructureChecker()
    {
        return new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), true, fieldFlagOperandRegistry, MockJqlOperandResolver.createSimpleSupport(), indexInfoResolver, searchContextVisibilityChecker);
    }

    private NavigatorStructureChecker<Version> createNavigatorStructureChecker(final boolean supportMultiLevelFunctions)
    {
        return new NavigatorStructureChecker<Version>(new ClauseNames(FIELD_NAME), supportMultiLevelFunctions, fieldFlagOperandRegistry, jqlOperandResolver, indexInfoResolver, searchContextVisibilityChecker);
    }

    private void assertValidate(final AndClause andClause, final NavigatorStructureChecker checker, final boolean isClauseValid)
    {
        assertEquals(isClauseValid, checker.checkSearchRequest(new QueryImpl(andClause), searchContext));
    }
}
