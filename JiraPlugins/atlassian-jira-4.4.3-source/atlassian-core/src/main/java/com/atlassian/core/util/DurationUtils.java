package com.atlassian.core.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.core.i18n.I18nTextProvider;
import com.atlassian.core.util.DateUtils.Duration;

import org.apache.commons.lang.StringUtils;

/**
 * i18n-aware parsing of duration strings. Understands the output of {@link DateUtils}' formats.
 */
public class DurationUtils
{
    /**
     * resource key for day unit
     */
    private static final String UNIT_DAY = "core.durationutils.unit.day";

    /**
     * resource key for hour unit
     */
    private static final String UNIT_HOUR = "core.durationutils.unit.hour";

    /**
     * resource key for minute unit.
     */
    private static final String UNIT_MINUTE = "core.durationutils.unit.minute";

    // A number, optional whitespace and an optional non-whitespace unit token with an optional ignored comma after
    private static final Pattern COUNT_WITH_OPTIONAL_UNITS = Pattern.compile("([,\\.\\xA0'\\p{Nd}]+)\\s*(?:([^,\\s]+),?)?\\s*");
    
    public static long getDurationSeconds(String durationStr, long secondsPerDay, long secondsPerWeek, final Duration defaultUnit,
            Locale locale, I18nTextProvider i18n)
        throws InvalidDurationException
    {
        Map<String, Duration> durationTokens = getDurationTokens(i18n);
        
        return getDurationSeconds(durationStr, secondsPerDay, secondsPerWeek, defaultUnit, locale, durationTokens);
    }
    
    public static long getDurationSeconds(String durationStr, long secondsPerDay, long secondsPerWeek, final Duration defaultUnit,
            Locale locale, Map<String, Duration> tokens)
        throws InvalidDurationException
    {
        if (StringUtils.isBlank(durationStr))
        {
            return 0;
        }

        durationStr = durationStr.trim();
        
        NumberFormat nf = DecimalFormat.getNumberInstance(locale);

        long seconds = 0;

        Matcher m = COUNT_WITH_OPTIONAL_UNITS.matcher(durationStr);
        
        while (m.lookingAt())
        {
            // Use ParsePosition to detect a parse that stops before the string ends
            ParsePosition pp = new ParsePosition(0);
            
            String number = m.group(1);
            
            Number n = nf.parse(number, pp);
            
            if (pp.getIndex() != number.length())
            {
                throw new InvalidDurationException("Bad number '" + number + "' in duration string '" + durationStr + "'");
            }
            
            String unitName = m.group(2);
            
            Duration unit;
            if (unitName != null)
            {
                unit = tokens.get(unitName);
                if (unit == null)
                {
                    throw new InvalidDurationException("No unit for '" + unitName + "'");
                }
            }
            else
            {
                unit = defaultUnit;
            }
            
            long s = (long) (unit.getModifiedSeconds(secondsPerDay, secondsPerWeek) * n.doubleValue());
            
            if (unit != defaultUnit && s % 60 != 0)
            {
                throw new InvalidDurationException("Durations must be in whole minutes");
            }
            
            seconds += s;
            
            m.region(m.end(), durationStr.length());
        }
        
        if (m.regionStart() != durationStr.length())
        {
            throw new InvalidDurationException("Invalid characters in duration: " + durationStr);
        }
        
        return seconds;
    }

    public static Map<String, Duration> getDurationTokens(I18nTextProvider i18n)
    {
        Map<String, Duration> tokens = new HashMap<String, DateUtils.Duration>();
        
        tokens.put(getDurationToken(i18n, UNIT_DAY), Duration.DAY);
        tokens.put(getDurationToken(i18n, UNIT_HOUR), Duration.HOUR);
        tokens.put(getDurationToken(i18n, UNIT_MINUTE), Duration.MINUTE);
        
        for (Duration d : Duration.values())
        {
            String n = d.name().toLowerCase();
            tokens.put(i18n.getText("core.dateutils." + n), d);
            tokens.put(i18n.getText("core.dateutils." + n + "s"), d);
        }
        
        return tokens;
    }

    private static String getDurationToken(I18nTextProvider i18n, String unit)
    {
        String s = i18n.getText(unit);
        return s.replace("{0}", "").trim();
    }
}
