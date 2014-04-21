/*
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 4/03/2002
 * Time: 15:15:27
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.atlassian.core.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Date;

public class TestDateUtils extends TestCase
{
    private static long MINUTE = 60;
    private static long HOUR = (60 * MINUTE);

    private static long SECOND_MS = 1000;
    private static long MINUTE_MS = SECOND_MS * 60;
    private static long HOUR_MS = MINUTE_MS * 60;
    private static long DAY_MS = HOUR_MS * 24;


    private ResourceBundle resourceBundle;

    protected void setUp() throws Exception
    {
        super.setUp();

        resourceBundle = ResourceBundle.getBundle("DateUtils", Locale.getDefault(), Thread.currentThread().getContextClassLoader());
    }

    public void testEqualTimeStamps1()
    {
        Timestamp testa = new Timestamp(100000000);
        testa.setTime(100000009);
        Timestamp testb = new Timestamp(100000000);
        testb.setTime(100000000);

        assertTrue((DateUtils.equalTimestamps(testa, testb)));
    }


    public void testEqualTimeStamps2()
    {
        Timestamp testa = new Timestamp(100000000);
        testa.setTime(100000000);
        Timestamp testb = new Timestamp(100000000);
        testb.setTime(100000009);

        assertTrue(DateUtils.equalTimestamps(testa, testb));
    }

    public void testEqualTimeStamps3()
    {
        Timestamp testa = new Timestamp(100000000);
        testa.setTime(100000010);
        Timestamp testb = new Timestamp(100000000);
        testb.setTime(100000000);
        assertTrue(!(DateUtils.equalTimestamps(testa, testb)));
    }

    public void testEqualTimeStamps4()
    {
        Timestamp testa = new Timestamp(100000000);
        testa.setTime(100000000);
        Timestamp testb = new Timestamp(100000000);
        testb.setTime(100000010);
        assertTrue(!(DateUtils.equalTimestamps(testa, testb)));
    }

    public void testFormatDate() throws Exception
    {
        assertEquals("2005-06-07 14:45:00,000", DateUtils.formatDateISO8601(new GregorianCalendar(2005, 5, 7, 14, 45).getTime()));
    }
    public void testDateDifference1()
    {
        _testDateDifference(0, 3, "0 minutes");
        _testDateDifference(0, 4, "0 seconds");
        _testDateDifference(cal(2000, 1, 1), cal(2000, 2, 1), 0, "0 months");
        _testDateDifference(cal(2000, 1, 1), cal(2000, 4, 1), 0, "2 months");
        _testDateDifference(cal(2000, 1, 1), cal(2000, 2, 1), 2, "29 days");
        _testDateDifference(cal(2000, 1, 1), cal(2000, 2, 15), 1, "1 month, 12 days");
        _testDateDifference(cal(2000, 1, 1), cal(2000, 2, 15), 2, "1 month, 12 days");
        _testDateDifference(cal(2000, 1, 1), cal(2000, 1, 1), 1, "0 days");
        _testDateDifference(cal(2000, 1, 1), cal(2000, 1, 1), 2, "0 hours");
        _testDateDifference(cal(2000, 1, 1), cal(2000, 12, 1), 1, "10 months, 25 days");
        _testDateDifference(cal(2000, 1, 1), cal(2000, 1, 2), 2, "1 day");
        _testDateDifference(cal(2000, 12, 1), cal(2001, 1, 1), 2, "1 month");
//        _testDateDifference(cal(2000, 12, 1), cal(2002, 1, 1), 2, "1 year, 1 month");
//        _testDateDifference(cal(1999, 12, 1), cal(2002, 1, 1), 2, "2 years, 1 month");
//        _testDateDifference(cal(2004, 8, 29), cal(2007, 4, 26), 3, "2 years, 7 months"); //previously said "138 weeks, 3 days ago"

        _testDateDifference(3 * DAY_MS + 5 * HOUR_MS + 4 * MINUTE_MS + 30 * SECOND_MS, 4, "3 days, 5 hours, 4 minutes, 30 seconds");
        _testDateDifference(3 * DAY_MS + 5 * HOUR_MS + 4 * MINUTE_MS + 1 * SECOND_MS, 4, "3 days, 5 hours, 4 minutes, 1 second");
        _testDateDifference(3 * DAY_MS + 5 * HOUR_MS + 4 * MINUTE_MS + 30 * SECOND_MS, 3, "3 days, 5 hours, 4 minutes");
        _testDateDifference(3 * DAY_MS + 5 * HOUR_MS + 4 * MINUTE_MS + 30 * SECOND_MS, 2, "3 days, 5 hours");
        _testDateDifference(3 * DAY_MS + 5 * HOUR_MS + 4 * MINUTE_MS + 30 * SECOND_MS, 1, "3 days");
        _testDateDifference(3 * DAY_MS + 5 * HOUR_MS + 4 * MINUTE_MS + 30 * SECOND_MS, 0, "0 months");

        _testDateDifference(1 * DAY_MS + 1 * HOUR_MS + 4 * MINUTE_MS + 30 * SECOND_MS, 2, "1 day, 1 hour");
        _testDateDifference(3 * DAY_MS + 5 * HOUR_MS + 1 * MINUTE_MS + 30 * SECOND_MS, 3, "3 days, 5 hours, 1 minute");

    }

    private static Calendar cal(int year1, int month1, int day1)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(year1, month1, day1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    private void _testDateDifference(long diff, int resolution, String expected)
    {
        long now = System.currentTimeMillis();
        assertEquals(expected, DateUtils.dateDifference(now - diff, now, resolution, resourceBundle));
    }

    private void _testDateDifference(Calendar cal1, Calendar cal2, int resolution, String expected)
    {
        assertEquals(expected, DateUtils.dateDifference(cal1.getTime().getTime(), cal2.getTime().getTime(), resolution, resourceBundle));
    }

    public void testGetDuration() throws Exception
    {
        assertEquals(0 * MINUTE, DateUtils.getDuration(""));
        assertEquals(0 * MINUTE, DateUtils.getDuration(" "));
        assertEquals(0, DateUtils.getDuration("0"));
        assertEquals(0 * MINUTE, DateUtils.getDuration(null));
        assertEquals(60 * MINUTE, DateUtils.getDuration("60"));
        assertEquals(60 * MINUTE, DateUtils.getDuration("60m"));
        assertEquals(60 * MINUTE, DateUtils.getDuration("1h"));
        assertEquals(120 * MINUTE, DateUtils.getDuration("2h"));
        assertEquals(60 * 24 * 3 * MINUTE, DateUtils.getDuration("3d"));
        assertEquals(60 * 24 * 7 * 4 * MINUTE, DateUtils.getDuration("4w"));
        assertEquals(60 * 24 * 7 * 4 * MINUTE, DateUtils.getDuration("4WeEks"));
        assertEquals(90 * MINUTE, DateUtils.getDuration("1h 30"));
        assertEquals((3 * 24 * 60 + 4 * 60 + 30) * MINUTE, DateUtils.getDuration("3d 4h 30m"));
        assertEquals((3 * 24 * 60 + 4 * 60 + 30) * MINUTE, DateUtils.getDuration("3d 4hours 30m"));
        assertEquals((3 * 24 * 60 + 4 * 60 + 30) * MINUTE, DateUtils.getDuration("3d, 4h, 30m"));
        assertEquals((3 * 24 * 60 + 4 * 60 + 30) * MINUTE, DateUtils.getDuration("3d, 4h, 30minute"));
        assertEquals((3 * 24 * 60 + 4 * 60 + 30) * MINUTE, DateUtils.getDuration("3days, 4hours, 30minutes"));
        assertEquals((4 * 7 * 24 * 60 + 3 * 24 * 60 + 4 * 60 + 30) * MINUTE, DateUtils.getDuration("4w 3d 4h 30m"));
        
        // try different default durations.
        assertEquals(5 * 24 * 60 * MINUTE , DateUtils.getDuration("5", DateUtils.Duration.DAY));
        assertEquals(7 * 60 * MINUTE , DateUtils.getDuration("7", DateUtils.Duration.HOUR));
        assertEquals(2 * 7 * 24 * 60 * MINUTE , DateUtils.getDuration("2", DateUtils.Duration.WEEK));

        // try different hours per day and days per week
        assertEquals(5 * 8 * 60 * MINUTE, DateUtils.getDuration("5d", 8, 7));
        assertEquals(2 * 5 * 8 * 60 * MINUTE, DateUtils.getDuration("2w", 8, 5));
        assertEquals(21 * 8 * 60 * MINUTE, DateUtils.getDuration("21", 8, 5, DateUtils.Duration.DAY));
        assertEquals(3 * 5 * 8 * 60 * MINUTE, DateUtils.getDuration("3", 8, 5, DateUtils.Duration.WEEK));

        // try some stuff with decimal fractions
        final long hour_2_5 = (long) (2.5 * 60 * MINUTE);
        assertEquals(hour_2_5, DateUtils.getDuration("2.5h", 8, 5));
        final long day_25 = (long) (.25 * 8 * 60 * MINUTE);
        assertEquals(day_25, DateUtils.getDuration(".25d", 8, 5));
        assertEquals(day_25, DateUtils.getDuration("0.25d", 8, 5));
        final long week_3_2 = (long) (3.2 * 5 * 8 * 60 * MINUTE);
        assertEquals(week_3_2, DateUtils.getDuration("3.2w", 8, 5));

        // just make sure they all work together
        assertEquals(hour_2_5 + day_25 + week_3_2, DateUtils.getDuration("3.2w .25d 2.5h", 8, 5));

        // negative numbers aren't allowed
        try
        {
            DateUtils.getDuration("-7.5d");
            fail();
        }
        catch (InvalidDurationException e) {}

        // no sub-minute fractions allowed
        try
        {
            DateUtils.getDuration("1.5m");
            fail();
        }
        catch (InvalidDurationException e) {}

        // no sub-minute fractions allowed
        try
        {
            // resolves to 15.6 minutes
            DateUtils.getDuration(".27h");
            fail();
        }
        catch (InvalidDurationException e) {}

        // must have a digit after the decimal
        try
        {
            DateUtils.getDuration("5.w");
            fail();
        }
        catch (InvalidDurationException e) {}

        try
        {
            DateUtils.getDuration("foo");
            fail();
        }
        catch (InvalidDurationException e)
        {
        }

        try
        {
            DateUtils.getDuration("60s");
            fail();
        }
        catch (InvalidDurationException e)
        {
        }

        try
        {
            DateUtils.getDuration("3dumdidum");
            fail();
        }
        catch (InvalidDurationException e)
        {
        }

        try
        {
            DateUtils.getDuration("10wikipedia");
            fail();
        }
        catch (InvalidDurationException e)
        {
        }

        try
        {
            DateUtils.getDuration("wikipedia");
            fail();
        }
        catch (InvalidDurationException e)
        {
        }

        try
        {
            DateUtils.getDuration("123120893wewerewrasdfdsfxcvb");
            fail();
        }
        catch (InvalidDurationException e)
        {
        }

        try
        {
            DateUtils.getDuration("13dasdys, 2weeks");
            fail();
        }
        catch (InvalidDurationException e)
        {
        }
    }

    public void testValidDuration() throws Exception
    {
        assertTrue(DateUtils.validDuration(""));
        assertTrue(DateUtils.validDuration(" "));
        assertTrue(DateUtils.validDuration(null));
        assertTrue(DateUtils.validDuration("60"));
        assertTrue(DateUtils.validDuration("60m"));
        assertTrue(DateUtils.validDuration("1h"));
        assertTrue(DateUtils.validDuration("2d"));
        assertTrue(DateUtils.validDuration("3w"));
        assertTrue(DateUtils.validDuration("1w 2d 3h 4m"));
        assertTrue(DateUtils.validDuration("1weeks 2days 3hours 4minutes"));
        assertTrue(DateUtils.validDuration("1weeks, 2days, 3hours, 4minutes"));

        assertFalse(DateUtils.validDuration("foo"));
        assertFalse(DateUtils.validDuration("60s"));
        assertFalse(DateUtils.validDuration("60magic"));
        assertFalse(DateUtils.validDuration("3dogs"));
        assertFalse(DateUtils.validDuration("4hippos"));
        assertFalse(DateUtils.validDuration("5windows"));
        assertFalse(DateUtils.validDuration("h"));
        assertFalse(DateUtils.validDuration("1w 2d 3h 27garbage"));
    }

    public void testGetDurationCustomDefault() throws Exception
    {
        assertEquals(0, DateUtils.getDuration("", DateUtils.Duration.DAY));
        assertEquals(0, DateUtils.getDuration(" ", DateUtils.Duration.WEEK));
        assertEquals(1209600, DateUtils.getDuration("2", DateUtils.Duration.WEEK));
        assertEquals(62, DateUtils.getDuration("1m 2", DateUtils.Duration.SECOND));
        assertEquals(841440, DateUtils.getDuration("6d 12h 44m 77", DateUtils.Duration.HOUR));

        // same tests as in above unit test but with extra parameter to make sure we don't break any parsing
        // by specifying custom default
        assertEquals(60 * MINUTE, DateUtils.getDuration("60m", DateUtils.Duration.HOUR));
        assertEquals(120 * MINUTE, DateUtils.getDuration("2h", DateUtils.Duration.MONTH));
        assertEquals(60 * 24 * 3 * MINUTE, DateUtils.getDuration("3d", DateUtils.Duration.SECOND));
        assertEquals(60 * 24 * 7 * 4 * MINUTE, DateUtils.getDuration("4w", DateUtils.Duration.DAY));
        assertEquals(60 * 24 * 7 * 4 * MINUTE, DateUtils.getDuration("4weeks", DateUtils.Duration.MINUTE));
        assertEquals((3 * 24 * 60 + 4 * 60 + 30) * MINUTE, DateUtils.getDuration("3d 4h 30m", DateUtils.Duration.WEEK));
        assertEquals((3 * 24 * 60 + 4 * 60 + 30) * MINUTE, DateUtils.getDuration("3d, 4h, 30m", DateUtils.Duration.YEAR));
        assertEquals((3 * 24 * 60 + 4 * 60 + 30) * MINUTE, DateUtils.getDuration("3days, 4hours, 30minutes", DateUtils.Duration.HOUR));
        assertEquals((4 * 7 * 24 * 60 + 3 * 24 * 60 + 4 * 60 + 30) * MINUTE, DateUtils.getDuration("4w 3d 4h 30m", DateUtils.Duration.WEEK));
        assertEquals(MINUTE, DateUtils.getDuration("1mInuTe", DateUtils.Duration.HOUR));
        assertEquals(MINUTE, DateUtils.getDuration("1mInuTes", DateUtils.Duration.HOUR));
        assertEquals(HOUR, DateUtils.getDuration("1HoUr", DateUtils.Duration.HOUR));
        assertEquals(HOUR, DateUtils.getDuration("1HoUrs", DateUtils.Duration.HOUR));
        assertEquals(24 * HOUR, DateUtils.getDuration("1DaY", DateUtils.Duration.HOUR));
        assertEquals(24 * HOUR, DateUtils.getDuration("1dAyS", DateUtils.Duration.HOUR));
        assertEquals(24 * 7 * HOUR, DateUtils.getDuration("1wEeK", DateUtils.Duration.HOUR));
        assertEquals(24 * 7 * HOUR, DateUtils.getDuration("1wEekS", DateUtils.Duration.HOUR));

        try
        {
            DateUtils.getDuration("foo");
            fail();
        }
        catch (InvalidDurationException e)
        {
            // expected
        }

        try
        {
            DateUtils.getDuration("60s");
            fail();
        }
        catch (InvalidDurationException e)
        {
            // expected
        }
    }

    public void testGetDuration2() throws InvalidDurationException
    {
        int hoursInDay = 8;
        int daysInWeek = 5;
        assertEquals(90 * MINUTE, DateUtils.getDuration("1h 30", hoursInDay, daysInWeek));
        assertEquals((3 * hoursInDay * 60 + 4 * 60 + 30) * MINUTE, DateUtils.getDuration("3d 4h 30m", hoursInDay, daysInWeek));
        assertEquals((4 * daysInWeek * hoursInDay * 60 + 3 * hoursInDay * 60 + 4 * 60 + 30) * MINUTE, DateUtils.getDuration("4w 3d 4h 30m", hoursInDay, daysInWeek));
    }

    public void testGetDurationWithNegative() throws InvalidDurationException
    {
        assertEquals(0, DateUtils.getDurationWithNegative(" "));
        assertEquals(0, DateUtils.getDurationWithNegative(""));
        assertEquals(90 * MINUTE, DateUtils.getDurationWithNegative("1h 30"));
        assertEquals(-90 * MINUTE, DateUtils.getDurationWithNegative("-1h 30"));
        assertEquals(-7 * HOUR, DateUtils.getDurationWithNegative("- 7h"));
        try
        {
            DateUtils.getDurationWithNegative("-7h -6m");
            fail("Should have thrown exception");
        }
        catch (InvalidDurationException e)
        {
        }
    }

    public void testDurationString() throws Exception
    {
        assertEquals("0m", DateUtils.getDurationString(0 * MINUTE));
        assertEquals("30m", DateUtils.getDurationString(30 * MINUTE));
        assertEquals("1h", DateUtils.getDurationString(60 * MINUTE));
        assertEquals("2h", DateUtils.getDurationString(120 * MINUTE));
        assertEquals("3d", DateUtils.getDurationString(60 * 24 * 3 * MINUTE));
        assertEquals("4w", DateUtils.getDurationString(4 * 7 * 24 * 60 * MINUTE));
        assertEquals("1h 30m", DateUtils.getDurationString(90 * MINUTE));
        assertEquals("3d 4h 30m", DateUtils.getDurationString((3 * 24 * 60 + 4 * 60 + 30) * MINUTE));
        assertEquals("4w 1d 4h 30m", DateUtils.getDurationString((4 * 7 * 24 * 60 + 24 * 60 + 4 * 60 + 30) * MINUTE));

        // change the number of days per week and hours per day
        assertEquals("17w 2d 4h 30m", DateUtils.getDurationString((4 * 7 * 24 * 60 + 24 * 60 + 4 * 60 + 30) * MINUTE, 8, 5));
        assertEquals("23w 1d 4h 30m", DateUtils.getDurationString((4 * 7 * 24 * 60 + 24 * 60 + 4 * 60 + 30) * MINUTE, 6, 5));
        assertEquals("10w 7d 4h 30m", DateUtils.getDurationString((4 * 7 * 24 * 60 + 24 * 60 + 4 * 60 + 30) * MINUTE, 8, 8));

    }

    public void testDurationStringWithNegative() throws Exception
    {
        assertEquals("-30m", DateUtils.getDurationStringWithNegative(-30 * MINUTE));
        assertEquals("30m", DateUtils.getDurationStringWithNegative(30 * MINUTE));
        assertEquals("-1h 30m", DateUtils.getDurationStringWithNegative(-90 * MINUTE));
    }

    public void testDurationPretty() throws Exception
    {
        // ensure we don't display seconds unless it is the only unit available
        assertEquals("0 minutes", DateUtils.getDurationPretty(0 * MINUTE, resourceBundle));
        assertEquals("1 second", DateUtils.getDurationPretty(1, resourceBundle)); // 12 seconds
        assertEquals("12 seconds", DateUtils.getDurationPretty(12, resourceBundle)); // 12 seconds
        assertEquals("1 minute", DateUtils.getDurationPretty(1 * MINUTE + 15, resourceBundle));
        assertEquals("30 minutes", DateUtils.getDurationPretty(30 * MINUTE + 15, resourceBundle));
        assertEquals("1 hour", DateUtils.getDurationPretty(60 * MINUTE, resourceBundle));
        assertEquals("2 hours", DateUtils.getDurationPretty(120 * MINUTE, resourceBundle));
        assertEquals("3 days", DateUtils.getDurationPretty(60 * 24 * 3 * MINUTE, resourceBundle));
        assertEquals("4 weeks", DateUtils.getDurationPretty(60 * 24 * 7 * 4 * MINUTE, resourceBundle));
        assertEquals("1 hour, 30 minutes", DateUtils.getDurationPretty(90 * MINUTE, resourceBundle));
        assertEquals("3 days, 4 hours, 30 minutes", DateUtils.getDurationPretty((3 * 24 * 60 + 4 * 60 + 30) * MINUTE, resourceBundle));
        assertEquals("4 weeks, 1 day, 4 hours, 30 minutes", DateUtils.getDurationPretty((4 * 7 * 24 * 60 + 24 * 60 + 4 * 60 + 30) * MINUTE, resourceBundle));
        //previously said "138 weeks, 3 days ago"
        assertEquals("1 year, 34 weeks, 2 days", DateUtils.getDurationPretty((cal(2007, 4, 26).getTimeInMillis() - cal(2005, 8, 29).getTimeInMillis()) / 1000, resourceBundle));
        assertEquals("2 years, 34 weeks, 3 days", DateUtils.getDurationPretty((cal(2007, 4, 26).getTimeInMillis() - cal(2004, 8, 29).getTimeInMillis()) / 1000, resourceBundle));

        // try with a different number of hours per day and days per week
        assertEquals("1 week", DateUtils.getDurationPretty(60 * 24 * 3 * MINUTE, 24, 3, resourceBundle));
        assertEquals("1 week, 4 days", DateUtils.getDurationPretty(60 * 24 * 3 * MINUTE, 8, 5, resourceBundle));

    }

    public void testDurationPrettySecondsResolution() throws Exception
    {
        // work just like DateUtils.getDurationPretty if there are seconds remainder in the duration
        assertEquals("0 seconds", DateUtils.getDurationPrettySecondsResolution(0 * MINUTE, resourceBundle));
        assertEquals("1 second", DateUtils.getDurationPrettySecondsResolution(1, resourceBundle)); // 12 seconds
        assertEquals("12 seconds", DateUtils.getDurationPrettySecondsResolution(12, resourceBundle)); // 12 seconds
        assertEquals("1 minute", DateUtils.getDurationPrettySecondsResolution(1 * MINUTE, resourceBundle));
        assertEquals("30 minutes", DateUtils.getDurationPrettySecondsResolution(30 * MINUTE, resourceBundle));
        assertEquals("1 hour", DateUtils.getDurationPrettySecondsResolution(60 * MINUTE, resourceBundle));
        assertEquals("2 hours", DateUtils.getDurationPrettySecondsResolution(120 * MINUTE, resourceBundle));
        assertEquals("3 days", DateUtils.getDurationPrettySecondsResolution(60 * 24 * 3 * MINUTE, resourceBundle));
        assertEquals("4 weeks", DateUtils.getDurationPrettySecondsResolution(60 * 24 * 7 * 4 * MINUTE, resourceBundle));
        assertEquals("1 hour, 30 minutes", DateUtils.getDurationPrettySecondsResolution(90 * MINUTE, resourceBundle));
        assertEquals("3 days, 4 hours, 30 minutes", DateUtils.getDurationPrettySecondsResolution((3 * 24 * 60 + 4 * 60 + 30) * MINUTE, resourceBundle));
        assertEquals("4 weeks, 1 day, 4 hours, 30 minutes", DateUtils.getDurationPrettySecondsResolution((4 * 7 * 24 * 60 + 24 * 60 + 4 * 60 + 30) * MINUTE, resourceBundle));
        //previously said "138 weeks, 3 days ago"
        assertEquals("1 year, 34 weeks, 2 days", DateUtils.getDurationPrettySecondsResolution((cal(2007, 4, 26).getTimeInMillis() - cal(2005, 8, 29).getTimeInMillis()) / 1000, resourceBundle));
        assertEquals("2 years, 34 weeks, 3 days", DateUtils.getDurationPrettySecondsResolution((cal(2007, 4, 26).getTimeInMillis() - cal(2004, 8, 29).getTimeInMillis()) / 1000, resourceBundle));

        // now we have seconds in the remainder
        assertEquals("1 minute, 23 seconds", DateUtils.getDurationPrettySecondsResolution(1 * MINUTE + 23, resourceBundle));
        assertEquals("30 minutes, 1 second", DateUtils.getDurationPrettySecondsResolution(30 * MINUTE + 1, resourceBundle));
        assertEquals("1 hour, 23 seconds", DateUtils.getDurationPrettySecondsResolution(60 * MINUTE + 23, resourceBundle));
        assertEquals("2 hours, 8 seconds", DateUtils.getDurationPrettySecondsResolution(120 * MINUTE + 8, resourceBundle));
        assertEquals("3 days, 12 seconds", DateUtils.getDurationPrettySecondsResolution(60 * 24 * 3 * MINUTE + 12, resourceBundle));
        assertEquals("4 weeks, 17 seconds", DateUtils.getDurationPrettySecondsResolution(60 * 24 * 7 * 4 * MINUTE + 17, resourceBundle));
        assertEquals("1 hour, 30 minutes, 4 seconds", DateUtils.getDurationPrettySecondsResolution(90 * MINUTE + 4, resourceBundle));
        assertEquals("3 days, 4 hours, 30 minutes, 39 seconds", DateUtils.getDurationPrettySecondsResolution((3 * 24 * 60 + 4 * 60 + 30) * MINUTE + 39, resourceBundle));
        assertEquals("4 weeks, 1 day, 4 hours, 30 minutes, 1 second", DateUtils.getDurationPrettySecondsResolution((4 * 7 * 24 * 60 + 24 * 60 + 4 * 60 + 30) * MINUTE + 1, resourceBundle));

        // try with different hours per day and days per week
        assertEquals("1 week, 12 seconds", DateUtils.getDurationPrettySecondsResolution(60 * 24 * 3 * MINUTE + 12, 24, 3, resourceBundle));
        assertEquals("1 week, 4 days, 12 seconds", DateUtils.getDurationPrettySecondsResolution(60 * 24 * 3 * MINUTE + 12, 8, 5, resourceBundle));
    }

    public void testTomorrowYesterday() throws Exception
    {
        final Date today = new Date();
        final Date tomorrow = DateUtils.tomorrow();
        final Date yesterday = DateUtils.yesterday();
        assertTrue(tomorrow.after(today));
        assertTrue(yesterday.before(today));
    }
    public void testDurationPrettySeconds() throws Exception
    {
        long secondsPerDay = 60 * 60 * 24;
        long secondsPerWeek = secondsPerDay * 3;
        assertEquals("1 week", DateUtils.getDurationPrettySeconds(60 * 24 * 3 * MINUTE, secondsPerDay, secondsPerWeek, resourceBundle));

        secondsPerDay = 60 * 60 * 8;
        secondsPerWeek = secondsPerDay * 5;
        assertEquals("1 week, 4 days", DateUtils.getDurationPrettySeconds(60 * 24 * 3 * MINUTE, secondsPerDay, secondsPerWeek, resourceBundle));
    }

    public void testToEndOfPeriod()
    {
        final int year = 2005;
        final int month = Calendar.OCTOBER;
        final int date = 15;
        final int hour = 13;
        final int minute = 22;
        final int second = 22;

        // Set the date we're playing with to 2005/10/15 13:22:22
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        // Try getting the maximum of the year. We should end up with 2005/12/31 23:59:59
        Calendar yearPeriod = DateUtils.toEndOfPeriod((Calendar) calendar.clone(), Calendar.YEAR);
        assertEquals(year, yearPeriod.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, yearPeriod.get(Calendar.MONTH));
        assertEquals(31, yearPeriod.get(Calendar.DATE));
        assertEquals(23, yearPeriod.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, yearPeriod.get(Calendar.MINUTE));
        assertEquals(59, yearPeriod.get(Calendar.SECOND));

        // Try getting the maximum of the month. We should end up with 2005/10/31 23:59:59
        Calendar monthPeriod = DateUtils.toEndOfPeriod((Calendar) calendar.clone(), Calendar.MONTH);
        assertEquals(year, monthPeriod.get(Calendar.YEAR));
        assertEquals(month, monthPeriod.get(Calendar.MONTH));
        assertEquals(31, monthPeriod.get(Calendar.DATE));
        assertEquals(23, monthPeriod.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, monthPeriod.get(Calendar.MINUTE));
        assertEquals(59, monthPeriod.get(Calendar.SECOND));

        // Try getting the maximum of the date. We should end up with 2005/10/15 23:59:59
        Calendar datePeriod = DateUtils.toEndOfPeriod((Calendar) calendar.clone(), Calendar.DATE);
        assertEquals(year, datePeriod.get(Calendar.YEAR));
        assertEquals(month, datePeriod.get(Calendar.MONTH));
        assertEquals(date, datePeriod.get(Calendar.DATE));
        assertEquals(23, datePeriod.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, datePeriod.get(Calendar.MINUTE));
        assertEquals(59, datePeriod.get(Calendar.SECOND));

        // Try getting the maximum of the hour. We should end up with 2005/10/15 13:59:59
        Calendar hourPeriod = DateUtils.toEndOfPeriod((Calendar) calendar.clone(), Calendar.HOUR_OF_DAY);
        assertEquals(year, hourPeriod.get(Calendar.YEAR));
        assertEquals(month, hourPeriod.get(Calendar.MONTH));
        assertEquals(date, hourPeriod.get(Calendar.DATE));
        assertEquals(hour, hourPeriod.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, hourPeriod.get(Calendar.MINUTE));
        assertEquals(59, hourPeriod.get(Calendar.SECOND));

        // Try getting the maximum of the minute. We should end up with 2005/10/15 13:22:59
        Calendar minutePeriod = DateUtils.toEndOfPeriod((Calendar) calendar.clone(), Calendar.MINUTE);
        assertEquals(year, minutePeriod.get(Calendar.YEAR));
        assertEquals(month, minutePeriod.get(Calendar.MONTH));
        assertEquals(date, minutePeriod.get(Calendar.DATE));
        assertEquals(hour, minutePeriod.get(Calendar.HOUR_OF_DAY));
        assertEquals(minute, minutePeriod.get(Calendar.MINUTE));
        assertEquals(59, minutePeriod.get(Calendar.SECOND));

        try
        {
            DateUtils.toEndOfPeriod((Calendar)calendar.clone(), -10000);
            fail();
        }
        catch (IllegalArgumentException expected) {}

    }

    public void testToStartOfPeriod()
    {
        final int year = 2005;
        final int month = Calendar.OCTOBER;
        final int date = 15;
        final int hour = 13;
        final int minute = 22;
        final int second = 22;

        // Set the date we're playing with to 2005/10/15 13:22:22
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        // Try getting the minimum of the year. We should end up with 2005/01/01 00:00:00
        Calendar yearPeriod = DateUtils.toStartOfPeriod((Calendar) calendar.clone(), Calendar.YEAR);
        assertEquals(year, yearPeriod.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, yearPeriod.get(Calendar.MONTH));
        assertEquals(1, yearPeriod.get(Calendar.DATE));
        assertEquals(0, yearPeriod.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, yearPeriod.get(Calendar.MINUTE));
        assertEquals(0, yearPeriod.get(Calendar.SECOND));

        // Try getting the minimum of the month. We should end up with 2005/10/01 00:00:00
        Calendar monthPeriod = DateUtils.toStartOfPeriod((Calendar) calendar.clone(), Calendar.MONTH);
        assertEquals(year, monthPeriod.get(Calendar.YEAR));
        assertEquals(month, monthPeriod.get(Calendar.MONTH));
        assertEquals(1, monthPeriod.get(Calendar.DATE));
        assertEquals(0, monthPeriod.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, monthPeriod.get(Calendar.MINUTE));
        assertEquals(0, monthPeriod.get(Calendar.SECOND));

        // Try getting the minimum of the date. We should end up with 2005/10/15 00:00:00
        Calendar datePeriod = DateUtils.toStartOfPeriod((Calendar) calendar.clone(), Calendar.DATE);
        assertEquals(year, datePeriod.get(Calendar.YEAR));
        assertEquals(month, datePeriod.get(Calendar.MONTH));
        assertEquals(date, datePeriod.get(Calendar.DATE));
        assertEquals(0, datePeriod.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, datePeriod.get(Calendar.MINUTE));
        assertEquals(0, datePeriod.get(Calendar.SECOND));

        // Try getting the minimum of the hour. We should end up with 2005/10/15 13:00:00
        Calendar hourPeriod = DateUtils.toStartOfPeriod((Calendar) calendar.clone(), Calendar.HOUR_OF_DAY);
        assertEquals(year, hourPeriod.get(Calendar.YEAR));
        assertEquals(month, hourPeriod.get(Calendar.MONTH));
        assertEquals(date, hourPeriod.get(Calendar.DATE));
        assertEquals(hour, hourPeriod.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, hourPeriod.get(Calendar.MINUTE));
        assertEquals(0, hourPeriod.get(Calendar.SECOND));

        // Try getting the minimum of the minute. We should end up with 2005/10/15 13:22:00
        Calendar minutePeriod = DateUtils.toStartOfPeriod((Calendar) calendar.clone(), Calendar.MINUTE);
        assertEquals(year, minutePeriod.get(Calendar.YEAR));
        assertEquals(month, minutePeriod.get(Calendar.MONTH));
        assertEquals(date, minutePeriod.get(Calendar.DATE));
        assertEquals(hour, minutePeriod.get(Calendar.HOUR_OF_DAY));
        assertEquals(minute, minutePeriod.get(Calendar.MINUTE));
        assertEquals(0, minutePeriod.get(Calendar.SECOND));
        
        try
        {
            DateUtils.toStartOfPeriod((Calendar)calendar.clone(), -10000);
            fail();
        }
        catch (IllegalArgumentException expected) {}
    }

    public void testGet24HourTime()
    {
        assertEquals(0, DateUtils.get24HourTime("AM", 12));
        assertEquals(0, DateUtils.get24HourTime("am", 12));
        assertEquals(12, DateUtils.get24HourTime("PM", 12));
        assertEquals(12, DateUtils.get24HourTime("pm", 12));
        assertEquals(11, DateUtils.get24HourTime("AM", 11));
        assertEquals(1, DateUtils.get24HourTime("AM", 1));
        assertEquals(17, DateUtils.get24HourTime("PM", 5));
        assertEquals(23, DateUtils.get24HourTime("PM", 11));
        assertEquals(23, DateUtils.get24HourTime("pm", 11));

        // passing in something other than "am" or "pm" is technically allowed...you get back whatever you send in.
        assertEquals(12, DateUtils.get24HourTime("foo", 12));
    }

    public void testDateCalendarGetters() throws Exception
    {
        final Calendar calendar = DateUtils.getCalendarDay(2003, 5, 21);
        final Date date = DateUtils.getDateDay(2003, 5, 21);

        assertEquals(2003, calendar.get(Calendar.YEAR));
        assertEquals(5, calendar.get(Calendar.MONTH));
        assertEquals(21, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(date, calendar.getTime());
    }
}
