package com.atlassian.jira.plugin.jql.function;

import com.atlassian.core.util.Clock;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import org.junit.Test;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @since v4.3
 */
public class TestStartOfMonthFunction extends AbstractDateFunctionTestCase
{
    private TerminalClause terminalClause = null;

    @Test
    public void testGetValues() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(TimeZone.getDefault()).anyTimes();
        replay(timeZoneManager);
        FunctionOperand function = new FunctionOperand(getFunctionName(), Collections.<String>emptyList());
        final List<QueryLiteral> value = getInstanceToTest().getValues(null, function, terminalClause);
        assertNotNull(value);
        assertEquals(1, value.size());
        
        GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(value.get(0).getLongValue().longValue());
        assertEquals(now.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals(now.get(Calendar.MONTH), cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        
        assertEquals(function, value.get(0).getSourceOperand());
    }
    
    @Test
    public void testknowDates() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(TimeZone.getDefault()).anyTimes();
        replay(timeZoneManager);
        doTest(new GregorianCalendar(2001, 6, 1, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20), TimeZone.getDefault());
        doTest(new GregorianCalendar(1900, 0, 1, 0, 0, 0), new GregorianCalendar(1900, 0, 1, 0, 0, 0), TimeZone.getDefault());
        doTest(new GregorianCalendar(2004, 1, 1, 0, 0, 0), new GregorianCalendar(2004, 1, 29, 12, 30, 20), TimeZone.getDefault());
        doTest(new GregorianCalendar(2004, 1, 1, 0, 0, 0), new GregorianCalendar(2004, 1, 28, 12, 30, 20), TimeZone.getDefault());
        doTest(new GregorianCalendar(2099, 6, 1, 0, 0, 0), new GregorianCalendar(2099, 6, 25, 12, 30, 20), TimeZone.getDefault());
        doTest(new GregorianCalendar(2010, 11, 1, 0, 0, 0), new GregorianCalendar(2010, 11, 31, 23, 59, 59), TimeZone.getDefault());
        doTest(new GregorianCalendar(2012, 2, 1, 0, 0, 0), new GregorianCalendar(2012, 2, 1, 0, 0, 0), TimeZone.getDefault());

        doTestwithIncrement("-1", new GregorianCalendar(2001, 5, 1, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-2", new GregorianCalendar(2001, 4, 1, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-3", new GregorianCalendar(2001, 3, 1, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-4", new GregorianCalendar(2001, 2, 1, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-5", new GregorianCalendar(2001, 1, 1, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-6", new GregorianCalendar(2001, 0, 1, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-7", new GregorianCalendar(2000, 11, 1, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-8", new GregorianCalendar(2000, 10, 1, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("1M", new GregorianCalendar(2001, 7, 1, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-1y", new GregorianCalendar(2000, 6, 1, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("+3", new GregorianCalendar(2001, 9, 1, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));

        doTestwithIncrement("+3w", new GregorianCalendar(2001, 6, 22, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("+3d", new GregorianCalendar(2001, 6, 4, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("+3h", new GregorianCalendar(2001, 6, 1, 3, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("+3m", new GregorianCalendar(2001, 6, 1, 0, 3, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));

        doTestwithIncrement("-3w", new GregorianCalendar(2001, 5, 10, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-3d", new GregorianCalendar(2001, 5, 28, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-3h", new GregorianCalendar(2001, 5, 30, 21, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
        doTestwithIncrement("-3m", new GregorianCalendar(2001, 5, 30, 23, 57, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20));
    }

     @Test
    public void testWithUserTimeZone() throws Exception
    {
        TimeZone userTimeZone = TimeZone.getTimeZone("Europe/Berlin");
        TimeZone systemTimeZone = TimeZone.getTimeZone("Australia/Sydney");

        GregorianCalendar expectedSystemTime = new GregorianCalendar(systemTimeZone);

        expectedSystemTime.set(Calendar.YEAR, 2011);
        expectedSystemTime.set(Calendar.MONTH, 2);
        expectedSystemTime.set(Calendar.DAY_OF_MONTH, 1);
        expectedSystemTime.set(Calendar.HOUR_OF_DAY, 10);
        expectedSystemTime.set(Calendar.MINUTE, 0);
        expectedSystemTime.set(Calendar.SECOND, 0);
        expectedSystemTime.getTime();

        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(userTimeZone).anyTimes();
        replay(timeZoneManager);

        GregorianCalendar systemTime = new GregorianCalendar(systemTimeZone);
        systemTime.set(Calendar.YEAR, 2011);
        systemTime.set(Calendar.MONTH, 3);
        systemTime.set(Calendar.DAY_OF_MONTH, 1);
        systemTime.set(Calendar.HOUR_OF_DAY, 4);
        systemTime.set(Calendar.MINUTE, 12);
        systemTime.set(Calendar.SECOND, 12);
        systemTime.getTime();

        doTest(expectedSystemTime, systemTime, systemTimeZone);
    }
    
    @Override
    String getFunctionName() 
    {
        return StartOfMonthFunction.FUNCTION_START_OF_MONTH;
    }

    @Override
    AbstractDateFunction getInstanceToTest() 
    {
        return getInstanceToTest(new ConstantClock(new Date()));
    }

    AbstractDateFunction getInstanceToTest(Clock clock) 
    {
        return new StartOfMonthFunction(clock, timeZoneManager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };
    }
}
