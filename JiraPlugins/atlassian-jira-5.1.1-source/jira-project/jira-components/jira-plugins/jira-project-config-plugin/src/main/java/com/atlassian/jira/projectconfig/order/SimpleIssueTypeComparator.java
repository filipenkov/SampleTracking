package com.atlassian.jira.projectconfig.order;

import com.atlassian.jira.projectconfig.beans.SimpleIssueType;

import java.util.Comparator;

/**
 * @since v4.4
 */
class SimpleIssueTypeComparator implements Comparator<SimpleIssueType>
{
    private final Comparator<? super String> nameComparator;

    SimpleIssueTypeComparator(Comparator<? super String> nameComparator)
    {
        this.nameComparator = nameComparator;
    }

    @Override
    public int compare(SimpleIssueType o1, SimpleIssueType o2)
    {
        //Default show first.
        if (o1.isDefault() == o2.isDefault())
        {
            //Sub-tasks shown last.
            if (o1.isSubTask() == o2.isSubTask())
            {
                return nameComparator.compare(o1.getName(), o2.getName());
            }
            else if (o1.isSubTask())
            {
                return 1;
            }
            else
            {
                return -1;
            }
        }
        else if (o1.isDefault())
        {
            return -1;
        }
        else
        {
            return 1;
        }
    }
}
