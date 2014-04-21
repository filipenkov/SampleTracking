package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.util.OutlookDate;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;

public class DateTimeConverterImpl implements DateTimeConverter
{
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public DateTimeConverterImpl(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public String getString(Date value)
    {
        if (value == null)
        {
            return "";
        }
        return getOutlookDate().formatDMYHMS(value);
    }

    public Timestamp getTimestamp(String stringValue) throws FieldValidationException
    {
        if (stringValue == null)
            return null;

        try
        {
            return new Timestamp(getOutlookDate().parseCompleteDateTime(stringValue).getTime());
        }
        catch (ParseException pe)
        {
            throw new FieldValidationException("Invalid date / time format.  Expected " + getOutlookDate().getCompleteDateTimeFormat());
        }
    }

    private OutlookDate getOutlookDate()
    {
        return jiraAuthenticationContext.getOutlookDate();
    }


}
