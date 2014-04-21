package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchContextImpl;
import com.atlassian.jira.util.ofbiz.GenericValueUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SelectConverterImpl implements SelectConverter
{
    private final OptionsManager optionsManager;

    public SelectConverterImpl(OptionsManager optionsManager)
    {
        this.optionsManager = optionsManager;
    }

    public String getString(Object value)
    {
        if (value == null)
        {
            return ALL_STRING;
        }
        else if (!(value instanceof String))
        {
            return null;
        }
        else
        {
            return (String)value;
        }
    }

    public String getObject(String stringValue)
    {
        if ("".equals(stringValue) || ALL_STRING.equals(stringValue) || "-2".equals(stringValue))
        {
            return null;
        }
        return stringValue;
    }

    public SearchContext getPossibleSearchContextFromValue(Object value, CustomField customField)
    {
        String stringValue = getString(value);
        Set projectIds = new HashSet();
        Set issueTypeIds = new HashSet();

        for (final FieldConfigScheme configScheme : customField.getConfigurationSchemes())
        {
            Set entries = configScheme.getConfigsByConfig().entrySet();
            for (Iterator iterator1 = entries.iterator(); iterator1.hasNext();)
            {
                Map.Entry entry = (Map.Entry) iterator1.next();
                FieldConfig config = (FieldConfig) entry.getKey();
                Options options = optionsManager.getOptions(config);
                if (options.getOptionForValue(stringValue, null) != null)
                {
                    if (configScheme.isGlobal())
                    {
                        return new SearchContextImpl();
                    }

                    // JRA-9303 It might be better to have getAssociatedProjects not return null but an empty list
                    final List associatedProjects = configScheme.getAssociatedProjects();
                    if (associatedProjects != null)
                    {
                        projectIds.addAll(GenericValueUtils.transformToLongIdsList(associatedProjects));
                    }
                    // JRA-16902 getAssociatedIssueTypes() can return null as well, causing NPE
                    final Set associatedIssueTypes = configScheme.getAssociatedIssueTypes();
                    if (associatedIssueTypes != null)
                    {
                        issueTypeIds.addAll(GenericValueUtils.transformToStringIdsList(associatedIssueTypes));
                    }
                }
            }
        }

        // Remove all values
        projectIds.remove(ALL_LONG);
        issueTypeIds.remove(ALL_STRING);

        return new SearchContextImpl(null, new ArrayList(projectIds), new ArrayList(issueTypeIds));
    }
}
