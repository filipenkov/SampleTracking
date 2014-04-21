package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.converters.StringConverter;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract class for Multi-select Custom field types for  either Users or Groups.
 * Note that there is also another multi-select Custom Field for arbitrary options - MultiSelectCFType.
 * <p>
 * The <em>Transport Object</em> for this Custom Field type is a Collection of Users or Collection of Groups;
 * depending on the subclass.
 * Some of these methods will also accept a Collection of String as an input parameter.
 * See the javadoc of {@link #updateValue(com.atlassian.jira.issue.fields.CustomField,com.atlassian.jira.issue.Issue,Object)},
 * {@link #createValue(com.atlassian.jira.issue.fields.CustomField,com.atlassian.jira.issue.Issue,Object)}, and
 * {@link #setDefaultValue(com.atlassian.jira.issue.fields.config.FieldConfig,Object)} for instance.
 * </p>
 *
 * @see com.atlassian.jira.issue.customfields.CustomFieldType
 * @see com.atlassian.jira.issue.customfields.impl.MultiSelectCFType
 */
public abstract class AbstractMultiCFType<T> extends TextCFType implements CustomFieldType
{
    // TODO: why are we extending TextCFType? This is a multi-select field and seems unrelated.
    // Meanwhile MultiSelectCFType is on its own hierarchy and seems more closely related especially in terms of Transport Object.

    /** Overriden, calls super constructor. */
    public AbstractMultiCFType(final CustomFieldValuePersister customFieldValuePersister, final StringConverter stringConverter, final GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, stringConverter, genericConfigManager);
    }

    /**
     * Returns a comparator for underlying type of this custom field.
     *
     * @return a comparator
     */
    abstract protected Comparator<T> getTypeComparator();

    /**
     * Converts a given underlying type to String.
     * If the value parameter is already of type String then this String is just returned.
     *
     * @param value underlying type
     * @return string representation of underlying type
     */
    abstract protected String convertTypeToString(Object value);

    /**
     * Converts a given String to underlying type
     *
     * @param string string representation of underlying type
     * @return underlying type
     */
    abstract protected T convertStringToType(String string);

    /** @see CustomFieldType#getDefaultValue(com.atlassian.jira.issue.fields.config.FieldConfig) */
    @Override
    public Object getDefaultValue(final FieldConfig fieldConfig)
    {
        final Object o = genericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());

        if (o == null)
        {
            return null;
        }
        else if (o instanceof Collection<?>)
        {
            //noinspection unchecked
            return convertStringsToTypes((Collection<String>) o);
        }
        else
        {
            throw new IllegalArgumentException("Value: " + o + " must be a collection. Type not allowed: " + o.getClass());
        }
    }

    /**
     * Sets the default value for a Custom Field.
     * <p>
     * The <em>Transport Object</em> for parameter value can be either a Collection of the underlying type (User or Group), or a
     * Collection of String.
     * </p>
     *
     * @param fieldConfig CustomField for which the default is being stored
     * @param value       <em>Transport Object</em> representing the value instance of the CustomField.
     * @see CustomFieldType#setDefaultValue(com.atlassian.jira.issue.fields.config.FieldConfig,Object)
     */
    @Override
    public void setDefaultValue(final FieldConfig fieldConfig, final Object value)
    {
        final Collection names = convertTypesToStringsIfRequired((Collection) value);
        if ((names == null) || names.isEmpty())
        {
            genericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), null);
        }
        else
        {
            genericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), names);
        }
    }

    /**
     * Create a multi-select value for an issue.
     * <p>
     * The <em>Transport Object</em> for parameter value can be either a Collection of the underlying type (User or Group), or a
     * Collection of String.
     * </p>
     *
     * @param customField {@link com.atlassian.jira.issue.fields.CustomField} for which the value is being stored
     * @param issue       The {@link com.atlassian.jira.issue.Issue}.
     * @param value       <em>Transport Object</em> representing the value instance of the CustomField.
     * @see CustomFieldType#createValue(com.atlassian.jira.issue.fields.CustomField,com.atlassian.jira.issue.Issue,Object)
     */
    @Override
    public void createValue(final CustomField customField, final Issue issue, final Object value)
    {
        customFieldValuePersister.createValues(customField, issue.getId(), PersistenceFieldType.TYPE_LIMITED_TEXT,
            convertTypesToStringsIfRequired((Collection) value));
    }

    /**
     * Update a multi-select value for an issue.
     * <p>
     * The <em>Transport Object</em> for parameter value can be either a Collection of the underlying type (User or Group), or a
     * Collection of String.
     * </p>
     *
     * @param customField {@link com.atlassian.jira.issue.fields.CustomField} for which the value is being stored
     * @param issue       The {@link com.atlassian.jira.issue.Issue}.
     * @param value       <em>Transport Object</em> representing the value instance of the CustomField.
     * @see CustomFieldType#updateValue(com.atlassian.jira.issue.fields.CustomField,com.atlassian.jira.issue.Issue,Object)
     */
    @Override
    public void updateValue(final CustomField customField, final Issue issue, final Object value)
    {
        customFieldValuePersister.updateValues(customField, issue.getId(), PersistenceFieldType.TYPE_LIMITED_TEXT,
            convertTypesToStringsIfRequired((Collection) value));
    }

    /** @see CustomFieldType#getValueFromIssue(com.atlassian.jira.issue.fields.CustomField,com.atlassian.jira.issue.Issue) */
    @Override
    public Object getValueFromIssue(final CustomField field, final Issue issue)
    {
        final List textValues = customFieldValuePersister.getValues(field, issue.getId(), PersistenceFieldType.TYPE_LIMITED_TEXT);
        return ((textValues == null) || textValues.isEmpty()) ? null : convertStringsToTypes(textValues);
    }

    /**
     * Returns a string representation of the value if not null.
     *
     * @param field not used
     * @param value value to create a change log for
     * @return string representaion of value if not null, empty string otherwise
     */
    @Override
    public String getChangelogValue(final CustomField field, final Object value)
    {
        return value == null ? "" : value.toString();
    }

    /**
     * Converts a collection of underlying types to a collection of string representations of underlying type. Returns
     * null when typedList is null.
     * <p>
     * If a Collection of String is passed, then a new Collection is still created, containing the original String values.
     * </p>
     *
     * @param typedList a collection of underlying types
     * @return a collection of string representations of underlying type
     */
    final protected Collection<String> convertTypesToStringsIfRequired(final Collection<?> typedList)
    {
        if (typedList == null)
        {
            return null;
        }
        return CollectionUtil.transform(typedList, new Function<Object, String>()
        {
            public String get(final Object item)
            {
                if (item instanceof String)
                {
                    // No conversion required
                    return (String) item;
                }
                return convertTypeToString(item);
            }
        });
    }

    /**
     * Converts a collection of string representations of underlying type to a collection of underlying types. Returns
     * null when given strings collection is null.
     *
     * @param strings a collection of string representations of underlying type
     * @return a collection of underlying types
     */
    final protected Collection<T> convertStringsToTypes(final Collection<String> strings)
    {
        if (strings == null)
        {
            return null;
        }
        final Set<T> retSet = new HashSet<T>();
        for (final Object element : strings)
        {
            final String string = ((String) element).trim();
            try
            {
                final T value = convertStringToType(string);
                if (value != null)
                {
                    retSet.add(value);
                }
            }
            catch (final FieldValidationException ignore)
            {}
        }
        final List<T> list = new ArrayList<T>(retSet);
        Collections.sort(list, getTypeComparator());
        return list;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitMultiField(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitMultiField(AbstractMultiCFType multiCustomFieldType);
    }
}
