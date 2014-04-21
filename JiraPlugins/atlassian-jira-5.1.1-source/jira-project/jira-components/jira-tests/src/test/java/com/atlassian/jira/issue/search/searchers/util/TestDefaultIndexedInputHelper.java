package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.SearchContextVisibilityChecker;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @since v4.0
 */
public class TestDefaultIndexedInputHelper extends MockControllerTestCase
{
    private User theUser = null;
    private SearchContextVisibilityChecker searchContextVisibilityChecker;
    private SearchContext searchContext;
    private SearchRequest searchRequest;

    @Before
    public void setUp() throws Exception
    {
        searchContext = mockController.getMock(SearchContext.class);
        searchRequest = mockController.getMock(SearchRequest.class);
        searchContextVisibilityChecker = mockController.getMock(SearchContextVisibilityChecker.class);
    }

    @Test
    public void testGetClauseForNavigatorValuesEmptySet() throws Exception
    {
        final String fieldName = "testfield";

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertNull(helper.getClauseForNavigatorValues(fieldName, Collections.<String>emptySet()));
        mockController.verify();
    }

    @Test
    public void testGetClauseForNavigatorValuesOneValueNotFlag() throws Exception
    {
        final String fieldName = "testfield";

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final String id = "45";
        final TerminalClause expectedClause = new TerminalClauseImpl(fieldName, Operator.EQUALS, 45L);
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id);
        mockController.setReturnValue(null);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertEquals(expectedClause, helper.getClauseForNavigatorValues(fieldName, Collections.singleton(id)));
        mockController.verify();
    }

    @Test
    public void testGetClauseForNavigatorValuesTwoValuesNotFlag() throws Exception
    {
        final String fieldName = "testfield";

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final String id1 = "45";
        final String id2 = "888";
        final TerminalClause expectedClause = new TerminalClauseImpl(fieldName, Operator.IN, new MultiValueOperand(45L, 888L));
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id1);
        mockController.setReturnValue(null);
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id2);
        mockController.setReturnValue(null);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertEquals(expectedClause, helper.getClauseForNavigatorValues(fieldName, CollectionBuilder.newBuilder(id1, id2).asListOrderedSet()));
        mockController.verify();
    }

    @Test
    public void testGetClauseForNavigatorValuesTwoValuesOneNotNumber() throws Exception
    {
        final String fieldName = "testfield";

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final String id1 = "45";
        final String id2 = "notanumber";
        final TerminalClause expectedClause = new TerminalClauseImpl(fieldName, Operator.IN, new MultiValueOperand(new SingleValueOperand(45L), new SingleValueOperand("notanumber")));
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id1);
        mockController.setReturnValue(null);
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id2);
        mockController.setReturnValue(null);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertEquals(expectedClause, helper.getClauseForNavigatorValues(fieldName, CollectionBuilder.newBuilder(id1, id2).asListOrderedSet()));
        mockController.verify();
    }

    @Test
    public void testGetClauseForNavigatorValuesOneValueIsListFlag() throws Exception
    {
        final String fieldName = "testfield";

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final String id = "-3";
        final Operand flagOperand = new MultiValueOperand(45L);
        final TerminalClause expectedClause = new TerminalClauseImpl(fieldName, Operator.IN, flagOperand);
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id);
        mockController.setReturnValue(flagOperand);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertEquals(expectedClause, helper.getClauseForNavigatorValues(fieldName, Collections.singleton(id)));
        mockController.verify();
    }

    @Test
    public void testGetClauseForNavigatorValuesOneValueIsFlagNotList() throws Exception
    {
        final String fieldName = "testfield";

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final String id = "-1";
        final Operand flagOperand = EmptyOperand.EMPTY;
        final TerminalClause expectedClause = new TerminalClauseImpl(fieldName, Operator.EQUALS, flagOperand);
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id);
        mockController.setReturnValue(flagOperand);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertEquals(expectedClause, helper.getClauseForNavigatorValues(fieldName, Collections.singleton(id)));
        mockController.verify();
    }

    @Test
    public void testGetClauseForNavigatorValuesTwoValuesOneIsListFlag() throws Exception
    {
        final String fieldName = "testfield";

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        final String id1 = "-3";
        final String id2 = "888";
        final Operand flagOperand = new MultiValueOperand(45L);
        final Operand singleOperand = new SingleValueOperand(888L);
        final Operand expectedMultiOperand = new MultiValueOperand(CollectionBuilder.newBuilder(flagOperand, singleOperand).asList());
        final TerminalClause expectedClause = new TerminalClauseImpl(fieldName, Operator.IN, expectedMultiOperand);
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id1);
        mockController.setReturnValue(flagOperand);
        fieldFlagOperandRegistry.getOperandForFlag(fieldName, id2);
        mockController.setReturnValue(null);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertEquals(expectedClause, helper.getClauseForNavigatorValues(fieldName, CollectionBuilder.newBuilder(id1, id2).asListOrderedSet()));
        mockController.verify();
    }

    @Test
    public void testGetAllNavigatorValuesAsStringsNoWhereClause() throws Exception
    {
        DefaultIndexedInputHelper helper = mockController.instantiate(DefaultIndexedInputHelper.class);
        final Set<String> strings = helper.getAllNavigatorValuesForMatchingClauses(theUser, new ClauseNames("balrg"), new QueryImpl(), searchContext);
        assertEquals(0, strings.size());
    }

    @Test
    public void testGetAllIndexValuesAsStringsNoWhereClause() throws Exception
    {
        DefaultIndexedInputHelper helper = mockController.instantiate(DefaultIndexedInputHelper.class);
        final Set<String> strings = helper.getAllIndexValuesForMatchingClauses(theUser, new ClauseNames("balrg"), new QueryImpl(), searchContext);
        assertEquals(0, strings.size());
    }

    @Test
    public void testGetAllNavigatorValuesAsStringsSingleValueOperandFiltered() throws Exception
    {
        final String fieldName = "testfield";
        final SingleValueOperand opAnything = new SingleValueOperand("55");

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opAnything);
        mockController.setReturnValue(null);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        indexInfoResolver.getIndexedValues("55");
        mockController.setReturnValue(Collections.singletonList("555"));

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("555").asSet());
        mockController.setReturnValue(Collections.emptySet());
        mockController.replay();

        final TerminalClauseImpl terminalClause = new TerminalClauseImpl(fieldName, Operator.EQUALS, opAnything);

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertNavigatorValidForSingleClause(helper, terminalClause);
        mockController.verify();
    }

    @Test
    public void testGetAllNavigatorValuesAsStringsSingleValueOperand() throws Exception
    {
        final String fieldName = "testfield";
        final SingleValueOperand opAnything = new SingleValueOperand("55");

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opAnything);
        mockController.setReturnValue(null);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        indexInfoResolver.getIndexedValues("55");
        mockController.setReturnValue(Collections.singletonList("555"));

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("555").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("555").asSet());
        mockController.replay();

        final TerminalClauseImpl terminalClause = new TerminalClauseImpl(fieldName, Operator.EQUALS, opAnything);

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertNavigatorValidForSingleClause(helper, terminalClause, "555");
        mockController.verify();
    }

    @Test
    public void testGetAllNavigatorValuesAsStringsMultiValueOperand() throws Exception
    {
        final String fieldName = "testfield";
        final SingleValueOperand opAnything = new SingleValueOperand("Anything");
        final SingleValueOperand op55 = new SingleValueOperand("55");
        final MultiValueOperand multiOp = new MultiValueOperand(CollectionBuilder.newBuilder(opAnything, op55).asList());
        final TerminalClauseImpl terminalClause = new TerminalClauseImpl(fieldName, Operator.EQUALS, multiOp);

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, multiOp);
        mockController.setReturnValue(null);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opAnything);
        mockController.setReturnValue(null);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, op55);
        mockController.setReturnValue(null);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        indexInfoResolver.getIndexedValues("Anything");
        mockController.setReturnValue(Collections.singletonList("Anything Resolved"));
        indexInfoResolver.getIndexedValues("55");
        mockController.setReturnValue(Collections.singletonList("555"));

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("Anything Resolved").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("Anything Resolved").asSet());

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("555").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("555").asSet());

        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertNavigatorValidForSingleClause(helper, terminalClause, "555", "Anything Resolved");
        mockController.verify();
    }

    @Test
    public void testGetAllNavigatorValuesAsStringsFunctionalOperandHasFlag() throws Exception
    {
        final String fieldName = "testfield";
        final FunctionOperand opFunc = new FunctionOperand("func");

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opFunc);
        mockController.setReturnValue(Collections.singleton("-2"));
        final OperandHandler operandHandler = mockController.getMock(OperandHandler.class);

        MockJqlOperandResolver jqlOperandSupport = new MockJqlOperandResolver();
        jqlOperandSupport.addHandler("func", operandHandler);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        mockController.replay();

        final TerminalClauseImpl terminalClause = new TerminalClauseImpl(fieldName, Operator.EQUALS, opFunc);

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, jqlOperandSupport, fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertNavigatorValidForSingleClause(helper, terminalClause, "-2");
        mockController.verify();
    }

    @Test
    public void testGetAllNavigatorValuesAsStringsFunctionalOperandHasNoFlag() throws Exception
    {
        final String fieldName = "testfield";
        final FunctionOperand opFunc = new FunctionOperand("func");
        final TerminalClauseImpl terminalClause = new TerminalClauseImpl(fieldName, Operator.EQUALS, opFunc);

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opFunc);
        mockController.setReturnValue(null);
        final OperandHandler operandHandler = mockController.getMock(OperandHandler.class);
        operandHandler.getValues(isA(QueryCreationContext.class), eq(opFunc), eq(terminalClause));
        mockController.setReturnValue(createQueryLiterals("value"));

        MockJqlOperandResolver jqlOperandSupport = new MockJqlOperandResolver();
        jqlOperandSupport.addHandler("func", operandHandler);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        indexInfoResolver.getIndexedValues("value");
        mockController.setReturnValue(Collections.singletonList("value Resolved"));

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("value Resolved").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("value Resolved").asSet());

        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, jqlOperandSupport, fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertNavigatorValidForSingleClause(helper, terminalClause, "value Resolved");
        mockController.verify();
    }

    @Test
    public void testNavigatorValuesWithNoFunctionOperands() throws Exception
    {
        final String fieldName = "testfield";
        final SingleValueOperand opAnything = new SingleValueOperand("Anything");
        final SingleValueOperand op55 = new SingleValueOperand(55L);
        final SingleValueOperand op11 = new SingleValueOperand(11L);
        final MultiValueOperand opMulti = new MultiValueOperand(CollectionBuilder.newBuilder(op55, op11).asList());

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opAnything);
        mockController.setReturnValue(null);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opMulti);
        mockController.setReturnValue(null);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, op55);
        mockController.setReturnValue(null);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, op11);
        mockController.setReturnValue(null);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        indexInfoResolver.getIndexedValues("Anything");
        mockController.setReturnValue(Collections.singletonList("Anything Resolved"));
        indexInfoResolver.getIndexedValues(55L);
        mockController.setReturnValue(Collections.singletonList("55"));
        indexInfoResolver.getIndexedValues(11L);
        mockController.setReturnValue(Collections.singletonList("11"));

        final QueryImpl searchQuery = new QueryImpl(new OrClause(CollectionBuilder.<Clause>newBuilder(new TerminalClauseImpl(fieldName, Operator.EQUALS, opAnything), new TerminalClauseImpl(fieldName, Operator.IN, opMulti)).asList()));
        searchRequest.getQuery();
        mockController.setDefaultReturnValue(searchQuery);

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("Anything Resolved").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("Anything Resolved").asSet());

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("55").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("55").asSet());

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("11").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("11").asSet());

        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertNavigatorValidForSearchRequest(fieldName, helper, "55", "11", "Anything Resolved");
    }

    @Test
    public void testNavigatorValuesWithMixedOperandsNoFlags() throws Exception
    {
        final String fieldName = "testfield";
        final SingleValueOperand opAnything = new SingleValueOperand("Anything");
        final FunctionOperand opFunction = new FunctionOperand("myFunction");
        final TerminalClauseImpl clause1 = new TerminalClauseImpl(fieldName, Operator.EQUALS, opAnything);
        final TerminalClauseImpl clause2 = new TerminalClauseImpl(fieldName, Operator.IN, opFunction);

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opAnything);
        mockController.setReturnValue(null);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opFunction);
        mockController.setReturnValue(null);

        final OperandHandler operandHandler = mockController.getMock(OperandHandler.class);
        operandHandler.getValues(isA(QueryCreationContext.class), eq(opAnything), eq(clause1));
        mockController.setReturnValue(createQueryLiterals("Anything"));
        operandHandler.getValues(isA(QueryCreationContext.class), eq(opFunction), eq(clause2));
        mockController.setReturnValue(createQueryLiterals(55L, 11L));

        MockJqlOperandResolver jqlOperandSupport = new MockJqlOperandResolver();
        jqlOperandSupport.addHandler(SingleValueOperand.OPERAND_NAME, operandHandler);
        jqlOperandSupport.addHandler("myFunction", operandHandler);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        indexInfoResolver.getIndexedValues("Anything");
        mockController.setReturnValue(Collections.singletonList("Anything Resolved"));
        indexInfoResolver.getIndexedValues(55L);
        mockController.setReturnValue(Collections.singletonList("55"));
        indexInfoResolver.getIndexedValues(11L);
        mockController.setReturnValue(Collections.singletonList("11"));

        final QueryImpl searchQuery = new QueryImpl(new OrClause(CollectionBuilder.<Clause>newBuilder(clause1, clause2).asList()));
        searchRequest.getQuery();
        mockController.setDefaultReturnValue(searchQuery);

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("Anything Resolved").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("Anything Resolved").asSet());

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("55", "11").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("55", "11").asSet());

        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, jqlOperandSupport, fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertNavigatorValidForSearchRequest(fieldName, helper, "55", "11", "Anything Resolved");
   }

    @Test
    public void testNavigatorValuesWithMixedOperandsAndFlags() throws Exception
    {
        final String fieldName = "testfield";
        final SingleValueOperand opAnything = new SingleValueOperand("Anything");
        final FunctionOperand opFunction = new FunctionOperand("myFunction");
        final TerminalClauseImpl clause1 = new TerminalClauseImpl(fieldName, Operator.EQUALS, opAnything);
        final TerminalClauseImpl clause2 = new TerminalClauseImpl(fieldName, Operator.IN, opFunction);

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opAnything);
        mockController.setReturnValue(null);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opFunction);
        mockController.setReturnValue(Collections.singleton("-2"));

        final OperandHandler operandHandler = mockController.getMock(OperandHandler.class);
        operandHandler.getValues(isA(QueryCreationContext.class), eq(opAnything), eq(clause1));
        mockController.setReturnValue(createQueryLiterals("Anything"));

        MockJqlOperandResolver jqlOperandSupport = new MockJqlOperandResolver();
        jqlOperandSupport.addHandler(SingleValueOperand.OPERAND_NAME, operandHandler);
        jqlOperandSupport.addHandler("myFunction", operandHandler);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        indexInfoResolver.getIndexedValues("Anything");
        mockController.setReturnValue(Collections.singletonList("Anything Resolved"));

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("Anything Resolved").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("Anything Resolved").asSet());

        final QueryImpl searchQuery = new QueryImpl(new OrClause(CollectionBuilder.<Clause>newBuilder(clause1, clause2).asList()));
        searchRequest.getQuery();
        mockController.setDefaultReturnValue(searchQuery);
        mockController.replay();


        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, jqlOperandSupport, fieldFlagOperandRegistry, searchContextVisibilityChecker);

        assertNavigatorValidForSearchRequest(fieldName, helper, "-2", "Anything Resolved");
    }

    @Test
    public void testNavigatorValuesWithMultiValueOperandsContainsFunctions() throws Exception
    {
        final String fieldName = "testfield";
        final SingleValueOperand opAnything = new SingleValueOperand("Anything");
        final FunctionOperand opFunction = new FunctionOperand("myFunction");
        final MultiValueOperand opMulti = new MultiValueOperand(CollectionBuilder.newBuilder(opAnything, opFunction).asList());
        final TerminalClauseImpl clause1 = new TerminalClauseImpl(fieldName, Operator.IN, opMulti);

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opMulti);
        mockController.setReturnValue(null);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opAnything);
        mockController.setReturnValue(null);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opFunction);
        mockController.setReturnValue(Collections.singleton("-2"));

        final OperandHandler operandHandler = mockController.getMock(OperandHandler.class);
        operandHandler.getValues(isA(QueryCreationContext.class), eq(opAnything), eq(clause1));
        mockController.setReturnValue(createQueryLiterals("Anything"));

        MockJqlOperandResolver jqlOperandSupport = new MockJqlOperandResolver();
        jqlOperandSupport.addHandler(SingleValueOperand.OPERAND_NAME, operandHandler);
        jqlOperandSupport.addHandler("myFunction", operandHandler);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        indexInfoResolver.getIndexedValues("Anything");
        mockController.setReturnValue(Collections.singletonList("Anything Resolved"));

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("Anything Resolved").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("Anything Resolved").asSet());

        final QueryImpl searchQuery = new QueryImpl(new OrClause(clause1));
        searchRequest.getQuery();
        mockController.setDefaultReturnValue(searchQuery);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, jqlOperandSupport, fieldFlagOperandRegistry, searchContextVisibilityChecker);

        assertNavigatorValidForSearchRequest(fieldName, helper, "-2", "Anything Resolved");
    }

    @Test
    public void testNavigatorValuesWithMultiValueOperandsContainsFlaggedFuncsAndOperands() throws Exception
    {
        final String fieldName = "testfield";
        final SingleValueOperand opAnything = new SingleValueOperand("ImAFlag");
        final FunctionOperand opFunction = new FunctionOperand("myFunction");
        final MultiValueOperand opMulti = new MultiValueOperand(CollectionBuilder.newBuilder(opAnything, opFunction).asList());

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opMulti);
        mockController.setReturnValue(null);
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opAnything);
        mockController.setReturnValue(Collections.singleton("-1"));
        fieldFlagOperandRegistry.getFlagForOperand(fieldName, opFunction);
        mockController.setReturnValue(Collections.singleton("-2"));

        final OperandHandler operandHandler = mockController.getMock(OperandHandler.class);

        MockJqlOperandResolver jqlOperandSupport = new MockJqlOperandResolver();
        jqlOperandSupport.addHandler(SingleValueOperand.OPERAND_NAME, operandHandler);
        jqlOperandSupport.addHandler("myFunction", operandHandler);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);

        final QueryImpl searchQuery = new QueryImpl(new OrClause(new TerminalClauseImpl(fieldName, Operator.IN, opMulti)));
        searchRequest.getQuery();
        mockController.setDefaultReturnValue(searchQuery);
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, jqlOperandSupport, fieldFlagOperandRegistry, searchContextVisibilityChecker);

        assertNavigatorValidForSearchRequest(fieldName, helper, "-1", "-2");
    }

    @Test
    public void testIndexValuesWithNoFunctionOperands() throws Exception
    {
        final String fieldName = "testfield";
        final SingleValueOperand opAnything = new SingleValueOperand("Anything");
        final MultiValueOperand opMulti = new MultiValueOperand(55L, 11L);

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        indexInfoResolver.getIndexedValues("Anything");
        mockController.setReturnValue(Collections.singletonList("Anything Resolved"));
        indexInfoResolver.getIndexedValues(55L);
        mockController.setReturnValue(Collections.singletonList("55"));
        indexInfoResolver.getIndexedValues(11L);
        mockController.setReturnValue(Collections.singletonList("11"));

        final QueryImpl searchQuery = new QueryImpl(new OrClause(CollectionBuilder.newBuilder(new TerminalClauseImpl(fieldName, Operator.EQUALS, opAnything), new TerminalClauseImpl(fieldName, Operator.IN, opMulti)).asList()));
        searchRequest.getQuery();
        mockController.setDefaultReturnValue(searchQuery);

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("Anything Resolved").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("Anything Resolved").asSet());

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("55", "11").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("55", "11").asSet());

        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertIndexValid(fieldName, helper, "55", "11", "Anything Resolved");
    }

    @Test
    public void testIndexValuesContextFilter() throws Exception
    {
        final String fieldName = "testfield";
        final SingleValueOperand opAnything = new SingleValueOperand("Anything");
        final MultiValueOperand opMulti = new MultiValueOperand(55L, 11L);

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        indexInfoResolver.getIndexedValues("Anything");
        mockController.setReturnValue(Collections.singletonList("Anything Resolved"));
        indexInfoResolver.getIndexedValues(55L);
        mockController.setReturnValue(Collections.singletonList("55"));
        indexInfoResolver.getIndexedValues(11L);
        mockController.setReturnValue(Collections.singletonList("11"));

        final QueryImpl searchQuery = new QueryImpl(new OrClause(CollectionBuilder.newBuilder(new TerminalClauseImpl(fieldName, Operator.EQUALS, opAnything), new TerminalClauseImpl(fieldName, Operator.IN, opMulti)).asList()));
        searchRequest.getQuery();
        mockController.setDefaultReturnValue(searchQuery);

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("Anything Resolved").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("Anything Resolved").asSet());

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("55", "11").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("11").asSet());

        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, MockJqlOperandResolver.createSimpleSupport(), fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertIndexValid(fieldName, helper, "11", "Anything Resolved");
    }

    @Test
    public void testIndexValuesWithMixedOperands() throws Exception
    {
        final String fieldName = "testfield";
        final SingleValueOperand opAnything = new SingleValueOperand("Anything");
        final FunctionOperand opFunction = new FunctionOperand("myFunction");
        final EmptyOperand opEmpty = EmptyOperand.EMPTY;
        final TerminalClauseImpl clause1 = new TerminalClauseImpl(fieldName, Operator.EQUALS, opAnything);
        final TerminalClauseImpl clause2 = new TerminalClauseImpl(fieldName, Operator.IN, opFunction);
        final TerminalClauseImpl clause3 = new TerminalClauseImpl(fieldName, Operator.IS, opEmpty);

        final FieldFlagOperandRegistry fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);

        final OperandHandler operandHandler = mockController.getMock(OperandHandler.class);
        operandHandler.getValues(isA(QueryCreationContext.class), eq(opAnything), eq(clause1));
        mockController.setReturnValue(createQueryLiterals("Anything"));
        operandHandler.getValues(isA(QueryCreationContext.class), eq(opFunction), eq(clause2));
        mockController.setReturnValue(createQueryLiterals(55L, 11L));
        operandHandler.getValues(isA(QueryCreationContext.class), eq(opEmpty), eq(clause3));
        mockController.setReturnValue(Collections.singletonList(new QueryLiteral()));

        MockJqlOperandResolver jqlOperandSupport = new MockJqlOperandResolver();
        jqlOperandSupport.addHandler(SingleValueOperand.OPERAND_NAME, operandHandler);
        jqlOperandSupport.addHandler("myFunction", operandHandler);
        jqlOperandSupport.addHandler(EmptyOperand.OPERAND_NAME, operandHandler);

        final IndexInfoResolver indexInfoResolver = mockController.getMock(IndexInfoResolver.class);
        indexInfoResolver.getIndexedValues("Anything");
        mockController.setReturnValue(Collections.singletonList("Anything Resolved"));
        indexInfoResolver.getIndexedValues(55L);
        mockController.setReturnValue(Collections.singletonList("55"));
        indexInfoResolver.getIndexedValues(11L);
        mockController.setReturnValue(Collections.singletonList("11"));

        final QueryImpl searchQuery = new QueryImpl(new OrClause(CollectionBuilder.newBuilder(clause1, clause2, clause3).asList()));
        searchRequest.getQuery();
        mockController.setDefaultReturnValue(searchQuery);

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("Anything Resolved").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("Anything Resolved").asSet());

        searchContextVisibilityChecker.FilterOutNonVisibleInContext(searchContext, CollectionBuilder.newBuilder("55", "11").asSet());
        mockController.setReturnValue(CollectionBuilder.newBuilder("55", "11").asSet());
       
        mockController.replay();

        DefaultIndexedInputHelper helper = new DefaultIndexedInputHelper(indexInfoResolver, jqlOperandSupport, fieldFlagOperandRegistry, searchContextVisibilityChecker);
        assertIndexValid(fieldName, helper, "55", "11", "Anything Resolved");
    }

    private void assertNavigatorValidForSingleClause(final DefaultIndexedInputHelper<?> helper, final TerminalClause clause, final String... contains)
    {
        final Set<String> strings = helper.getAllNavigatorValues(theUser, searchContext, clause.getName(), clause.getOperand(), clause);
        final List<String> expected = Arrays.asList(contains);
        assertEquals(expected.size(), strings.size());
        assertTrue(expected.containsAll(strings));
    }

    private void assertNavigatorValidForSearchRequest(final String fieldName, final DefaultIndexedInputHelper<?> helper, final String... contains)
    {
        final Set<String> strings = helper.getAllNavigatorValuesForMatchingClauses(theUser, new ClauseNames(fieldName), searchRequest.getQuery(), searchContext);

        final List<String> expected = Arrays.asList(contains);
        assertEquals(expected.size(), strings.size());
        assertTrue(expected.containsAll(strings));

    }

    private void assertIndexValid(final String fieldName, final DefaultIndexedInputHelper<?> helper, final String... contains)
    {
        final Set<String> strings = helper.getAllIndexValuesForMatchingClauses(theUser, new ClauseNames(fieldName), searchRequest.getQuery(), searchContext);

        final List<String> expected = Arrays.asList(contains);
        assertEquals(expected.size(), strings.size());
        assertTrue(expected.containsAll(strings));
    }


    private List<QueryLiteral> createQueryLiterals(String... value)
    {
        List<QueryLiteral> literals = new ArrayList<QueryLiteral>();
        for (String s : value)
        {
            literals.add(createLiteral(s));
        }
        return literals;
    }

    private List<QueryLiteral> createQueryLiterals(Long... value)
    {
        List<QueryLiteral> literals = new ArrayList<QueryLiteral>();
        for (Long s : value)
        {
            literals.add(createLiteral(s));
        }
        return literals;
    }
}
