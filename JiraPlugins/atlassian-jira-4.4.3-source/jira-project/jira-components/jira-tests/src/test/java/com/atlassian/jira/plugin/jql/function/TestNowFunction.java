package com.atlassian.jira.plugin.jql.function;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestNowFunction extends MockControllerTestCase
{
    private TerminalClause terminalClause = null;
    private TimeZoneManager timeZoneManager;

    @Before
    public void setUp() throws Exception
    {
        timeZoneManager = createMock(TimeZoneManager.class);
    }

    @Test
    public void testValidateTooManyArguments() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(TimeZone.getDefault()).anyTimes();
        replay(timeZoneManager);
        NowFunction nowFunction = new NowFunction(timeZoneManager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };
        FunctionOperand function = new FunctionOperand(NowFunction.FUNCTION_NOW, Arrays.asList("should", "not", "be", "here"));
        final MessageSet messageSet = nowFunction.validate(null, function, terminalClause);
        assertTrue(messageSet.hasAnyMessages());
        assertTrue(messageSet.hasAnyErrors());
        assertFalse(messageSet.hasAnyWarnings());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'now' expected '0' arguments but received '4'.", messageSet.getErrorMessages().iterator().next());
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
    public void testGetValues() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(TimeZone.getDefault()).anyTimes();
        replay(timeZoneManager);
        final Date currentDate = new Date();
        NowFunction nowFunction = new NowFunction(new ConstantClock(currentDate), timeZoneManager);
        FunctionOperand function = new FunctionOperand(NowFunction.FUNCTION_NOW, Collections.<String>emptyList());
        final List<QueryLiteral> value = nowFunction.getValues(null, function, terminalClause);
        assertNotNull(value);
        assertEquals(1, value.size());
        assertEquals(currentDate.getTime(), value.get(0).getLongValue().longValue());
        assertEquals(function, value.get(0).getSourceOperand());
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(TimeZone.getDefault()).anyTimes();
        replay(timeZoneManager);
        final Date currentDate = new Date();
        NowFunction nowFunction = new NowFunction(new ConstantClock(currentDate), timeZoneManager);
        assertEquals(0, nowFunction.getMinimumNumberOfExpectedArguments());
    }
}
