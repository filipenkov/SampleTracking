package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.imports.project.customfield.SelectCustomFieldImporter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.GroupSelectorField;
import com.atlassian.jira.issue.customfields.MultipleCustomFieldType;
import com.atlassian.jira.issue.customfields.MultipleSettableCustomFieldType;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.config.item.SettableOptionsConfigItem;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.OptionUtils;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.statistics.SelectStatisticsMapper;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.util.ErrorCollection;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * <p>Multiple Select Type</p>
 * <dl>
 * <dt><strong>Transport Object Type</strong></dt>
 * <dd>{@link Collection}</dd>
 * <dt><strong>Singular Object Type</strong></dt>
 * <dd>{@link String}</dd>
 * </dl>
 */
public class MultiSelectCFType extends AbstractMultiSettableCFType
        implements MultipleSettableCustomFieldType, MultipleCustomFieldType, SortableCustomField<List<String>>, GroupSelectorField, ProjectImportableCustomField
{
    public static final String COMMA_REPLACEMENT = "&#44;";
    private static final Logger log = Logger.getLogger(MultiSelectCFType.class);

    private final ProjectCustomFieldImporter projectCustomFieldImporter;

    public MultiSelectCFType(final OptionsManager optionsManager, final CustomFieldValuePersister valuePersister, final GenericConfigManager genericConfigManager)
    {
        super(optionsManager, valuePersister, genericConfigManager);
        projectCustomFieldImporter = new SelectCustomFieldImporter();
    }

    public Set<Long> getIssueIdsWithValue(final CustomField field, final Option option)
    {
        if (option != null)
        {
            return customFieldValuePersister.getIssueIdsWithValue(field, PersistenceFieldType.TYPE_LIMITED_TEXT, option.getOptionId().toString());
        }
        else
        {
            return emptySet();
        }
    }

    public void setDefaultValue(final FieldConfig fieldConfig, final Object value)
    {
        Collection<Option> values = (Collection<Option>) value;
        List<Long> defaultIds = new ArrayList<Long>();
        if (values != null)
        {
            for (Option o : values)
            {
                defaultIds.add(o.getOptionId());
            }
        }
        genericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), defaultIds);
    }

    public Object getDefaultValue(final FieldConfig fieldConfig)
    {
        List<Option> options = new ArrayList<Option>();
        Collection<Long> optionIds = (Collection<Long>) genericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        if (optionIds != null)
        {
            for (Long optionId : optionIds)
            {
                Option option = optionsManager.findByOptionId(optionId);
                if (option != null)
                {
                    options.add(option);
                }
            }
        }
        return options;
    }

    public Object getValueFromCustomFieldParams(final CustomFieldParams parameters) throws FieldValidationException
    {
        final Collection<?> values = parameters.getAllValues();
        if (CustomFieldUtils.isCollectionNotEmpty(values))
        {
            List<Object> options = new ArrayList<Object>();
            for (Object value : values)
            {
                options.add(getSingularObjectFromString((String) value));
            }
            return options;
        }
        else
        {
            return null;
        }
    }

    public Object getStringValueFromCustomFieldParams(final CustomFieldParams parameters)
    {
        return parameters.getValuesForNullKey();
    }

    public void validateFromParams(final CustomFieldParams relevantParams, final ErrorCollection errorCollectionToAddTo, final FieldConfig config)
    {
        @SuppressWarnings ("unchecked")
        final Collection<String> params = relevantParams.getAllValues();
        if ((params == null) || params.isEmpty())
        {
            return;
        }

        final CustomField customField = config.getCustomField();

        for (final String paramValue : params)
        {
            if ("-1".equals(paramValue))
            {
                if (params.size() > 1)
                {
                    errorCollectionToAddTo.addError(customField.getId(), getI18nBean().getText("admin.errors.cannot.specify.none"));
                }
            }
            else
            {
                // Validate options
                final Options options = optionsManager.getOptions(config);
                Long optionId = null;
                try
                {
                    optionId = Long.valueOf(paramValue);
                }
                catch (NumberFormatException e)
                {
                    errorCollectionToAddTo.addError(customField.getId(), getI18nBean().getText("admin.errors.invalid.value.passed.for.customfield",
                            "'" + paramValue + "'", "'" + customField + "'", options.getRootOptions().toString()));
                }
                if (options.getOptionById(optionId) == null)
                {
                    errorCollectionToAddTo.addError(customField.getId(), getI18nBean().getText("admin.errors.invalid.value.passed.for.customfield",
                            "'" + paramValue + "'", "'" + customField + "'", options.getRootOptions().toString()));
                }
            }
        }
    }

    //these methods all operate on the object level

    /**
     * Create a select-list CF value for an issue
     *
     * @param value A {@link Collection}.
     */
    public void createValue(final CustomField customField, final Issue issue, final Object value)
    {
        Collection<?> objects = getValueCollectionFromObjects(value);
        customFieldValuePersister.createValues(customField, issue.getId(), PersistenceFieldType.TYPE_LIMITED_TEXT, objects);
    }

    public void updateValue(final CustomField customField, final Issue issue, final Object value)
    {
        Collection<?> objects = getValueCollectionFromObjects(value);
        customFieldValuePersister.updateValues(customField, issue.getId(), PersistenceFieldType.TYPE_LIMITED_TEXT, objects);
    }

    public void removeValue(final CustomField field, final Issue issue, final Option option)
    {
        if (option != null)
        {
            customFieldValuePersister.removeValue(field, issue.getId(), PersistenceFieldType.TYPE_LIMITED_TEXT, option.getOptionId().toString());
        }
    }

    private Collection<?> getValueCollectionFromObjects(Object value)
    {
        if (value == null)
        {
            return null;
        }
        Collection<String> stringCollection = new ArrayList<String>();
        for (Option option : (Collection<Option>) value)
        {
            stringCollection.add(getStringFromSingularObject(option));
        }
        return stringCollection;
    }

    public Object getValueFromIssue(final CustomField field, final Issue issue)
    {
        final List<?> values = customFieldValuePersister.getValues(field, issue.getId(), PersistenceFieldType.TYPE_LIMITED_TEXT);
        if ((values == null) || values.isEmpty())
        {
            return null;
        }
        else
        {
            List<Object> options = new ArrayList<Object>();
            for (Object value : values)
            {
                options.add(getSingularObjectFromString((String) value));
            }
            return options;
        }
    }

    public String getChangelogValue(final CustomField field, final Object value)
    {
        if (value != null)
        {
            ArrayList<String> stringValues = new ArrayList<String>();
            ArrayList<Option> array = (ArrayList<Option>) value;
            for (Option option : array)
            {
                stringValues.add(option.getOptionId().toString());
            }
            return StringUtils.join(stringValues, ",");
        }
        return "";
    }

    @Override
    public String getChangelogString(CustomField field, Object value)
    {
        ArrayList<String> stringValues = new ArrayList<String>();
        if (value != null)
        {
            ArrayList<Option> array = (ArrayList<Option>) value;
            for (Option option : array)
            {
                stringValues.add(option.getValue());
            }
            return StringUtils.join(stringValues, ",");
        }
        return "";
    }

    public Object getSingularObjectFromString(final String string) throws FieldValidationException
    {
        if ("-1".equals(string))
        {
            return null;
        }
        return getOptionFromStringValue(string);
    }

    private Option getOptionFromStringValue(String selectValue)
            throws FieldValidationException
    {
        final Long aLong = OptionUtils.safeParseLong(selectValue);
        if (aLong != null)
        {
            final Option option = optionsManager.findByOptionId(aLong);
            if (option != null)
            {
                return option;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    public String getStringFromSingularObject(final Object optionObject)
    {
        if (optionObject == null)
        {
            return null;
        }
        if (optionObject instanceof Option)
        {
            Option option = (Option) optionObject;
            return option.getOptionId().toString();
        }
        else
        {
            log.warn("Object passed '" + optionObject + "' is not an Option but " +
                    optionObject != null ? " of type " + optionObject.getClass() : " is null");
            return null;
        }
    }

    @Override
    public List<FieldConfigItemType> getConfigurationItemTypes()
    {
        final List<FieldConfigItemType> configurationItemTypes = super.getConfigurationItemTypes();
        configurationItemTypes.add(new SettableOptionsConfigItem(this, optionsManager));
        return configurationItemTypes;
    }

    public int compare(final List<String> customFieldObjectValue1, final List<String> customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        final Options options = getOptions(fieldConfig, null);

        if (options != null)
        {
            final Long i1 = getLowestIndex(customFieldObjectValue1, options);
            final Long i2 = getLowestIndex(customFieldObjectValue2, options);

            return i1.compareTo(i2);
        }

        log.info("No options were found.");
        return 0;
    }

    public ProjectCustomFieldImporter getProjectImporter()
    {
        return projectCustomFieldImporter;
    }

    private Long getLowestIndex(final List<String> l, final Options options)
    {
        Long lowest = new Long(Long.MAX_VALUE);

        for (final String name : l)
        {
            final Option o = options.getOptionById(Long.valueOf(name));
            if ((o != null) && (o.getSequence() != null) && (o.getSequence().compareTo(lowest) < 0))
            {
                lowest = o.getSequence();
            }
        }

        return lowest;
    }

    /**
     * Parses the given comma-separated String value into a Collection. Whitespace is trimmed and blank fields are
     * discarded. If literal commas are required, then they can be escaped with a backslash. Therefore the input String
     * <code>"red, white\, and blue"</code> would produce two tokens in its list: <ul> <li>red</li> <li>white, and
     * blue</li> </ul>
     *
     * @param value The comma-separated input String.
     * @return Collection of tokens parsed from the input value.
     * @see #getStringFromTransferObject(java.util.Collection)
     */
    public static Collection<String> extractTransferObjectFromString(final String value)
    {
        if (value == null)
        {
            return null;
        }
        final Collection<String> valuesToAdd = new ArrayList<String>();
        // Commas can be escaped with a backslash if we actually want it in our value text.
        // So we replace instances of "\," with "&#44;"
        final String[] a = StringUtils.split(StringUtils.replace(value, "\\,", COMMA_REPLACEMENT), ",");

        for (final String s : a)
        {
            // put a comma back wherever we have the "replacement" text.
            final String s2 = StringUtils.replace(s, COMMA_REPLACEMENT, ",");
            // Now trim whitespace
            final String s3 = StringUtils.trimToNull(s2);
            if (s3 != null)
            {
                // We only add non-blank values to our list.
                valuesToAdd.add(s3);
            }
        }

        return valuesToAdd;
    }

    /**
     * Takes a Collection of values and creates a comma-separated String that represents this Collection. <p> If any
     * items in the collection include literal commas, then these commas are escaped with a prepended backslash. eg a
     * list that looks like: <ul> <li>All sorts</li> <li>Tom, Dick, and Harry</li> </ul> Will be turned into a string
     * that looks like "All sorts,Tom\, Dick\, and Harry" </p>
     *
     * @param collection a collection of Strings to be comma separated
     */
    public static String getStringFromTransferObject(final Collection<String> collection)
    {
        if (collection != null)
        {
            final StringBuffer sb = new StringBuffer();
            for (final Iterator<String> iterator = collection.iterator(); iterator.hasNext();)
            {
                String s = iterator.next();
                s = StringUtils.replace(s, ",", "\\,");
                sb.append(s);
                if (iterator.hasNext())
                {
                    sb.append(",");
                }
            }
            return sb.toString();
        }
        else
        {
            return null;
        }
    }

    // override
    @Override
    public boolean valuesEqual(final Object v1, final Object v2)
    {
        if (v1 == v2)
        {
            return true;
        }

        if ((v1 == null) || (v2 == null))
        {
            return false;
        }

        // We expect the values to both be Collections
        if ((v1 instanceof Collection) && (v2 instanceof Collection))
        {
            // we want the equality test to not be order-dependant. see JRA-15105
            return CollectionUtils.isEqualCollection((Collection<?>) v1, (Collection<?>) v2);
        }
        {
            // THIS IS NOT EXPECTED TO HAPPEN.
            log.error("Unexpected value types for MultiSelectCFType. v1 = '" + v1 + "' v2 = '" + v2 + "'.");
            // best guess:
            return v1.equals(v2);
        }
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitMultiSelect(this);
        }

        return super.accept(visitor);
    }

    public Query getQueryForGroup(final String fieldName, final String groupName)
    {
        return new TermQuery(new Term(fieldName + SelectStatisticsMapper.RAW_VALUE_SUFFIX, groupName));
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitMultiSelect(MultiSelectCFType multiSelectCustomFieldType);
    }
}
