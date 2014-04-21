package com.atlassian.jira.jql.context;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlCascadingSelectLiteralUtil;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.jql.validator.AlwaysValidOperatorUsageValidator;
import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @since v4.0
 */
public class TestCascadingSelectCustomFieldClauseContextFactory extends MockControllerTestCase
{
    private CustomField customField;
    private JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil;
    private JqlOperandResolver jqlOperandResolver;
    private ContextSetUtil contextSetUtil;
    private JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil;
    private OperatorUsageValidator operatorUsageValidator;
    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        customField = mockController.getMock(CustomField.class);
        jqlSelectOptionsUtil = mockController.getMock(JqlSelectOptionsUtil.class);
        fieldConfigSchemeClauseContextUtil = mockController.getMock(FieldConfigSchemeClauseContextUtil.class);
        jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        contextSetUtil = mockController.getMock(ContextSetUtil.class);
        jqlCascadingSelectLiteralUtil = mockController.getMock(JqlCascadingSelectLiteralUtil.class);
        operatorUsageValidator = mockController.addObjectInstance(new AlwaysValidOperatorUsageValidator());

    }

    @After
    public void tearDown() throws Exception
    {
        customField = null;
        jqlSelectOptionsUtil = null;
        fieldConfigSchemeClauseContextUtil = null;
        jqlOperandResolver = null;
        contextSetUtil = null;
        jqlCascadingSelectLiteralUtil = null;
        operatorUsageValidator = null;

    }

    @Test
    public void testNoSchemes() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");

        expect(customField.getConfigurationSchemes()).andReturn(Collections.<FieldConfigScheme>emptyList());
        replay();

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator);
        final ClauseContext clauseContext = factory.getClauseContext(theUser, clause);

        assertEquals(ClauseContextImpl.createGlobalClauseContext(), clauseContext);

        verify();
    }

    @Test
    public void testNullSchemes() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");

        expect(customField.getConfigurationSchemes()).andReturn(null);
        replay();

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator);
        final ClauseContext clauseContext = factory.getClauseContext(theUser, clause);

        assertEquals(ClauseContextImpl.createGlobalClauseContext(), clauseContext);

        verify();
    }

    @Test
    public void testBadOperator() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.LIKE, "fine");
        final ClauseContext context1 = createContextForProjects(1, 2);
        final ClauseContext context2 = createContextForProjects(5);
        final ClauseContext context3 = createContextForProjects(1292);

        final FieldConfigScheme scheme = getMock(FieldConfigScheme.class);
        expect(scheme.isGlobal()).andReturn(false).anyTimes();

        expect(customField.getConfigurationSchemes()).andReturn(Arrays.asList(scheme, scheme));

        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(context1);
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(context2);

        expect(contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asSet())).andReturn(context3);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator);
        
        replay();
        assertEquals(context3, factory.getClauseContext(theUser, testClause));
        verify();
    }

    @Test
    public void testBadOperatorUsage() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");
        final ClauseContext context1 = createContextForProjects(1, 2);
        final ClauseContext context2 = createContextForProjects(5);
        final ClauseContext context3 = createContextForProjects(1292);

        final FieldConfigScheme scheme = getMock(FieldConfigScheme.class);
        expect(scheme.isGlobal()).andReturn(false).anyTimes();

        expect(customField.getConfigurationSchemes()).andReturn(Arrays.asList(scheme, scheme));

        operatorUsageValidator = getMock(OperatorUsageValidator.class);
        expect(operatorUsageValidator.check(theUser, testClause)).andReturn(false);

        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(context1);
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(context2);

        expect(contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asSet())).andReturn(context3);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator);

        replay();
        assertEquals(context3, factory.getClauseContext(theUser, testClause));
        verify();
    }

    @Test
    public void testPostiveQueryWithPositiveOptions() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");
        final MockOption option1 = new MockOption(null, null, null, null, null, 25L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final ClauseContext context1 = createContextForProjects(1, 2);

        final FieldConfigScheme scheme = getMock(FieldConfigScheme.class);
        expect(scheme.isGlobal()).andReturn(false).anyTimes();

        expect(customField.getConfigurationSchemes()).andReturn(Arrays.asList(scheme, scheme));

        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option1, option2));
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(context1);

        //This second scheme should not be included because it has no options.
        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList());

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {

                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                positiveOption.add(option1);
            }
        };

        replay();
        assertEquals(context1, factory.getClauseContext(theUser, testClause));
        verify();
    }

    @Test
    public void testPositiveQueryWithPositiveAndNegativeOptions() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");
        final MockOption option1 = new MockOption(null, null, null, null, null, 25L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final MockOption option2child2 = new MockOption(option2, null, null, null, null, 28L);
        final MockOption option2child1 = new MockOption(option2, null, null, null, null, 27L);
        option2.setChildOptions(Arrays.asList(option2child1, option2child2));

        final ClauseContext context1 = createContextForProjects(1, 56);

        final FieldConfigScheme scheme = getMock(FieldConfigScheme.class);
        expect(scheme.isGlobal()).andReturn(false).anyTimes();

        expect(customField.getConfigurationSchemes()).andReturn(Arrays.asList(scheme, scheme));

        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option1, option2));
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(context1);

        //This second scheme because it has no options in the scheme not in the exclude list.
        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option2, option2child1, option2child2));

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {
                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                positiveOption.add(option1);
                negativeOption.add(option2);
            }
        };

        replay();
        assertEquals(context1, factory.getClauseContext(theUser, testClause));
        verify();
    }

    @Test
    public void testPositiveQueryWithPositiveAndEmpty() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");
        final ClauseContext context1 = createContextForProjects(1, 56);

        final FieldConfigScheme scheme = getMock(FieldConfigScheme.class);

        expect(customField.getConfigurationSchemes()).andReturn(Arrays.asList(scheme, scheme));

        expect(scheme.isGlobal()).andReturn(false);
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(context1);

        expect(scheme.isGlobal()).andReturn(true);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {
                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                positiveOption.add(null);
            }
        };

        replay();
        assertEquals(ClauseContextImpl.createGlobalClauseContext(), factory.getClauseContext(theUser, testClause));
        verify();
    }

    @Test
    public void testPositiveQueryWithNoPositive() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");
        final MockOption option1 = new MockOption(null, null, null, null, null, 25L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final MockOption option2child2 = new MockOption(option2, null, null, null, null, 28L);
        final MockOption option2child1 = new MockOption(option2, null, null, null, null, 27L);
        option2.setChildOptions(Arrays.asList(option2child1, option2child2));

        final FieldConfigScheme scheme = getMock(FieldConfigScheme.class);

        expect(customField.getConfigurationSchemes()).andReturn(Arrays.asList(scheme, scheme));
        expect(scheme.isGlobal()).andReturn(true);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {
                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                negativeOption.add(option2);
                negativeOption.add(option1);
            }
        };

        replay();
        assertEquals(ClauseContextImpl.createGlobalClauseContext(), factory.getClauseContext(theUser, testClause));
        verify();
    }

    @Test
    public void testNegativeQueryWithPositiveOptions() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.NOT_EQUALS, "fine");
        final MockOption option1 = new MockOption(null, null, null, null, null, 25L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final ClauseContext context1 = createContextForProjects(1, 2);

        final FieldConfigScheme scheme = getMock(FieldConfigScheme.class);
        expect(scheme.isGlobal()).andReturn(false).anyTimes();

        expect(customField.getConfigurationSchemes()).andReturn(Arrays.asList(scheme, scheme));

        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option1, option2));
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(context1);

        //This second scheme should not be included because it only has options that we are to exclude.
        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option1));

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {

                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                positiveOption.add(option1);
            }
        };

        replay();
        assertEquals(context1, factory.getClauseContext(theUser, testClause));
        verify();
    }

    @Test
    public void testNegativeQueryWithPositiveAndNegativeOptions() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.NOT_EQUALS, "fine");
        final MockOption option1 = new MockOption(null, null, null, null, null, 25L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final MockOption option2child2 = new MockOption(option2, null, null, null, null, 28L);
        final MockOption option2child1 = new MockOption(option2, null, null, null, null, 27L);
        option2.setChildOptions(Arrays.asList(option2child1, option2child2));

        final ClauseContext context1 = createContextForProjects(1, 2);
        final ClauseContext context2 = createContextForProjects(3);
        final ClauseContext context3 = createContextForProjects(3);

        final FieldConfigScheme scheme = getMock(FieldConfigScheme.class);
        expect(scheme.isGlobal()).andReturn(false).anyTimes();

        expect(customField.getConfigurationSchemes()).andReturn(Arrays.asList(scheme, scheme));

        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option1, option2));
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(context1);

        //This second scheme should not be included because it only has options that we are to exclude.
        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option2, option2child1, option2child2));
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(context2);

        expect(contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asSet())).andReturn(context3);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {

                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                positiveOption.add(option2);
                negativeOption.add(option2child1);
            }
        };

        replay();
        assertEquals(context3, factory.getClauseContext(theUser, testClause));
        verify();
    }

    @Test
    public void testNegativeQueryWithNegativeOptions() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.NOT_IN, "fine");
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final MockOption option2child2 = new MockOption(option2, null, null, null, null, 28L);
        final MockOption option2child1 = new MockOption(option2, null, null, null, null, 27L);
        option2.setChildOptions(Arrays.asList(option2child1, option2child2));

        final ClauseContext context2 = createContextForProjects(3);

        final FieldConfigScheme scheme = getMock(FieldConfigScheme.class);
        expect(scheme.isGlobal()).andReturn(false).anyTimes();

        expect(customField.getConfigurationSchemes()).andReturn(Arrays.asList(scheme, scheme));

        //Will not be included because here are no options.
        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Collections.<Option>emptyList());

        //This second scheme should not be included because it only has options that we are to exclude.
        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option2, option2child1, option2child2));
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(context2);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {
                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                negativeOption.add(option2child1);
            }
        };

        replay();
        assertEquals(context2, factory.getClauseContext(theUser, testClause));
        verify();
    }

    @Test
    public void testNegativeQueryWithNoOptions() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.NOT_IN, "fine");
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final MockOption option2child2 = new MockOption(option2, null, null, null, null, 28L);
        final MockOption option2child1 = new MockOption(option2, null, null, null, null, 27L);
        option2.setChildOptions(Arrays.asList(option2child1, option2child2));

        final ClauseContext context2 = createContextForProjects(3);
        final ClauseContext context3 = createContextForProjects(64);
        final ClauseContext context4 = createContextForProjects(34938);

        final FieldConfigScheme scheme = getMock(FieldConfigScheme.class);
        expect(scheme.isGlobal()).andReturn(false).anyTimes();

        expect(customField.getConfigurationSchemes()).andReturn(Arrays.asList(scheme, scheme));

        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(context3);
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(context2);
        expect(contextSetUtil.union(CollectionBuilder.newBuilder(context2, context3).asSet())).andReturn(context4);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {
                assertEquals(theUser, user);
                assertEquals(testClause, clause);
            }
        };

        replay();
        assertEquals(context4, factory.getClauseContext(theUser, testClause));
        verify();
    }

    @Test
    public void testGlobalSchemeConfig() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.LIKE, "fine");
        final ClauseContext context3 = createContextForProjects(64);

        final FieldConfigScheme scheme = getMock(FieldConfigScheme.class);

        expect(customField.getConfigurationSchemes()).andReturn(Arrays.asList(scheme, scheme, scheme));

        expect(scheme.isGlobal()).andReturn(false);
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(context3);

        expect(scheme.isGlobal()).andReturn(true);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator);

        replay();
        assertEquals(ClauseContextImpl.createGlobalClauseContext(), factory.getClauseContext(theUser, testClause));
        verify();
    }

    @Test
    public void testNoGeneratedContext() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");
        final MockOption option1 = new MockOption(null, null, null, null, null, 25L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final ClauseContext context1 = createContextForProjects(1, 2);
        final ClauseContext context2 = createContextForProjects(5);

        final FieldConfigScheme scheme = getMock(FieldConfigScheme.class);
        expect(scheme.isGlobal()).andReturn(false).anyTimes();

        expect(customField.getConfigurationSchemes()).andReturn(Arrays.asList(scheme, scheme));

        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option1, option2));
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(context1);

        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option1, option2));
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(theUser, scheme)).andReturn(context2);

        expect(contextSetUtil.union(CollectionBuilder.newBuilder(context1, context2).asSet())).andReturn(new ClauseContextImpl());

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {

                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                positiveOption.add(option1);
            }
        };

        replay();
        assertEquals(ClauseContextImpl.createGlobalClauseContext(), factory.getClauseContext(theUser, testClause));
        verify();        
    }

    @Test
    public void testNoApplicableConfigurations() throws Exception
    {
        final TerminalClause testClause = new TerminalClauseImpl("one", Operator.EQUALS, "fine");
        final MockOption option1 = new MockOption(null, null, null, null, null, 25L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);

        final FieldConfigScheme scheme = getMock(FieldConfigScheme.class);
        expect(scheme.isGlobal()).andReturn(false).anyTimes();

        expect(customField.getConfigurationSchemes()).andReturn(Arrays.asList(scheme));
        expect(jqlSelectOptionsUtil.getOptionsForScheme(scheme)).andReturn(Arrays.<Option>asList(option2));

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator)
        {
            @Override
            void fillOptions(final User user, final TerminalClause clause, final Set<Option> positiveOption, final Set<Option> negativeOption)
            {

                assertEquals(theUser, user);
                assertEquals(testClause, clause);

                positiveOption.add(option1);
            }
        };

        replay();
        assertEquals(ClauseContextImpl.createGlobalClauseContext(), factory.getClauseContext(theUser, testClause));
        verify();
    }

    @Test
    public void testFillOptions() throws Exception
    {
        final Operand operand = new SingleValueOperand("one");
        final TerminalClause testClause = new TerminalClauseImpl("test", Operator.EQUALS, operand);
        final QueryLiteral literal1 = new QueryLiteral(operand, 1L);
        final QueryLiteral literal2 = new QueryLiteral(operand, 2L);
        final QueryLiteral literal3 = new QueryLiteral(operand, 3L);
        final MockOption option1 = new MockOption(null, null, null, null, null, 25L);
        final MockOption option2 = new MockOption(null, null, null, null, null, 26L);
        final MockOption option3 = new MockOption(null, null, null, null, null, 242L);
        final MockOption option4 = new MockOption(null, null, null, null, null, 27L);

        final List<QueryLiteral> testLiterals = Arrays.asList(literal1, literal2, literal3);

        expect(jqlOperandResolver.getValues(theUser, operand, testClause)).andReturn(testLiterals);

        jqlCascadingSelectLiteralUtil = new JqlCascadingSelectLiteralUtil(jqlSelectOptionsUtil)
        {
            @Override
            public void processPositiveNegativeOptionLiterals(final List<QueryLiteral> inputLiterals, final List<QueryLiteral> positiveLiterals, final List<QueryLiteral> negativeLiterals)
            {
                assertEquals(testLiterals, inputLiterals);
                positiveLiterals.add(literal1);
                positiveLiterals.add(literal2);
                negativeLiterals.add(literal3);
            }
        };

        expect(jqlSelectOptionsUtil.getOptions(customField, literal1, true)).andReturn(Collections.<Option>singletonList(option1));
        expect(jqlSelectOptionsUtil.getOptions(customField, literal2, true)).andReturn(Arrays.<Option>asList(option2, option3));
        expect(jqlSelectOptionsUtil.getOptions(customField, literal3, true)).andReturn(Arrays.<Option>asList(option4));

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator);

        Set<Option> posOpts = new HashSet<Option>();
        Set<Option> negOpts = new HashSet<Option>();

        replay();
        factory.fillOptions(theUser, testClause, posOpts, negOpts);

        assertEquals(CollectionBuilder.<Option>newBuilder(option1, option2, option3).asSet(), posOpts);
        assertEquals(CollectionBuilder.<Option>newBuilder(option4).asSet(), negOpts);

        verify();
    }

    @Test
    public void testFillOptionsNullLiterals() throws Exception
    {
        final Operand operand = new SingleValueOperand("one");
        final TerminalClause testClause = new TerminalClauseImpl("test", Operator.EQUALS, operand);

        expect(jqlOperandResolver.getValues(theUser, operand, testClause)).andReturn(null);

        final CascadingSelectCustomFieldClauseContextFactory factory = new CascadingSelectCustomFieldClauseContextFactory(customField, contextSetUtil, jqlSelectOptionsUtil,
                fieldConfigSchemeClauseContextUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil, operatorUsageValidator);

        Set<Option> posOpts = new HashSet<Option>();
        Set<Option> negOpts = new HashSet<Option>();

        replay();
        factory.fillOptions(theUser, testClause, posOpts, negOpts);

        assertTrue(posOpts.isEmpty());
        assertTrue(negOpts.isEmpty());

        verify();
    }

    private ClauseContext createContextForProjects(int...projects)
    {
        Set<ProjectIssueTypeContext> ctxs = new HashSet<ProjectIssueTypeContext>();
        for (int project : projects)
        {
            ctxs.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl((long)project), AllIssueTypesContext.getInstance()));
        }
        return new ClauseContextImpl(ctxs);
    }
}
