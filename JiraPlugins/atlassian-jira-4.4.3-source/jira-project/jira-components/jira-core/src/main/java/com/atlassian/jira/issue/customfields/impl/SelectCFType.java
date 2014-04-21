package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.SelectCustomFieldImporter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.GroupSelectorField;
import com.atlassian.jira.issue.customfields.MultipleCustomFieldType;
import com.atlassian.jira.issue.customfields.MultipleSettableCustomFieldType;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.config.item.SettableOptionsConfigItem;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.converters.StringConverter;
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
import com.atlassian.jira.util.ErrorCollection;
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

public class SelectCFType extends TextCFType
        implements MultipleSettableCustomFieldType, MultipleCustomFieldType, SortableCustomField<String>, GroupSelectorField
{
    private final SelectConverter selectConverter;
    private final OptionsManager optionsManager;
    private final ProjectCustomFieldImporter projectCustomFieldImporter;

    private static final Logger log = Logger.getLogger(SelectCFType.class);

    public SelectCFType(final CustomFieldValuePersister customFieldValuePersister, final StringConverter stringConverter, final SelectConverter selectConverter, final OptionsManager optionsManager, final GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, stringConverter, genericConfigManager);
        this.selectConverter = selectConverter;
        this.optionsManager = optionsManager;
        projectCustomFieldImporter = new SelectCustomFieldImporter();
    }

    @Override
    public Set remove(final CustomField field)
    {
        final Set issues = super.remove(field);
        optionsManager.removeCustomFieldOptions(field);
        return issues;
    }

    /**
     * This default implementation will remove all values from the custom field for an issue. Since there can only be
     * one value for each CustomField instance, this implementation can safely ignore the objectValue
     *
     * @param option - ignored
     */
    public void removeValue(final CustomField field, final Issue issue, final Option option)
    {
        updateValue(field, issue, null);
    }

    @Override
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

    @Override
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

    public Set getIssueIdsWithValue(final CustomField field, final Option option)
    {
        if (option != null)
        {
            return customFieldValuePersister.getIssueIdsWithValue(field, PersistenceFieldType.TYPE_LIMITED_TEXT, option.getOptionId().toString());
        }
        else
        {
            return Collections.EMPTY_SET;
        }
    }

    @Override
    public List getConfigurationItemTypes()
    {
        final List configurationItemTypes = super.getConfigurationItemTypes();
        configurationItemTypes.add(new SettableOptionsConfigItem(this, optionsManager));
        return configurationItemTypes;
    }

    @Override
    public void validateFromParams(final CustomFieldParams relevantParams, final ErrorCollection errorCollectionToAddTo, final FieldConfig config)
    {
        final String selectedString = (String) relevantParams.getFirstValueForNullKey();

        if (StringUtils.isNotBlank(selectedString) && !"-1".equals(selectedString))
        {
            // Test to see if the non blank value exists in the options
            final Options options = optionsManager.getOptions(config);
            final CustomField customField = config.getCustomField();
            final String validOptions = createValidOptionsString(options);
            Long optionId = null;
            try
            {
                optionId = Long.valueOf(selectedString);
            }
            catch (NumberFormatException e)
            {
                errorCollectionToAddTo.addError(customField.getId(), getI18nBean().getText("admin.errors.invalid.value.passed.for.customfield",
                        "'" + selectedString + "'", "'" + customField + "'", validOptions));
            }
            if ((options != null) && (options.getOptionById(optionId) == null))
            {

                errorCollectionToAddTo.addError(customField.getId(), getI18nBean().getText("admin.errors.invalid.value.passed.for.customfield",
                        "'" + selectedString + "'", "'" + customField + "'", validOptions));
            }
        }

    }

    public void setDefaultValue(final FieldConfig fieldConfig, final Object value)
    {
        Option option = (Option) value;
        Long id = null;
        if (option != null)
        {
            id = option.getOptionId();
        }
        genericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), id);
    }

    public Object getDefaultValue(final FieldConfig fieldConfig)
    {
        List<Option> options = new ArrayList<Option>();
        Long optionId = (Long) genericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        if (optionId == null)
        {
            return null;
        }
        return optionsManager.findByOptionId(optionId);
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
            Object value = values.iterator().next();
            return getSingularObjectFromString((String) value);
        }
    }

    @Override
    public String getChangelogString(CustomField field, Object value)
    {
        return value == null ? null : ((Option) value).getValue();
    }

    private String createValidOptionsString(final Options options)
    {
        final List<Object> rootOptions = options.getRootOptions();
        final StringBuffer validOptions = new StringBuffer();

        for (Iterator<Object> optionIterator = rootOptions.iterator(); optionIterator.hasNext();)
        {
            Object obj = optionIterator.next();
            if (obj instanceof Option)
            {
                Option option = (Option) obj;
                validOptions.append(option.getOptionId() + "[" + option.getValue() + "]");
            }
            else
            {
                //This should never happen
                validOptions.append(obj.toString());
            }

            if (optionIterator.hasNext())
            {
                validOptions.append(", ");
            }
        }
        validOptions.append(", -1");
        return validOptions.toString();
    }

    //------------------------------------------------------------------------------------------- MultiSettable Methods
    public Options getOptions(final FieldConfig config, final JiraContextNode jiraContextNode)
    {
        return optionsManager.getOptions(config);
    }

    // -------------------------------------------------------------------------------- Sortable custom field
    @Override
    public int compare(final String customFieldObjectValue1, final String customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        final Options options = getOptions(fieldConfig, null);

        if (options != null)
        {
            final int v1 = options.indexOf(options.getOptionById(Long.valueOf(customFieldObjectValue1)));
            final int v2 = options.indexOf(options.getOptionById(Long.valueOf(customFieldObjectValue2)));

            if (v1 > v2)
            {
                return 1;
            }
            else if (v1 < v2)
            {
                return -1;
            }
            else
            {
                return 0;
            }

        }
        else
        {
            log.info("No options were found.");
            return 0;
        }
    }

    @Override
    public ProjectCustomFieldImporter getProjectImporter()
    {
        return projectCustomFieldImporter;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitSelect(this);
        }

        return super.accept(visitor);
    }

    public Query getQueryForGroup(final String fieldID, String groupName)
    {
        return new TermQuery(new Term(fieldID + SelectStatisticsMapper.RAW_VALUE_SUFFIX, groupName));
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitSelect(SelectCFType selectCustomFieldType);
    }
}
