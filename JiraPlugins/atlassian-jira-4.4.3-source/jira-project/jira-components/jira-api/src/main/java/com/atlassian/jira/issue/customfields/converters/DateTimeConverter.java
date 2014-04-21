package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.jira.issue.customfields.impl.FieldValidationException;

import java.sql.Timestamp;
import java.util.Date;

public interface DateTimeConverter
{
    public String getString(Date value);

    public Timestamp getTimestamp(String stringValue) throws FieldValidationException;
}
