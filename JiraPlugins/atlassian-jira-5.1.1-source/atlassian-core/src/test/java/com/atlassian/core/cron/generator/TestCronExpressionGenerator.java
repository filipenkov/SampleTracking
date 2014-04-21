package com.atlassian.core.cron.generator;

import com.atlassian.core.cron.CronEditorBean;
import com.atlassian.core.util.DateUtils;
import junit.framework.TestCase;

/**
 *
 */
public class TestCronExpressionGenerator extends TestCase
{
    public void testDailyCronWithRunOnce()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        cronEditorBean.setHoursRunOnce("9");
        cronEditorBean.setHoursRunOnceMeridian(DateUtils.AM);
        cronEditorBean.setMinutes("35");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals("0 35 9 ? * *", cronExpressionGenerator.getCronExpressionFromInput(cronEditorBean));
    }

    public void testDailyCronWithFromAndTo()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);

        cronEditorBean.setHoursFrom("1");
        cronEditorBean.setHoursFromMeridian(DateUtils.AM);
        cronEditorBean.setHoursTo("1");
        cronEditorBean.setHoursToMeridian(DateUtils.PM);

        cronEditorBean.setIncrementInMinutes("180");

        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals("0 0 1-12/3 ? * *", cronExpressionGenerator.getCronExpressionFromInput(cronEditorBean));
    }

    public void testDayOfWeekWithRunOnce()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAYS_OF_WEEK_SPEC_MODE);

        cronEditorBean.setHoursRunOnce("9");
        cronEditorBean.setHoursRunOnceMeridian(DateUtils.AM);
        cronEditorBean.setMinutes("35");

        cronEditorBean.setSpecifiedDaysOfWeek("2,3,4");

        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals("0 35 9 ? * 2,3,4", cronExpressionGenerator.getCronExpressionFromInput(cronEditorBean));
    }

    public void testDayOfWeekWithFromTo()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAYS_OF_WEEK_SPEC_MODE);

        cronEditorBean.setHoursFrom("1");
        cronEditorBean.setHoursFromMeridian(DateUtils.AM);
        cronEditorBean.setHoursTo("1");
        cronEditorBean.setHoursToMeridian(DateUtils.PM);

        cronEditorBean.setIncrementInMinutes("180");

        cronEditorBean.setSpecifiedDaysOfWeek("2,3,4");

        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals("0 0 1-12/3 ? * 2,3,4", cronExpressionGenerator.getCronExpressionFromInput(cronEditorBean));
    }

    public void testDayOfMonth()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAYS_OF_MONTH_SPEC_MODE);

        cronEditorBean.setMinutes("59");
        cronEditorBean.setHoursRunOnce("12");
        cronEditorBean.setHoursRunOnceMeridian(DateUtils.PM);

        cronEditorBean.setDayOfMonth("13");
        cronEditorBean.setDayOfWeekOfMonth(false);

        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals("0 59 12 13 * ?", cronExpressionGenerator.getCronExpressionFromInput(cronEditorBean));
    }

    public void testDayOfWeekOfMonth()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAYS_OF_MONTH_SPEC_MODE);

        cronEditorBean.setMinutes("59");
        cronEditorBean.setHoursRunOnce("12");
        cronEditorBean.setHoursRunOnceMeridian(DateUtils.AM);

        cronEditorBean.setSpecifiedDaysOfWeek("2");
        cronEditorBean.setDayInMonthOrdinal("3");
        cronEditorBean.setDayOfWeekOfMonth(true);

        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals("0 59 0 ? * 2#3", cronExpressionGenerator.getCronExpressionFromInput(cronEditorBean));
    }

    public void testGenerateDailySpecWithIntervalAndRunOnce()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        cronEditorBean.setIncrementInMinutes("60");
        cronEditorBean.setHoursRunOnce("9");
        cronEditorBean.setHoursRunOnceMeridian(DateUtils.AM);
        cronEditorBean.setMinutes("35");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        try
        {
            cronExpressionGenerator.generateDailySpec(cronEditorBean);
            fail();
        }
        catch (IllegalStateException expected)
        {
            // ayay
        }
    }

    public void testGenerateDailySpecWithNoIntervalAndNoRunOnce()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        try
        {
            cronExpressionGenerator.generateDailySpec(cronEditorBean);
            fail();
        }
        catch (IllegalStateException expected)
        {
            // ayay
        }
    }

    public void testGenerateDailySpecRunOnce()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        cronEditorBean.setHoursRunOnce("4");
        cronEditorBean.setMinutes("25");
        cronEditorBean.setIncrementInMinutes("0");
        cronEditorBean.setHoursRunOnceMeridian(DateUtils.PM);
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        String dailySpec = cronExpressionGenerator.generateDailySpec(cronEditorBean);
        assertEquals("0 25 16", dailySpec);
    }

    public void testGenerateDailySpecRunOnceInMonthMode()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAYS_OF_MONTH_SPEC_MODE);
        cronEditorBean.setHoursRunOnce("4");
        cronEditorBean.setMinutes("25");
        // Set this to something other than one, this should be ignored because of the mode
        // This is a special case.
        cronEditorBean.setIncrementInMinutes("120");
        cronEditorBean.setHoursRunOnceMeridian(DateUtils.PM);
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        String dailySpec = cronExpressionGenerator.generateDailySpec(cronEditorBean);
        assertEquals("0 25 16", dailySpec);
    }

    public void testGenerateDailySpecFromToWithHourInterval()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        cronEditorBean.setHoursFrom("4");
        cronEditorBean.setHoursFromMeridian(DateUtils.AM);
        cronEditorBean.setHoursTo("4");
        cronEditorBean.setHoursToMeridian(DateUtils.PM);
        cronEditorBean.setIncrementInMinutes("120");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        String dailySpec = cronExpressionGenerator.generateDailySpec(cronEditorBean);
        assertEquals("0 0 4-15/2", dailySpec);
    }

    public void testGenerateDailySpecFromToWithMinuteInterval()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        cronEditorBean.setHoursFrom("4");
        cronEditorBean.setHoursFromMeridian(DateUtils.AM);
        cronEditorBean.setHoursTo("4");
        cronEditorBean.setHoursToMeridian(DateUtils.PM);
        cronEditorBean.setIncrementInMinutes("15");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        String dailySpec = cronExpressionGenerator.generateDailySpec(cronEditorBean);
        assertEquals("0 0/15 4-15", dailySpec);
    }

    public void testGenerateDailySpecWithEqualFromToMinuteInterval()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        cronEditorBean.setHoursFrom("4");
        cronEditorBean.setHoursFromMeridian(DateUtils.AM);
        cronEditorBean.setHoursTo("4");
        cronEditorBean.setHoursToMeridian(DateUtils.AM);
        cronEditorBean.setIncrementInMinutes("15");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        String dailySpec = cronExpressionGenerator.generateDailySpec(cronEditorBean);
        assertEquals("0 0/15 *", dailySpec);
    }

    public void testGenerateDailySpecWithEqualFromToHourInterval()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setMode(CronEditorBean.DAILY_SPEC_MODE);
        cronEditorBean.setHoursFrom("4");
        cronEditorBean.setHoursFromMeridian(DateUtils.AM);
        cronEditorBean.setHoursTo("4");
        cronEditorBean.setHoursToMeridian(DateUtils.AM);
        cronEditorBean.setIncrementInMinutes("120");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        String dailySpec = cronExpressionGenerator.generateDailySpec(cronEditorBean);
        assertEquals("0 0 */2", dailySpec);
    }

    public void testGenerateDaysOfWeekSpecWithDaysSet()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        String specifiedDaysOfWeek = "2,3,4";
        cronEditorBean.setSpecifiedDaysOfWeek(specifiedDaysOfWeek);
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals(specifiedDaysOfWeek, cronExpressionGenerator.generateDaysOfWeekSpec(cronEditorBean));
    }

    public void testGenerateDaysOfWeekSpecWithDaysNotSet()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        try
        {
            cronExpressionGenerator.generateDaysOfWeekSpec(cronEditorBean);
            fail();
        }
        catch (IllegalStateException e)
        {
            // Woo Hoo
        }
    }

    public void testGenerateDayOfMonthSpecWithDayOfMonthSet()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        String dayOfMonth = "20";
        cronEditorBean.setDayOfMonth(dayOfMonth);
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals(dayOfMonth + " * ?", cronExpressionGenerator.generateDayOfMonthSpec(cronEditorBean));
    }

    public void testGenerateDayOfMonthSpecWithDayOfMonthNotSet()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        try
        {
            cronExpressionGenerator.generateDaysOfWeekSpec(cronEditorBean);
            fail();
        }
        catch (IllegalStateException ise)
        {
            // WQooo hoo
        }
    }

    public void testGenerateDayOfWeekOfMonthSpecLastMonday()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setDayInMonthOrdinal("L");
        cronEditorBean.setSpecifiedDaysOfWeek("2");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals("? * 2L", cronExpressionGenerator.generateDayOfWeekOfMonthSpec(cronEditorBean));
    }

    public void testGenerateDayOfWeekOfMonthSpecSecondTuesday()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setDayInMonthOrdinal("2");
        cronEditorBean.setSpecifiedDaysOfWeek("3");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();
        assertEquals("? * 3#2", cronExpressionGenerator.generateDayOfWeekOfMonthSpec(cronEditorBean));
    }

    public void testGenerateDayOfWeekOfMonthWithoutOrdinal()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setSpecifiedDaysOfWeek("3");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();

        try
        {
            cronExpressionGenerator.generateDayOfWeekOfMonthSpec(cronEditorBean);
            fail();
        }
        catch (IllegalStateException e)
        {
            // Wook hoo
        }
    }

    public void testGenerateDayOfWeekOfMonthWithoutSpecifiedDaysPerWeek()
    {
        CronEditorBean cronEditorBean = new CronEditorBean();
        cronEditorBean.setDayInMonthOrdinal("2");
        CronExpressionGenerator cronExpressionGenerator = new CronExpressionGenerator();

        try
        {
            cronExpressionGenerator.generateDayOfWeekOfMonthSpec(cronEditorBean);
            fail();
        }
        catch (IllegalStateException e)
        {
            // Wook hoo
        }
    }


}
