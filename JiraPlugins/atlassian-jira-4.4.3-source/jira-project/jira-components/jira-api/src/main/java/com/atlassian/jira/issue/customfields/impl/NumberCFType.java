package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.util.velocity.NumberTool;

import java.util.Map;

public class NumberCFType extends AbstractSingleFieldType implements SortableCustomField<Double>, ProjectImportableCustomField
{
    private final DoubleConverter doubleConverter;
    private final ProjectCustomFieldImporter projectCustomFieldImporter;

    public NumberCFType(final CustomFieldValuePersister customFieldValuePersister, final DoubleConverter doubleConverter, final GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, genericConfigManager);
        this.doubleConverter = doubleConverter;
        projectCustomFieldImporter = new NoTransformationCustomFieldImporter();
    }

    @Override
    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_DECIMAL;
    }

    public String getStringFromSingularObject(final Object customFieldObject)
    {
        assertObjectImplementsType(Double.class, customFieldObject);
        return doubleConverter.getString((Double) customFieldObject);
    }

    public Object getSingularObjectFromString(final String string) throws FieldValidationException
    {
        return doubleConverter.getDouble(string);
    }

    @Override
    public String getChangelogValue(final CustomField field, final Object value)
    {
        if (value == null)
        {
            return "";
        }
        else
        {
            assertObjectImplementsType(Double.class, value);
            return doubleConverter.getStringForChangelog((Double) value);
        }
    }


    public int compare(final Double customFieldObjectValue1, final Double customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        return customFieldObjectValue1.compareTo(customFieldObjectValue2);
    }

    @Override
    protected Object getDbValueFromObject(final Object customFieldObject)
    {
        return customFieldObject;
    }

    @Override
    protected Object getObjectFromDbValue(final Object databaseValue) throws FieldValidationException
    {
        return databaseValue;
    }

    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        final Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);
        map.put("numberTool", new NumberTool(getI18nBean().getLocale()));
        return map;
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
            return ((Visitor) visitor).visitNumber(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitNumber(NumberCFType numberCustomFieldType);
    }
}
