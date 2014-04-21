package com.atlassian.streams.api.common;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static com.atlassian.streams.api.common.Predicates.containsAnyIssueKey;
import static com.atlassian.streams.api.common.Predicates.containsIssueKey;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class PredicatesTest
{
    @Test
    public void assertThatContainsIssueKeyMatchesIssueKey()
    {
        assertTrue(containsIssueKey("JRA-9").apply("Let me tell you the story of JRA-9"));
    }

    @Test
    public void assertThatContainsIssueKeyDoesNotMatchCrucibleReviewKeys()
    {
        assertFalse(containsIssueKey("JRA-9").apply("JRA-12 / CR-JRA-9: Fixed indentation"));
    }

    @Test
    public void assertThatContainsIssueKeyDoesNotFindTrivialSubstringMatches()
    {
        assertFalse(containsIssueKey("ONE-23").apply("NONE-23 or ONE-234"));
    }

    @Test
    public void assertThatContainsAnyIssueKeyFindsAnIssueKey()
    {
        assertTrue(containsAnyIssueKey(ImmutableList.of("ONE-1", "JRA-9", "JRA-1330")).apply("Is JRA-1330 next?"));
    }

    @Test
    public void assertThatContainsAnyIssueKeyReturnsFalseWithNoKeys()
    {
        assertFalse(containsAnyIssueKey(ImmutableList.<String>of()).apply("Is JRA-1330 next?"));
    }

    @Test
    public void assertThatContainsAnyIssueKeyReturnsFalseWhenNoIssueKeysMatch()
    {
        assertFalse(containsAnyIssueKey(ImmutableList.<String>of("JRA-9")).apply("No issue keys here..."));
    }
}

