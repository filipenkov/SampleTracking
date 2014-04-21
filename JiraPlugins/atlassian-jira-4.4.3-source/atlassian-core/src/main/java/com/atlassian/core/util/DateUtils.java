package com.atlassian.core.util;

import com.opensymphony.util.TextUtils;
import org.apache.log4j.Category;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtils
{
    private static Category log = Category.getInstance(DateUtils.class);
    // starts with some digits (e.g. "22"). optionally has a dot followed by more digits (e.g. ".45"). or starts
    // with a dot followed by multiple digits (".25"). finally there is other "stuff" which is the unit of time
    // (e.g. "d" or "hours").
    // Good = 24h 35.5 0.2h .8h
    // Bad = 24.h -23h
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?|\\.\\d+)(.+)");

    public enum Duration
    {
        SECOND(1),
        MINUTE(60),
        HOUR(60 * MINUTE.getSeconds()),
        DAY(24 * HOUR.getSeconds())
                {
                    @Override
                    public long getModifiedSeconds(final long secondsPerDay, final long secondsPerWeek)
                    {
                        return secondsPerDay;
                    }
                },
        WEEK(7 * DAY.getSeconds())
                {
                    @Override
                    public long getModifiedSeconds(final long secondsPerDay, final long secondsPerWeek)
                    {
                        return secondsPerWeek;
                    }
                },
        MONTH(31 * DAY.getSeconds())
                {
                    // since a month has 31 days in it, clearly you shouldn't be using this and expecting precision.
                    // stick with the ones above.
                    @Override
                    public long getModifiedSeconds(final long secondsPerDay, final long secondsPerWeek)
                    {
                        return 31 * secondsPerDay;
                    }
                },
        YEAR(52 * WEEK.getSeconds())
                {
                    // as with MONTH above, you shouldn't expect precision when you use YEAR. In particular a year
                    // has 52 weeks...which is NOT the same as 365 days with every 4th year having 366.
                    @Override
                    public long getModifiedSeconds(final long secondsPerDay, final long secondsPerWeek)
                    {
                        return 52 * secondsPerWeek;
                    }
                };

        public long getSeconds()
        {
            return seconds;
        }

        public long getMilliseconds()
        {
            return 1000L * getSeconds();
        }

        /**
         * Sometimes customers configure the meaning of "day" or "week" to mean something like "1 day = 8 hours".
         * @param secondsPerDay how many seconds are in a "day"
         * @param secondsPerWeek how many seconds are in a "week" (based on number of days per week and hours per day)
         * @return number of seconds in the duration, taking into account the modified definition of "day" and "week"
         */
        public long getModifiedSeconds(final long secondsPerDay, final long secondsPerWeek)
        {
            return getSeconds();
        }

        private final long seconds;

        private Duration(final long seconds)
        {
            this.seconds = seconds;
        }
    }

    // these are obsoleted by the above but we need to keep them for backwards compatability
    public static final long SECOND_MILLIS = Duration.SECOND.getMilliseconds();
    public static final long MINUTE_MILLIS = Duration.MINUTE.getMilliseconds();
    public static final long HOUR_MILLIS = Duration.HOUR.getMilliseconds();
    public static final long DAY_MILLIS = Duration.DAY.getMilliseconds();
    public static final long MONTH_MILLIS = Duration.MONTH.getMilliseconds();
    public static final long YEAR_MILLIS = Duration.YEAR.getMilliseconds();

    public static final String AM = "am";
    public static final String PM = "pm";

    /*
       It's important these stay ordered from biggest to smallest.
       Everything in this must be able to be zeroed without changing anything
       that comes before it in the list. So don't add WEEK!
     */
    private static final int[] CALENDAR_PERIODS = { Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH,
                                                    Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND };

    // This is used by the Velocity templates as a bean
    private final ResourceBundle resourceBundle;

    public static class DateRange
    {
        public final java.util.Date startDate;
        public final java.util.Date endDate;

        public DateRange(java.util.Date startDate, java.util.Date endDate)
        {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }


    public DateUtils(ResourceBundle resourceBundle)
    {
        this.resourceBundle = resourceBundle;
    }

    /** compares if these two timestamps are within 10 milliseconds of each other (precision error)
     * @param t1 first timestamp to compare
     * @param t2 second timestamp to compare
     * @return true if the two timestamps are within 10 milliseconds of one another
     */
    public static boolean equalTimestamps(Timestamp t1, Timestamp t2)
    {
        return (Math.abs(t1.getTime() - t2.getTime()) < 10L);
    }

    /** Date Format to be used for internal logging operations */
    public static final DateFormat ISO8601DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

    public String dateDifferenceBean(long dateA, long dateB, long resolution, ResourceBundle resourceBundle)
    {
        return dateDifference(dateA, dateB, resolution, resourceBundle);
    }

    /**
     * Resolution is the degree of difference.
     * <p/>
     * 0 = months
     * 1 = days
     * 2 = hours
     * 3 = minutes
     * 4 = seconds
     * @param dateA first date to compare
     * @param dateB second date to compare
     * @param resolution the degree of difference
     * @param resourceBundle contains localizations for core.dateutils strings
     * @return the difference between the two dates as a human readable string (i.e. 2 months, 3 days, 4 hours)
     */
    public static String dateDifference(long dateA, long dateB, long resolution, ResourceBundle resourceBundle)
    {
        long months, days, hours, minutes, seconds;
        StringBuffer sb = new StringBuffer();
        long difference = Math.abs(dateB - dateA);

        resolution--;
        months = difference / Duration.MONTH.getMilliseconds();
        if (months > 0)
        {
            difference = difference % Duration.MONTH.getMilliseconds();
            if (months > 1)
            {
                sb.append(months).append(" ").append(getText(resourceBundle, "core.dateutils.months")).append(", ");
            }
            else
            {
                sb.append(months).append(" ").append(getText(resourceBundle, "core.dateutils.month")).append(", ");
            }
        }

        if (resolution < 0)
        {
            if (sb.length() == 0)
            {
                return "0 " + getText(resourceBundle, "core.dateutils.months");
            }
            else
            {
                return sb.substring(0, sb.length() - 2);
            }
        }
        else
        {
            resolution--;
            days = difference / Duration.DAY.getMilliseconds();
            if (days > 0)
            {
                difference = difference % Duration.DAY.getMilliseconds();
                if (days > 1)
                {
                    sb.append(days).append(" ").append(getText(resourceBundle, "core.dateutils.days")).append(", ");
                }
                else
                {
                    sb.append(days).append(" ").append(getText(resourceBundle, "core.dateutils.day")).append(", ");
                }
            }
        }

        if (resolution < 0)
        {
            if (sb.length() == 0)
            {
                return "0 " + getText(resourceBundle, "core.dateutils.days");
            }
            else
            {
                return sb.substring(0, sb.length() - 2);
            }
        }
        else
        {
            resolution--;
            hours = difference / Duration.HOUR.getMilliseconds();
            if (hours > 0)
            {
                difference = difference % Duration.HOUR.getMilliseconds();
                if (hours > 1)
                {
                    sb.append(hours).append(" ").append(getText(resourceBundle, "core.dateutils.hours")).append(", ");
                }
                else
                {
                    sb.append(hours).append(" ").append(getText(resourceBundle, "core.dateutils.hour")).append(", ");
                }
            }
        }

        if (resolution < 0)
        {
            if (sb.length() == 0)
            {
                return "0 " + getText(resourceBundle, "core.dateutils.hours");
            }
            else
            {
                return sb.substring(0, sb.length() - 2);
            }
        }
        else
        {
            resolution--;
            minutes = difference / Duration.MINUTE.getMilliseconds();
            if (minutes > 0)
            {
                difference = difference % Duration.MINUTE.getMilliseconds();
                if (minutes > 1)
                {
                    sb.append(minutes).append(" ").append(getText(resourceBundle, "core.dateutils.minutes")).append(", ");
                }
                else
                {
                    sb.append(minutes).append(" ").append(getText(resourceBundle, "core.dateutils.minute")).append(", ");
                }
            }
        }

        if (resolution < 0)
        {
            if (sb.length() == 0)
            {
                return "0 " + getText(resourceBundle, "core.dateutils.minutes");
            }
            else
            {
                return sb.substring(0, sb.length() - 2);
            }
        }
        else
        {
            resolution--;
            seconds = difference / Duration.SECOND.getMilliseconds();
            if (seconds > 0)
            {
                if (seconds > 1)
                {
                    sb.append(seconds).append(" ").append(getText(resourceBundle, "core.dateutils.seconds")).append(", ");
                }
                else
                {
                    sb.append(seconds).append(" ").append(getText(resourceBundle, "core.dateutils.second")).append(", ");
                }
            }
        }

        if (resolution <= 0 && sb.length() == 0)
        {
            return "0 " + getText(resourceBundle, "core.dateutils.seconds");
        }

        if (sb.length() > 2)
        {
            return sb.substring(0, sb.length() - 2);
        }
        else
        {
            return "";
        }
    }


    public static String formatDateISO8601(Date ts)
    {
        return ISO8601DateFormat.format(ts);
    }

    /**
     * Check whether a given duration string is valid
     *
     * @param s the duration string
     * @return true if it a valid duration
     */
    public static boolean validDuration(String s)
    {
        try
        {
            getDuration(s);
            return true;
        }
        catch (InvalidDurationException e)
        {
            return false;
        }
    }

    /**
     * Given a duration string, get the number of seconds it represents (all case insensitive):
     * <ul>
     * <li>w = weeks
     * <li>d = days
     * <li>h = hours
     * <li>m = minutes
     * </ul>
     * If no category is specified, assume minutes.<br>
     * Each field must be separated by a space, and they can come in any order.
     * Case is ignored.
     * <p/>
     * ie 2h = 7200, 60m = 3600, 3d = 259200, 30m
     *
     * @param durationStr the duration string
     * @return the duration in seconds
     * @throws InvalidDurationException if the duration is invalid
     */
    public static long getDuration(String durationStr) throws InvalidDurationException
    {
        return getDuration(durationStr, Duration.MINUTE);
    }

    /**
     * Given a duration string, get the number of seconds it represents (all case insensitive):
     * <ul>
     * <li>w = weeks
     * <li>d = days
     * <li>h = hours
     * <li>m = minutes
     * </ul>
     * ie 2h = 7200, 60m = 3600, 3d = 259200, 30m
     *
     * @param durationStr the duration string
     * @param defaultUnit the unit used when another is not specified in the durationStr
     * @return the duration in seconds
     * @throws InvalidDurationException if the duration is invalid
     */
    public static long getDuration(final String durationStr, final Duration defaultUnit) throws InvalidDurationException
    {
        return getDurationSeconds(durationStr, Duration.DAY.getSeconds(), Duration.WEEK.getSeconds(), defaultUnit);
    }

    /**
     * This function retrieves a duration in seconds that depends on number of hours in a day and
     * days in a week. The default unit is MINUTE (i.e. "2" == "2 minutes")
     *
     * @param durationStr to convert to a duration
     * @param hoursPerDay Number of hourse i day
     * @param daysPerWeek Days Per Week
     * @return the duration in seconds
     * @throws InvalidDurationException if its badly formatted duration
     */
    public static long getDuration(String durationStr, int hoursPerDay, int daysPerWeek) throws InvalidDurationException
    {
        return getDuration(durationStr, hoursPerDay, daysPerWeek, Duration.MINUTE);
    }

    /**
     * This function retrieves a duration in seconds that depends on number of hours in a day and
     * days in a week
     *
     * @param durationStr to convert to a duration
     * @param hoursPerDay Number of hourse i day
     * @param daysPerWeek Days Per Week
     * @param defaultUnit the unit used when one is not specified on a measure in the durationStr
     * @return the duration in seconds
     * @throws InvalidDurationException if its badly formatted duration
     */
    public static long getDuration(String durationStr, int hoursPerDay, int daysPerWeek, final Duration defaultUnit) throws InvalidDurationException
    {
        long secondsInDay = hoursPerDay * Duration.HOUR.getSeconds();
        long secondsPerWeek = daysPerWeek * secondsInDay;
        return getDurationSeconds(durationStr, secondsInDay, secondsPerWeek, defaultUnit);
    }

    /**
     * Get a duration string with the possibility of a negative.
     * <p/>
     * A duration will be considered negative if the first non-space character is a - sign.
     *
     * @param durationStr the duration string
     * @return the duration in seconds, which can be negative
     * @throws InvalidDurationException if its a badly formatted duration
     */
    public static long getDurationWithNegative(String durationStr) throws InvalidDurationException
    {
        String cleanedDurationStr = TextUtils.noNull(durationStr).trim();
        if (!TextUtils.stringSet(cleanedDurationStr))
        {
            return 0;
        }

        boolean negative = false;

        if (cleanedDurationStr.charAt(0) == '-')
        {
            negative = true;
        }

        if (negative)
        {
            return 0 - getDuration(cleanedDurationStr.substring(1));
        }
        else
        {
            return getDuration(cleanedDurationStr);
        }
    }

    /**
     * Convert a duration string in the number of seconds it represents.
     * This method takes seconds per day and seconds per weeks instead of "hours per day" or "days per week"
     * because we may want a non-integral number of hours per day.
     * @param durationStr the duration string
     * @param secondsPerDay number of seconds in a working "day" (e.g. could be equal 6.5 hours)
     * @param secondsPerWeek number of seconds in a working "week" (e.g. could be equal to 4.5 days)
     * @param defaultUnit the unit to use for numbers with no unit specified (e.g. "12")
     * @return the number of seconds representing the duration string
     * @throws InvalidDurationException if the duration string cannot be parsed
     */
    public static long getDurationSeconds(String durationStr, long secondsPerDay, long secondsPerWeek, final Duration defaultUnit)
            throws InvalidDurationException
    {
        long time = 0;

        if (org.apache.commons.lang.StringUtils.isBlank(durationStr))
        {
            return 0;
        }

        durationStr = durationStr.trim().toLowerCase();

        // if we have more than one 'token', parse each separately
        if (durationStr.indexOf(" ") > 0)
        {
            StringTokenizer st = new StringTokenizer(durationStr, ", ");
            while (st.hasMoreTokens())
            {
                time += getDurationSeconds(st.nextToken(), secondsPerDay, secondsPerWeek, defaultUnit);
            }
        }
        else
        {
            try // detect if we just have a number
            {
                time = Long.parseLong(durationStr.trim()) * defaultUnit.getModifiedSeconds(secondsPerDay, secondsPerWeek);
            }
            catch (Exception ex) // otherwise get the value
            {
                final Matcher matcher = DURATION_PATTERN.matcher(durationStr);
                if(matcher.matches())
                {
                    // the regex will have two groups. the "number part" and the "unit" part
                    final String numberAsString = matcher.group(1);
                    final BigDecimal number = new BigDecimal(numberAsString);

                    final long unit = getUnit(matcher.group(2), secondsPerDay, secondsPerWeek);
                    final BigDecimal seconds = number.multiply(BigDecimal.valueOf(unit));
                    try
                    {
                        // we track time in seconds but care about accuracy to the minute. if the decimal fraction
                        // specified would require sub-minute accuracy to store then we want an InvalidDurationException.

                        // If we allowed second-accuracy then people could enter somewhat nonsensical times like "2.27 hours"
                        // which we would gladly store as 2 hours, 16 minutes, and 2 seconds. But since we never display
                        // the second part of the time a user would just see "2 hours, 16 minutes". "2.28 hours" would also
                        // be displayed as "2 hours, 16 minutes". We want to preserve a 1:1 mapping between allowable decimal
                        // fractions and pretty formatted durations.

                        // this call will trigger an exception if rounding would occur...we will propagate that
                        // as an InvalidDurationException
                        //noinspection ResultOfMethodCallIgnored
                        seconds.divide(BigDecimal.valueOf(60)).intValueExact();

                        // if we got here then we are only storing minutes and not seconds.
                        time = seconds.intValueExact();

                    }
                    catch (ArithmeticException e)
                    {
                        throw new InvalidDurationException("Specified decimal fraction duration cannot maintain precision", e);
                    }
                }
                else
                {
                    throw new InvalidDurationException("Unable to parse duration string: " + durationStr);
                }
            }
        }
        return time;
    }

    /**
     * Given a string such as "d" or "days" return the number of seconds in one of those units
     * @param unit the string to investigate.
     * @param secondsPerDay the number of seconds in a working day
     * @param secondsPerWeek the number of seconds in a working week
     * @return the number of seconds in the unit described by the string
     * @throws InvalidDurationException if the string isn't a valid unit of time
     */
    private static long getUnit(final String unit, final long secondsPerDay, final long secondsPerWeek) throws InvalidDurationException
    {
        long time;
        switch ((int) unit.charAt(0))
        {
            case(int) 'm':
                validateDurationUnit(unit.substring(0), Duration.MINUTE);
                time = Duration.MINUTE.getSeconds();
                break;
            case(int) 'h':
                validateDurationUnit(unit.substring(0), Duration.HOUR);
                time = Duration.HOUR.getSeconds();
                break;
            case(int) 'd':
                validateDurationUnit(unit.substring(0), Duration.DAY);
                time = secondsPerDay;
                break;
            case(int) 'w':
                validateDurationUnit(unit.substring(0), Duration.WEEK);
                time = secondsPerWeek;
                break;
            default:
                throw new InvalidDurationException("Not a valid duration string");
        }
        return time;
    }

    private static String validateDurationUnit(final String durationString, final Duration duration)
            throws InvalidDurationException
    {

        if (durationString.length() > 1)
        {
            String singular = duration.name().toLowerCase();
            String plural = duration.name().toLowerCase() + "s";

            if (durationString.indexOf(plural) != -1)
            {
                return durationString.substring(durationString.indexOf(plural));
            }
            else if (durationString.indexOf(singular) != -1)
            {
                return durationString.substring(durationString.indexOf(singular));
            }
            else
            {
                throw new InvalidDurationException("Not a valid durationString string");
            }
        }
        return durationString.substring(1);
    }



    /**
     * Get String representation of a duration
     * <p/>
     *
     * @param seconds Number of seconds
     * @return String representing duration, eg: "1h 30m"
     * @see #getDurationStringWithNegative(long)
     */
    public static String getDurationString(long seconds)
    {
        return getDurationStringSeconds(seconds, Duration.DAY.getSeconds(), Duration.WEEK.getSeconds());
    }

    /**
     * Get String representation of a (possibly negative) duration.
     * <p/>
     *
     * @param seconds Number of seconds
     * @return String representing duration, eg: "-1h 30m"
     * @see #getDurationString(long)
     */
    public static String getDurationStringWithNegative(long seconds)
    {
        if (seconds < 0)
        {
            return "-" + getDurationString(-seconds);
        }
        else
        {
            return getDurationString(seconds);
        }
    }

    /**
     * Get a duration string representing the given number of seconds. The string will use the largest unit possible.
     * (i.e. 1w 3d)
     * @param l the number of seconds
     * @param hoursPerDay hours in a working day
     * @param daysPerWeek days in a working week
     * @return the duration string
     */
    public static String getDurationString(long l, int hoursPerDay, int daysPerWeek)
    {
        long secondsInDay = hoursPerDay * Duration.HOUR.getSeconds();
        long secondsPerWeek = daysPerWeek * secondsInDay;
        return getDurationStringSeconds(l, secondsInDay, secondsPerWeek);
    }

    /**
     * Get a duration string representing the given number of seconds. The string will use the largest unit possible.
     * (i.e. 1w 3d).
     * Use this method when you want to specify a non-integral number of hours in a day (e.g. 7.5) or days per week.
     * @param l the number of seconds
     * @param secondsPerDay the number of seconds in a working day
     * @param secondsPerWeek the number of seconds in a working week
     * @return the formatted duration string
     */
    public static String getDurationStringSeconds(long l, long secondsPerDay, long secondsPerWeek)
    {
        if (l == 0)
        {
            return "0m";
        }

        StringBuffer result = new StringBuffer();

        if (l >= secondsPerWeek)
        {
            result.append((l / secondsPerWeek));
            result.append("w ");
            l = l % secondsPerWeek;
        }

        if (l >= secondsPerDay)
        {
            result.append((l / secondsPerDay));
            result.append("d ");
            l = l % secondsPerDay;
        }

        if (l >= Duration.HOUR.getSeconds())
        {
            result.append((l / Duration.HOUR.getSeconds()));
            result.append("h ");
            l = l % Duration.HOUR.getSeconds();
        }

        if (l >= Duration.MINUTE.getSeconds())
        {
            result.append((l / Duration.MINUTE.getSeconds()));
            result.append("m ");
        }

        return result.toString().trim();
    }

    /**
     * Converts a number of seconds into a pretty formatted data string.  The resolution is in minutes.  So if the number of seconds is greater than a minute, it will
     * only be shown down top minute resolution.  If the number of seconds is less than a minute it will be shown in seconds.
     * <p/>
     * So for example <code>76</code> becomes <code>'1 minute'</code>, while <code>42</code> becomes <code>'42 seconds'</code>
     *
     * @param numSecs        the number of seconds in the duration
     * @param resourceBundle a resouce bundle for i18n
     * @return a string in readable pretty duration format, using minute resolution
     */
    public static String getDurationPretty(long numSecs, ResourceBundle resourceBundle)
    {
        return getDurationPrettySeconds(numSecs, Duration.DAY.getSeconds(), Duration.WEEK.getSeconds(), resourceBundle, false);
    }

    /**
     * Converts a number of seconds into a pretty formatted data string.  The resolution is in minutes.  So if the number of seconds is greater than a minute, it will
     * only be shown down top minute resolution.  If the number of seconds is less than a minute it will be shown in seconds.
     * <p/>
     * So for example <code>76</code> becomes <code>'1 minute'</code>, while <code>42</code> becomes <code>'42 seconds'</code>
     *
     * @param numSecs        the number of seconds in the duration
     * @param hoursPerDay    the hours in a day
     * @param daysPerWeek    the number of days in a week
     * @param resourceBundle a resouce bundle for i18n
     * @return a string in readable pretty duration format, using minute resolution
     */
    public static String getDurationPretty(long numSecs, int hoursPerDay, int daysPerWeek, ResourceBundle resourceBundle)
    {
        long secondsInDay = hoursPerDay * Duration.HOUR.getSeconds();
        long secondsPerWeek = daysPerWeek * secondsInDay;
        return getDurationPrettySeconds(numSecs, secondsInDay, secondsPerWeek, resourceBundle, false);
    }

    /**
     * Converts a number of seconds into a pretty formatted data string.  The resolution is in seconds.
     * <p/>
     * So for example <code>76</code> becomes <code>'1 minute, 16 seconds'</code>, while <code>42</code> becomes <code>'42 seconds'</code>
     *
     * @param numSecs        the number of seconds in the duration
     * @param resourceBundle a resouce bundle for i18n
     * @return a string in readable pretty duration format, using second resolution
     */
    public static String getDurationPrettySecondsResolution(long numSecs, ResourceBundle resourceBundle)
    {
        return getDurationPrettySeconds(numSecs, Duration.DAY.getSeconds(), Duration.WEEK.getSeconds(), resourceBundle, true);
    }

    /**
     * Converts a number of seconds into a pretty formatted data string.  The resolution is in seconds.
     * <p/>
     * So for example <code>76</code> becomes <code>'1 minute, 16 seconds'</code>, while <code>42</code> becomes <code>'42 seconds'</code>
     *
     * @param numSecs        the number of seconds in the duration
     * @param hoursPerDay    the hours in a day
     * @param daysPerWeek    the number of days in a week
     * @param resourceBundle a resouce bundle for i18n
     * @return a string in readable pretty duration format, using second resolution
     */
    public static String getDurationPrettySecondsResolution(long numSecs, int hoursPerDay, int daysPerWeek, ResourceBundle resourceBundle)
    {
        long secondsInDay = hoursPerDay * Duration.HOUR.getSeconds();
        long secondsPerWeek = daysPerWeek * secondsInDay;
        return getDurationPrettySeconds(numSecs, secondsInDay, secondsPerWeek, resourceBundle, true);
    }

    /**
     * Get a pretty formatted duration for the given number of seconds. (e.g. "4 days, 2 hours, 30 minutes")
     * @param numSecs the number of seconds in the duration
     * @param secondsPerDay the number of seconds in a "day"
     * @param secondsPerWeek the number of seconds in a "week"
     * @param resourceBundle the bundle containing translations for the strings used in the pretty string (e.g. "days")
     * @param secondsDuration if false only display down to the minute even if there are some seconds, else display seconds
     * @return the formatted pretty duration
     */
    private static String getDurationPrettySeconds(long numSecs, long secondsPerDay, long secondsPerWeek, ResourceBundle resourceBundle, boolean secondsDuration)
    {
        // use perWeek to calculate perYear because that already has "days per week" already figured in. if a week only had 3 days, for instance then
        // doing secondsPerDay * 365 would overestimate how much we can get done in a year.
        final long secondsPerYear = secondsPerWeek * 52;
        return getDurationPrettySeconds(numSecs, secondsPerYear, secondsPerDay, secondsPerWeek, resourceBundle, secondsDuration);
    }

    /**
     * Get a pretty formatted duration for the given number of seconds. (e.g. "4 days, 2 hours, 30 minutes")
     * @param numSecs the number of seconds in the duration
     * @param secondsPerDay the number of seconds in a "day"
     * @param secondsPerWeek the number of seconds in a "week"
     * @param resourceBundle the bundle containing translations for the strings used in the pretty string (e.g. "days")
     * @return the formatted pretty duration
     */
    public static String getDurationPrettySeconds(long numSecs, long secondsPerDay, long secondsPerWeek, ResourceBundle resourceBundle)
    {
        return getDurationPrettySeconds(numSecs, secondsPerDay, secondsPerWeek, resourceBundle, false);
    }

    /*
     * This implementation method returns things in "minute resolution" unless the secondResolution flag is true
     */
    private static String getDurationPrettySeconds(long numSecs, long secondsPerYear, long secondsPerDay, long secondsPerWeek, ResourceBundle resourceBundle, boolean secondResolution)
    {
        if (numSecs == 0)
        {
            if (secondResolution)
            {
                return "0 " + getText(resourceBundle, "core.dateutils.seconds");
            }
            else
            {
                return "0 " + getText(resourceBundle, "core.dateutils.minutes");
            }
        }

        StringBuffer result = new StringBuffer();

        if (numSecs >= secondsPerYear)
        {
            long years = numSecs / secondsPerYear;
            result.append(years).append(' ');

            if (years > 1)
            {
                result.append(getText(resourceBundle, "core.dateutils.years"));
            }
            else
            {
                result.append(getText(resourceBundle, "core.dateutils.year"));
            }

            result.append(", ");
            numSecs = numSecs % secondsPerYear;
        }

        if (numSecs >= secondsPerWeek)
        {
            long weeks = numSecs / secondsPerWeek;
            result.append(weeks).append(' ');

            if (weeks > 1)
            {
                result.append(getText(resourceBundle, "core.dateutils.weeks"));
            }
            else
            {
                result.append(getText(resourceBundle, "core.dateutils.week"));
            }

            result.append(", ");
            numSecs = numSecs % secondsPerWeek;
        }

        if (numSecs >= secondsPerDay)
        {
            long days = numSecs / secondsPerDay;
            result.append(days).append(' ');

            if (days > 1)
            {
                result.append(getText(resourceBundle, "core.dateutils.days"));
            }
            else
            {
                result.append(getText(resourceBundle, "core.dateutils.day"));
            }

            result.append(", ");
            numSecs = numSecs % secondsPerDay;
        }

        if (numSecs >= Duration.HOUR.getSeconds())
        {
            long hours = numSecs / Duration.HOUR.getSeconds();
            result.append(hours).append(' ');

            if (hours > 1)
            {
                result.append(getText(resourceBundle, "core.dateutils.hours"));
            }
            else
            {
                result.append(getText(resourceBundle, "core.dateutils.hour"));
            }

            result.append(", ");
            numSecs = numSecs % Duration.HOUR.getSeconds();
        }

        if (numSecs >= Duration.MINUTE.getSeconds())
        {
            long minute = numSecs / Duration.MINUTE.getSeconds();
            result.append(minute).append(' ');


            if (minute > 1)
            {
                result.append(getText(resourceBundle, "core.dateutils.minutes"));
            }
            else
            {
                result.append(getText(resourceBundle, "core.dateutils.minute"));
            }

            result.append(", ");

            // if we want seconds resolution we need to reduce it down to seconds here
            if (secondResolution)
            {
                numSecs = numSecs % Duration.MINUTE.getSeconds();
            }
        }

        if (numSecs >= 1 && numSecs < Duration.MINUTE.getSeconds())
        {
            result.append(numSecs).append(' ');


            if (numSecs > 1)
            {
                result.append(getText(resourceBundle, "core.dateutils.seconds"));
            }
            else
            {
                result.append(getText(resourceBundle, "core.dateutils.second"));
            }

            result.append(", ");
        }

        if (result.length() > 2) // remove the ", " on th end
        {
            return result.substring(0, result.length() - 2);
        }
        else
        {
            return result.toString();
        }
    }

    /** This is used by the Velocity templates as a bean
     * @param l a duration in seconds
     * @return a pretty formatted version of the duration
     */
    public String formatDurationPretty(long l)
    {
        return DateUtils.getDurationPretty(l, resourceBundle);
    }

    /** This is used by the Velocity templates as a bean
     * @param seconds duration as a string
     * @return a pretty formatted version of the duration
     */
    public String formatDurationPretty(String seconds)
    {
        return DateUtils.getDurationPretty(Long.parseLong(seconds), resourceBundle);
    }


    /** This is used by the WebWork tags as a bean
     * Despite the name it doesn't actually format a duration string. It takes a long.
     * @param l a duration in seconds
     * @return a pretty formatted version of the duration
     * @deprecated You should be calling formatDurationPretty
     * @see #formatDurationPretty
     */
    public String formatDurationString(long l)
    {
        return DateUtils.getDurationPretty(l, resourceBundle);
    }

    private static String getText(ResourceBundle resourceBundle, String key)
    {
        try
        {
            return resourceBundle.getString(key);
        }
        catch (MissingResourceException e)
        {
            log.error(e);
            return "";
        }
    }

    /**
     * Change the date of a Calendar object so that it has the maximum resolution
     * of "period" where period is one of the constants in CALENDAR_PERIODS above.
     * <p/>
     * e.g. to obtain the maximum value for a month, call toEndOfPeriod(calendarObject, Calendar.MONTH)
     *
     * @param calendar The Calendar to change
     * @param period   The period to "maximise"
     * @return A modified Calendar object
     */
    public static Calendar toEndOfPeriod(Calendar calendar, int period)
    {
        boolean zero = false;

        for (int calendarPeriod : CALENDAR_PERIODS)
        {
            if (zero)
            {
                calendar.set(calendarPeriod, calendar.getMaximum(calendarPeriod));
            }

            if (calendarPeriod == period)
            {
                zero = true;
            }
        }

        if (!zero)
        {
            throw new IllegalArgumentException("unknown Calendar period: " + period);
        }

        return calendar;
    }

    /**
     * Change the date of a Calendar object so that it has the minimum resolution
     * of "period" where period is one of the constants in CALENDAR_PERIODS above.
     * @param calendar calendar to modify
     * @param period the new calendar period from CALENDAR_PERIODS
     * @return the calendar that was passed in
     */
    public static Calendar toStartOfPeriod(Calendar calendar, int period)
    {
        boolean zero = false;
        for (int calendarPeriod : CALENDAR_PERIODS)
        {
            if (zero)
            {
                if (calendarPeriod == Calendar.DAY_OF_MONTH)
                {
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                }
                else
                {
                    calendar.set(calendarPeriod, 0);
                }
            }

            if (calendarPeriod == period)
            {
                zero = true;
            }

        }

        if (!zero)
        {
            throw new IllegalArgumentException("unknown Calendar period: " + period);
        }

        return calendar;
    }

    /**
     * Given a period, and a date that falls within that period, create a range of dates such
     * that the period is contained exactly within [startDate <= {range} < endDate]
     *
     * @param date   a calendar object of a date falling in that range
     * @param period something in CALENDAR_PERIODS
     * @return resulting range of dates
     */
    public static DateRange toDateRange(Calendar date, int period)
    {
        // defensively copy the calendar so we don't break anything outside.
        Calendar cal = (Calendar) date.clone();
        toStartOfPeriod(cal, period);
        Date startDate = new Date(cal.getTimeInMillis());
        cal.add(period, 1);
        Date endDate = new Date(cal.getTimeInMillis());

        return new DateRange(startDate, endDate);
    }

    public static Calendar getCalendarDay(int year, int month, int day)
    {
        return initCalendar(year, month, day, 0, 0, 0, 0);
    }

    public static Date getDateDay(int year, int month, int day)
    {
        return getCalendarDay(year, month, day).getTime();
    }

    public static Date getSqlDateDay(int year, int month, int day)
    {
        return new java.sql.Date(getCalendarDay(year, month, day).getTimeInMillis());
    }

    public static int get24HourTime(final String meridianIndicator, final int hours)
    {
        // two special cases 12 AM & 12 PM
        if (hours == 12)
        {
            if (AM.equalsIgnoreCase(meridianIndicator))
            {
                return 0;
            }

            if (PM.equalsIgnoreCase(meridianIndicator))
            {
                return 12;
            }
        }

        final int onceMeridianAdjustment = PM.equalsIgnoreCase(meridianIndicator) ? 12 : 0;
        return hours + onceMeridianAdjustment;
    }


    public static Date tomorrow()
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    public static Date yesterday()
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }

    private static Calendar initCalendar(int year, int month, int day, int hour, int minute, int second, int millis)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, millis);
        return calendar;
    }
}
