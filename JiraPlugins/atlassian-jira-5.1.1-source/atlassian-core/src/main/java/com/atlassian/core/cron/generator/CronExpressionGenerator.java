package com.atlassian.core.cron.generator;

import com.atlassian.core.cron.CronEditorBean;
import com.atlassian.core.util.DateUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Used to generate a cron string based on the state of a {@link com.atlassian.core.cron.CronEditorBean}.
 */
public class CronExpressionGenerator
{
    private static final String DAY_IN_MONTH_SEPARATOR = "#";
    private static final String LAST_DAY_IN_MONTH_FLAG = "L";

    /**
     * This is a utility method that will process the parameters that the view put into the form and create a
     * cron string from the inputs. This cron string must be validated, there is no guarantee that this output is
     * a valid cron string.
     *
     * @param cronEditorBean the state of the editor form to use for input.
     * @return a cron string that represents the user inputs in cron format.
     */
    public String getCronExpressionFromInput(CronEditorBean cronEditorBean)
    {
        String cronSpec = null;

        if (cronEditorBean.isDailyMode())
        {
            //generate a 'daily' spec
            cronSpec = generateDailySpec(cronEditorBean) + " ? * *";
        }
        else if (cronEditorBean.isDayPerWeekMode())
        {
            //generate a 'days per week' spec
            cronSpec = generateDailySpec(cronEditorBean) + " ? * " + generateDaysOfWeekSpec(cronEditorBean);
        }
        else if (cronEditorBean.isDaysPerMonthMode())
        {
            //generate a 'days per month' spec
            cronSpec = generateDailySpec(cronEditorBean) + " " + generateDaysOfMonthOptSpec(cronEditorBean);
        }
        else if (cronEditorBean.isAdvancedMode())
        {
            cronSpec = cronEditorBean.getCronString();
        }

        return cronSpec;
    }

    /**
     * This method can generate the fourth, fifth and sixth elements of the cron string: days of month, months of year
     * (left as *) and days of week.
     * <p/>
     * This method reads the daysOfMonthOpt select radio buttons and determines which 'Days of Month' cron option the
     * user has chosen and delegates to one of two helper methods: generateDayOfMonthSpec() or
     * generateDayOfWeekOfMonthSpec().
     *
     * @return a string that represents this portion of the cron string.
     */
    String generateDaysOfMonthOptSpec(CronEditorBean cronEditorBean)
    {
        //delegate to helper method
        if (cronEditorBean.isDayOfWeekOfMonth())
        {
            return generateDayOfWeekOfMonthSpec(cronEditorBean);
        }
        else
        {
            return generateDayOfMonthSpec(cronEditorBean);
        }
    }

    String generateDayOfWeekOfMonthSpec(CronEditorBean cronEditorBean)
    {
        String dayInMonthOrdinal = cronEditorBean.getDayInMonthOrdinal();
        if (dayInMonthOrdinal == null)
        {
            throw new IllegalStateException("You must have an ordinal set when generating the day of week of month cron portion: " + cronEditorBean.getCronString());
        }
        if (!LAST_DAY_IN_MONTH_FLAG.equalsIgnoreCase(dayInMonthOrdinal))
        {
            dayInMonthOrdinal = DAY_IN_MONTH_SEPARATOR + dayInMonthOrdinal;
        }
        String specifiedDaysPerWeek = cronEditorBean.getSpecifiedDaysPerWeek();
        if (specifiedDaysPerWeek == null)
        {
            throw new IllegalStateException("The days per week must be specified when creating a days per week cron portion: " + cronEditorBean.getCronString());
        }
        String specSegment = specifiedDaysPerWeek + dayInMonthOrdinal;
        return "? * " + specSegment;
    }

    /**
     * This function returns a string representing the last three cron elements. The day of the month is resolved from
     * the monthDay select control.
     * <p/>
     * Possible segments generated by this method are of the form: '1-31 * ?'
     */
    String generateDayOfMonthSpec(CronEditorBean cronEditorBean)
    {
        String monthDay = cronEditorBean.getDayOfMonth();
        if (monthDay == null)
        {
            throw new IllegalStateException("The day of month must not be null when creating a day of month cron portion: " + cronEditorBean.getCronString());
        }
        return monthDay + " * ?";
    }

    /**
     * This method generates the last mandatory element of the cron string: days of the week.
     * <p/>
     * The check boxes representing the days of the week are looped through and incrementally appended to a string
     * which is then returned.
     */
    String generateDaysOfWeekSpec(CronEditorBean cronEditorBean)
    {
        if (StringUtils.isBlank(cronEditorBean.getSpecifiedDaysPerWeek()))
        {
            throw new IllegalStateException("The days per week must be specified when creating a days per week cron portion: " + cronEditorBean.getCronString());
        }
        return cronEditorBean.getSpecifiedDaysPerWeek();
    }

    /**
     * This method generates the first three elements of the cron string: seconds, minutes and hours.
     */
    String generateDailySpec(CronEditorBean cronEditorBean)
    {
        //resolve base string from frequency select control
        StringBuffer dailyString = new StringBuffer("0 ");

        int increment = getIntFromString(cronEditorBean.getIncrementInMinutes());

        //specify a precise time
        if (increment == 0 || cronEditorBean.isDaysPerMonthMode())
        {

            if (cronEditorBean.getHoursRunOnceMeridian() == null)
            {
                throw new IllegalStateException("You must specify a run once hour meridian when generating a daily spec with no interval: " + cronEditorBean.getCronString());
            }
            if (cronEditorBean.getHoursRunOnce() == null)
            {
                throw new IllegalStateException("You must specify a run once hour when generating a daily spec with no interval: " + cronEditorBean.getCronString());
            }
            if (cronEditorBean.getMinutes() == null)
            {
                throw new IllegalStateException("You must specify a minutes when generating a daily spec with no interval: " + cronEditorBean.getCronString());
            }

            //read & clean input from "at" controls
            int atHours = getIntFromString(cronEditorBean.getHoursRunOnce());
            int atMins = getIntFromString(cronEditorBean.getMinutes());

            atHours = DateUtils.get24HourTime(cronEditorBean.getHoursRunOnceMeridian(), atHours);

            //replace base string tokens
            dailyString.append(atMins);
            dailyString.append(" ");
            dailyString.append(atHours);
        }
        //specify a time range
        else
        {
            // The minutes field is always 0
            dailyString.append("0");
            // If the increment is a minute increment
            if (increment < 60)
            {
                dailyString.append("/");
                dailyString.append(increment);
            }

            dailyString.append(" ");

            // Check that we have what we need
            if (cronEditorBean.getHoursFrom() == null)
            {
                throw new IllegalStateException("You must specify a from hour when generating a daily spec with an interval: " + cronEditorBean.getCronString());
            }
            if (cronEditorBean.getHoursFromMeridian() == null)
            {
                throw new IllegalStateException("You must specify a from hour meridian when generating a daily spec with an interval: " + cronEditorBean.getCronString());
            }

            if (cronEditorBean.getHoursTo() == null)
            {
                throw new IllegalStateException("You must specify a to hour when generating a daily spec with an interval: " + cronEditorBean.getCronString());
            }
            if (cronEditorBean.getHoursToMeridian() == null)
            {
                throw new IllegalStateException("You must specify a to hour meridian when generating a daily spec with an interval: " + cronEditorBean.getCronString());
            }

            //read & clean input from "from" & "till" controls
            int fromHours = DateUtils.get24HourTime(cronEditorBean.getHoursFromMeridian(), getIntFromString(cronEditorBean.getHoursFrom()));
            int toHours = DateUtils.get24HourTime(cronEditorBean.getHoursToMeridian(), getIntFromString(cronEditorBean.getHoursTo()));

            int hourIncrement = increment / 60;

            if (cronEditorBean.is24HourRange())
            {
                dailyString.append("*");
            }
            else
            {
                dailyString.append(fromHours);
                dailyString.append("-");
                // JRA-13503: since cronEditorBean.getHoursTo() is exclusive, but cron expressions are inclusive,
                // we need to decrement the "to" hour from the bean by 1 to get the correct representation
                dailyString.append(decrementHourByOne(toHours));
            }

            if (hourIncrement >= 1)
            {
                dailyString.append("/");
                dailyString.append(hourIncrement);
            }
        }

        return dailyString.toString();
    }

    int getIntFromString(String string)
    {
        if (string != null && !StringUtils.isEmpty(string))
        {
            return Integer.parseInt(string);
        }
        return 0;
    }
    
    private int decrementHourByOne(int hour)
    {
        return hour == 0 ? 23 : hour - 1;
    }
}