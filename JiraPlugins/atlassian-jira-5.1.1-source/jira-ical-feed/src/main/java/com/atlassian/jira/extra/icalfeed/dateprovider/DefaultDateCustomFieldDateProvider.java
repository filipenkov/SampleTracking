package com.atlassian.jira.extra.icalfeed.dateprovider;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.DateCFType;
import com.atlassian.jira.issue.customfields.impl.DateTimeCFType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.sql.Timestamp;
import java.util.Date;

public class DefaultDateCustomFieldDateProvider extends CustomFieldDateProvider
{
    private final CustomFieldManager customFieldManager;

    public DefaultDateCustomFieldDateProvider(CustomFieldManager customFieldManager)
    {
        this.customFieldManager = customFieldManager;
    }

    @Override
    protected boolean handlesCustomFieldType(CustomFieldType customFieldType)
    {
        return isDateType(customFieldType) || isDateTimeType(customFieldType);
    }

    private boolean isDateTimeType(CustomFieldType customFieldType)
    {
        return customFieldType instanceof DateTimeCFType;
    }

    private boolean isDateType(CustomFieldType customFieldType)
    {
        return customFieldType instanceof DateCFType;
    }

    @Override
    public DateTime getStart(Issue issue, Field field)
    {
        DateTime start = null;
        CustomField customField = (CustomField) field;
        if (isCustomFieldAssociatedWithIssue(issue, customField))
        {
            Object customFieldValue = issue.getCustomFieldValue(customField);
            if (null != customFieldValue)
            {
                if (customFieldValue instanceof Timestamp)
                    start = new DateTime(((Timestamp) customFieldValue).getTime());
                else if (customFieldValue instanceof Date)
                    start = new DateTime(customFieldValue);
            }
        }

        return start;
    }

    private boolean isCustomFieldAssociatedWithIssue(Issue issue, CustomField customField)
    {
        for (CustomField issueCustomField : customFieldManager.getCustomFieldObjects(issue))
            if (StringUtils.equals(issueCustomField.getId(), customField.getId()))
                return true;

        return false;
    }

    @Override
    public DateTime getEnd(Issue issue, Field field, DateTime startDate)
    {
        return null == startDate ? null : startDate.plusMinutes(60 * (isAllDay(issue, field) ? 24 : 1));
    }

    @Override
    public boolean isAllDay(Issue issue, Field field)
    {
        return isDateType(((CustomField) field).getCustomFieldType());
    }
}
