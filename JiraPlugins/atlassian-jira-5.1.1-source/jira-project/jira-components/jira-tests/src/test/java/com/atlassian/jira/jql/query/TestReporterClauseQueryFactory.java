package com.atlassian.jira.jql.query;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.jql.resolver.UserResolverImpl;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;

/**
 * @since v4.0
 */
public class TestReporterClauseQueryFactory extends MockControllerTestCase
{
    @Test
    public void testUnsupportedOperators() throws Exception
    {
        final Operator[] invalidOperators = { Operator.GREATER_THAN, Operator.GREATER_THAN_EQUALS, Operator.LESS_THAN, Operator.LESS_THAN_EQUALS, Operator.LIKE };

        final SingleValueOperand singleValueOperand = new SingleValueOperand("testOperand");

        for (Operator invalidOperator : invalidOperators)
        {
            final UserPickerSearchService userPicker = mockController.getMock(UserPickerSearchService.class);
            UserResolver userResolver = new UserResolverImpl(userPicker);
            mockController.replay();

            ReporterClauseQueryFactory reporterClauseQueryFactory = new ReporterClauseQueryFactory(userResolver, MockJqlOperandResolver.createSimpleSupport());

            TerminalClause terminalClause = new TerminalClauseImpl("reporter", invalidOperator, singleValueOperand);

            final QueryFactoryResult result = reporterClauseQueryFactory.getQuery(null, terminalClause);
            assertEquals(QueryFactoryResult.createFalseResult(), result);
            mockController.verify();
            mockController.reset();
        }

        mockController.replay();
    }

}
