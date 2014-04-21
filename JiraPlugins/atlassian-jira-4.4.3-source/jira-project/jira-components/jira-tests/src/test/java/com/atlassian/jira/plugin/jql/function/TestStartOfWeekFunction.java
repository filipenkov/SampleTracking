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
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @since v4.3
 */
public class TestStartOfWeekFunction extends AbstractDateFunctionTestCase
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
        now.set(Calendar.DAY_OF_WEEK, now.getFirstDayOfWeek());
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(value.get(0).getLongValue().longValue());
        assertEquals(now.get(Calendar.YEAR), cal.get(Calendar.YEAR));
        assertEquals(now.get(Calendar.MONTH), cal.get(Calendar.MONTH));
        assertEquals(now.get(Calendar.WEEK_OF_MONTH), cal.get(Calendar.WEEK_OF_MONTH));
        assertEquals(new GregorianCalendar().getFirstDayOfWeek(), cal.get(Calendar.DAY_OF_WEEK));
        assertEquals(now.getActualMinimum(0), cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(now.getActualMinimum(0), cal.get(Calendar.MINUTE));
        assertEquals(now.getActualMinimum(0), cal.get(Calendar.SECOND));
        
        assertEquals(function, value.get(0).getSourceOperand());
    }

    @Test
    public void testWithUserTimeZone() throws Exception
    {
        TimeZone userTimeZone = TimeZone.getTimeZone("Europe/Berlin");
        TimeZone systemTimeZone = TimeZone.getTimeZone("Australia/Sydney");

        GregorianCalendar expectedSystemTime = new GregorianCalendar(systemTimeZone);

        expectedSystemTime.set(Calendar.YEAR, 2011);
        expectedSystemTime.set(Calendar.MONTH, 2);
        expectedSystemTime.set(Calendar.DAY_OF_MONTH, 27);
        expectedSystemTime.set(Calendar.HOUR_OF_DAY, 10);
        expectedSystemTime.set(Calendar.MINUTE, 0);
        expectedSystemTime.set(Calendar.SECOND, 0);
        expectedSystemTime.getTime();

        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(userTimeZone).anyTimes();
        replay(timeZoneManager);

        GregorianCalendar systemTime = new GregorianCalendar(systemTimeZone);
        systemTime.set(Calendar.YEAR, 2011);
        systemTime.set(Calendar.MONTH, 3);
        systemTime.set(Calendar.DAY_OF_MONTH, 3);
        systemTime.set(Calendar.HOUR_OF_DAY, 4);
        systemTime.set(Calendar.MINUTE, 12);
        systemTime.set(Calendar.SECOND, 12);
        systemTime.getTime();

        doTest(expectedSystemTime, systemTime, systemTimeZone);
    }

    @Test
    public void testknowDates() throws Exception
    {
        expect(timeZoneManager.getLoggedInUserTimeZone()).andReturn(TimeZone.getDefault()).anyTimes();
        replay(timeZoneManager);
        Locale saveLocale = Locale.getDefault();
        try
        {
            // First day is Monday
            Locale.setDefault(new Locale("fr", "FR")); 
            assertEquals(Calendar.MONDAY, new GregorianCalendar().getFirstDayOfWeek());
            // Test Known wednesday to tuesday
            doTest(new GregorianCalendar(2010, 4, 10, 0, 0, 0), new GregorianCalendar(2010, 4, 12, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 10, 0, 0, 0), new GregorianCalendar(2010, 4, 13, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 10, 0, 0, 0), new GregorianCalendar(2010, 4, 14, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 10, 0, 0, 0), new GregorianCalendar(2010, 4, 15, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 10, 0, 0, 0), new GregorianCalendar(2010, 4, 16, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 17, 0, 0, 0), new GregorianCalendar(2010, 4, 17, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 17, 0, 0, 0), new GregorianCalendar(2010, 4, 18, 12, 30, 20), TimeZone.getDefault());
            // Some other days
            doTest(new GregorianCalendar(2001, 6, 23, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(1900, 0, 1, 0, 0, 0), new GregorianCalendar(1900, 0, 1, 0, 0, 0), TimeZone.getDefault());
            doTest(new GregorianCalendar(2004, 1, 23, 0, 0, 0), new GregorianCalendar(2004, 1, 29, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2004, 1, 23, 0, 0, 0), new GregorianCalendar(2004, 1, 28, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2099, 6, 20, 0, 0, 0), new GregorianCalendar(2099, 6, 25, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 11, 27, 0, 0, 0), new GregorianCalendar(2010, 11, 31, 23, 59, 59), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 8, 27, 0, 0, 0), new GregorianCalendar(2010, 9, 1, 0, 0, 0), TimeZone.getDefault());
            doTest(new GregorianCalendar(2012, 1, 27, 0, 0, 0), new GregorianCalendar(2012, 2, 1, 0, 0, 0), TimeZone.getDefault());

            // First day is Sunday
            Locale.setDefault(new Locale("en", "US")); 
            // Test Known wednesday to tuesday
            doTest(new GregorianCalendar(2010, 4, 9, 0, 0, 0), new GregorianCalendar(2010, 4, 12, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 9, 0, 0, 0), new GregorianCalendar(2010, 4, 13, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 9, 0, 0, 0), new GregorianCalendar(2010, 4, 14, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 9, 0, 0, 0), new GregorianCalendar(2010, 4, 15, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 16, 0, 0, 0), new GregorianCalendar(2010, 4, 16, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 16, 0, 0, 0), new GregorianCalendar(2010, 4, 17, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 4, 16, 0, 0, 0), new GregorianCalendar(2010, 4, 18, 12, 30, 20), TimeZone.getDefault());
            // Some other days
            doTest(new GregorianCalendar(2001, 6, 22, 0, 0, 0), new GregorianCalendar(2001, 6, 25, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(1899, 11, 31, 0, 0, 0), new GregorianCalendar(1900, 0, 1, 0, 0, 0), TimeZone.getDefault());
            doTest(new GregorianCalendar(2004, 1, 29, 0, 0, 0), new GregorianCalendar(2004, 1, 29, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2004, 1, 22, 0, 0, 0), new GregorianCalendar(2004, 1, 28, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2099, 6, 19, 0, 0, 0), new GregorianCalendar(2099, 6, 25, 12, 30, 20), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 11, 26, 0, 0, 0), new GregorianCalendar(2010, 11, 31, 23, 59, 59), TimeZone.getDefault());
            doTest(new GregorianCalendar(2010, 8, 26, 0, 0, 0), new GregorianCalendar(2010, 9, 1, 0, 0, 0), TimeZone.getDefault());
            doTest(new GregorianCalendar(2012, 1, 26, 0, 0, 0), new GregorianCalendar(2012, 2, 1, 0, 0, 0), TimeZone.getDefault());
        }
        finally
        {
            Locale.setDefault(saveLocale); 
        }
    }
    
    @Override
    String getFunctionName() 
    {
        return StartOfWeekFunction.FUNCTION_START_OF_WEEK;
    }

    @Override
    AbstractDateFunction getInstanceToTest() 
    {
        return getInstanceToTest(new ConstantClock(new Date()));
    }

    AbstractDateFunction getInstanceToTest(Clock clock) 
    {
        return new StartOfWeekFunction(clock, timeZoneManager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return new MockI18nBean();
            }
        };
    }
}
