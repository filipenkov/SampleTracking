package com.atlassian.jira.jql.operand;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.plugin.jql.function.JqlFunction;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import java.util.Collections;
import java.util.List;

/**
 * @since v4.0
 */
public class TestFunctionOperandHandler extends ListeningTestCase
{
    private String fieldName = "field";
    private User theUser = null;
    private QueryCreationContext queryCreationContext = new QueryCreationContextImpl(theUser);

    @Test
    public void testConstructor() throws Exception
    {
        try
        {
            new FunctionOperandHandler(null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testIsFunction() throws Exception
    {
        final JqlFunction jqlFunction = createMock(JqlFunction.class);

        replay(jqlFunction);

        final FunctionOperandHandler operandHandler = new FunctionOperandHandler(jqlFunction);
        assertTrue(operandHandler.isFunction());

        verify(jqlFunction);
    }

    @Test
    public void testIsEmpty() throws Exception
    {
        final JqlFunction jqlFunction = createMock(JqlFunction.class);

        replay(jqlFunction);

        final FunctionOperandHandler operandHandler = new FunctionOperandHandler(jqlFunction);
        assertFalse(operandHandler.isEmpty());

        verify(jqlFunction);
    }

    @Test
    public void testGetValues() throws Exception
    {
        final JqlFunction jqlFunction = createMock(JqlFunction.class);

        final FunctionOperand operand = new FunctionOperand("blah");
        final TerminalClause terminalClause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        final List<QueryLiteral> values = Collections.emptyList();
        expect(jqlFunction.getValues(queryCreationContext, operand, terminalClause)).andReturn(values);

        replay(jqlFunction);

        final FunctionOperandHandler operandHandler = new FunctionOperandHandler(jqlFunction);
        assertEquals(values, operandHandler.getValues(queryCreationContext, operand, terminalClause));

        verify(jqlFunction);
    }

    @Test
    public void testValidate() throws Exception
    {
        final JqlFunction jqlFunction = createMock(JqlFunction.class);

        final FunctionOperand operand = new FunctionOperand("blah");
        final TerminalClause terminalClause = new TerminalClauseImpl(fieldName, Operator.EQUALS, operand);

        final MessageSetImpl expectedMessages = new MessageSetImpl();
        expect(jqlFunction.validate(null, operand, terminalClause)).andReturn(expectedMessages);

        replay(jqlFunction);

        final FunctionOperandHandler operandHandler = new FunctionOperandHandler(jqlFunction);
        assertEquals(expectedMessages, operandHandler.validate(null, operand, terminalClause));

        verify(jqlFunction);
    }

    @Test
    public void testIsList() throws Exception
    {
        final JqlFunction jqlFunction = createMock(JqlFunction.class);

        expect(jqlFunction.isList()).andReturn(false);

        replay(jqlFunction);

        final FunctionOperandHandler operandHandler = new FunctionOperandHandler(jqlFunction);
        assertFalse(operandHandler.isList());

        verify(jqlFunction);
    }

    @Test
    public void testGetJqlFunction() throws Exception
    {
        final JqlFunction jqlFunction = createMock(JqlFunction.class);

        replay(jqlFunction);

        final FunctionOperandHandler operandHandler = new FunctionOperandHandler(jqlFunction);
        assertSame(jqlFunction, operandHandler.getJqlFunction());

        verify(jqlFunction);
    }
}
