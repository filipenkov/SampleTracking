/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestIssueUtils extends ListeningTestCase
{
    GenericValue issue1;
    List issueList;

    @Before
    public void setUp() throws Exception
    {
        issue1 = new MockGenericValue("Issue", MapBuilder.newBuilder()
                .add("id", 1L)
                .add("key", "fookey1")
                .add("project", 111L)
                .add("summary", "foosum1")
                .add("security", 1L).
                toMap());

        issueList = CollectionBuilder.newBuilder(
                issue1,
                new MockGenericValue("Issue", MapBuilder.newBuilder()
                        .add("id", 2L)
                        .add("key", "fookey2")
                        .add("project", 111L)
                        .add("summary", "foosum1")
                        .add("security", 1L)
                        .toMap()),
                new MockGenericValue("Issue", MapBuilder.newBuilder()
                        .add("id", 3L)
                        .add("key", "fookey3")
                        .add("project", 111L)
                        .add("summary", "foosum1")
                        .add("security", 1L)
                        .toMap()),
                new MockGenericValue("Issue", MapBuilder.newBuilder()
                        .add("id", 4L)
                        .add("key", "fookey4")
                        .add("project", 111L)
                        .add("summary", "foosum1")
                        .add("security", 2L)
                        .toMap()),
                new MockGenericValue("Issue", MapBuilder.newBuilder()
                        .add("id", 5L)
                        .add("key", "fookey5")
                        .add("project", 111L)
                        .add("summary", "foosum1")
                        .add("security", 2L)
                        .toMap()),
                new MockGenericValue("Issue", MapBuilder.newBuilder()
                        .add("id", 6L)
                        .add("key", "fookey6")
                        .add("project", 111L)
                        .add("summary", "foosum2")
                        .add("security", 2L)
                        .toMap())).
                asMutableList();
    }

    @Test
    public void shouldFilterWithPredicate() throws Exception
    {
        Predicate<GenericValue> filter = new Predicate<GenericValue>()
        {
            public boolean evaluate(final GenericValue input)
            {
                return !input.getLong("id").equals(1L);
            }
        };

        IssueUtils.filterIssues(issueList, filter);

        assertEquals(1, issueList.size());
        assertTrue(issueList.contains(issue1));
    }
}
