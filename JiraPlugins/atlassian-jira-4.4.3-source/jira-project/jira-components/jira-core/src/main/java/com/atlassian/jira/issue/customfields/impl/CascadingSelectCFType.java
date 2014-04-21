package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.imports.project.customfield.CascadingSelectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.issue.Issue;
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
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ObjectUtils;
import org.apache.commons.collections.MultiHashMap;
import org.apache.log4j.Logger;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Cascading Select Custom Field Type allows for multiple dependent select lists.</p>
 * <dl>
 * <dt><strong>Transport Object Type</strong></dt>
 * <dd>{@link CustomFieldParams} The <em>key</em> in the CustomFieldParams object represents the field depth of the
 * select list. eg. a key of null is the root parent, and key of "1" is the first level select list. As at JIRA 3.0,
 * there can only be one level.</dd>
 *
 * <dt><strong>Singular Object Type</strong></dt>
 * <dd>{@link Option}</dd>
 * </dl>
 */
public class CascadingSelectCFType extends AbstractMultiSettableCFType implements  MultipleSettableCustomFieldType, SortableCustomField, ProjectImportableCustomField
{
    public static final String PARENT_KEY = null;
    public static final String CHILD_KEY = "1";

    private static final Logger log = Logger.getLogger(CascadingSelectCFType.class);
    public static final PersistenceFieldType CASCADE_VALUE_TYPE = PersistenceFieldType.TYPE_LIMITED_TEXT;

    private final ProjectCustomFieldImporter projectCustomFieldImporter;

    public CascadingSelectCFType(OptionsManager optionsManager, CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager)
    {
        super(optionsManager, customFieldValuePersister, genericConfigManager);
        this.projectCustomFieldImporter = new CascadingSelectCustomFieldImporter();
    }



    // -----------------------------------------------------------------------------------------------------  Validation

    public void validateFromParams(CustomFieldParams relevantParams, ErrorCollection errorCollectionToAddTo, FieldConfig config)
    {
        if (relevantParams == null || relevantParams.isEmpty())
        {
            return;
        }

        String customFieldId = config.getCustomField().getId();

        Option parentOption;
        try
        {
            // Get the parent option, it will be the 0'ith element in the relevant params
            parentOption = extractOptionFromParams(0, relevantParams);
        }
        catch (FieldValidationException e)
        {
            parentOption = null;
        }

        // If the selected parent option does not resolve to a value in the DB we should throw an error
        if(parentOption == null)
        {
            List params = new ArrayList(relevantParams.getValuesForKey(null));
            // If there was no value selected for the parent or the 'None/All' option was selected we let them pass
            // and in this case we do not care about what the child values are since the parent is none.
            if (!params.isEmpty() && !isNoneOptionSelected(params))
            {
                errorCollectionToAddTo.addError(customFieldId, getI18nBean().getText("admin.errors.option.invalid.parent", "'" + params.get(0).toString() + "'"));
            }
        }
        else
        {
            // Since we are sure that the parent value is non-null and resovles to a valid option lets make sure that
            // it is valid in the FieldConfig for where we are.
            if(!parentOptionValidForConfig(config, parentOption))
            {
                errorCollectionToAddTo.addError(customFieldId, getI18nBean().getText("admin.errors.option.invalid.for.context",
                        "'" + parentOption.getValue() + "'", "'" + config.getName() + "'"));
            }
            else
            {
                // Iterate through all the non-parent options
                for (int i = 1; i < relevantParams.getAllKeys().size(); i++)
                {
                    try
                    {
                        // Get the param for this current option
                        List params = new ArrayList(relevantParams.getValuesForKey(String.valueOf(i)));

                        // Get the option object from the params only if they have not selected the "None/All" option
                        Option currentOption = null;

                        // If the user has not selected 'None/All' then we should try to resolve the option into an
                        // object and then check that the object is valid in the FieldConfig for where we are.
                        if(!isNoneOptionSelected(params))
                        {
                            // get the option from the params
                            currentOption = extractOptionFromParams(i, relevantParams);

                            // check that the supplied option is valid in the config supplied
                            if(!currentOptionValidForConfig(config, currentOption))
                            {
                                String optionValue = (currentOption == null) ?  params.get(0).toString() : currentOption.getValue();
                                errorCollectionToAddTo.addError(customFieldId, getI18nBean().getText("admin.errors.option.invalid.for.context",
                                        "'" + optionValue + "'", "'" + config.getName() + "'"));
                                return;
                            }
                        }

                        // make certain that the current option (if it exists) has a parent, that the parent is what we
                        // expect it to be (the parent that was submitted as a param)
                        if (currentOption != null && currentOption.getParentOption() != null
                                && !parentOption.equals(currentOption.getParentOption()) )
                        {
                            errorCollectionToAddTo.addError(customFieldId, getI18nBean().getText("admin.errors.option.invalid.for.parent","'" + currentOption.getValue() + "'", "'" + parentOption.getValue() + "'"));
                        }
                    }
                    catch (FieldValidationException e)
                    {
                        errorCollectionToAddTo.addError(customFieldId, e.getMessage());
                    }
                }
            }
        }
    }

    private boolean isNoneOptionSelected(List params)
    {
        Object parentOptionParamObj = params.iterator().next();
        if (parentOptionParamObj instanceof String)
        {
            String parentOptionParam = (String) parentOptionParamObj;
            boolean noneSelected = "-1".equals(parentOptionParam);
            return noneSelected;
        }
        return false;
    }

    private boolean parentOptionValidForConfig(FieldConfig config, Option parentOption)
    {
        final Options options = optionsManager.getOptions(config);
        if(options != null)
        {
            Collection rootOptions = options.getRootOptions();
            if(rootOptions != null)
            {
                return rootOptions.contains(parentOption);
            }
        }
        return false;
    }

    private boolean currentOptionValidForConfig(FieldConfig config, Option currentOption)
    {
        final Options options = optionsManager.getOptions(config);
        if(options != null)
        {
            Collection rootOptions = options.getRootOptions();
            if(rootOptions != null)
            {
                if (currentOption != null)
                {
                    return options.getOptionById(currentOption.getOptionId()) != null;
                }
            }
        }
        return false;
    }

    // --------------------------------------------------------------------------------------------- Persistance Methods

    //these methods all operate on the object level

    /**
     * Create a cascading select-list instance for an issue.
     *
     * @param value Must be a {@link CustomFieldParams}, or the method has no effect.
     */
    public void createValue(CustomField field, Issue issue, Object value)
    {
        persistValues(value, field, issue, true);
    }

    public void updateValue(CustomField field, Issue issue, Object value)
    {
        customFieldValuePersister.updateValues(field, issue.getId(), CASCADE_VALUE_TYPE, null);
        persistValues(value, field, issue, false);
    }

    public void removeValue(CustomField field, Issue issue, Option option)
    {
        if (option != null)
        {
            customFieldValuePersister.removeValue(field, issue.getId(), PersistenceFieldType.TYPE_LIMITED_TEXT, option.getOptionId().toString());
        }
    }



    // --------------------------------------------------------------------------------------  CustomFieldParams methods

    public Object getValueFromIssue(CustomField field, Issue issue)
    {
        return retrieveCascadingValues(field, issue, false);
    }

    public Object getValueFromCustomFieldParams(CustomFieldParams relevantParams) throws FieldValidationException
    {
        if (relevantParams != null && !relevantParams.isEmpty())
        {
            Map newMap = new MultiHashMap();
            for (int i = 0; i < relevantParams.getAllKeys().size(); i++)
            {
                Option currentOption = extractOptionFromParams(i, relevantParams);
                if (currentOption != null)
                {
                    newMap.put(i == 0 ? null : String.valueOf(i),
                               currentOption);
                }
            }

            return returnParamsObject(newMap, relevantParams.getCustomField());
        }
        else
        {
            return null;
        }

    }

    public Object getStringValueFromCustomFieldParams(CustomFieldParams parameters)
    {
        return parameters;
    }

    // -------------------------------------------------------------------------------------------------------- Defaults


    public Object getDefaultValue(FieldConfig fieldConfig)
    {
        final Object o = genericConfigManager.retrieve(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        if (o != null)
        {
            final CustomFieldParams params = new CustomFieldParamsImpl(fieldConfig.getCustomField(), o);
            params.transformStringsToObjects();
            return params;
        }
        else
        {
            return null;
        }
    }

    public void setDefaultValue(FieldConfig fieldConfig, Object value)
    {
        final CustomFieldParams customFieldParams = (CustomFieldParams) value;

        if (customFieldParams != null)
        {
            customFieldParams.transformObjectsToStrings();
            customFieldParams.setCustomField(null);
        }

        genericConfigManager.update(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), customFieldParams);
    }




    // --------------------------------------------------------------------------------------------------  Miscellaneous

    public String getChangelogValue(CustomField field, Object value)
    {
        if (value != null)
        {
            CustomFieldParams params = (CustomFieldParams) value;
            StringBuffer sb = new StringBuffer();
            Set keys = params.getAllKeys();

            for (Iterator iterator = keys.iterator(); iterator.hasNext();)
            {
                String s = (String) iterator.next();
                if (s == null)
                {
                    sb.append("Parent values: ");
                }
                else
                {
                    sb.append("Level "+s+" values: ");
                }

                Collection c = params.getValuesForKey(s);

                for (Iterator iterator1 = c.iterator(); iterator1.hasNext();)
                {
                    Option option = (Option) iterator1.next();
                    sb.append(option.getValue() + "("+ option.getOptionId() + ")");
                    if (iterator1.hasNext())
                    {
                        sb.append(", ");
                    }
                    else
                    {
                        sb.append(". ");
                    }
                }

            }
            return sb.toString();
        }
        else
        {
            return "";
        }
    }



    /**
     * Returns a list of Issue Ids matching the "value" note that the value in this instance is the single object
     *
     * @param field
     * @param option
     */
    public Set getIssueIdsWithValue(CustomField field, Option option)
    {
        Set allIssues = new HashSet();

        // Add for current option
        if (option != null)
        {
            allIssues.addAll(customFieldValuePersister.getIssueIdsWithValue(field, CASCADE_VALUE_TYPE, option.getOptionId().toString()));

            // Add for children
            List childOptions = option.retrieveAllChildren(null);
            if (childOptions != null && !childOptions.isEmpty())
            {
                for (Iterator iterator = childOptions.iterator(); iterator.hasNext();)
                {
                    Option childOption = (Option) iterator.next();
                    allIssues.addAll(customFieldValuePersister.getIssueIdsWithValue(field, CASCADE_VALUE_TYPE, childOption.getOptionId().toString()));
                }
            }
        }

        return allIssues;
    }


    public String getStringFromSingularObject(Object optionObject)
    {
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

    public Object getSingularObjectFromString(String string) throws FieldValidationException
    {
        return getOptionFromStringValue(string);
    }

    public List getConfigurationItemTypes()
    {
        final List configurationItemTypes = super.getConfigurationItemTypes();
        configurationItemTypes.add(new SettableOptionsConfigItem(this, optionsManager));
        return configurationItemTypes;
    }

    //----------------------------------------------------------------------------------------- - Private Helper Methods

    private Option extractOptionFromParams(int nLevel, CustomFieldParams relevantParams) throws FieldValidationException
    {
        String cascadeLevel = null;
        // We always treat the zero'ith element as null in the relevantParams map
        if (nLevel >= 1)
        {
            cascadeLevel = String.valueOf(nLevel);
        }
        Collection params = relevantParams.getValuesForKey(cascadeLevel);
        String selectValue = null;
        if (params != null && !params.isEmpty())
        {
            Object o = params.iterator().next();
            if (o instanceof String)
            {
                selectValue = (String) o;
                if (!ObjectUtils.isValueSelected(selectValue))
                {
                    return null;
                }
            }
            else if (o instanceof Option)
            {
                return (Option) o;
            }
            else
            {
                throw new FieldValidationException("Value: '"+o+"' is of invalid type:" + o.getClass());
            }
        }

        return getOptionFromStringValue(selectValue);
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
                throw new FieldValidationException("'" + aLong + "' is an invalid Option");
            }
        }
        else
        {
            throw new FieldValidationException("Value: '" + selectValue + "' is an invalid Option");
        }
    }


    private CustomFieldParams returnParamsObject(Map defaultsMap, CustomField field)
    {
        if (defaultsMap != null && !defaultsMap.isEmpty())
        {
            CustomFieldParams customFieldParams = new CustomFieldParamsImpl(field, defaultsMap);
            return customFieldParams;
        }
        else
        {
            return null;
        }
    }


    private Option getOptionValueForParentId(CustomField field, String sParentOptionId, Issue issue)
    {
        Collection values;

        values = customFieldValuePersister.getValues(field, issue.getId(), CASCADE_VALUE_TYPE, sParentOptionId);


        if (values != null && !values.isEmpty())
        {
            String optionId = (String) values.iterator().next();
            return optionsManager.findByOptionId(OptionUtils.safeParseLong(optionId));
        }
        else
        {
            return null;
        }
    }

    private void throwValidationException(String selectValue, CustomField customField, Collection allowedValues)
            throws FieldValidationException
    {
        throw new FieldValidationException("Invalid value '" + selectValue + "' passed for customfield '" +
                                                 customField + ". Allowed values are " + allowedValues);
    }


    private void persistValues(Object value, CustomField field, Issue issue, boolean createFlag)
    {
        if (!(value instanceof CustomFieldParams))
        {
            return;
        }

        CustomFieldParams relevantParams = (CustomFieldParams) value;
        Collection allOptionIds = relevantParams.getAllValues();

        for (Iterator iterator = allOptionIds.iterator(); iterator.hasNext();)
        {
            Option option = getOptionFromObject(iterator.next());
            if (option != null)
            {
                final String parentKey = option.getParentOption() != null ? option.getParentOption().getOptionId().toString() : null;
                final List optionIdList = EasyList.build(option.getOptionId().toString());
                if (createFlag)
                {
                    customFieldValuePersister.createValues(field, issue.getId(), CASCADE_VALUE_TYPE, optionIdList, parentKey);
                }
                else
                {
                    customFieldValuePersister.updateValues(field, issue.getId(), CASCADE_VALUE_TYPE, optionIdList, parentKey);

                }
            }
        }
    }


    private CustomFieldParams retrieveCascadingValues(CustomField field, Issue issue, boolean returnStringValues)
    {
        Map defaultsMap = new MultiHashMap();

        int counter = 0;
        Option currentOption = getOptionValueForParentId(field, null, issue);
        while (currentOption != null)
        {
            Object value;
            if (returnStringValues)
            {
                value = currentOption.getOptionId().toString();
            }
            else
            {
                value = currentOption;
            }

            defaultsMap.put(counter == 0 ? null : String.valueOf(counter),
                            value);

            // Iterate
            String parentOptionId = currentOption.getOptionId().toString();
            currentOption = getOptionValueForParentId(field, parentOptionId, issue);
            counter++;
        }

        return returnParamsObject(defaultsMap, field);
    }

    /**
     * This method is used to deal with the "fluid" situation we still have around type for values in CustomFieldParam
     * objects
     * @param o
     */
    private Option getOptionFromObject(Object o)
    {
        if (o instanceof String)
        {
            String optionIdString = (String) o;
            Option option = optionsManager.findByOptionId(OptionUtils.safeParseLong(optionIdString));
            return option;
        }
        else if (o instanceof Option)
        {
            return (Option) o;
        }
        else
        {
            return null;
        }
    }

    public Map getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem)
    {
        final HttpServletRequest request = ServletActionContext.getRequest();
        return EasyMap.build("request", request);
    }

    // -------------------------------------------------------------------------------------------------- Compare
    public int compare(Object o1, Object o2, FieldConfig fieldConfig)
    {
        CustomFieldParams cfParams1 = (CustomFieldParams) o1;
        CustomFieldParams cfParams2 = (CustomFieldParams) o2;

        Option option1 = (Option) cfParams1.getFirstValueForNullKey();
        Option option2 = (Option) cfParams2.getFirstValueForNullKey();

        int parentCompare = compareOption(option1, option2);
        if (parentCompare == 0)
        {
            // Compare child Options, if parents are the same
            Option childOption1 = (Option) cfParams1.getFirstValueForKey(CHILD_KEY);
            Option childOption2 = (Option) cfParams2.getFirstValueForKey(CHILD_KEY);

            return compareOption(childOption1, childOption2);
        }
        else
        {
            return parentCompare;
        }
    }

    public int compareOption(Option option1, Option option2)
    {
        if (option1 == null && option2 == null) return 0;
        else if (option1 == null) return -1;
        else if (option2 == null) return 1;
        else return option1.getSequence().compareTo(option2.getSequence());
    }

    public ProjectCustomFieldImporter getProjectImporter()
    {
        return this.projectCustomFieldImporter;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitCascadingSelect(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitCascadingSelect(CascadingSelectCFType cascadingSelectCustomFieldType);
    }
}