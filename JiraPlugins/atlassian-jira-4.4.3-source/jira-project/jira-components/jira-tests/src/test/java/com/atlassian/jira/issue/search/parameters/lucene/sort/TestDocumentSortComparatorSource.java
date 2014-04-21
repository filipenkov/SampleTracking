/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.search.parameters.lucene.sort;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.lucene.index.IndexReader;

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;
import com.atlassian.jira.local.ListeningTestCase;

public class TestDocumentSortComparatorSource extends ListeningTestCase
{
    @Test
    public void testEqualsForNull()
    {
        final DocumentSortComparatorSource source = new DocumentSortComparatorSource(null);

        assertEquals(source, source);
        assertEquals(source.hashCode(), source.hashCode());

        final DocumentSortComparatorSource source2 = new DocumentSortComparatorSource(null);

        assertEquals(source, source2);
        assertEquals(source.hashCode(), source2.hashCode());
    }

    @Test
    public void testEquals()
    {
        final AtomicInteger hashCodeCalled = new AtomicInteger(0);
        final AtomicInteger equalsCodeCalled = new AtomicInteger(0);

        final SortComparator sortComparator2 = new SortComparator()
        {
            public int compare(final IndexReader indexReader, final int docId, final int docId2)
            {
                throw new UnsupportedOperationException("Not Implemented.");
            }
        };

        final SortComparator sortComparator = new SortComparator()
        {
            public int compare(final IndexReader indexReader, final int docId, final int docId2)
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
                Assert.assertSame(sortComparator2, obj);
                equalsCodeCalled.incrementAndGet();
                return true;
            }
        };

        final DocumentSortComparatorSource source = new DocumentSortComparatorSource(sortComparator);

        assertEquals(source, source);
        assertEquals(10, source.hashCode());
        assertEquals(1, hashCodeCalled.get());
        // Should not be called as we are comparing the same instance
        assertEquals(0, equalsCodeCalled.get());

        final DocumentSortComparatorSource source2 = new DocumentSortComparatorSource(sortComparator2);
        assertTrue(source.equals(source2));
        assertEquals(1, equalsCodeCalled.get());

        final SortComparator sortComparator3 = new SortComparator()
        {
            public int compare(final IndexReader indexReader, final int docId, final int docId2)
            {
                throw new UnsupportedOperationException("Not Implemented.");
            }

            @Override
            public boolean equals(final Object obj)
            {
                Assert.assertSame(sortComparator, obj);
                equalsCodeCalled.incrementAndGet();
                return false;
            }
        };

        // Ensure the return value from sort comparitor is respected
        final DocumentSortComparatorSource source3 = new DocumentSortComparatorSource(sortComparator3);
        assertFalse(source3.equals(source));
        assertEquals(2, equalsCodeCalled.get());

        assertFalse(source.equals(null));
        assertFalse(source.equals(new Object()));
        assertFalse(source.equals(new MappedSortComparator(null)));
    }
}
