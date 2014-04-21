package com.atlassian.jira.jql.query;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlTimetrackingDurationSupport;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestAbstractTimeTrackingClauseQueryFactory extends MockControllerTestCase
{
    private MockJqlOperandResolver jqlOperandResolver;
    private JqlTimetrackingDurationSupport jqlTimetrackingDurationSupport;
    private ApplicationProperties applicationProperties;

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        jqlTimetrackingDurationSupport = mockController.getMock(JqlTimetrackingDurationSupport.class);
        applicationProperties = mockController.getMock(ApplicationProperties.class);
    }

    @Test
    public void testTimeTrackingDisabled() throws Exception
    {
        applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        mockController.setReturnValue(false);

        mockController.replay();
        final AbstractTimeTrackingClauseQueryFactory factory = new AbstractTimeTrackingClauseQueryFactory("field", jqlOperandResolver, jqlTimetrackingDurationSupport, applicationProperties);
        final QueryFactoryResult result = factory.getQuery(null, new TerminalClauseImpl("clause", Operator.EQUALS, "a"));
        assertEquals(QueryFactoryResult.createFalseResult(), result);
        mockController.verify();
    }

    @Test
    public void testTimeTrackingEnabled() throws Exception
    {
        final TerminalClauseImpl clause = new TerminalClauseImpl("clause", Operator.EQUALS, "a");

        applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        mockController.setReturnValue(true);

        final GenericClauseQueryFactory genericClauseQueryFactory = mockController.getMock(GenericClauseQueryFactory.class);
        genericClauseQueryFactory.getQuery(null, clause);
        mockController.setReturnValue(null);

        mockController.replay();
        final AbstractTimeTrackingClauseQueryFactory factory = new AbstractTimeTrackingClauseQueryFactory("field", jqlOperandResolver, jqlTimetrackingDurationSupport, applicationProperties)
        {
            @Override
            GenericClauseQueryFactory createGenericClauseQueryFactory(final String indexField, final JqlOperandResolver operandResolver, final List<OperatorSpecificQueryFactory> operatorFactories)
            {
                return genericClauseQueryFactory;
            }
        };

        factory.getQuery(null, clause);
        mockController.verify();
    }
}
