package com.atlassian.streams.spi;

import org.joda.time.DateTimeZone;

/**
 * Default implementation of {@link FormatPreferenceProvider} for products
 * which do not have configurable formats and/or do not have formats accessible via their SPIs.
 */
public class DefaultFormatPreferenceProvider implements FormatPreferenceProvider
{
    public String getTimeFormatPreference()
    {
        return "h:mm a";
    }

    public String getDateFormatPreference()
    {
        return "d MMM yyyy";
    }

    public String getDateTimeFormatPreference()
    {
        return new StringBuilder()
            .append(getDateFormatPreference())
            .append(" ")
            .append(getTimeFormatPreference())
            .toString();
    }

    public DateTimeZone getUserTimeZone()
    {
        return DateTimeZone.getDefault();
    }
}
