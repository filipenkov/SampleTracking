package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.link.IssueLink;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class SubTaskFieldComparator implements Comparator
{
    private final SubTaskManager subTaskManager;

    public SubTaskFieldComparator()
    {
        this.subTaskManager = ComponentManager.getInstance().getSubTaskManager();
    }

    public int compare(Object o1, Object o2)
    {
        GenericValue issue1 = (GenericValue) o1;
        GenericValue issue2 = (GenericValue) o2;

        Integer value1 = getComparisonValue(subTaskManager.getSubTaskIssueLinks(issue1.getLong("id")));
        Integer value2 = getComparisonValue(subTaskManager.getSubTaskIssueLinks(issue2.getLong("id")));

        return value1.compareTo(value2);
    }

    private Integer getComparisonValue(Collection subTaskIssueLinks)
    {
        if (subTaskIssueLinks == null || subTaskIssueLinks.isEmpty())
        {
            return new Integer(2);
        }

        for (Iterator iterator = subTaskIssueLinks.iterator(); iterator.hasNext();)
        {
            IssueLink issueLink = (IssueLink) iterator.next();
            GenericValue issue = issueLink.getDestination();
            if (issue != null && !TextUtils.stringSet(issue.getString(IssueFieldConstants.RESOLUTION)))
            {
                // We have found a sub-task that is not resolved (closed). No need to process further.
                return new Integer(0);
            }
        }

        // All sub-tasks are resolved (closed)
        return new Integer(1);
    }
}
