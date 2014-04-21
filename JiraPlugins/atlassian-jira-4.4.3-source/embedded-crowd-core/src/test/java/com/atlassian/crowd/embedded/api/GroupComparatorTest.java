package com.atlassian.crowd.embedded.api;

import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GroupComparatorTest
{
    @Test
    public void testComparator() throws Exception
    {
        List<Group> groups = new ArrayList<Group>(3);
        groups.add(new ImmutableGroup("cats"));
        groups.add(new ImmutableGroup("ants"));
        groups.add(new ImmutableGroup("BATS"));

        // test sorting is case-insensitive
        Collections.sort(groups, GroupComparator.GROUP_COMPARATOR);
        assertEquals("ants", groups.get(0).getName());
        assertEquals("BATS", groups.get(1).getName());
        assertEquals("cats", groups.get(2).getName());
    }
}
