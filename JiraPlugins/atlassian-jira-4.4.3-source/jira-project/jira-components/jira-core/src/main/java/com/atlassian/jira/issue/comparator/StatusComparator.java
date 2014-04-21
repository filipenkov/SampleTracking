/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.issue.Issue;
import org.ofbiz.core.entity.GenericValue;

public class StatusComparator extends IssueConstantsComparator
{
    // The GV passed is a 'status' already
    protected GenericValue getConstant(GenericValue i1)
    {
        return i1;
    }

    protected GenericValue getConstant(Issue i1)
    {
        return null;
    }
}
