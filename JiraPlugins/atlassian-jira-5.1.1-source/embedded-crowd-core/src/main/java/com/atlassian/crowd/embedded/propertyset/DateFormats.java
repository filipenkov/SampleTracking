package com.atlassian.crowd.embedded.propertyset;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public abstract class DateFormats
{
    public static final String DATE_PROPERTY_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * Returns a DateFormat that formats dates as ISO date format in UTC.
     */
    public static DateFormat getDateFormat()
    {
        DateFormat df = new SimpleDateFormat(DATE_PROPERTY_FORMAT_PATTERN);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df;
    }

    // prevent instantiation
    private DateFormats()
    {
    }
}