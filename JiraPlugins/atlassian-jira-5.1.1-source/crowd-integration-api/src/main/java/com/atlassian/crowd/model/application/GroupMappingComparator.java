package com.atlassian.crowd.model.application;

import java.util.Comparator;

public class GroupMappingComparator implements Comparator<GroupMapping>
{
    public int compare(final GroupMapping o1, final GroupMapping o2)
    {
        int directoryNameCompare = String.CASE_INSENSITIVE_ORDER.compare(o1.getDirectory().getName(), o2.getDirectory().getName());
        if (directoryNameCompare == 0)
        {
            return String.CASE_INSENSITIVE_ORDER.compare(o1.getGroupName(), o2.getGroupName());
        }
        else
        {
            return directoryNameCompare;
        }
    }
}
