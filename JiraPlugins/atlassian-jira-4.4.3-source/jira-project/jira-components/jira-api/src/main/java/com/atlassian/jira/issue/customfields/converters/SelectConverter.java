package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchContext;

public interface SelectConverter
{
    public static final Long ALL_LONG = new Long(-1);
    public static final String ALL_STRING = "-1";

    public String getString(Object value);

    public String getObject(String stringValue);

    public SearchContext getPossibleSearchContextFromValue(Object value, CustomField customField);
}