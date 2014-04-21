package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.jira.jql.context.ClauseContextImpl;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.jql.context.QueryContextImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.util.JqlCascadingSelectLiteralUtil;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.plugin.jql.function.CascadeOptionFunction;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.easymock.EasyMock;

import java.util.Collections;
import java.util.List;

/**
 * @since v4.0
 */
public class TestCascadingSelectCustomFieldSearchInputTransformer extends MockControllerTestCase
{
    private CustomField customField;
    private SelectConverter selectConverter;
    private JqlOperandResolver jqlOperandResolver;
    private JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private final ClauseNames names = new ClauseNames("cf[100]");
    private final String url = "cf_100";
    private JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil;
    private SearchContext searchContext;
    private QueryContextConverter queryContextConverter;
    private User theUser = null;
    private CustomFieldInputHelper customFieldInputHelper;

    @Before
    public void setUp() throws Exception
    {
        customField = mockController.getMock(CustomField.class);
        selectConverter = mockController.getMock(SelectConverter.class);
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlSelectOptionsUtil = mockController.getMock(JqlSelectOptionsUtil.class);
        jqlCascadingSelectLiteralUtil = mockController.getMock(JqlCascadingSelectLiteralUtil.class);
        searchContext = mockController.getMock(SearchContext.class);
        queryContextConverter = mockController.getMock(QueryContextConverter.class);
        customFieldInputHelper = getMock(CustomFieldInputHelper.class);

        EasyMock.expect(customField.getName()).andStubReturn("ABC");
        EasyMock.expect(customFieldInputHelper.getUniqueClauseName(theUser, names.getPrimaryName(), "ABC")).andStubReturn(names.getPrimaryName());
    }

    @Test
    public void testGetParamsFromSearchRequestNotValid() throws Exception
    {
        final TerminalClauseImpl whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, "blah");
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(false, null);
        
        mockController.replay();
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }
        };
        
        assertNull(transformer.getParamsFromSearchRequest(null, query, searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestMultipleClauses() throws Exception
    {
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, "blah");
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause, whereClause).asList());

        mockController.replay();
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }
        };

        assertNull(transformer.getParamsFromSearchRequest(null, query, searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestUnsupportedOperators() throws Exception
    {
        final String name = "cf[100]";
        Query query1 = new QueryImpl(new TerminalClauseImpl(name, Operator.NOT_EQUALS, "blah"));
        Query query3 = new QueryImpl(new TerminalClauseImpl(name, Operator.NOT_IN, "blah"));
        Query query4 = new QueryImpl(new TerminalClauseImpl(name, Operator.IS_NOT, "blah"));
        Query query5 = new QueryImpl(new TerminalClauseImpl(name, Operator.LESS_THAN_EQUALS, "blah"));
        Query query6 = new QueryImpl(new TerminalClauseImpl(name, Operator.GREATER_THAN_EQUALS, "blah"));
        Query query7 = new QueryImpl(new TerminalClauseImpl(name, Operator.LIKE, "blah"));
        Query query8 = new QueryImpl(new TerminalClauseImpl(name, Operator.NOT_LIKE, "blah"));

        mockController.replay();
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        assertNull(transformer.getParamsFromSearchRequest(null, query1, searchContext));
        assertNull(transformer.getParamsFromSearchRequest(null, query3, searchContext));
        assertNull(transformer.getParamsFromSearchRequest(null, query4, searchContext));
        assertNull(transformer.getParamsFromSearchRequest(null, query5, searchContext));
        assertNull(transformer.getParamsFromSearchRequest(null, query6, searchContext));
        assertNull(transformer.getParamsFromSearchRequest(null, query7, searchContext));
        assertNull(transformer.getParamsFromSearchRequest(null, query8, searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestMultipleLiterals() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blah");
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        jqlOperandResolver.getValues(theUser, operand, whereClause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral("10"), createLiteral("10")).asList());

        mockController.replay();
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        assertNull(transformer.getParamsFromSearchRequest(theUser, query, searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestNullLiterals() throws Exception
    {
        final EmptyOperand operand = EmptyOperand.EMPTY;
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        Query searchQuery = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        jqlOperandResolver.getValues(theUser, operand, whereClause);
        mockController.setReturnValue(null);

        mockController.replay();
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        assertNull(transformer.getParamsFromSearchRequest(theUser, searchQuery, searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestEmptyLiteral() throws Exception
    {
        final EmptyOperand operand = EmptyOperand.EMPTY;
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        jqlOperandResolver.getValues(theUser, operand, whereClause);
        final QueryLiteral literal = new QueryLiteral();
        mockController.setReturnValue(CollectionBuilder.newBuilder(literal).asList());

        jqlCascadingSelectLiteralUtil.isNegativeLiteral(literal);
        mockController.setReturnValue(false);

        mockController.replay();
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        assertNull(transformer.getParamsFromSearchRequest(theUser, query, searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestMultipleOptions() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blah");
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        final QueryLiteral literal = createLiteral("10");
        final QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl());
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        jqlCascadingSelectLiteralUtil.isNegativeLiteral(literal);
        mockController.setReturnValue(false);

        jqlOperandResolver.getValues(theUser, operand, whereClause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(literal).asList());

        queryContextConverter.getQueryContext(searchContext);
        mockController.setReturnValue(queryContext);

        jqlSelectOptionsUtil.getOptions(customField, queryContext, literal, true);
        final Option option = new MockOption(null, null, null, null, null, null);
        mockController.setReturnValue(CollectionBuilder.newBuilder(option, option).asList());

        mockController.replay();
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        assertNull(transformer.getParamsFromSearchRequest(theUser, query, searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestNoOption() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blah");
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        final QueryLiteral literal = createLiteral("10");
        final QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl());
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        jqlCascadingSelectLiteralUtil.isNegativeLiteral(literal);
        mockController.setReturnValue(false);

        jqlOperandResolver.getValues(theUser, operand, whereClause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(literal).asList());

        queryContextConverter.getQueryContext(searchContext);
        mockController.setReturnValue(queryContext);

        jqlSelectOptionsUtil.getOptions(customField, queryContext, literal, true);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        mockController.replay();
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(theUser, query, searchContext);
        final CustomFieldParams expectedResult = new CustomFieldParamsImpl(customField);
        expectedResult.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton("10"));

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestParentOption() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blah");
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        final QueryLiteral literal = createLiteral("10");
        final QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl());
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        jqlCascadingSelectLiteralUtil.isNegativeLiteral(literal);
        mockController.setReturnValue(false);

        jqlOperandResolver.getValues(theUser, operand, whereClause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(literal).asList());

        queryContextConverter.getQueryContext(searchContext);
        mockController.setReturnValue(queryContext);

        jqlSelectOptionsUtil.getOptions(customField, queryContext, literal, true);
        final Option option = new MockOption(null, null, null, null, null, 20L);
        mockController.setReturnValue(CollectionBuilder.newBuilder(option).asList());

        mockController.replay();
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(theUser, query, searchContext);
        final CustomFieldParams expectedResult = new CustomFieldParamsImpl(customField);
        expectedResult.put(null, Collections.singleton(option.getOptionId().toString()));

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestChildOption() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("blah");
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        final QueryLiteral literal = createLiteral("10");
        final QueryContext queryContext = new QueryContextImpl(new ClauseContextImpl());
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        jqlCascadingSelectLiteralUtil.isNegativeLiteral(literal);
        mockController.setReturnValue(false);

        jqlOperandResolver.getValues(theUser, operand, whereClause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(literal).asList());

        queryContextConverter.getQueryContext(searchContext);
        mockController.setReturnValue(queryContext);

        jqlSelectOptionsUtil.getOptions(customField, queryContext, literal, true);
        final Option parentOption = new MockOption(null, null, null, null, null, 10L);
        final Option option = new MockOption(parentOption, null, null, null, null, 20L);
        mockController.setReturnValue(CollectionBuilder.newBuilder(option).asList());

        mockController.replay();
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        final CustomFieldParams result = transformer.getParamsFromSearchRequest(theUser, query, searchContext);
        final CustomFieldParams expectedResult = new CustomFieldParamsImpl(customField);
        expectedResult.put(null, Collections.singleton(parentOption.getOptionId().toString()));
        expectedResult.put("1", Collections.singleton(option.getOptionId().toString()));

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestFunctionNoValues() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand("blah");
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        jqlOperandResolver.getValues(theUser, operand, whereClause);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        mockController.replay();
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        assertNull(transformer.getParamsFromSearchRequest(theUser, query, searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestCascadeFunctionOneArg() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, "parent");
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        jqlOperandResolver.getValues(theUser, operand, whereClause);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        mockController.replay();
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        CustomFieldParamsImpl expectedResult = new CustomFieldParamsImpl(customField);
        expectedResult.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton("parent"));

        assertEquals(expectedResult, transformer.getParamsFromSearchRequest(theUser, query, searchContext));
        mockController.verify();
    }

    @Test
    public void testGetParamsFromSearchRequestCascadeFunctionTwoArgs() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, "parent", "child");
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        jqlOperandResolver.getValues(theUser, operand, whereClause);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        mockController.replay();
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        CustomFieldParamsImpl expectedResult = new CustomFieldParamsImpl(customField);
        expectedResult.put(CascadingSelectCFType.PARENT_KEY, Collections.singleton("parent"));
        expectedResult.put(CascadingSelectCFType.CHILD_KEY, Collections.singleton("child"));

        assertEquals(expectedResult, transformer.getParamsFromSearchRequest(theUser, query, searchContext));
        mockController.verify();
    }
    
    @Test
    public void testGetParamsFromSearchRequestCascadeFunctionNoArgs() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand(CascadeOptionFunction.FUNCTION_CASCADE_OPTION);
        final TerminalClause whereClause = new TerminalClauseImpl("blah", Operator.EQUALS, operand);
        Query query = new QueryImpl(whereClause);

        final SimpleNavigatorCollectorVisitor visitor = new MySimpleNavigatorCollectingVistor(true, CollectionBuilder.newBuilder(whereClause).asList());

        jqlOperandResolver.getValues(theUser, operand, whereClause);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        mockController.replay();
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor()
            {
                return visitor;
            }

        };

        assertNull(transformer.getParamsFromSearchRequest(theUser, query, searchContext));
        mockController.verify();
    }

    @Test
    public void testGetClauseFromParamsNoValues() throws Exception
    {
        final CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField);

        mockController.replay();

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        assertNull(transformer.getClauseFromParams(theUser, customFieldParams));
        
        mockController.verify();
    }

    @Test
    public void testGetClauseFromParamsParentSpecified() throws Exception
    {
        final CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField);
        customFieldParams.put(null, Collections.singleton("20"));
        final MockOption option = new MockOption(null, null, null, "parent", null, 20L);

        final TerminalClause expectedClause = new TerminalClauseImpl(names.getPrimaryName(), Operator.IN, new FunctionOperand(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, "20"));

        jqlSelectOptionsUtil.getOptionById(20L);
        mockController.setReturnValue(option);

        mockController.replay();

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        assertEquals(expectedClause, transformer.getClauseFromParams(theUser, customFieldParams));
        
        mockController.verify();
    }

    @Test
    public void testGetClauseFromParamsParentSpecifiedButDoesntExist() throws Exception
    {
        final CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField);
        customFieldParams.put(null, Collections.singleton("20"));

        jqlSelectOptionsUtil.getOptionById(20L);
        mockController.setReturnValue(null);

        mockController.replay();

        final TerminalClause expectedClause = new TerminalClauseImpl(names.getPrimaryName(), Operator.EQUALS, 20);

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        assertEquals(expectedClause, transformer.getClauseFromParams(theUser, customFieldParams));

        mockController.verify();
    }

    @Test
    public void testGetClauseFromParamsChildSpecified() throws Exception
    {
        final CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField);

        final MockOption parentOption = new MockOption(null, null, null, "parent", null, 20L);
        final MockOption childOption = new MockOption(parentOption, null, null, "child", null, 40L);
        parentOption.setChildOptions(Collections.singletonList(childOption));

        customFieldParams.put(null, Collections.singleton("20"));
        customFieldParams.put("1", Collections.singleton("40"));

        final TerminalClause expectedClause = new TerminalClauseImpl(names.getPrimaryName(), Operator.IN, new FunctionOperand(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, "20", "40"));

        jqlSelectOptionsUtil.getOptionById(40L);
        mockController.setReturnValue(childOption);

        mockController.replay();

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        assertEquals(expectedClause, transformer.getClauseFromParams(theUser, customFieldParams));

        mockController.verify();
    }

    @Test
    public void testGetClauseFromParamsChildSpecifiedNoParent() throws Exception
    {
        final CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField);

        final MockOption parentOption = new MockOption(null, null, null, "parent", null, 20L);
        final MockOption childOption = new MockOption(null, null, null, "child", null, 40L);
        parentOption.setChildOptions(Collections.singletonList(childOption));

        customFieldParams.put(null, Collections.singleton("20"));
        customFieldParams.put("1", Collections.singleton("40"));

        jqlSelectOptionsUtil.getOptionById(40L);
        mockController.setReturnValue(childOption);

        mockController.replay();

        final TerminalClause expectedClause = new TerminalClauseImpl(names.getPrimaryName(), Operator.EQUALS, 40);

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        assertEquals(expectedClause, transformer.getClauseFromParams(theUser, customFieldParams));

        mockController.verify();
    }

    @Test
    public void testGetClauseFromParamsParentInvalid() throws Exception
    {
        final CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField);
        customFieldParams.put(null, Collections.singleton("INVALID"));

        mockController.replay();

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        final TerminalClause expectedResult = new TerminalClauseImpl(names.getPrimaryName(), Operator.EQUALS, "INVALID");
        assertEquals(expectedResult, transformer.getClauseFromParams(theUser, customFieldParams));

        mockController.verify();
    }

    @Test
    public void testGetClauseFromParamsChildInvalid() throws Exception
    {
        final CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField);
        customFieldParams.put(null, Collections.singleton("INVALID"));
        customFieldParams.put("1", Collections.singleton("INVALID CHILD"));

        mockController.replay();

        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);

        final TerminalClause expectedResult = new TerminalClauseImpl(names.getPrimaryName(), Operator.EQUALS, "INVALID CHILD");
        assertEquals(expectedResult, transformer.getClauseFromParams(theUser, customFieldParams));

        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormTheyDo() throws Exception
    {
        mockController.replay();
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            protected CustomFieldParams getParamsFromSearchRequest(final User searcher, final Query query, final SearchContext searchContext)
            {
                return new CustomFieldParamsImpl();
            }
        };

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), searchContext));
        mockController.verify();
    }

    @Test
    public void testDoRelevantClausesFitFilterFormTheyDont() throws Exception
    {
        mockController.replay();
        final CascadingSelectCustomFieldSearchInputTransformer transformer = new CascadingSelectCustomFieldSearchInputTransformer(names, customField, url, selectConverter, jqlOperandResolver, jqlSelectOptionsUtil, jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper)
        {
            @Override
            protected CustomFieldParams getParamsFromSearchRequest(final User searcher, final Query query, final SearchContext searchContext)
            {
                return null;
            }
        };

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), searchContext));
        mockController.verify();
    }

    static class MySimpleNavigatorCollectingVistor extends SimpleNavigatorCollectorVisitor
    {
        private final boolean valid;
        private final List<TerminalClause> clauses;

        public MySimpleNavigatorCollectingVistor(boolean isValid, List<TerminalClause> clauses)
        {
            super("blah");
            valid = isValid;
            this.clauses = clauses;
        }

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

        @Override
        public Void visit(final AndClause andClause)
        {
            return super.visit(andClause);
        }

        @Override
        public Void visit(final NotClause notClause)
        {
            return super.visit(notClause);
        }

        @Override
        public Void visit(final OrClause orClause)
        {
            return super.visit(orClause);
        }

        @Override
        public Void visit(final TerminalClause terminalClause)
        {
            return super.visit(terminalClause);
        }
    }
}
