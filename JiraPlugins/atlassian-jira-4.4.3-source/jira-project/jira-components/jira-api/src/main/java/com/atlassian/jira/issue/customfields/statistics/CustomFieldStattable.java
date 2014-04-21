package com.atlassian.jira.issue.customfields.statistics;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.statistics.StatisticsMapper;

public interface CustomFieldStattable
{
    public StatisticsMapper getStatisticsMapper(CustomField customField);
}
