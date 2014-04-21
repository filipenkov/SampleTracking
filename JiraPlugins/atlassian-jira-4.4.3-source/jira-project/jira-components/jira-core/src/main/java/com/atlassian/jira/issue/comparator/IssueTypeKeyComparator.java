package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.config.ConstantsManager;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 * <p/>
 * Used to compare to issue types when they are used as keys in schemes
 */
public class IssueTypeKeyComparator implements Comparator
{
    private final ConstantsManager constantsManager;

    public IssueTypeKeyComparator(ConstantsManager constantsManager)
    {
        this.constantsManager = constantsManager;
    }

    public int compare(Object o1, Object o2)
    {
        if ((o1 instanceof String || o1 == null) && (o2 instanceof String || o2 == null))
        {
            if (o1 == null)
            {
                if (o2 == null)
                {
                    return 0;
                }
                else
                {
                    return -1;
                }
            }
            else
            {
                if (o2 == null)
                {
                    return 1;
                }
                else
                {
                    // Both issue types are not null - compare using sequences
                    GenericValue issueTypeGV1 =  constantsManager.getIssueType((String) o1);
                    GenericValue issueTypeGV2 = constantsManager.getIssueType((String) o2);

                    Long sequence1 = issueTypeGV1.getLong("sequence");
                    Long sequence2 = issueTypeGV2.getLong("sequence");

                    if (sequence1 == null)
                    {
                        if (sequence2 == null)
                        {
                            return 0;
                        }
                        else
                        {
                            return -1;
                        }
                    }
                    else
                    {
                        if (sequence2 == null)
                        {
                            return 1;
                        }
                        else
                        {
                            return sequence1.compareTo(sequence2);
                        }
                    }
                }
            }
        }

        throw new IllegalArgumentException("Can only compare ids of issue types.");
    }
}
