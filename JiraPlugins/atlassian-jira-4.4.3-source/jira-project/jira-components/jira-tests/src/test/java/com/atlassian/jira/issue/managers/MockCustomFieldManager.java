/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.managers;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.exception.RemoveException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.*;

public class  MockCustomFieldManager implements CustomFieldManager
{

    private List customFields = new ArrayList();

    public List getCustomFieldObjects(SearchContext searchContext)
    {
        return null;
    }

    public Class getCustomFieldSearcherClass(String key)
    {
        return null;
    }

    public void removeProjectAssociations(GenericValue project)
    {

    }

    public void removeProjectCategoryAssociations(GenericValue projectCategory)
    {

    }

    public void removeCustomField(CustomField customField)
    {
        customFields.remove(customField);
    }

    public void removeCustomFieldValues(GenericValue genericValue) throws GenericEntityException
    {

    }

    public CustomField getCustomFieldObject(Long id)
    {
        if (id == null)
        {
            return null;
        }
        String idString = id.toString();

        for (Iterator iterator = customFields.iterator(); iterator.hasNext();)
        {
            CustomField customField = (CustomField) iterator.next();
            if (idString.equals(customField.getId()) || ("customfield_" + idString).equals(customField.getId()))
            {
                return customField;
            }
        }

        return null;
    }

    public CustomField getCustomFieldObject(String id)
    {
        return getCustomFieldObject(CustomFieldUtils.getCustomFieldId(id));
    }

    public List getCustomFieldObjects()
    {
        return customFields;
    }

    public List getGlobalCustomFieldObjects()
    {
        return null;
    }

    public void refresh()
    {

    }

    public void clear()
    {
    }

    public List getCustomFieldObjects(Long projectId, String issueType)
    {
        return null;
    }

    public List getCustomFieldObjects(Long projectId, List issueTypes)
    {
        return null;
    }

    public List getCustomFieldObjects(GenericValue issue)
    {
        return null;
    }

    public List getCustomFieldObjects(Issue issue)
    {
        return null;
    }

    public List getCustomFieldTypes()
    {
        return null;
    }

    Map customFieldTypeMap = new HashMap();
    public CustomFieldType getCustomFieldType(String key)
    {
        return (CustomFieldType) customFieldTypeMap.get(key);
    }

    public void addCustomFieldType(String key, CustomFieldType customFieldType)
    {
        customFieldTypeMap.put(key, customFieldType);
    }

    public List getCustomFieldSearchers(CustomFieldType customFieldType)
    {
        return null;
    }

    public CustomFieldSearcher getCustomFieldSearcher(String key)
    {
        return null;
    }

    public void addCustomField(CustomField customField)
    {
        customFields.add(customField);
    }

    public CustomField getCustomFieldObjectByName(String customFieldName)
    {
        return null;
    }

    public Collection getCustomFieldObjectsByName(final String customFieldName)
    {
        Collection objectsByName = new ArrayList();
        for (Iterator iterator = customFields.iterator(); iterator.hasNext();)
        {
            CustomField customField = (CustomField) iterator.next();
            if (customField.getName().equals(customFieldName))
            {
                objectsByName.add(customField);
            }
        }
        return objectsByName;
    }

    public CustomField createCustomField(String fieldName, String description, CustomFieldType fieldType, CustomFieldSearcher customFieldSearcher, List contexts, List issueTypes) throws GenericEntityException
    {
        return null;
    }

    public void removeCustomFieldPossiblyLeavingOrphanedData(final Long id) throws RemoveException
    {
    }

    public CustomField getCustomFieldInstance(GenericValue customFieldGv)
    {
        return null;
    }
}

