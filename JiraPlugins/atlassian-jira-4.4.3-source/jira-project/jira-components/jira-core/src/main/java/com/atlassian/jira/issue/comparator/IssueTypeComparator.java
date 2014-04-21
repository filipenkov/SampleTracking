/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import org.ofbiz.core.entity.GenericValue;

public class IssueTypeComparator extends IssueConstantsComparator
{
    protected GenericValue getConstant(GenericValue i1)
    {
        return ManagerFactory.getConstantsManager().getIssueType(i1.getString("type"));
    }

    protected GenericValue getConstant(Issue i1)
    {
        IssueType issueType = i1.getIssueTypeObject();
        if (issueType == null)
            return null;
        else
            return issueType.getGenericValue();
    }
}
