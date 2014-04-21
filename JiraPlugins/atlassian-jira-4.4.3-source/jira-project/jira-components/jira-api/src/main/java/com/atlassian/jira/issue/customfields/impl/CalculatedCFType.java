package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.ErrorCollection;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class CalculatedCFType extends AbstractCustomFieldType implements SortableCustomField
{
    public Set remove(CustomField field)
    {
        return Collections.EMPTY_SET;
    }

    public void validateFromParams(CustomFieldParams relevantParams, ErrorCollection errorCollectionToAddTo, FieldConfig config)
    {
        // Do nothing
    }

    public void createValue(CustomField field, Issue issue, Object value)
    {
        // Do nothing
    }

    public void updateValue(CustomField field, Issue issue, Object value)
    {
        // Do nothing
    }

    public void removeValue(CustomField arg0, GenericValue arg1, Object arg2)
    {
        // Do nothing
    }

    public Object getDefaultValue(FieldConfig fieldConfig)
    {
        return null;
    }

    public void setDefaultValue(FieldConfig fieldConfig, Object value)
    {
        // Do nothing
    }

    public String getChangelogValue(CustomField field, Object value)
    {
        return null;
    }

    public Object getValueFromCustomFieldParams(CustomFieldParams parameters) throws FieldValidationException
    {
        Object o = parameters.getFirstValueForKey(null);

        return getSingularObjectFromString((String) o);
    }

    public Object getStringValueFromCustomFieldParams(CustomFieldParams parameters)
    {
        return parameters.getFirstValueForNullKey();
    }

    public int compare(Object o1, Object o2, FieldConfig fieldConfig)
    {
        if (o1 != null && o1 instanceof Comparable && o2 instanceof Comparable)
        {
            Comparable comparable1 = (Comparable) o1;
            return comparable1.compareTo(o2);
        }

        return 0;
    }

    public List getConfigurationItemTypes()
    {
        // No defaults for calculated custom fields and can't be Collections.EMPTY_LIST since it needs to be mutatble
        return new ArrayList();
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitCalculated(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitCalculated(CalculatedCFType calculatedCustomFieldType);
    }
}

