package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.converters.DatePickerConverter;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.DateField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.history.DateTimeFieldChangeLogHelper;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.util.concurrent.LazyReference;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

public class DateCFType extends AbstractSingleFieldType
        implements SortableCustomField, ProjectImportableCustomField, DateField
{
    protected final DatePickerConverter dateConverter;
    private final DateTimeFieldChangeLogHelper dateTimeFieldChangeLogHelper;
    private final ProjectCustomFieldImporter projectCustomFieldImporter;
    private final DateFieldFormat dateFieldFormat;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;
    private final LazyReference<DateTimeFormatter> iso8601Formatter = new LazyIso8601DateFormatter();

    public DateCFType(CustomFieldValuePersister customFieldValuePersister, DatePickerConverter dateConverter, GenericConfigManager genericConfigManager, DateTimeFieldChangeLogHelper dateTimeFieldChangeLogHelper, DateFieldFormat dateFieldFormat, DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        super(customFieldValuePersister, genericConfigManager);
        this.dateConverter = dateConverter;
        this.dateTimeFieldChangeLogHelper = dateTimeFieldChangeLogHelper;
        this.projectCustomFieldImporter = new NoTransformationCustomFieldImporter();
        this.dateFieldFormat = dateFieldFormat;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
    }

    /**
     * @deprecated since v4.4. Use {@link DateCFType #} instead.
     */
    public DateCFType(CustomFieldValuePersister customFieldValuePersister, DatePickerConverter dateConverter, GenericConfigManager genericConfigManager)
    {
      this(customFieldValuePersister, dateConverter, genericConfigManager, ComponentAccessor.getComponentOfType(DateTimeFieldChangeLogHelper.class), ComponentAccessor.getComponentOfType(DateFieldFormat.class), ComponentAccessor.getComponentOfType(DateTimeFormatterFactory.class));
    }

    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_DATE;
    }

    protected Object getDbValueFromObject(Object customFieldObject)
    {
        return (Timestamp) customFieldObject;
    }

    protected Object getObjectFromDbValue(Object databaseValue) throws FieldValidationException
    {
        return (Timestamp) databaseValue;
    }

    @Override
    public String getChangelogString(CustomField field, Object value)
    {
        if (value == null)
            return "";
        return getStringFromSingularObject(value);
    }

    @Override
    public String getChangelogValue(CustomField field, Object value)
    {
        if (value == null)
            return "";
        assertObjectImplementsType(Date.class, value);
        Date date = (Date) value;
        return dateTimeFieldChangeLogHelper.createChangelogValueForDateField(date);
    }

    public String getStringFromSingularObject(Object customFieldObject)
    {
        assertObjectImplementsType(Date.class, customFieldObject);
        return dateConverter.getString((Date) customFieldObject);
    }

    public Object getSingularObjectFromString(String string) throws FieldValidationException
    {
        return dateConverter.getTimestamp(string);
    }

    public int compare(Object v1, Object v2, FieldConfig fieldConfig)
    {
        return ((Date) v1).compareTo((Date) v2);
    }

    public Object getDefaultValue(FieldConfig fieldConfig)
    {
        Date defaultValue = (Date) genericConfigManager.retrieve(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        if (isUseNow(defaultValue))
        {
            defaultValue = new Timestamp(new Date().getTime());
        }

        return defaultValue;
    }

    // -------------------------------------------------------------------------------------------------- View Helpers

    public boolean isUseNow(Date date)
    {
        return DatePickerConverter.USE_NOW_DATE.equals(date);
    }

    public boolean isUseNow(FieldConfig fieldConfig)
    {
        Date defaultValue = (Date) genericConfigManager.retrieve(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        return isUseNow(defaultValue);
    }

    public String getNow()
    {
        return dateConverter.getString(new Date());
    }

    public ProjectCustomFieldImporter getProjectImporter()
    {
        return this.projectCustomFieldImporter;
    }

    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem)
    {
        Map<String, Object> velocityParameters = super.getVelocityParameters(issue, field, fieldLayoutItem);
        velocityParameters.put("dateFieldFormat", dateFieldFormat);
        velocityParameters.put("iso8601Formatter", iso8601Formatter.get());

        return velocityParameters;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitDate(this);
        }
        
        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitDate(DateCFType dateCustomFieldType);
    }

    private class LazyIso8601DateFormatter extends LazyReference<DateTimeFormatter>
    {
        @Override
        protected DateTimeFormatter create()
        {
            return dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.ISO_8601_DATE).withSystemZone();
        }
    }
}
