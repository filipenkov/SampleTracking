package com.atlassian.jira.jql.query;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlDurationSupport;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import java.util.List;

/**
 * @since v4.0
 */
public class TestAbstractTimeTrackingClauseQueryFactory extends MockControllerTestCase
{
    private MockJqlOperandResolver jqlOperandResolver;
    private JqlDurationSupport jqlDurationSupport;
    private ApplicationProperties applicationProperties;

    @Before
    public void setUp() throws Exception
    {
        jqlOperandResolver = MockJqlOperandResolver.createSimpleSupport();
        jqlDurationSupport = mockController.getMock(JqlDurationSupport.class);
        applicationProperties = mockController.getMock(ApplicationProperties.class);
    }

    @Test
    public void testTimeTrackingDisabled() throws Exception
    {
        applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        mockController.setReturnValue(false);

        mockController.replay();
        final AbstractTimeTrackingClauseQueryFactory factory = new AbstractTimeTrackingClauseQueryFactory("field", jqlOperandResolver, jqlDurationSupport, applicationProperties);
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
        final AbstractTimeTrackingClauseQueryFactory factory = new AbstractTimeTrackingClauseQueryFactory("field", jqlOperandResolver, jqlDurationSupport, applicationProperties)
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
