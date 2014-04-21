package com.atlassian.jira.jql.query;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operator.Operator;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

/**
 * Test for {@link com.atlassian.jira.jql.query.ValidatingDecoratorQueryFactory}.
 *
 * @since v4.0
 */
public class TestValidatingDecoratorQueryFactory extends MockControllerTestCase
{
    @Test
    public void testValidQuery() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("5757", Operator.LIKE, new MultiValueOperand(19L, 282L, 4L));
        final QueryFactoryResult delegateResult = new QueryFactoryResult(new TermQuery(new Term("a", "b")));
        final QueryCreationContext creationContext = new QueryCreationContextImpl(null);

        final OperatorUsageValidator usageValidator = getMock(OperatorUsageValidator.class);
        expect(usageValidator.check(null, clause)).andReturn(true);

        final ClauseQueryFactory delegate = getMock(ClauseQueryFactory.class);
        expect(delegate.getQuery(creationContext, clause)).andReturn(delegateResult);
    
        final ClauseQueryFactory queryFactory = instantiate(ValidatingDecoratorQueryFactory.class);

        assertEquals(delegateResult, queryFactory.getQuery(creationContext, clause));

        verify();
    }

    @Test
    public void testInvalidQuery() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("5757", Operator.LIKE, new MultiValueOperand(19L, 282L, 4L));
        final QueryCreationContext creationContext = new QueryCreationContextImpl(null);

        final OperatorUsageValidator usageValidator = getMock(OperatorUsageValidator.class);
        expect(usageValidator.check(null, clause)).andReturn(false);

        final ClauseQueryFactory queryFactory = instantiate(ValidatingDecoratorQueryFactory.class);

        assertEquals(QueryFactoryResult.createFalseResult(), queryFactory.getQuery(creationContext, clause));

        verify();
    }
}
