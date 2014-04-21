package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
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
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang.StringUtils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

public class DateTimeCFType extends AbstractSingleFieldType
        implements SortableCustomField<Date>, ProjectImportableCustomField, DateField
{
    private final ProjectCustomFieldImporter projectCustomFieldImporter;
    private final DateTimeFormatter datePickerFormatter;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ApplicationProperties applicationProperties;
    private final DateTimeFieldChangeLogHelper dateTimeFieldChangeLogHelper;

    public DateTimeCFType(final CustomFieldValuePersister customFieldValuePersister, final GenericConfigManager genericConfigManager, DateTimeFormatterFactory dateTimeFormatterFactory, JiraAuthenticationContext jiraAuthenticationContext, ApplicationProperties applicationProperties, DateTimeFieldChangeLogHelper dateTimeFieldChangeLogHelper)
    {
        super(customFieldValuePersister, genericConfigManager);
        this.applicationProperties = applicationProperties;
        this.dateTimeFieldChangeLogHelper = dateTimeFieldChangeLogHelper;
        projectCustomFieldImporter = new NoTransformationCustomFieldImporter();
        datePickerFormatter = dateTimeFormatterFactory.formatter().forLoggedInUser().withStyle(DateTimeStyle.DATE_TIME_PICKER);
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    /**
     * @deprecated since v4.4. Use {@link com.atlassian.jira.issue.customfields.impl.DateTimeCFType} instead.
     */
    public DateTimeCFType(final CustomFieldValuePersister customFieldValuePersister, final GenericConfigManager genericConfigManager, DateTimeFormatterFactory dateTimeFormatterFactory, JiraAuthenticationContext jiraAuthenticationContext, ApplicationProperties applicationProperties)
    {
        this(customFieldValuePersister, genericConfigManager, dateTimeFormatterFactory, jiraAuthenticationContext, applicationProperties, ComponentAccessor.getComponentOfType(DateTimeFieldChangeLogHelper.class));
    }

    @Override
    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_DATE;
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

    public String getStringFromSingularObject(final Object customFieldObject)
    {
        assertObjectImplementsType(Date.class, customFieldObject);
        return datePickerFormatter.format((Date) customFieldObject);
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
        return dateTimeFieldChangeLogHelper.createChangelogValueForDateTimeField(date);
    }

    public Object getSingularObjectFromString(final String string) throws FieldValidationException
    {
        if (StringUtils.isEmpty(string))
        {
            return null;
        }

        try
        {
            Date date = datePickerFormatter.parse(string);

            return new Timestamp(date.getTime());
        }
        catch (IllegalArgumentException e)
        {
            final I18nHelper i18nBean = jiraAuthenticationContext.getI18nHelper();
            throw new FieldValidationException(i18nBean.getText("fields.validation.data.format", applicationProperties.getDefaultBackedString(APKeys.JIRA_DATE_TIME_PICKER_JAVA_FORMAT)));
        }
    }

    public int compare(final Date v1, final Date v2, final FieldConfig fieldConfig)
    {
        return v1.compareTo(v2);
    }

    @Override
    public Object getDefaultValue(final FieldConfig fieldConfig)
    {
        Date defaultValue = (Date) genericConfigManager.retrieve(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        if (isUseNow(defaultValue))
        {
            defaultValue = new Timestamp(new Date().getTime());
        }

        return defaultValue;
    }

    // -------------------------------------------------------------------------------------------------- View Helpers
    public boolean isUseNow(final Date date)
    {
        return DatePickerConverter.USE_NOW_DATE.equals(date);
    }

    public boolean isUseNow(final FieldConfig fieldConfig)
    {
        final Date defaultValue = (Date) genericConfigManager.retrieve(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        return isUseNow(defaultValue);
    }

    public String getNow()
    {
        return datePickerFormatter.format(new Date());
    }

    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        final Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);
        map.put("dateTimePicker", Boolean.TRUE);
        map.put("datePickerFormatter", datePickerFormatter);
        map.put("titleFormatter", datePickerFormatter.withStyle(DateTimeStyle.COMPLETE));
        map.put("iso8601Formatter", datePickerFormatter.withStyle(DateTimeStyle.ISO_8601_DATE_TIME));

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
            return ((Visitor) visitor).visitDateTime(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitDateTime(DateTimeCFType dateTimeCFType);
    }
}
