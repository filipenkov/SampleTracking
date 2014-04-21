package com.atlassian.jira.plugin.customfield;

import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.module.ModuleFactory;
import webwork.action.Action;

import java.util.Map;
import java.util.Set;


public abstract class CustomFieldSearcherModuleDescriptor extends JiraResourcedModuleDescriptor<CustomFieldSearcher>
{
    public CustomFieldSearcherModuleDescriptor(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    public abstract String getSearchHtml(CustomField customField,
                                CustomFieldValueProvider provider,
                                SearchContext searchContext,
                                FieldValuesHolder fieldValuesHolder,
                                Map displayParameters,
                                Action action,
                                Map velocityParams);

    public abstract String getViewHtml(CustomField customField,
                                CustomFieldValueProvider provider,
                                SearchContext searchContext,
                                FieldValuesHolder fieldValuesHolder,
                                Map displayParameters,
                                Action action,
                                Map velocityParams);

    public abstract String getViewHtml(CustomField field, Object value);

    public abstract String getStatHtml(CustomField field, Object value, String urlPrefix);

    public abstract Set getValidCustomFieldKeys();
}
