/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.search.parameters.lucene.sort;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.search.IssueComparator;
import com.atlassian.jira.issue.util.SimpleMockIssueFactory;

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;
import com.atlassian.jira.local.ListeningTestCase;

public class TestIssueSortComparator extends ListeningTestCase
{
    private final IssueFactory issueFactory = new SimpleMockIssueFactory();

    @Test
    public void testNullIssueComparatorInCtor()
    {
        try
        {
            new IssueSortComparator(null, new SimpleMockIssueFactory());
            fail("NPE Expected");
        }
        catch (final NullPointerException yay)
        {}
    }

    @Test
    public void testNullIssueFactoryInCtor()
    {
        try
        {
            new IssueSortComparator(new IssueComparator()
            {
                public int compare(final Issue issue1, final Issue issue2)
                {
                    return 0;
                }
            }, null);
            fail("NPE Expected");
        }
        catch (final NullPointerException yay)
        {}
    }

    @Test
    public void testEqualsForNull()
    {
        final IssueComparator issueComparator = new IssueComparator()
        {
            public int compare(final Issue issue1, final Issue issue2)
            {
                return 0;
            }
        };

        final IssueSortComparator source = new IssueSortComparator(issueComparator, issueFactory);

        // Ensure the equals and hashCode methods are not totally screwed
        assertEquals(source, source);
        assertEquals(source.hashCode(), source.hashCode());
        assertFalse(source.equals(null));

        final IssueSortComparator source2 = new IssueSortComparator(issueComparator, issueFactory);

        assertEquals(source, source2);
        assertEquals(source.hashCode(), source2.hashCode());
    }

    @Test
    public void testEquals()
    {
        final AtomicInteger hashCodeCalled = new AtomicInteger(0);
        final AtomicInteger equalsCodeCalled = new AtomicInteger(0);

        final IssueComparator issueComparator2 = new IssueComparator()
        {
            public int compare(final Issue issue1, final Issue issue2)
            {
                throw new UnsupportedOperationException("Not Implemented.");
            }
        };

        final IssueComparator issueComparator = new IssueComparator()
        {
            public int compare(final Issue issue1, final Issue issue2)
            {
                throw new UnsupportedOperationException("Not Implemented.");
            }

            @Override
            public int hashCode()
            {
                hashCodeCalled.incrementAndGet();
                return 10;
            }

            @Override
            public boolean equals(final Object obj)
            {
                Assert.assertSame(issueComparator2, obj);
                equalsCodeCalled.incrementAndGet();
                return true;
            }
        };

        final IssueComparator issueComparator3 = new IssueComparator()
        {
            public int compare(final Issue issue1, final Issue issue2)
            {
                throw new UnsupportedOperationException("Not Implemented.");
            }

            @Override
            public boolean equals(final Object obj)
            {
                Assert.assertSame(issueComparator, obj);
                equalsCodeCalled.incrementAndGet();
                return false;
            }
        };

        final IssueSortComparator issueSortComparator = new IssueSortComparator(issueComparator, issueFactory);

        assertEquals(issueSortComparator, issueSortComparator);
        assertEquals(10, issueSortComparator.hashCode());
        assertEquals(1, hashCodeCalled.get());
        // Should not be called as we are comparing the same instance
        assertEquals(0, equalsCodeCalled.get());

        final IssueSortComparator issueSortComparator2 = new IssueSortComparator(issueComparator2, issueFactory);
        assertTrue(issueSortComparator.equals(issueSortComparator2));
        assertEquals(1, equalsCodeCalled.get());

        // Ensure the returned value from the issue comparitor is respected.
        final IssueSortComparator issueSortComparator3 = new IssueSortComparator(issueComparator3, issueFactory);
        assertFalse(issueSortComparator3.equals(issueSortComparator));
        assertEquals(2, equalsCodeCalled.get());

        assertFalse(issueSortComparator.equals(null));
        assertFalse(issueSortComparator.equals(new Object()));
    }
}
