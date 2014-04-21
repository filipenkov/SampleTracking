package com.atlassian.core.cron;

import com.atlassian.core.cron.generator.CronExpressionGenerator;
import com.atlassian.core.cron.parser.CronExpressionParser;
import junit.framework.TestCase;

/**
 *
 */
public class TestRoundTripParserGenerator extends TestCase
{

    public void testHoursMinutes()
    {
        assertCronStringRoundTripsOk("0 10 5 ? * *");
    }

    public void testHoursWithIncrement3()
    {
        assertCronStringRoundTripsOk("0 0 4-17/3 ? * *");
    }

    public void testHoursWithIncrement2()
    {
        assertCronStringRoundTripsOk("0 0 4-17/2 ? * *");
    }

    public void testHoursWithIncrement1()
    {
        assertCronStringRoundTripsOk("0 0 4-17/1 ? * *");
    }

    public void testHourRangeWithMinutesIncrement15()
    {
        assertCronStringRoundTripsOk("0 0/15 5-10 ? * *");
    }

    public void testHourRangeWithMinutesIncrement30()
    {
        assertCronStringRoundTripsOk("0 0/30 1-10 ? * *");
    }

    public void testHourRangeStartingAtMidnight()
    {
        assertCronStringRoundTripsOk("0 0 0-5/2 ? * *");
    }

    public void testHourRangeEndingAtMidnight()
    {
        assertCronStringRoundTripsOk("0 0 18-23/2 ? * *");
    }

    public void testHourRangeAllDayWithIncrement15()
    {
        assertCronStringRoundTripsOk("0 0/15 * ? * *");
    }

    public void testHourRangeAllDayWithIncrement120()
    {
        assertCronStringRoundTripsOk("0 0 */2 ? * *");
    }

    /**
     * If both hour and minute increments are specified, and the hour increment is more than 1, then the resulting
     * CronEditorBean will be in "advanced mode", and thus should round trip okay
     */
    public void testBothHourAndMinuteIncrements()
    {
        assertCronStringRoundTripsOk("0 0/15 */2 ? * *");
    }

    /**
     * The following cron expr will also result in "advanced mode" being triggered
     */
    public void testMinuteIncrementForSingleHour()
    {
        assertCronStringRoundTripsOk("0 0/15 6 ? * *");
    }

    public void testMinuteIncrementForSingleHourRange()
    {
        assertCronStringRoundTripsOk("0 0/15 6-6 ? * *");
    }

    public void testMidnight()
    {
        assertCronStringRoundTripsOk("0 0 0 ? * *");
    }

    public void testDayOfMonthRunOnce()
    {
        assertCronStringRoundTripsOk("0 30 9 24 * ?");
    }

    // NOTE: a day of month AND day of week of month with from/to is not valid, day of month only does run once

    public void testDayOfWeekRunOnce()
    {
        assertCronStringRoundTripsOk("0 30 9 ? * 2,3,4");
    }

    public void testOneDayOfWeekRunOnce()
    {
        assertCronStringRoundTripsOk("0 30 9 ? * 2");
    }


    public void testDayOfWeekFromTo()
    {
        assertCronStringRoundTripsOk("0 0 9-20/2 ? * 2,3,4");
    }

    public void testDayOfWeekOfMonthRunOnce()
    {
        assertCronStringRoundTripsOk("0 20 9 ? * 2#2");
        assertCronStringRoundTripsOk("0 20 0 ? * 7#4");
    }

    public void testDayOfWeekOfMonthRunOnceLastOfMonth()
    {
        assertCronStringRoundTripsOk("0 20 9 ? * 2L");
    }

    public void testRunOnceAllDaysOfWeek()
    {
        assertCronStringRoundTripsOk("0 45 9 ? * 1,2,3,4,5,6,7");
    }

    public void testYearMode()
    {
        assertCronStringRoundTripsOk("0 15 10 * * ? *");
    }

    public void testAdvanced()
    {
        assertCronStringRoundTripsOk("0 0 10 1/3 * ?");
    }

    public void testDefault() {
        assertCronStringRoundTripsOk(CronExpressionParser.DEFAULT_CRONSTRING);
    }

    void assertCronStringRoundTripsOk(String cronString)
    {
        CronEditorBean bean = new CronExpressionParser(cronString).getCronEditorBean();
        CronExpressionGenerator generator = new CronExpressionGenerator();
        assertEquals(cronString, generator.getCronExpressionFromInput(bean));
    }

    void assertCronStringRoundTripsNotOk(String cronString)
    {
        try
        {
            assertCronStringRoundTripsOk(cronString);
        }
        catch (Error e)
        {
            // expected
            return;
        }
        fail("This cronstring should have not passed the test: " + cronString);
    }
}
