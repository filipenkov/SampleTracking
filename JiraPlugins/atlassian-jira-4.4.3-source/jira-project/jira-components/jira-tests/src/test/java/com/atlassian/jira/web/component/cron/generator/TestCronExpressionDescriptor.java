package com.atlassian.jira.web.component.cron.generator;

import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.util.LocaleParser;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.component.cron.parser.CronExpressionParser;

/**
 * TODO: javadoc
 */
public class TestCronExpressionDescriptor extends LegacyJiraMockTestCase
{
    private void assertPrettySchedule(String expectedPretty, String cronString)
    {
        CronExpressionParser parser = new CronExpressionParser(cronString);
        I18nBean i18n = new I18nBean(LocaleParser.parseLocale("en_AU")); // locale specific test
        CronExpressionDescriptor descriptor = new CronExpressionDescriptor(i18n);
        assertEquals(expectedPretty, descriptor.getPrettySchedule(parser.getCronEditorBean()));
    }

    public void testGetPrettyScheduleInvalid()
    {
        String invalidString = "Invalid 2 3 4 5 6";
        assertPrettySchedule(invalidString, invalidString);
    }

    public void testGetPrettyScheduleDailyAt10()
    {
        assertPrettySchedule("Daily at 10:00 am", "0 0 10 * * *");
    }

    public void testGetPrettyScheduleDailyAt1035()
    {
        assertPrettySchedule("Daily at 10:35 am", "0 35 10 * * *");
    }

    public void testGetPrettyScheduleDailyBetween10And3Every2Hours()
    {
        assertPrettySchedule("Daily every 2 hours from 10:00 am to 3:00 pm", "0 0 10-14/2 * * *");
    }

    public void testGetPrettyScheduleDailyBetween9And2Every15Minutes()
    {
        assertPrettySchedule("Daily every 15 minutes from 9:00 am to 2:00 pm", "0 0/15 9-13 * * *");
    }

    public void testGetPrettyScheduleDailyAllDayEvery15Minutes()
    {
        assertPrettySchedule("Daily every 15 minutes", "0 0/15 * * * *");
    }

    public void testGetPrettyScheduleDailyAllDayEvery2Hours()
    {
        assertPrettySchedule("Daily every 2 hours", "0 0 */2 * * *");
    }

    public void testGetPrettyScheduleWithHoursAndMinutesIncrementsRoundTrips()
    {
        final String expression = "0 0/15 */2 ? * *";
        assertPrettySchedule(expression, expression);
    }

    public void testGetPrettyScheduleWithSingleHourAndMinutesIncrementsRoundTrips()
    {
        final String expression = "0 0/15 6 ? * *";
        assertPrettySchedule(expression, expression);
    }

    public void testGetPrettyScheduleWithSingleHourRangeAndMinutesIncrementsRoundTrips()
    {
        assertPrettySchedule("Daily every 15 minutes from 6:00 am to 7:00 am", "0 0/15 6-6 ? * *");
    }

    public void testGetPrettyScheduleDaysPerWeekMonday()
    {
        assertPrettySchedule("Each Monday at 10:00 am", "0 0 10 * * 2");
    }

    public void testGetPrettyScheduleDaysPerWeekSunTueThu()
    {
        assertPrettySchedule("Each Sunday, Tuesday and Thursday at 10:00 am", "0 0 10 * * 3,1,5");
    }

    public void testGetPrettyScheduleDaysPerWeekTueAndThurs()
    {
        assertPrettySchedule("Each Tuesday and Thursday at 10:00 am", "0 0 10 * * 3,5");
    }

    public void testGetPrettyScheduleDaysPerWeekMondayTueAndThurs()
    {
        assertPrettySchedule("Each Monday, Tuesday and Thursday at 10:00 am", "0 0 10 * * 2,3,5");
    }

    public void testGetPrettyScheduleDaysPerWeekMondayTueWithRange()
    {
        assertPrettySchedule("Each Monday and Tuesday every 30 minutes from 8:00 am to 11:00 am", "0 0/30 8-10 * * 2,3");
    }

    public void testGetPrettyScheduleDaysPerMonthFirstOfMonth()
    {
        assertPrettySchedule("The 1st day of every month at 10:00 am", "0 0 10 1 * ?");
    }

    public void testGetPrettyScheduleDaysPerMonthLastOfMonth()
    {
        assertPrettySchedule("The last day of every month at 1:25 pm", "0 25 13 L * ?");
    }

    public void testGetPrettyScheduleDaysPerMonthSecondWednesday()
    {
        assertPrettySchedule("The second Wednesday of every month at 4:25 pm", "0 25 16 ? * 4#2");
    }

    public void testGetPrettyScheduleDaysPerMonthLastFriday()
    {
        assertPrettySchedule("The last Friday of every month at 8:15 am", "0 15 8 ? * 6L");
    }

    public void testGetPrettyScheduleAdvanced()
    {
        assertPrettySchedule("30 0 10 * 1 ?", "30 0 10 * 1 ?"); // seconds field and January month
    }

    public void testGetPrettyScheduleDaily10AMNonPreferredGeneratorFormat()
    {
        assertPrettySchedule("Daily at 10:00 am", "0 0 10 * * ?");
    }
}
