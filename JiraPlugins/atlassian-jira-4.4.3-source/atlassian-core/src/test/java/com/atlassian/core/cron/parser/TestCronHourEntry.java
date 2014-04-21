package com.atlassian.core.cron.parser;

import com.atlassian.core.util.DateUtils;
import junit.framework.TestCase;

/**
 *
 */
public class TestCronHourEntry extends TestCase
{
    public void testCronHoursEntryNoIncrementOrRange()
    {
        CronHoursEntry cronHoursEntry = new CronHoursEntry("2");
        assertTrue(cronHoursEntry.isValid());
        assertEquals(2, cronHoursEntry.getRunOnce());
        assertEquals(DateUtils.AM, cronHoursEntry.getRunOnceMeridian());
        assertEquals(-1, cronHoursEntry.getFrom());
        assertNull(cronHoursEntry.getFromMeridian());
        assertEquals(-1, cronHoursEntry.getTo());
        assertNull(cronHoursEntry.getToMeridian());
        assertEquals(-1, cronHoursEntry.getIncrement());
        assertFalse(cronHoursEntry.hasIncrement());
        assertTrue(cronHoursEntry.isRunOnce());
    }

    public void testCronHoursEntryWithIncrementNoRange()
    {
        CronHoursEntry cronHoursEntry = new CronHoursEntry("2/3");
        assertTrue(cronHoursEntry.isValid());
        assertEquals(-1, cronHoursEntry.getRunOnce());
        assertNull(cronHoursEntry.getRunOnceMeridian());
        assertEquals(2, cronHoursEntry.getFrom());
        assertEquals(DateUtils.AM, cronHoursEntry.getFromMeridian());
        assertEquals(2, cronHoursEntry.getTo());
        assertEquals(DateUtils.AM, cronHoursEntry.getToMeridian());
        assertEquals(3, cronHoursEntry.getIncrement());
        assertTrue(cronHoursEntry.hasIncrement());
        assertFalse(cronHoursEntry.isRunOnce());
    }

    public void testCronHoursEntryWithIncrementAndRange()
    {
        CronHoursEntry cronHoursEntry = new CronHoursEntry("2-16/3");
        assertTrue(cronHoursEntry.isValid());
        assertEquals(2, cronHoursEntry.getFrom());
        assertEquals(DateUtils.AM, cronHoursEntry.getFromMeridian());
        assertEquals(5, cronHoursEntry.getTo());
        assertEquals(DateUtils.PM, cronHoursEntry.getToMeridian());
        assertEquals(-1, cronHoursEntry.getRunOnce());
        assertNull(cronHoursEntry.getRunOnceMeridian());
        assertEquals(3, cronHoursEntry.getIncrement());
        assertTrue(cronHoursEntry.hasIncrement());
        assertFalse(cronHoursEntry.isRunOnce());
    }

    public void testInvalidCronHours()
    {
        CronHoursEntry cronHoursEntry = new CronHoursEntry("2,16/3");
        assertFalse(cronHoursEntry.isValid());
    }

    public void testOutOfRange()
    {
        assertFalse(new CronHoursEntry("42").isValid());
        assertFalse(new CronHoursEntry("1-25").isValid());
        assertFalse(new CronHoursEntry("0-24").isValid());
        assertFalse(new CronHoursEntry("44/2").isValid());
    }

    public void test12HourEdgeCases()
    {
        CronHoursEntry cronHoursEntry = new CronHoursEntry("0-11/3");
        assertTrue(cronHoursEntry.isValid());
        assertEquals(12, cronHoursEntry.getFrom());
        assertEquals(DateUtils.AM, cronHoursEntry.getFromMeridian());
        assertEquals(12, cronHoursEntry.getTo());
        assertEquals(DateUtils.PM, cronHoursEntry.getToMeridian());
        assertEquals(-1, cronHoursEntry.getRunOnce());
        assertFalse(cronHoursEntry.isRunOnce());

        cronHoursEntry = new CronHoursEntry("12-23/3");
        assertTrue(cronHoursEntry.isValid());
        assertEquals(12, cronHoursEntry.getFrom());
        assertEquals(DateUtils.PM, cronHoursEntry.getFromMeridian());
        assertEquals(12, cronHoursEntry.getTo());
        assertEquals(DateUtils.AM, cronHoursEntry.getToMeridian());
        assertEquals(-1, cronHoursEntry.getRunOnce());
        assertFalse(cronHoursEntry.isRunOnce());
    }

    public void testStar() throws Exception
    {
        CronHoursEntry cronHoursEntry = new CronHoursEntry("*");
        assertTrue(cronHoursEntry.isValid());
        assertEquals(12, cronHoursEntry.getFrom());
        assertEquals(DateUtils.AM, cronHoursEntry.getFromMeridian());
        assertEquals(12, cronHoursEntry.getTo());
        assertEquals(DateUtils.AM, cronHoursEntry.getToMeridian());
        assertEquals(1, cronHoursEntry.getIncrement());
        assertTrue(cronHoursEntry.hasIncrement());
        assertEquals(-1, cronHoursEntry.getRunOnce());
        assertFalse(cronHoursEntry.isRunOnce());
    }

    public void testStarWithIncrement() throws Exception
    {
        CronHoursEntry cronHoursEntry = new CronHoursEntry("*/2");
        assertTrue(cronHoursEntry.isValid());
        assertEquals(12, cronHoursEntry.getFrom());
        assertEquals(DateUtils.AM, cronHoursEntry.getFromMeridian());
        assertEquals(12, cronHoursEntry.getTo());
        assertEquals(DateUtils.AM, cronHoursEntry.getToMeridian());
        assertEquals(2, cronHoursEntry.getIncrement());
        assertTrue(cronHoursEntry.hasIncrement());
        assertEquals(-1, cronHoursEntry.getRunOnce());
        assertFalse(cronHoursEntry.isRunOnce());
    }

}
