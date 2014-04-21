package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.converters.StringConverter;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.web.bean.BulkEditBean;

public class ReadOnlyCFType extends TextCFType
{
    public ReadOnlyCFType(CustomFieldValuePersister customFieldValuePersister, StringConverter stringConverter, GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, stringConverter, genericConfigManager);
    }

    public void updateValue(CustomField customField, Issue issue, Object value)
    {
       if (value != null)
       {
           super.updateValue(customField, issue, value);
       }
    }

    public String getChangelogValue(CustomField field, Object value)
    {
        if (value != null)
        {
            return super.getChangelogValue(field, value);
        }
        else
        {
            return null;
        }
    }

    // Read only - not editable
    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        return "bulk.edit.unavailable";
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitReadOnly(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitReadOnly(ReadOnlyCFType readOnlyCustomFieldType);
    }
}
