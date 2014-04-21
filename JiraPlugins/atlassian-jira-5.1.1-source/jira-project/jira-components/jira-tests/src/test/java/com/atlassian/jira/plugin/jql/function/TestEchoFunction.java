package com.atlassian.jira.plugin.jql.function;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test for {@link com.atlassian.jira.plugin.jql.function.EchoFunction}.
 *
 * @since v4.0
 */
public class TestEchoFunction extends ListeningTestCase
{
    @Test
    public void testValidate()
    {
        final EchoFunction function = new EchoFunction();
        final MessageSet messageSet = function.validate(null, new FunctionOperand(EchoFunction.ECHO_FUNCTION), new TerminalClauseImpl("what", "sss"));
        assertNotNull(messageSet);
        assertFalse(messageSet.hasAnyMessages());
    }

    @Test
    public void testDataType() throws Exception
    {
        final EchoFunction function = new EchoFunction();
        assertEquals(JiraDataTypes.ALL, function.getDataType());        
    }

    @Test
    public void testGetValues()
    {
        final EchoFunction function = new EchoFunction();
        List<QueryLiteral> actualList = function.getValues(null, new FunctionOperand(EchoFunction.ECHO_FUNCTION), null);
        assertEquals(Collections.<QueryLiteral>emptyList(), actualList);

        actualList = function.getValues(null, new FunctionOperand(EchoFunction.ECHO_FUNCTION, "one"), null);
        assertEquals(Collections.singletonList(createLiteral("one")), actualList);

        actualList = function.getValues(null, new FunctionOperand(EchoFunction.ECHO_FUNCTION, "one", "two"), null);
        assertEquals(Arrays.asList(createLiteral("one"), createLiteral("two")), actualList);
    }

    @Test
    public void testEmpty() throws Exception
    {
        final EchoFunction function = new EchoFunction();
        List<QueryLiteral> actualList = function.getValues(null, new FunctionOperand(EchoFunction.ECHO_FUNCTION), null);
        assertEquals(Collections.<QueryLiteral>emptyList(), actualList);

        actualList = function.getValues(null, new FunctionOperand(EchoFunction.ECHO_FUNCTION, "none"), null);
        assertEquals(Collections.singletonList(new QueryLiteral()), actualList);

        actualList = function.getValues(null, new FunctionOperand(EchoFunction.ECHO_FUNCTION, "none", "\"none\""), null);
        assertEquals(Arrays.asList(new QueryLiteral(), createLiteral("none")), actualList);
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments()
    {
        final EchoFunction function = new EchoFunction();
        assertEquals(0, function.getMinimumNumberOfExpectedArguments());
    }

}
