package com.atlassian.jira.issue.index;

import com.atlassian.jira.issue.history.DateRangeBuilder;

/**
 * Representsa a system field that can be searched by JQL
 *
 * @since v5.0
 */
public class ChangeHistoryFieldConfiguration
{
    private final DateRangeBuilder dateRangeBuilder;
    private final String emptyValue;
    private final boolean supportsIdSearching;


    public ChangeHistoryFieldConfiguration(DateRangeBuilder dateRangeBuilder, String emptyValue, boolean supportsIdSearching)
    {
        this.dateRangeBuilder = dateRangeBuilder;
        this.emptyValue = emptyValue;
        this.supportsIdSearching = supportsIdSearching;
    }

    public DateRangeBuilder getDateRangeBuilder()
    {
        return dateRangeBuilder;
    }

    public String getEmptyValue()
    {
        return emptyValue;
    }

    public boolean supportsIdSearching()
    {
        return supportsIdSearching;
    }
}
