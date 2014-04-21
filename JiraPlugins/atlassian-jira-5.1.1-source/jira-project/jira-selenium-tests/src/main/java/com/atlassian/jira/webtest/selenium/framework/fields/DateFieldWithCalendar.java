package com.atlassian.jira.webtest.selenium.framework.fields;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.components.CalendarPopup;
import com.atlassian.jira.webtest.selenium.framework.core.AbstractSeleniumPageObject;
import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a date field in JIRA with calendar popup.
 *
 * @since v4.2
 */
public class DateFieldWithCalendar extends AbstractSeleniumPageObject implements Field<Date>
{

    private final String dateFormat;
    private final String elementLocator;
    private final String calendarTrigger;

    public DateFieldWithCalendar(SeleniumContext ctx, String elementLocator, String calendarTrigger, String dateFormat)
    {
        super(ctx);
        this.elementLocator = elementLocator;
        this.calendarTrigger = calendarTrigger;
        this.dateFormat = dateFormat;
    }

    public Date getValue()
    {
        final String dateString = getStringValue();
        try
        {
            return new SimpleDateFormat(dateFormat).parse(dateString);
        }
        catch (ParseException e)
        {
            throw new RuntimeException("Unable to parse date '" + dateString + "' using '" + dateFormat + "'.", e);
        }
    }

    public CalendarPopup openCalendar()
    {
        final CalendarPopup popup = new CalendarPopup(elementLocator, calendarTrigger, context);
        popup.open();
        return popup;
    }

    // TODO use calendar popup (member variable)

    public String getStringValue()
    {
        assertThat.elementPresent(elementLocator);
        return StringUtils.trimToNull(client.getValue(elementLocator));
    }

    public void assertVisible()
    {
        assertThat.elementPresentByTimeout(elementLocator);
    }

    public static DateFieldWithCalendar createForDateTimeCustomField(SeleniumContext ctx, long customFieldId)
    {
        String elementLocator = String.format("id=customfield_%d", customFieldId);
        String calendarTarget = String.format("id=customfield_%d-trigger", customFieldId);

        return new DateFieldWithCalendar(ctx, elementLocator, calendarTarget, "dd/MMM/yy hh:mm a");
    }

    public static DateFieldWithCalendar createForDateCustomField(SeleniumContext ctx, long customFieldId)
    {
        String elementLocator = String.format("id=customfield_%d", customFieldId);
        String calendarTarget = String.format("id=customfield_%d-trigger", customFieldId);

        return new DateFieldWithCalendar(ctx, elementLocator, calendarTarget, "dd/MMM/yy");
    }
}
