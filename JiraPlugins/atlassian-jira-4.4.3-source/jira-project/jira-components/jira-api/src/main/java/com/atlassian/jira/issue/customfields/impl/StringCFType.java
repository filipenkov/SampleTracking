package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;

public abstract class StringCFType extends AbstractSingleFieldType
{
    public StringCFType(final CustomFieldValuePersister customFieldValuePersister, final GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, genericConfigManager);
    }

    @Override
    protected Object getDbValueFromObject(final Object customFieldObject)
    {
        return getStringFromSingularObject(customFieldObject);
    }

    @Override
    protected Object getObjectFromDbValue(final Object databaseValue) throws FieldValidationException
    {
        return getSingularObjectFromString((String) databaseValue);
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitString(this);
        }
        
        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitString(StringCFType stringCustomFieldType);
    }
}
