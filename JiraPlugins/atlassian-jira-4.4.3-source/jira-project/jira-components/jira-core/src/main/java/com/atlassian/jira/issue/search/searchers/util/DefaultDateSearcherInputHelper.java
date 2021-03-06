package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.jira.issue.customfields.converters.DateConverter;
import com.atlassian.jira.issue.customfields.converters.DateTimeConverter;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.timezone.TimeZoneManager;
import org.apache.commons.lang.StringUtils;

import java.util.Calendar;
import java.util.Date;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Helper class to parse JQL clauses and determine if they are suitable for usage in the Navigator or Search URL.
 *
 * @since v4.0
 */
public class DefaultDateSearcherInputHelper extends AbstractDateSearchInputHelper
{
    private final JqlDateSupport jqlDateSupport;
    private final DateConverter primaryDateConverter;
    private final DateTimeConverter dateTimeConverter;
    private final TimeZoneManager timeZoneManager;

    public DefaultDateSearcherInputHelper(final DateSearcherConfig config, final JqlOperandResolver operandResolver, final JqlDateSupport jqlDateSupport, final DateConverter primaryDateConverter, final DateTimeConverter dateTimeConverter, TimeZoneManager timeZoneManager)
    {
        super(config, operandResolver);
        this.jqlDateSupport = notNull("jqlDateSupport", jqlDateSupport);
        this.primaryDateConverter = notNull("primaryDateConverter", primaryDateConverter);
        this.dateTimeConverter = notNull("dateTimeConverter", dateTimeConverter);
        this.timeZoneManager = notNull("timeZoneManager", timeZoneManager);
    }

    protected ParseDateResult getValidNavigatorDate(final QueryLiteral dateLiteral, final boolean allowTimeComponent)
    {
        final Date date;
        if (dateLiteral.getLongValue() != null)
        {
            date = jqlDateSupport.convertToDate(dateLiteral.getLongValue());

            // if our long didnt convert, we should just return the literal as a string
            if (date == null)
            {
                return new ParseDateResult(true, dateLiteral.getLongValue().toString());
            }
        }
        else if (StringUtils.isNotBlank(dateLiteral.getStringValue()))
        {
            date = jqlDateSupport.convertToDate(dateLiteral.getStringValue());

            // if our string didnt convert, we should just return the literal as a string
            if (date == null)
            {
                return new ParseDateResult(true, dateLiteral.getStringValue());
            }
        }
        else
        {
            return null;
        }

        // if the searcher allows time, we want to use the date time format
        // if the date has a time component, then regardless of whether its supported, we want to use date time format
        // because it allows us to report the error to the user
        if (allowTimeComponent)
        {
            return new ParseDateResult(true, dateTimeConverter.getString(date));
        }
        else if (hasTimeComponent(date))
        {
            return new ParseDateResult(false, dateTimeConverter.getString(date));
        }
        else
        {
            return new ParseDateResult(true, primaryDateConverter.getString(date));
        }
    }

    private boolean hasTimeComponent(final Date date)
    {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeZone(timeZoneManager.getLoggedInUserTimeZone());
        cal.setTime(date);
        //Forces the calendar to re-calculate
        cal.getTime();

        // only check hours and minutes
        return (cal.get(Calendar.HOUR_OF_DAY) != 0) || (cal.get(Calendar.MINUTE) != 0);
    }
}
