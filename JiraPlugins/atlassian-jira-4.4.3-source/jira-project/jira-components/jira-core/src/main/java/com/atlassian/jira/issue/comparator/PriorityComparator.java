/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.priority.Priority;
import org.ofbiz.core.entity.GenericValue;

/**
 * Compares priority {@linkn GenericValue GenericValues}. See also
 * {@link com.atlassian.jira.issue.comparator.PriorityObjectComparator}.
 */
public class PriorityComparator extends IssueConstantsComparator
{
    protected GenericValue getConstant(GenericValue i1)
    {
        return ManagerFactory.getConstantsManager().getPriority(i1.getString("priority"));
    }

    protected GenericValue getConstant(Issue i1)
    {
        Priority priorityObject = i1.getPriorityObject();
        if (priorityObject == null)
            return null;
        else
            return priorityObject.getGenericValue();
    }
}
