/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

/**
 * Compares two CustomField {@code GenericValue}s.
 */
public class CustomFieldComparator implements Comparator
{
    public int compare(Object o1, Object o2)
    {
        if (o1 == null && o2 == null)
            return 0;

        if (o1 == null)
            return -1;

        if (o2 == null)
            return 1;

        if (o1 instanceof GenericValue && o2 instanceof GenericValue)
        {
            final GenericValue customField1 = (GenericValue) o1;
            final GenericValue customField2 = (GenericValue) o2;
            final String entityName = "CustomField";

            if (customField1.getEntityName().equals(entityName) && customField2.getEntityName().equals(entityName))
            {
                return compareNames(customField1.getString("name"), customField2.getString("name"));
            }
            else
            {
                throw new IllegalArgumentException("Objects passed must be GenericValues of type " + entityName + ".");
            }
        }
        else
            throw new IllegalArgumentException("Objects passed must be GenericValues.");
    }

    private int compareNames(String name1, String name2)
    {
        if (name1 == null && name2 == null)
            return 0;

        if (name1 == null)
            return -1;

        if (name2 == null)
            return 1;

        return name1.compareTo(name2);
    }
}
