package com.atlassian.core.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import com.atlassian.core.i18n.I18nTextProvider;
import com.atlassian.core.util.DateUtils.Duration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestDurationUtils
{
    private static final long SECONDS_IN_DAY = 60 * 60 * 24;
    private static final long SECONDS_IN_WEEK = SECONDS_IN_DAY * 7;
    
    static Map<String, Duration> defaultDurationTokens()
    {
        Map<String, Duration> tokens = new HashMap<String, Duration>();
        tokens.put("d", Duration.DAY);
        tokens.put("h", Duration.HOUR);
        tokens.put("m", Duration.MINUTE);
        return tokens;
    }
    
    static Map<String, Duration> durationTokensForPrettyFormat()
    {
        Map<String, Duration> tokens = new HashMap<String, Duration>();
        tokens.put("day", Duration.DAY);
        tokens.put("hour", Duration.HOUR);
        tokens.put("minute", Duration.MINUTE);
        tokens.put("week", Duration.WEEK);
        tokens.put("year", Duration.YEAR);
        tokens.put("days", Duration.DAY);
        tokens.put("hours", Duration.HOUR);
        tokens.put("minutes", Duration.MINUTE);
        tokens.put("weeks", Duration.WEEK);
        tokens.put("years", Duration.YEAR);
        return tokens;
    }
    
    static long toSeconds(String s, Duration defaultUnit) throws InvalidDurationException
    {
        return DurationUtils.getDurationSeconds(s, SECONDS_IN_DAY, SECONDS_IN_WEEK, defaultUnit, Locale.ENGLISH, defaultDurationTokens());
    }
    
    static long toSeconds(String s) throws InvalidDurationException
    {
        return toSeconds(s, Locale.ENGLISH, defaultDurationTokens());
    }
    
    static long toSeconds(String s, Map<String, Duration> durationTokens) throws InvalidDurationException
    {
        return toSeconds(s, Locale.ENGLISH, durationTokens);
    }
    
    static long toSeconds(String s, Locale locale, Map<String, Duration> durationTokens) throws InvalidDurationException
    {
        return DurationUtils.getDurationSeconds(s, SECONDS_IN_DAY, SECONDS_IN_WEEK, null, locale, durationTokens);
    }
    
    static boolean validDuration(String s, Locale locale, Map<String, Duration> durationTokens)
    {
        try
        {
            DurationUtils.getDurationSeconds(s, SECONDS_IN_DAY, SECONDS_IN_WEEK, null, locale, durationTokens);
            return true;
        }
        catch (InvalidDurationException ide)
        {
            return false;
        }
    }
    
    @Test
    public void blankStringIsEmptyDuration() throws InvalidDurationException
    {
        assertEquals(0, toSeconds(null));
        assertEquals(0, toSeconds(""));
        assertEquals(0, toSeconds(" "));
    }
    
    @Test
    public void parseJustNumberUsingDefaultUnit() throws InvalidDurationException
    {
        assertEquals(1, toSeconds("1", Duration.SECOND));
        assertEquals(60, toSeconds("1", Duration.MINUTE));
    }
    
    @Test(expected = InvalidDurationException.class)
    public void badNumbersCauseException() throws InvalidDurationException
    {
        toSeconds("1.0.5", Duration.SECOND);
    }
    
    @Test
    public void parseJustNumberUsingDefaultUnitWithLeadingTrailingSpaces() throws InvalidDurationException
    {
        assertEquals(1, toSeconds(" 1", Duration.SECOND));
        assertEquals(1, toSeconds("1 ", Duration.SECOND));
        assertEquals(1, toSeconds(" 1 ", Duration.SECOND));
    }
    
    @Test
    public void moreThanOneNumberWithOneUsingDefaultUnits() throws InvalidDurationException
    {
        assertEquals(90, toSeconds("1m 30", Duration.SECOND));
    }
    
    @Test
    public void moreThanOneNumberWithOneUsingDefaultUnitsWithLeadingTrailingSpaces() throws InvalidDurationException
    {
        assertEquals(90, toSeconds(" 1m 30", Duration.SECOND));
        assertEquals(90, toSeconds("1m 30 ", Duration.SECOND));
        assertEquals(90, toSeconds(" 1m 30 ", Duration.SECOND));
    }
    
    @Test(expected = InvalidDurationException.class)
    public void secondsNotSupportedAsUnit() throws InvalidDurationException
    {
        toSeconds("30s");
    }
    
    @Test
    public void moreThanOneNumberWithUnitsSpecified() throws InvalidDurationException
    {
        assertEquals(5400, toSeconds("1h 30m"));
        assertEquals(5400, toSeconds("30m 1h"));
        assertEquals(129600, toSeconds("12h 1d"));
        assertEquals(129600, toSeconds("1d 12h"));
    }

    @Test
    public void fractionalUnitsPermitted() throws InvalidDurationException
    {
        assertEquals(1800, toSeconds("0.5h"));
    }
    
    @Test(expected = InvalidDurationException.class)
    public void durationsMustRoundToWholeMinutes() throws InvalidDurationException
    {
        toSeconds("0.5m");
    }
    
    @Test
    public void durationUsingDifferentUnits() throws InvalidDurationException
    {
        assertEquals(86400, toSeconds("1t", Collections.singletonMap("t", Duration.DAY)));
    }
    
    @Test
    public void durationUsingMultipleDifferentUnits() throws InvalidDurationException
    {
        Map<String, Duration> durations = new HashMap<String, Duration>();
        durations.put("t", Duration.DAY);
        durations.put("h", Duration.HOUR);
        
        assertEquals(129600, toSeconds("12h 1t", durations));
    }
    
    @Test
    public void durationUsingDifferentDecimalFormat() throws InvalidDurationException
    {
        assertEquals(5400, toSeconds("1,5h", Locale.GERMANY, Collections.singletonMap("h", Duration.HOUR)));
    }
    
    @Test
    public void durationUsingDifferentDigits() throws InvalidDurationException
    {
        // Indian numerals: 1234.5
        assertEquals(4444200, toSeconds("\u0967,\u0968\u0969\u096A.\u096Bh", new Locale("hi", "IN"), defaultDurationTokens()));
    }
    
    @Test
    public void durationUsingNonAsciiUnits() throws InvalidDurationException
    {
        Map<String, Duration> durations = new HashMap<String, Duration>();
        durations.put("\u65e5", Duration.DAY);
        durations.put("\u6642\u9593", Duration.HOUR);
        durations.put("\u5206", Duration.MINUTE);
        
        assertEquals(352860, toSeconds("1 \u5206 2 \u6642\u9593 4 \u65e5", durations));
    }
    
    @Test
    public void durationTokensFromI18nResources()
    {
        Map<String, String> props = new HashMap<String, String>();
        props.put("core.durationutils.unit.hour", "{0}h");
        props.put("core.durationutils.unit.day", "{0}d");
        props.put("core.durationutils.unit.minute", "{0}m");
        props.put("core.dateutils.second", "second");
        props.put("core.dateutils.seconds", "seconds");
        props.put("core.dateutils.minute", "minute");
        props.put("core.dateutils.minutes", "minutes");
        props.put("core.dateutils.hour", "hour");
        props.put("core.dateutils.hours", "hours");
        props.put("core.dateutils.day", "day");
        props.put("core.dateutils.days", "days");
        props.put("core.dateutils.week", "week");
        props.put("core.dateutils.weeks", "weeks");
        props.put("core.dateutils.month", "month");
        props.put("core.dateutils.months", "months");
        props.put("core.dateutils.year", "year");
        props.put("core.dateutils.years", "years");
        
        Map<String, Duration> expectedDurations = new HashMap<String, Duration>();
        expectedDurations.put("d", Duration.DAY);
        expectedDurations.put("h", Duration.HOUR);
        expectedDurations.put("m", Duration.MINUTE);
        expectedDurations.put("second", Duration.SECOND);
        expectedDurations.put("seconds", Duration.SECOND);
        expectedDurations.put("day", Duration.DAY);
        expectedDurations.put("hour", Duration.HOUR);
        expectedDurations.put("minute", Duration.MINUTE);
        expectedDurations.put("week", Duration.WEEK);
        expectedDurations.put("month", Duration.MONTH);
        expectedDurations.put("year", Duration.YEAR);
        expectedDurations.put("days", Duration.DAY);
        expectedDurations.put("hours", Duration.HOUR);
        expectedDurations.put("minutes", Duration.MINUTE);
        expectedDurations.put("weeks", Duration.WEEK);
        expectedDurations.put("months", Duration.MONTH);
        expectedDurations.put("years", Duration.YEAR);
        
        Map<String, Duration> durations = DurationUtils.getDurationTokens(i18nForProps(props));
        
        assertEquals(expectedDurations, durations);
    }
    
    @Test
    public void durationTokensFromI18nResourcesNonAsciiUnits()
    {
        Map<String, String> props = new HashMap<String, String>();
        props.put("core.durationutils.unit.hour", "{0} \u6642\u9593");
        props.put("core.durationutils.unit.day", "{0} \u65e5");
        props.put("core.durationutils.unit.minute", "{0} \u5206");
        props.put("core.dateutils.second", "\u79d2");
        props.put("core.dateutils.seconds", "\u79d2");
        props.put("core.dateutils.minute", "\u5206");
        props.put("core.dateutils.minutes", "\u5206");
        props.put("core.dateutils.hour", "\u6642\u9593");
        props.put("core.dateutils.hours", "\u6642\u9593");
        props.put("core.dateutils.day", "\u65e5");
        props.put("core.dateutils.days", "\u65e5");
        props.put("core.dateutils.week", "\u9031");
        props.put("core.dateutils.weeks", "\u9031");
        props.put("core.dateutils.month", "\u6708");
        props.put("core.dateutils.months", "\u6708");
        props.put("core.dateutils.year", "\u5e74");
        props.put("core.dateutils.years", "\u5e74");

        Map<String, Duration> expectedDurations = new HashMap<String, Duration>();
        expectedDurations.put("\u79d2", Duration.SECOND);
        expectedDurations.put("\u65e5", Duration.DAY);
        expectedDurations.put("\u6642\u9593", Duration.HOUR);
        expectedDurations.put("\u5206", Duration.MINUTE);
        expectedDurations.put("\u9031", Duration.WEEK);
        expectedDurations.put("\u6708", Duration.MONTH);
        expectedDurations.put("\u5e74", Duration.YEAR);
        
        Map<String, Duration> durations = DurationUtils.getDurationTokens(i18nForProps(props));
        
        assertEquals(expectedDurations, durations);
    }
    
    @Test(expected = MissingResourceException.class)
    public void durationTokensFailsWhenResourceMissing()
    {
        DurationUtils.getDurationTokens(i18nForProps(Collections.<String, String>emptyMap()));
    }
    
    @Test
    public void acceptPrettyFormatDurations() throws InvalidDurationException
    {
        assertEquals(0, toSeconds("0 minutes", durationTokensForPrettyFormat()));
        assertEquals(600, toSeconds("10 minutes", durationTokensForPrettyFormat()));
        assertEquals(3660, toSeconds("1 hour, 1 minute", durationTokensForPrettyFormat()));
        assertEquals(90060, toSeconds("1 day, 1 hour, 1 minute", durationTokensForPrettyFormat()));
        assertEquals(694860, toSeconds("1 week, 1 day, 1 hour, 1 minute", durationTokensForPrettyFormat()));
    }
    
    @Test
    public void ensureBadStringsFailToParse()
    {
        assertFalse(validDuration("h", Locale.ENGLISH, defaultDurationTokens()));
        assertFalse(validDuration("0x", Locale.ENGLISH, defaultDurationTokens()));
    }
    
    static I18nTextProvider i18nForProps(final Map<String, String> props)
    {
        return new I18nTextProvider()
        {
            public String getText(String key) throws MissingResourceException
            {
                if (props.containsKey(key))
                {
                    return props.get(key);
                }
                else
                {
                    throw new MissingResourceException("No 18n string available for '" + key + "'", getClass().getName(), key);
                }
            }
            
            public String getText(String key, Object[] args)
            {
                throw new UnsupportedOperationException("Test stub");
            }
        };
    }
}
