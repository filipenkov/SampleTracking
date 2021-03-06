package com.atlassian.jira.jql.operand;

import com.atlassian.jira.issue.index.ChangeHistoryFieldConfigurationManager;
import com.atlassian.query.clause.WasClause;
import com.atlassian.query.clause.WasClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operator.Operator;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @since v4.4
 */
public class TestEmptyWasClauseOperandHandler
{
    @Test
    public void testGetValues()
    {
        _assertEmptyClause("Status", "-1");
        _assertEmptyClause("reporter", "issue_no_reporter");
        _assertEmptyClause("assignee", "unassigned");
        _assertEmptyClause("fixVersion", "-1");
        _assertEmptyClause("priority", "-1");
        _assertEmptyClause("resolution", "-1");
    }

    private void _assertEmptyClause(final String field, final String value)
    {
        final Operand emptyOperand =  new EmptyOperand();
        WasClause wasClause = new WasClauseImpl(field, Operator.WAS, emptyOperand, null);
        final List<QueryLiteral> values = new EmptyWasClauseOperandHandler(new ChangeHistoryFieldConfigurationManager()).getEmptyValue(wasClause);
        assertNotNull(values);
        assertEquals(1, values.size());
        if (value == null)
        {
            assertEquals(new QueryLiteral(emptyOperand), values.get(0));
        }
        else
        {
            assertEquals(new QueryLiteral(emptyOperand, value), values.get(0));
        }
    }
}
