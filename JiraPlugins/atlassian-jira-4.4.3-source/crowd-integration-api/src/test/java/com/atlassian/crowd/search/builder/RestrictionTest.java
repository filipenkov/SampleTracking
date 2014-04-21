package com.atlassian.crowd.search.builder;

import com.atlassian.crowd.search.query.entity.restriction.*;
import com.atlassian.crowd.embedded.api.SearchRestriction;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class RestrictionTest
{
    private final PropertyRestriction<String> exactlyMatchesTerm = new TermRestriction<String>(new PropertyImpl<String>("key", String.class), MatchMode.EXACTLY_MATCHES, "value");
    private final PropertyRestriction<String> startsWithTerm = new TermRestriction<String>(new PropertyImpl<String>("key", String.class), MatchMode.STARTS_WITH, "value");
    private final PropertyRestriction<String> containsTerm = new TermRestriction<String>(new PropertyImpl<String>("key", String.class), MatchMode.CONTAINS, "value");

    @Test
    public void testExactlyMatching()
    {
        SearchRestriction result = Restriction.on(PropertyUtils.ofTypeString("key")).exactlyMatching("value");

        assertEquals(exactlyMatchesTerm, result);
    }

    @Test
    public void testStartingWith()
    {
        SearchRestriction result = Restriction.on(PropertyUtils.ofTypeString("key")).startingWith("value");

        assertEquals(startsWithTerm, result);
    }

    @Test
    public void testContaining()
    {
        SearchRestriction result = Restriction.on(PropertyUtils.ofTypeString("key")).containing("value");

        assertEquals(containsTerm, result);
    }
}
