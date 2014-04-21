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
public class CustomFieldComparator implements Comparator<GenericValue>
{
    public int compare(GenericValue o1, GenericValue o2)
    {
        if (o1 == null && o2 == null)
            return 0;

        if (o1 == null)
            return -1;

        if (o2 == null)
            return 1;

        final String entityName = "CustomField";
        if (o1.getEntityName().equals(entityName) && o2.getEntityName().equals(entityName))
        {
            return CustomFieldComparators.compareNames(o1.getString("name"), o2.getString("name"));
        }
        else
        {
            throw new IllegalArgumentException("Objects passed must be GenericValues of type " + entityName + ".");
        }
    }
}
