package com.atlassian.jira.extra.icalfeed.dateprovider;

import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;

@SuppressWarnings("unused")
public abstract class CustomFieldDateProvider implements DateProvider
{
    @Override
    public boolean handles(Field field)
    {
        if (field instanceof CustomField)
        {
            CustomField customField = (CustomField) field;
            return handlesCustomFieldType(customField.getCustomFieldType());
        }

        return false;
    }

    protected abstract boolean handlesCustomFieldType(CustomFieldType customFieldType);
}
