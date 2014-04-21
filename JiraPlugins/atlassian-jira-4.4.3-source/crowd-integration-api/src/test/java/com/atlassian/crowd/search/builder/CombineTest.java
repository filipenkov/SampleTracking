package com.atlassian.crowd.search.builder;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.search.query.entity.restriction.BooleanRestriction;
import com.atlassian.crowd.search.query.entity.restriction.BooleanRestrictionImpl;
import static org.junit.Assert.assertEquals;

import com.atlassian.crowd.search.query.entity.restriction.PropertyUtils;
import org.junit.Test;

public class CombineTest
{
    private final SearchRestriction restriction1 = Restriction.on(PropertyUtils.ofTypeString("key1")).exactlyMatching("value1");
    private final SearchRestriction restriction2 = Restriction.on(PropertyUtils.ofTypeString("key2")).startingWith("value2");
    private final SearchRestriction restriction3 = Restriction.on(PropertyUtils.ofTypeString("key3")).containing("value3");

    private final SearchRestriction disjuction = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.OR,  restriction1, restriction2, restriction3);
    private final SearchRestriction conjuction = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND,  restriction1, restriction2, restriction3);

    @Test
    public void testAnyOf()
    {
        BooleanRestriction result = Combine.anyOf(restriction1, restriction2, restriction3);

        assertEquals(disjuction, result);
    }

    @Test
    public void testAllOf()
    {
        BooleanRestriction result = Combine.allOf(restriction1, restriction2, restriction3);

        assertEquals(conjuction, result);
    }
}
