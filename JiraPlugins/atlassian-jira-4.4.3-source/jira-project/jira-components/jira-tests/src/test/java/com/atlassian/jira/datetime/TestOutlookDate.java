/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.datetime;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.util.ConstantClock;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.OutlookDate;
import com.opensymphony.module.propertyset.PropertySet;
import org.joda.time.DateTime;

import javax.mail.internet.MailDateFormat;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class TestOutlookDate extends LegacyJiraMockTestCase
{
    DateFormat timeFormatter = new SimpleDateFormat("hh:mm a", Locale.US);
    DateFormat dayFormatter = new SimpleDateFormat("EEEE hh:mm a", Locale.US);
    DateFormat completeFormatter = new SimpleDateFormat("dd/MMM/yy hh:mm a", Locale.US);
    DateFormat dmyFormatter = new SimpleDateFormat("dd/MMM/yy", Locale.US);

    public TestOutlookDate(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);
    }

    @Override
    protected void tearDown() throws Exception
    {
        final PropertySet propertySet =  PropertiesManager.getInstance().getPropertySet();

        propertySet.setString(APKeys.JIRA_LF_DATE_TIME, null);
        propertySet.setString(APKeys.JIRA_LF_DATE_DAY, null);
        propertySet.setString(APKeys.JIRA_LF_DATE_COMPLETE, null);
        propertySet.setString(APKeys.JIRA_LF_DATE_DMY, null);

        super.tearDown();
    }

    public void testFormatDMY() throws ParseException
    {
        final Date date = dmyFormatter.parse("15/Jul/02");
        final OutlookDate outlook = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);
        assertEquals("15/Jul/02", outlook.formatDMY(date));
        assertEquals("15/Jul/02", outlook.formatDMY(new Timestamp(date.getTime())));
    }

    public void testToday()
    {
        final OutlookDate outlook = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);
        final Date date = new Date();
        assertEquals("Today " + timeFormatter.format(date), outlook.format(date));
        assertEquals("Today " + timeFormatter.format(date), outlook.format(new Timestamp(date.getTime())));

        delayIfAtMidnight();
        assertEquals("Today " + timeFormatter.format(new Date()), outlook.format());
    }

    public void testParseDatePickerDate() throws Exception
    {
        final OutlookDate outlook = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);
        assertNotNull(outlook.parseDatePicker("04/Jul/07 12:16 PM"));
    }

    /**
     * If this falls at midnight there is a slight chance that the two dates will be on different days
     * (as it happened on 5/11/2007 :-)
     */
    private void delayIfAtMidnight()
    {
        final Calendar cal = Calendar.getInstance();
        if ((cal.get(Calendar.HOUR_OF_DAY) == 0) && (cal.get(Calendar.MINUTE) == 0))
        {
            // delay for 5 seconds if running in the first minute of the day
            try
            {
                Thread.sleep(5000); // sleep for 5 seconds
            }
            catch (final InterruptedException e)
            {
                // carry on
            }
        }
    }

    public void testYesterday()
    {
        final OutlookDate outlook = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);
        final DateTime dateTime = new DateTime();
        final Date date = dateTime.minusDays(1).toDate();
        assertEquals("Yesterday " + timeFormatter.format(date), outlook.format(date));
    }

    public void testThisWeek()
    {
        final OutlookDate outlook = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);
        final Date date = new Date(System.currentTimeMillis() - (3 * DateUtils.DAY_MILLIS));
        assertEquals(dayFormatter.format(date), outlook.format(date));
    }

    public void testOldDate()
    {
        final OutlookDate outlook = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);
        final DateTime dateTime = new DateTime();
        final Date date = dateTime.minusDays(7).toDate();
        assertEquals(completeFormatter.format(date), outlook.format(date));
    }

    public void testParseDate() throws ParseException
    {
        final OutlookDate outlook = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);
        final Date date = new DateTime(2000, 1, 1, 11, 23, 0, 0).toDate();
        assertEquals(date, outlook.parseCompleteDateTime("1/Jan/00 11:23 AM"));
    }

    /**
     * This method is used to test the performace of the testDaysAgo method. It is run
     * manually rather than part of the automated test suite.
     */
    public void _testDaysAgoPerf()
    {
        for (int i = 0; i < 100000; i++)
        {
            testDaysAgo();
        }
    }

    public void testDaysAgo()
    {
        final OutlookDate outlook = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);

        //-------- Todays Dates ----------//
        //create a date at 5am
        final GregorianCalendar todayFiveAm = new GregorianCalendar(2003, 7, 21, 5, 0);
        final Date todayFiveAmDate = todayFiveAm.getTime();

        //create a date at 11pm
        final GregorianCalendar todayElevenPm = new GregorianCalendar(2003, 7, 21, 23, 0);
        final Date todayElevenPmDate = todayElevenPm.getTime();

        //-------- Tomorrow's Dates ----------//
        //create a date tomorrow at 5am
        final GregorianCalendar tomorrowFiveAm = new GregorianCalendar(2003, 7, 22, 5, 0);
        final Date tomorrowFiveAmDate = tomorrowFiveAm.getTime();

        //create a date tomorrow at 11pm
        final GregorianCalendar tomorrowElevenPm = new GregorianCalendar(2003, 7, 22, 23, 0);
        final Date tomorrowElevenPmDate = tomorrowElevenPm.getTime();

        //-------- Yesterday's Dates ----------//
        //create a date yesterday at 10pm
        final GregorianCalendar yesterdayTenPm = new GregorianCalendar(2003, 7, 20, 22, 0);
        final Date yesterdayTenPmDate = yesterdayTenPm.getTime();

        //both test today vs yesterday, so should both return one day
        assertEquals(OutlookDate.TODAY, outlook.daysAgo(todayFiveAmDate, todayElevenPmDate));
        assertEquals(OutlookDate.TODAY, outlook.daysAgo(todayElevenPmDate, todayFiveAmDate));

        assertEquals(OutlookDate.YESTERDAY, outlook.daysAgo(yesterdayTenPmDate, todayElevenPmDate));
        assertEquals(OutlookDate.YESTERDAY, outlook.daysAgo(yesterdayTenPmDate, todayFiveAmDate));

        assertEquals(OutlookDate.THIS_WEEK, outlook.daysAgo(yesterdayTenPmDate, tomorrowElevenPmDate));
        assertEquals(OutlookDate.THIS_WEEK, outlook.daysAgo(yesterdayTenPmDate, tomorrowFiveAmDate));
    }

    public void testSmartFormatter() throws Exception
    {
        final int year = 2008;
        final int month = 1;
        // centre point
        final Date today = new GregorianCalendar(year, month, 15, 3, 4, 5).getTime();

        assertSmartKeyNull(today, year, month, 1);
        assertSmartKeyNull(today, year, month, 2);
        assertSmartKeyNull(today, year, month, 3);
        assertSmartKeyNull(today, year, month, 4);
        assertSmartKeyNull(today, year, month, 5);
        assertSmartKeyNull(today, year, month, 6);
        assertSmartKeyNull(today, year, month, 7);
        assertSmartKeyLastWeek(today, year, month, 8, 5);
        assertSmartKeyLastWeek(today, year, month, 9, 6);
        assertSmartKeyLastWeek(today, year, month, 10, 7);
        assertSmartKeyLastWeek(today, year, month, 11, 1);
        assertSmartKeyLastWeek(today, year, month, 12, 2);
        assertSmartKeyLastWeek(today, year, month, 13, 3);
        assertSmartKeyEquals(today, year, month, 14, "common.concepts.yesterday");
        assertSmartKeyEquals(today, year, month, 15, "common.concepts.today");
        assertSmartKeyEquals(today, year, month, 16, "common.concepts.tomorrow");
        assertSmartKeyNextWeek(today, year, month, 17, 7);
        assertSmartKeyNextWeek(today, year, month, 18, 1);
        assertSmartKeyNextWeek(today, year, month, 19, 2);
        assertSmartKeyNextWeek(today, year, month, 20, 3);
        assertSmartKeyNextWeek(today, year, month, 21, 4);
        assertSmartKeyNextWeek(today, year, month, 22, 5);
        assertSmartKeyNull(today, year, month, 23);
        assertSmartKeyNull(today, year, month, 24);
        assertSmartKeyNull(today, year, month, 25);
        assertSmartKeyNull(today, year, month, 26);
        assertSmartKeyNull(today, year, month, 27);
        assertSmartKeyNull(today, year, month, 28);

        // this is in March, we are just smartasses
        assertSmartKeyNull(today, year, month, 29);
        assertSmartKeyNull(today, year, month, 30);
        assertSmartKeyNull(today, year, month, 31);
        assertSmartKeyNull(today, year, month, 32);
    }

    /**
     * In St Louis (where our builds run) Daylight Savings Time started on Sunday, March 9, 2008 at 2:00 AM local standard time
     * Note: in other time zones, this shouldn't fail - it just doesn't check DST.
     */
    public void testSmartFormatterDST1()
    {
        final int year = 2008;
        final int month = 2; // 0-based months
        // centre point
        final Date today = new GregorianCalendar(year, month, 14, 3, 4, 5).getTime();

        assertSmartKeyNull(today, year, month, 1);
        assertSmartKeyNull(today, year, month, 2);
        assertSmartKeyNull(today, year, month, 3);
        assertSmartKeyNull(today, year, month, 4);
        assertSmartKeyNull(today, year, month, 5);
        assertSmartKeyNull(today, year, month, 6);
        assertSmartKeyLastWeek(today, year, month, 7, 5);
        assertSmartKeyLastWeek(today, year, month, 8, 6);
        assertSmartKeyLastWeek(today, year, month, 9, 7);
        assertSmartKeyLastWeek(today, year, month, 10, 1);
        assertSmartKeyLastWeek(today, year, month, 11, 2);
        assertSmartKeyLastWeek(today, year, month, 12, 3);
        assertSmartKeyEquals(today, year, month, 13, "common.concepts.yesterday");
        assertSmartKeyEquals(today, year, month, 14, "common.concepts.today");
        assertSmartKeyEquals(today, year, month, 15, "common.concepts.tomorrow");
        assertSmartKeyNextWeek(today, year, month, 16, 7);
        assertSmartKeyNextWeek(today, year, month, 17, 1);
        assertSmartKeyNextWeek(today, year, month, 18, 2);
        assertSmartKeyNextWeek(today, year, month, 19, 3);
        assertSmartKeyNextWeek(today, year, month, 20, 4);
        assertSmartKeyNextWeek(today, year, month, 21, 5);
        assertSmartKeyNull(today, year, month, 22);
        assertSmartKeyNull(today, year, month, 23);
        assertSmartKeyNull(today, year, month, 24);
        assertSmartKeyNull(today, year, month, 25);
        assertSmartKeyNull(today, year, month, 26);
        assertSmartKeyNull(today, year, month, 27);
        assertSmartKeyNull(today, year, month, 28);
        assertSmartKeyNull(today, year, month, 29);
        assertSmartKeyNull(today, year, month, 30);
        assertSmartKeyNull(today, year, month, 31);
    }

    /**
     * In St Louis (where our builds run) Daylight Savings Time started on Sunday, November 2, 2008 at 2:00 AM local daylight time
     * Note: in other time zones, this shouldn't fail - it just doesn't check DST.
     */
    public void testSmartFormatterDST2()
    {
        final int year = 2008;
        final int month = 10; // 0-based months
        // centre point
        final Date today = new GregorianCalendar(year, month, 1, 3, 4, 5).getTime();

        assertSmartKeyNull(today, year, month - 1, 24);
        assertSmartKeyLastWeek(today, year, month - 1, 25, 6);
        assertSmartKeyLastWeek(today, year, month - 1, 26, 7);
        assertSmartKeyLastWeek(today, year, month - 1, 27, 1);
        assertSmartKeyLastWeek(today, year, month - 1, 28, 2);
        assertSmartKeyLastWeek(today, year, month - 1, 29, 3);
        assertSmartKeyLastWeek(today, year, month - 1, 30, 4);
        assertSmartKeyEquals(today, year, month - 1, 31, "common.concepts.yesterday");
        assertSmartKeyEquals(today, year, month, 1, "common.concepts.today");
        assertSmartKeyEquals(today, year, month, 2, "common.concepts.tomorrow");
        assertSmartKeyNextWeek(today, year, month, 3, 1);
        assertSmartKeyNextWeek(today, year, month, 4, 2);
        assertSmartKeyNextWeek(today, year, month, 5, 3);
        assertSmartKeyNextWeek(today, year, month, 6, 4);
        assertSmartKeyNextWeek(today, year, month, 7, 5);
        assertSmartKeyNextWeek(today, year, month, 8, 6);
        assertSmartKeyNull(today, year, month, 9);
    }

    public void testFormatSmartShowTime() throws Exception
    {
        final ApplicationProperties applicationProperties = ComponentManager.getComponentInstanceOfType(ApplicationProperties.class);
        final JiraAuthenticationContext jiraAuthenticationContext = ComponentManager.getComponentInstanceOfType(JiraAuthenticationContext.class);
        final I18nHelper.BeanFactory i18n = ComponentManager.getComponentInstanceOfType(I18nHelper.BeanFactory.class);
        final JodaFormatterCache jodaFormatterCache = new JodaFormatterCache();

        DateTimeFormatterServiceProvider dateTimeFormatterServiceProvider = createDateTimeFormatterServiceProvider(applicationProperties, jiraAuthenticationContext, i18n);

        final Date today = new GregorianCalendar(2008, 1, 15, 3, 4, 5).getTime();
        DateTimeRelativeDatesAlwaysWithTime alwaysWithTime = new DateTimeRelativeDatesAlwaysWithTime(dateTimeFormatterServiceProvider, applicationProperties, ComponentManager.getComponentInstanceOfType(TimeZoneService.class), jiraAuthenticationContext, jodaFormatterCache, new ConstantClock(today));

        for (int i = 1; i <= 28; i++)
        {
            assertTimePresent(alwaysWithTime, 2008, 1, i);
        }
    }

    public void testFormatSmartHideTime() throws Exception
    {
        final ApplicationProperties applicationProperties = ComponentManager.getComponentInstanceOfType(ApplicationProperties.class);
        final JiraAuthenticationContext jiraAuthenticationContext = ComponentManager.getComponentInstanceOfType(JiraAuthenticationContext.class);
        final I18nHelper.BeanFactory i18n = ComponentManager.getComponentInstanceOfType(I18nHelper.BeanFactory.class);
        final JodaFormatterCache jodaFormatterCache = new JodaFormatterCache();

        DateTimeFormatterServiceProvider dateTimeFormatterServiceProvider = createDateTimeFormatterServiceProvider(applicationProperties, jiraAuthenticationContext, i18n);
        final Date today = new GregorianCalendar(2008, 1, 15, 3, 4, 5).getTime();
        DateTimeRelativeNoTimeFormatter noTimeFormatter = new DateTimeRelativeNoTimeFormatter(dateTimeFormatterServiceProvider, applicationProperties, ComponentManager.getComponentInstanceOfType(TimeZoneService.class), jiraAuthenticationContext, jodaFormatterCache, new ConstantClock(today));
        for (int i = 1; i <= 28; i++)
        {
            assertTimeNotPresent(noTimeFormatter, 2008, 1, i);
        }
    }

    public void testFormatSmartShowTimeOnlyOnDays() throws Exception
    {
        final ApplicationProperties applicationProperties = ComponentManager.getComponentInstanceOfType(ApplicationProperties.class);
        final JiraAuthenticationContext jiraAuthenticationContext = ComponentManager.getComponentInstanceOfType(JiraAuthenticationContext.class);
        final I18nHelper.BeanFactory i18n = ComponentManager.getComponentInstanceOfType(I18nHelper.BeanFactory.class);
        final JodaFormatterCache jodaFormatterCache = new JodaFormatterCache();

        DateTimeFormatterServiceProvider dateTimeFormatterServiceProvider = createDateTimeFormatterServiceProvider(applicationProperties, jiraAuthenticationContext, i18n);

        final Date today = new GregorianCalendar(2008, 1, 15, 3, 4, 5).getTime();
        DateTimeRelativeDatesWithTimeFormatter dateTimeRelativeDatesWithTimeFormatter = new DateTimeRelativeDatesWithTimeFormatter(dateTimeFormatterServiceProvider, applicationProperties, ComponentManager.getComponentInstanceOfType(TimeZoneService.class), jiraAuthenticationContext, jodaFormatterCache, new ConstantClock(today));

        for (int i = 1; i <= 7; i++)
        {
            assertTimeNotPresent(dateTimeRelativeDatesWithTimeFormatter, 2008, 1, i);
        }
        for (int i = 8; i <= 22; i++)
        {
            assertTimePresent(dateTimeRelativeDatesWithTimeFormatter, 2008, 1, i);
        }
        for (int i = 23; i <= 28; i++)
        {
            assertTimeNotPresent(dateTimeRelativeDatesWithTimeFormatter, 2008, 1, i);
        }
    }

    private DateTimeFormatterServiceProvider createDateTimeFormatterServiceProvider(final ApplicationProperties applicationProperties, final JiraAuthenticationContext jiraAuthenticationContext, final I18nHelper.BeanFactory i18n)
    {
        return new DateTimeFormatterServiceProvider()
            {
                @Override
                public String getDefaultBackedString(String key)
                {
                    return applicationProperties.getDefaultBackedString(key);
                }

                @Override
                public String getUnescapedText(String key)
                {
                    return i18n.getInstance(jiraAuthenticationContext.getLoggedInUser()).getUnescapedText(key);
                }

                @Override
                public String getText(String key, Object... parameters)
                {
                   return i18n.getInstance(jiraAuthenticationContext.getLoggedInUser()).getText(key, parameters);
                }
            };
    }

    private void assertTimePresent(DateTimeFormatStrategy dateTimeFormatStrategy, final int year, final int month, final int day)
    {
        final Date dayStart = new GregorianCalendar(year, month, day, 0, 0, 0).getTime();
        assertTrue(dateTimeFormatStrategy.format(new DateTime(dayStart), Locale.getDefault()).contains("12:00 AM"));
        final Date dayEnd = new GregorianCalendar(year, month, day, 23, 59, 59).getTime();
        assertTrue(dateTimeFormatStrategy.format(new DateTime(dayEnd), Locale.getDefault()).contains("11:59 PM"));
    }

    private void assertTimeNotPresent(DateTimeFormatStrategy dateTimeFormatStrategy, final int year, final int month, final int day)
    {
        final Date dayStart = new GregorianCalendar(year, month, day, 0, 0, 0).getTime();
        assertFalse(dateTimeFormatStrategy.format(new DateTime(dayStart), Locale.getDefault()).contains("12:00 AM"));
        final Date dayEnd = new GregorianCalendar(year, month, day, 23, 59, 59).getTime();
        assertFalse(dateTimeFormatStrategy.format(new DateTime(dayEnd),Locale.getDefault()).contains("11:59 PM"));
    }

    private void assertSmartKeyNull(final Date today, final int year, final int month, final int day)
    {
        // note: time of day should not make a difference
        final Date dayStart = new GregorianCalendar(year, month, day, 0, 0, 0).getTime();
        assertNull(AbstractDateTimeRelativeDatesFormatter.RelativeFormatter.getDayI18nKey(new DateTime(dayStart), new DateTime(today)));
        final Date dayEnd = new GregorianCalendar(year, month, day, 23, 59, 59).getTime();
        assertNull(AbstractDateTimeRelativeDatesFormatter.RelativeFormatter.getDayI18nKey(new DateTime(dayEnd), new DateTime(today)));
    }

    private void assertSmartKeyLastWeek(final Date today, final int year, final int month, final int day, final int dow)
    {
        // note: time of day should not make a difference
        final Date dayStart = new GregorianCalendar(year, month, day, 0, 0, 0).getTime();
        assertEquals("common.date.relative.days.last." + dow, AbstractDateTimeRelativeDatesFormatter.RelativeFormatter.getDayI18nKey(new DateTime(dayStart), new DateTime(today)));
        final Date dayEnd = new GregorianCalendar(year, month, day, 23, 59, 59).getTime();
        assertEquals("common.date.relative.days.last." + dow, AbstractDateTimeRelativeDatesFormatter.RelativeFormatter.getDayI18nKey(new DateTime(dayEnd), new DateTime(today)));
    }

    private void assertSmartKeyEquals(final Date today, final int year, final int month, final int day, final String key)
    {
        // note: time of day should not make a difference
        final Date dayStart = new GregorianCalendar(year, month, day, 0, 0, 0).getTime();
        assertEquals(key, AbstractDateTimeRelativeDatesFormatter.RelativeFormatter.getDayI18nKey(new DateTime(dayStart), new DateTime(today)));
        final Date dayEnd = new GregorianCalendar(year, month, day, 23, 59, 59).getTime();
        assertEquals(key, AbstractDateTimeRelativeDatesFormatter.RelativeFormatter.getDayI18nKey(new DateTime(dayEnd), new DateTime(today)));
    }

    private void assertSmartKeyNextWeek(final Date today, final int year, final int month, final int day, final int dow)
    {
        // note: time of day should not make a difference
        final Date dayStart = new GregorianCalendar(year, month, day, 0, 0, 0).getTime();
        assertEquals("common.date.relative.days.next." + dow, AbstractDateTimeRelativeDatesFormatter.RelativeFormatter.getDayI18nKey(new DateTime(dayStart), new DateTime(today)));
        final Date dayEnd = new GregorianCalendar(year, month, day, 23, 59, 59).getTime();
        assertEquals("common.date.relative.days.next." + dow, AbstractDateTimeRelativeDatesFormatter.RelativeFormatter.getDayI18nKey(new DateTime(dayEnd), new DateTime(today)));
    }

    public void testDaysAgoAcrossNewYear()
    {
        final OutlookDate outlook = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);

        //create a date at 1am
        final GregorianCalendar todayOneAm = new GregorianCalendar(2003, 1, 1, 1, 0);
        final Date todayFiveAmDate = todayOneAm.getTime();

        //create a date yesterday at 11pm
        final GregorianCalendar yesterdayTenPm = new GregorianCalendar(2002, 12, 31, 23, 0);
        final Date yesterdayTenPmDate = yesterdayTenPm.getTime();

        //test today vs yesterday, so should return one day
        assertEquals(1, outlook.daysAgo(yesterdayTenPmDate, todayFiveAmDate));
    }

    public void testIsDatePickerDateDate()
    {
        final OutlookDate outlook = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);
        assertTrue(outlook.isDatePickerDate("15/May/2000"));
        assertFalse(outlook.isDatePickerDate("15/May/"));
        assertFalse(outlook.isDatePickerDate("15foobar"));
        assertFalse(outlook.isDatePickerDate(""));
        assertFalse(outlook.isDatePickerDate(null));
    }

    public void testFormatIso8601() throws ParseException
    {
        final Date date = dmyFormatter.parse("15/Jul/02");
        final OutlookDate outlook = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);
        assertEquals("2002-07-15T00-00", outlook.formatIso8601(date));
        assertEquals("2002-07-15T00-00", outlook.formatIso8601(new Timestamp(date.getTime())));
    }

    public void testFormatIso8601Date() throws ParseException
    {
        final Date date = dmyFormatter.parse("15/Jul/02");
        final OutlookDate outlook = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);
        assertEquals("2002-07-15", outlook.formatIso8601Date(date));
        assertEquals("2002-07-15", outlook.formatIso8601Date(new Timestamp(date.getTime())));
    }

    public void testFormatRss() throws ParseException
    {
        final Date date = dmyFormatter.parse("15/Jul/02");
        final String expected = "Mon, 15 Jul 2002 00:00:00 " + new SimpleDateFormat("Z").format(date);
        final OutlookDate outlook = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);
        assertEquals(expected, outlook.formatRss(date));
        assertEquals(expected, outlook.formatRss(new Timestamp(date.getTime())));
    }

    public void testFormatRssRfc822() throws ParseException
    {
        final Date date = dmyFormatter.parse("15/Jul/02");
        final String expected = "Mon, 15 Jul 2002 00:00:00 " + new SimpleDateFormat("Z").format(date);
        final OutlookDate outlook = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);
        assertEquals(expected, outlook.formatRssRfc822(date));
        assertEquals(expected, outlook.formatRssRfc822(new Timestamp(date.getTime())));
    }

    /*
     Test the MailDateFormat to see if it can parse differing formats.  This is used by OutlookDate
     */
    public void testMailDateFormat()
    {
        Date dt;
        ParsePosition pp;
        final MailDateFormat mailDateFormat = new MailDateFormat();

        pp = new ParsePosition(0);
        dt = mailDateFormat.parse("Wed, 22 Aug 2007 10:00:10 +1000 (GMT+10:00)", pp);
        assertEquals(-1, pp.getErrorIndex());
        assertNotNull(dt);

        pp = new ParsePosition(0);
        dt = mailDateFormat.parse("Wed, 22 Aug 2007 10:00:10 +1000", pp);
        assertEquals(-1, pp.getErrorIndex());
        assertNotNull(dt);

        // ok it looks good can it parse a SimpleDate equivalent
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        String dateStr = sdf.format(dt);

        pp = new ParsePosition(0);
        dt = mailDateFormat.parse(dateStr, pp);
        assertEquals(-1, pp.getErrorIndex());
        assertNotNull(dt);

        final OutlookDate outlook = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.ENGLISH);
        dateStr = outlook.formatRssRfc822(dt);
        pp = new ParsePosition(0);
        dt = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").parse(dateStr, pp);
        assertEquals(-1, pp.getErrorIndex());
        assertNotNull(dt);

    }
}
