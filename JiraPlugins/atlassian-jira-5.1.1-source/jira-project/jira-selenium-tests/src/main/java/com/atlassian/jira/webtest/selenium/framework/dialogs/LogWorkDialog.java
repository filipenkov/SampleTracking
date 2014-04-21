package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.components.CalendarPopup;
import com.atlassian.jira.webtest.selenium.framework.model.ActionType;
import com.atlassian.jira.webtest.selenium.framework.model.LegacyIssueOperation;
import com.atlassian.jira.webtest.selenium.framework.model.Locators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents the "Log Work" dialog. 
 *
 * @since v4.1
 */
public class LogWorkDialog extends AbstractIssueDialog<LogWorkDialog>
{
    private static final String LOCATOR_STARTED = Locators.ID.create("log-work-date-logged-date-picker");
    private static final String LOCATOR_STARTED_CAL = Locators.ID.create("log-work-date-logged-icon");
    private static final String ENGLISH_FORMAT = "d/MMM/yy hh:mm a";

    public LogWorkDialog(SeleniumContext ctx)
    {
        super(LegacyIssueOperation.LOG_WORK, LogWorkDialog.class, ActionType.NEW_PAGE, ctx);
    }

    public CalendarPopup openDateStartedCalendar()
    {
        final CalendarPopup calendar = new CalendarPopup(LOCATOR_STARTED, LOCATOR_STARTED_CAL, context);
        calendar.open();
        return calendar;
    }

    // TODO move to popup

    public String getStartDateString()
    {
        return client.getValue(LOCATOR_STARTED);
    }

    public Date getStartDateEnglish()
    {
        final String dateString = getStartDateString();
        try
        {
            return new SimpleDateFormat(ENGLISH_FORMAT).parse(dateString);
        }
        catch (ParseException e)
        {
            throw new RuntimeException("Unable to parse date '" + dateString + "'.", e);
        }
    }
}
