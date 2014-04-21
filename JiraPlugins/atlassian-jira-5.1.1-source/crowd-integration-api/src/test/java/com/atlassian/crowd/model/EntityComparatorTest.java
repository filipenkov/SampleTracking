package com.atlassian.crowd.model;

import junit.framework.TestCase;
import org.junit.Test;

public class EntityComparatorTest
{
    @Test
    public void testCanFindRelevantComparators() throws Exception
    {
        // Doesn't test much apart from that the EntityComparator gives you "something" for expected types.

        EntityComparator.of(String.class);
        EntityComparator.of(com.atlassian.crowd.model.user.User.class);
        EntityComparator.of(com.atlassian.crowd.embedded.api.User.class);
        EntityComparator.of(com.atlassian.crowd.model.group.Group.class);
        EntityComparator.of(com.atlassian.crowd.embedded.api.Group.class);
        try
        {
            EntityComparator.of(Integer.class);
            TestCase.fail();
        }
        catch (IllegalStateException ex)
        {
            // expected
        }
    }
}