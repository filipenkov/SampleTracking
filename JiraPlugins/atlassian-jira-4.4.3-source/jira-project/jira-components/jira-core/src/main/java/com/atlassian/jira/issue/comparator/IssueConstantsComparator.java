/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.issue.Issue;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.concurrent.Immutable;
import java.util.Comparator;

@Immutable
public abstract class IssueConstantsComparator implements java.util.Comparator
{
    public static final Comparator ISSUE_TYPE_COMPARATOR = new IssueTypeComparator();
    public static final Comparator RESOLUTION_COMPARATOR = new ResolutionComparator();
    //because the priorities are stored in 'descending' order in the database, we need to reverse it
    public static final Comparator PRIORITY_COMPARATOR = new ReverseComparator(new PriorityComparator());
    public static final Comparator STATUS_COMPARATOR = new StatusComparator();

    public int compare(Object o1, Object o2)
    {
        if (o1 == null && o2 == null)
            return 0;
        else if (o2 == null) // any value is less than null
            return -1;
        else if (o1 == null) // null is greater than any value
            return 1;

        GenericValue constant1;
        GenericValue constant2;
        if (o1 instanceof Issue)
        {
            constant1 = getConstant((Issue) o1);
            constant2 = getConstant((Issue) o2);
        }
        else
        {
            constant1 = getConstant((GenericValue) o1);
            constant2 = getConstant((GenericValue) o2);
        }

        return ConstantsComparator.COMPARATOR.compare(constant1, constant2);
    }

    protected abstract GenericValue getConstant(GenericValue i1);
    protected abstract GenericValue getConstant(Issue i1);
}
