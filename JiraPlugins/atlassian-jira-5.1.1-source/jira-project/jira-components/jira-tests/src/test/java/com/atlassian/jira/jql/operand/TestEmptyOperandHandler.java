package com.atlassian.jira.jql.operand;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operator.Operator;

import java.util.List;

/**
 * @since v4.0
 */
public class TestEmptyOperandHandler extends ListeningTestCase
{
    private User theUser = null;
    private QueryCreationContext queryCreationContext = new QueryCreationContextImpl(theUser);

    @Test
    public void testValidate() throws Exception
    {
        TerminalClause terminalClause = new TerminalClauseImpl("bah", Operator.EQUALS, new EmptyOperand());
        final MessageSet messageSet = new EmptyOperandHandler().validate(theUser, new EmptyOperand(), terminalClause);
        assertNotNull(messageSet);
        assertFalse(messageSet.hasAnyMessages());
    }

    @Test
    public void testGetValues() throws Exception
    {
        TerminalClause terminalClause = new TerminalClauseImpl("bah", Operator.EQUALS, new EmptyOperand());
        final List<QueryLiteral> values = new EmptyOperandHandler().getValues(queryCreationContext, new EmptyOperand(), terminalClause);
        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals(new QueryLiteral(), values.get(0));
    }

    @Test
    public void testIsList() throws Exception
    {
        assertFalse(new EmptyOperandHandler().isList());
    }

    @Test
    public void testIsEmpty() throws Exception
    {
        assertTrue(new EmptyOperandHandler().isEmpty());
    }

    @Test
    public void testIsFunction() throws Exception
    {
        assertFalse(new EmptyOperandHandler().isFunction());
    }
}
