package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.issue.customfields.converters.StringConverter;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;

public class TextAreaCFType extends RenderableTextCFType
{
    public TextAreaCFType(CustomFieldValuePersister customFieldValuePersister, StringConverter stringConverter,
                          GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, stringConverter, genericConfigManager);
    }

    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_UNLIMITED_TEXT;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitTextArea(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitTextArea(TextAreaCFType textAreaCustomFieldType);
    }
}
