package com.atlassian.streams.jira;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.sal.api.timezone.TimeZoneManager;
import com.atlassian.streams.spi.FormatPreferenceProvider;

import org.joda.time.DateTimeZone;

import static com.google.common.base.Preconditions.checkNotNull;

public class JiraFormatPreferenceProvider implements FormatPreferenceProvider
{
    private final DateTimeFormatterFactory formatterFactory;
    private final TimeZoneManager timeZoneManager;

    public JiraFormatPreferenceProvider(DateTimeFormatterFactory formatterFactory,
                                        TimeZoneManager timeZoneManager)
    {
        this.formatterFactory = checkNotNull(formatterFactory, "formatterFactory");
        this.timeZoneManager = checkNotNull(timeZoneManager, "timeZoneManager");
    }

    public String getTimeFormatPreference()
    {
        return formatterFactory.formatter().withStyle(DateTimeStyle.TIME).getFormatHint();
    }

    public String getDateFormatPreference()
    {
        return formatterFactory.formatter().withStyle(DateTimeStyle.DATE).getFormatHint();
    }

    public String getDateTimeFormatPreference()
    {
        return formatterFactory.formatter().withStyle(DateTimeStyle.COMPLETE).getFormatHint();
    }

    public DateTimeZone getUserTimeZone()
    {
        try
        {
            return DateTimeZone.forTimeZone(timeZoneManager.getUserTimeZone());
        }
        catch (IllegalArgumentException e)
        {
            return DateTimeZone.getDefault();
        }
    }
}
