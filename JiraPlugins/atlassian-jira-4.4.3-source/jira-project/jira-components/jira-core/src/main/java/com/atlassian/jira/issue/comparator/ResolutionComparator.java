/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.resolution.Resolution;
import org.ofbiz.core.entity.GenericValue;

public class ResolutionComparator extends IssueConstantsComparator
{
    protected GenericValue getConstant(GenericValue i1)
    {
        return ManagerFactory.getConstantsManager().getResolution(i1.getString("resolution"));
    }

    protected GenericValue getConstant(Issue i1)
    {
        Resolution resolutionObject = i1.getResolutionObject();
        if (resolutionObject == null)
            return null;
        else
            return resolutionObject.getGenericValue();
    }
}
