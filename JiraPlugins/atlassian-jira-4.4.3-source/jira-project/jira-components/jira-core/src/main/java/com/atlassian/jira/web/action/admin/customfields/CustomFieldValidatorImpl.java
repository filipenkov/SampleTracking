package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ObjectUtils;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.List;

public class CustomFieldValidatorImpl implements CustomFieldValidator
{
    private final CustomFieldManager customFieldManager;

    public CustomFieldValidatorImpl(final CustomFieldManager customFieldManager)
    {
        this.customFieldManager = customFieldManager;
    }

    public ErrorCollection validateType(final String fieldType)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();

        if (fieldType == null)
        {
            errorCollection.addErrorMessage(getI18nBean().getText("admin.errors.customfields.no.field.type.specified"));
        }
        else
        {
            try
            {
                if (customFieldManager.getCustomFieldType(fieldType) == null)
                {
                    errorCollection.addErrorMessage(getI18nBean().getText("admin.errors.customfields.invalid.field.type"));
                }
            }
            catch (IllegalArgumentException e)
            {
                // IllegalArgumentException for invalid fieldType plugin key
                errorCollection.addErrorMessage(getI18nBean().getText("admin.errors.customfields.invalid.field.type"));
            }
        }

        return errorCollection;
    }

    public boolean isValidType(final String fieldType)
    {
        return !validateType(fieldType).hasAnyErrors();
    }

    public ErrorCollection validateDetails(final String fieldName, final String fieldType, final String searcher)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();

        if (StringUtils.isBlank(fieldName))
        {
            errorCollection.addError("fieldName", getI18nBean().getText("admin.errors.customfields.no.name"));
        }

        // Validate that the searcher is valid
        if (ObjectUtils.isValueSelected(searcher))
        {
            final CustomFieldSearcher customFieldSearcher = customFieldManager.getCustomFieldSearcher(searcher);
            if (customFieldSearcher == null)
            {
                errorCollection.addError("searcher", getI18nBean().getText("admin.errors.customfields.unknown.searcher"));
            }
            else
            {
                final CustomFieldType customFieldType = customFieldManager.getCustomFieldType(fieldType);

                if (customFieldType != null)
                {
                    if (!isValidSearcher(customFieldSearcher, customFieldType))
                    {
                        errorCollection.addError("searcher", getI18nBean().getText("admin.errors.customfields.invalid.searcher"));
                    }
                }
            }
        }

        return errorCollection;
    }

    protected boolean isValidSearcher(final CustomFieldSearcher searcher, final CustomFieldType customFieldType)
    {
        if (searcher != null)
        {
            final List<CustomFieldSearcher> searchers = customFieldManager.getCustomFieldSearchers(customFieldType);

            // The searchers is not null, ensure that we have valid searchers for the field
            if ((searchers != null) && !searchers.isEmpty())
            {
                // If the searcher is not null ensure that it is a valid one
                for (final Iterator iterator = searchers.iterator(); iterator.hasNext();)
                {
                    final CustomFieldSearcher issueSearcher = (CustomFieldSearcher) iterator.next();
                    // Compare the searcher using complete keys, and not the equals() method, as searchers are bound to field object
                    // and all we care about here is wether the searcher can be selected, rather then check if it is the exact same searcher.
                    if (issueSearcher.getDescriptor().getCompleteKey().equals(searcher.getDescriptor().getCompleteKey()))
                    {
                        return true;
                    }
                }

                return false;
            }
            else
            {
                // If the field does not have any valid searchers then the searcher is not valid.
                return false;
            }
        }

        // It is possible to choose 'no searcher' for any custom field.
        return true;
    }

    protected I18nHelper getI18nBean()
    {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
    }

}
