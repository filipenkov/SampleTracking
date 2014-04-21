package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.status.Status;
import org.ofbiz.core.entity.GenericValue;

public class IssueStatusComparator extends IssueConstantsComparator
{
    protected GenericValue getConstant(GenericValue i1)
    {
        return ManagerFactory.getConstantsManager().getStatus(i1.getString("status"));
    }

    protected GenericValue getConstant(Issue i1)
    {
        Status statusObject = i1.getStatusObject();
        if (statusObject != null)
            return statusObject.getGenericValue();
        else
            return null;
    }
}
