package com.atlassian.jira.issue.customfields.manager;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.customfields.option.LazyLoadedOption;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.option.OptionsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.PrimitiveMap;
import com.atlassian.jira.util.CollectionReorderer;
import com.atlassian.jira.util.EasyList;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DefaultOptionsManager implements OptionsManager
{
    private static final class Entity
    {
        static final String ID = "id";
        static final String CONFIG_ID = "customfieldconfig";
        static final String PARENT_OPTION = "parentoptionid";
        static final String ISSUE_VALUE = "value";
        static final String CUSTOMFIELD_ID = "customfield";
        static final String SEQUENCE = "sequence";
        static final String DISABLED = "disabled";
    }

    public static final String ENTITY_CONFIG_ID = Entity.CONFIG_ID;

    private static final String DB_ASC_SUFFIX = " ASC";

    private static final Logger log = Logger.getLogger(DefaultOptionsManager.class);

    // NB: In the current implementation, the order that the list is important. Changing this will require changes in the method logic
    private static final List<String> ORDER_BY_LIST = Collections.unmodifiableList(EasyList.build(Entity.PARENT_OPTION + DB_ASC_SUFFIX,
            Entity.SEQUENCE + DB_ASC_SUFFIX));

    private static final String TABLE_CUSTOMFIELD_OPTION = "CustomFieldOption";

    private final OfBizDelegator delegator;
    private final CollectionReorderer<Option> reorderer;
    private final FieldConfigManager fieldConfigManager;

    public DefaultOptionsManager(final OfBizDelegator delegator, final CollectionReorderer<Option> reorderer, final FieldConfigManager fieldConfigManager)
    {
        this.delegator = delegator;
        this.reorderer = reorderer;
        this.fieldConfigManager = fieldConfigManager;
    }

    public Options getOptions(final FieldConfig fieldConfig)
    {
        if (fieldConfig != null)
        {
            final Map<String, Object> params = new PrimitiveMap.Builder().add(Entity.CONFIG_ID, fieldConfig.getId()).toMap();

            return new OptionsImpl(convertGVsToOptions(delegator.findByAnd(TABLE_CUSTOMFIELD_OPTION, params, ORDER_BY_LIST)), fieldConfig, reorderer,
                    this);
        }
        else
        {
            return null;
        }
    }

    public void setRootOptions(final FieldConfig fieldConfig, final Options options)
    {
        //remove existing custom field options
        removeCustomFieldConfigOptions(fieldConfig);

        if (options != null)
        {
            int counter = 0;
            for (final Object o : options)
            {
                if (!(o instanceof Option))
                {
                    log.error("Can only set options for 'Option'.  This value was " + o == null ? null : o.getClass().getName());
                    return;
                }
                final Option option = (Option) o;

                final Map<String, Object> params = new PrimitiveMap.Builder().add(Entity.CUSTOMFIELD_ID, fieldConfig.getCustomField().getIdAsLong()).add(
                        Entity.CONFIG_ID, fieldConfig.getId()).add(Entity.ISSUE_VALUE, option.getValue()).add(Entity.PARENT_OPTION, (String) null).add(
                        Entity.SEQUENCE, new Long(counter)).toMap();

                delegator.createValue(TABLE_CUSTOMFIELD_OPTION, params);

                counter++;
            }
        }
    }

    public void removeCustomFieldOptions(final CustomField customField)
    {
        delegator.removeByAnd(TABLE_CUSTOMFIELD_OPTION, EasyMap.build(Entity.CUSTOMFIELD_ID, customField.getIdAsLong()));
    }

    public void removeCustomFieldConfigOptions(final FieldConfig fieldConfig)
    {
        delegator.removeByAnd(TABLE_CUSTOMFIELD_OPTION, EasyMap.build(Entity.CONFIG_ID, fieldConfig.getId()));
    }

    public void updateOptions(final Collection<Option> options)
    {
        if (options != null)
        {
            for (final Option option : options)
            {
                option.store();
            }
        }

    }

    public Option createOption(final FieldConfig fieldConfig, final Long parentOptionId, final Long sequence, final String value)
    {
        final Map<String, Object> params = new PrimitiveMap.Builder().add(Entity.CUSTOMFIELD_ID, fieldConfig.getCustomField().getIdAsLong()).add(
                Entity.CONFIG_ID, fieldConfig.getId()).add(Entity.ISSUE_VALUE, value).add(Entity.PARENT_OPTION, parentOptionId).add(Entity.SEQUENCE,
                sequence).add(Entity.DISABLED, "N").toMap();

        final GenericValue gv = delegator.createValue(TABLE_CUSTOMFIELD_OPTION, params);

        if (gv != null)
        {
            return new LazyLoadedOption(gv, this, fieldConfigManager);
        }
        else
        {
            return null;
        }
    }

    public void deleteOptionAndChildren(final Option option)
    {
        final List<GenericValue> gvToRemove = convertOptionsToGV(option.retrieveAllChildren(null));
        gvToRemove.add(option.getGenericValue());

        delegator.removeAll(gvToRemove);
    }

    public List<Option> getAllOptions()
    {
        final List<GenericValue> optionGVs = delegator.findAll(TABLE_CUSTOMFIELD_OPTION, ORDER_BY_LIST);
        return convertGVsToOptions(optionGVs);
    }

    public void disableOption(Option option)
    {
        option.setDisabled(Boolean.TRUE);
        option.store();
    }

    public void enableOption(Option option)
    {
        option.setDisabled(Boolean.FALSE);
        option.store();
    }

    public void setValue(Option option, String value)
    {
        option.setValue(value);
        option.store();
    }

    public List<Option> findByOptionValue(final String value)
    {
        final List<Option> allOptions = getAllOptions();
        List<Option> matchingOptions = new ArrayList<Option>();
        for (Option allOption : allOptions)
        {
            if (allOption.getValue().equalsIgnoreCase(value))
            {
                matchingOptions.add(allOption);
            }
        }
        return matchingOptions;
    }

    public Option findByOptionId(final Long optionId)
    {
        final GenericValue option = delegator.findByPrimaryKey(TABLE_CUSTOMFIELD_OPTION, EasyMap.build(Entity.ID, optionId));
        return option == null ? null : new LazyLoadedOption(option, this, fieldConfigManager);
    }

    public List<Option> findByParentId(final Long parentOptionId)
    {
        final List<GenericValue> optionGVs = delegator.findByAnd(TABLE_CUSTOMFIELD_OPTION, EasyMap.build(Entity.PARENT_OPTION, parentOptionId),
                ORDER_BY_LIST);
        return convertGVsToOptions(optionGVs);
    }

    private List<GenericValue> convertOptionsToGV(final List<Option> options)
    {
        return new ArrayList<GenericValue>(CollectionUtil.transform(options, new OptionToGenericValue()));
    }

    List<Option> convertGVsToOptions(final List<GenericValue> optionGvs)
    {
        return new ArrayList<Option>(CollectionUtil.transform(optionGvs, new GenericValueToOption()));
    }

    private final class GenericValueToOption implements Function<GenericValue, Option>
    {
        public Option get(final GenericValue input)
        {
            return new LazyLoadedOption(input, DefaultOptionsManager.this, fieldConfigManager);
        }
    }

    private static final class OptionToGenericValue implements Function<Option, GenericValue>
    {
        public GenericValue get(final Option option)
        {
            return option.getGenericValue();
        }
    }
}
