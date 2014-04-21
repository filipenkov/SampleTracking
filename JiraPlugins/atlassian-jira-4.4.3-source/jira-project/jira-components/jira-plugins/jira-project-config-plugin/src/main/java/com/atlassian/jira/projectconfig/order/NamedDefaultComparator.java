package com.atlassian.jira.projectconfig.order;

import com.atlassian.jira.projectconfig.beans.NamedDefault;

import java.util.Comparator;

/**
 * @since v4.4
 */
class NamedDefaultComparator implements Comparator<NamedDefault>
{
    private final Comparator<? super String> nameComparator;

    public NamedDefaultComparator(Comparator<? super String> nameComparator)
    {
        this.nameComparator = nameComparator;
    }

    @Override
    public int compare(NamedDefault o1, NamedDefault o2)
    {
        //Default is shown first.
        if (o1.isDefault() == o2.isDefault())
        {
            return nameComparator.compare(o1.getName(), o2.getName());
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
