package com.atlassian.jira.plugin.jql.function;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.3
 */
public abstract class AbstractDateFunctionTestCase extends MockControllerTestCase
{
    private TerminalClause terminalClause = null;

    protected TimeZoneManager timeZoneManager;

    @Before
    public void setUp()
    {
        timeZoneManager = createMock(TimeZoneManager.class);
    }

	@Test
	public void testValidateTooManyArguments() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(TimeZone.getDefault()).anyTimes();
        replay(timeZoneManager);
        AbstractDateFunction dateFunction = getInstanceToTest();
        
        FunctionOperand function = new FunctionOperand(getFunctionName(), Arrays.asList("1d"));
        MessageSet messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Collections.<String>emptyList());
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("should", "not", "be", "here"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertTrue(messageSet.hasAnyMessages());
        assertTrue(messageSet.hasAnyErrors());
        assertFalse(messageSet.hasAnyWarnings());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function '" + getFunctionName() + "' expected between '0' and '1' arguments but received '4'.", messageSet.getErrorMessages().iterator().next());
    }

	@Test
	public void testValidateIncrements() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(TimeZone.getDefault()).anyTimes();
        replay(timeZoneManager);
        AbstractDateFunction dateFunction = getInstanceToTest();
        
        FunctionOperand function = new FunctionOperand(getFunctionName(), Arrays.asList("1d"));
        MessageSet messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("-1d"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("+1"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("1w"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("1m"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("1M"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("1y"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("-3w"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("+3d"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("-2778M"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertFalse(messageSet.hasAnyMessages());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("-6q"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertTrue(messageSet.hasAnyMessages());
        assertTrue(messageSet.hasAnyErrors());
        assertFalse(messageSet.hasAnyWarnings());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Duration for function '" + getFunctionName() + "' should have the format (+/-)n(yMwdm), e.g -1M for 1 month earlier.", messageSet.getErrorMessages().iterator().next());

        function = new FunctionOperand(getFunctionName(), Arrays.asList("should", "not", "be", "here"));
        messageSet = dateFunction.validate(null, function, terminalClause);
        assertTrue(messageSet.hasAnyMessages());
        assertTrue(messageSet.hasAnyErrors());
        assertFalse(messageSet.hasAnyWarnings());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function '" + getFunctionName() + "' expected between '0' and '1' arguments but received '4'.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testDataType() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(TimeZone.getDefault()).anyTimes();
        replay(timeZoneManager);
        NowFunction handler = new NowFunction(timeZoneManager);
        assertEquals(JiraDataTypes.DATE, handler.getDataType());        
    }

    @Test
    public abstract void testGetValues() throws Exception;

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(TimeZone.getDefault()).anyTimes();
        replay(timeZoneManager);
        AbstractDateFunction dateFunction = getInstanceToTest();
        assertEquals(0, dateFunction.getMinimumNumberOfExpectedArguments());
    }
    
    abstract String getFunctionName();
    abstract AbstractDateFunction getInstanceToTest();
    abstract AbstractDateFunction getInstanceToTest(Clock aClock);

	public void doTest(Calendar expected, Calendar test, TimeZone systemTimeZone) throws Exception
    {   
        // Test with a known date
        Clock aClock = new ConstantClock(test.getTime());
        
        FunctionOperand function = new FunctionOperand(getFunctionName(), Collections.<String>emptyList());
        final List<QueryLiteral> value = getInstanceToTest(aClock).getValues(null, function, terminalClause);
        assertNotNull(value);
        assertEquals(1, value.size());
        
        GregorianCalendar cal = new GregorianCalendar(systemTimeZone);
        cal.setTimeInMillis(value.get(0).getLongValue().longValue());
        assertEquals(expected.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals(expected.get(Calendar.MONTH), cal.get(Calendar.MONTH));
        assertEquals(expected.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(expected.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(expected.get(Calendar.MINUTE), cal.get(Calendar.MINUTE));
        assertEquals(expected.get(Calendar.SECOND), cal.get(Calendar.SECOND));
    }

	public void doTestwithIncrement(String increment, Calendar expected, Calendar test)
                    throws Exception
    {   
        // Test with a known date
        Clock aClock = new ConstantClock(test.getTime());
        
        FunctionOperand function = new FunctionOperand(getFunctionName(), Arrays.asList(increment));
        final List<QueryLiteral> value = getInstanceToTest(aClock).getValues(null, function, terminalClause);
        assertNotNull(value);
        assertEquals(1, value.size());
        
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(value.get(0).getLongValue().longValue());
        assertEquals(expected.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals(expected.get(Calendar.MONTH), cal.get(Calendar.MONTH));
        assertEquals(expected.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(expected.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(expected.get(Calendar.MINUTE), cal.get(Calendar.MINUTE));
        assertEquals(expected.get(Calendar.SECOND), cal.get(Calendar.SECOND));
    }

}
