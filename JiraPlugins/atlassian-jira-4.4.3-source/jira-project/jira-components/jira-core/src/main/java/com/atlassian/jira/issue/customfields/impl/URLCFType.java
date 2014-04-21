package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.opensymphony.util.UrlUtils;

public class URLCFType extends StringCFType implements SortableCustomField<String>, ProjectImportableCustomField
{
    private final ProjectCustomFieldImporter projectCustomFieldImporter;

    public URLCFType(final CustomFieldValuePersister customFieldValuePersister, final GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, genericConfigManager);
        projectCustomFieldImporter = new NoTransformationCustomFieldImporter();
    }

    public String getStringFromSingularObject(final Object value)
    {
        return (String) value;
    }

    public Object getSingularObjectFromString(final String string) throws FieldValidationException
    {
        // JRA-14998 - trim URLs before validating. URLs will also be saved in trim form.
        final String uri = (string == null) ? null : string.trim();
        if (!UrlUtils.verifyHierachicalURI(uri))
        {
            throw new FieldValidationException("Not a valid URL");
        }
        return uri;
    }

    public int compare(final String customFieldObjectValue1, final String customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        return customFieldObjectValue1.compareTo(customFieldObjectValue2);
    }

    @Override
    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_LIMITED_TEXT;
    }

    public ProjectCustomFieldImporter getProjectImporter()
    {
        return projectCustomFieldImporter;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitURL(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitURL(URLCFType urlCustomFieldType);
    }
}
