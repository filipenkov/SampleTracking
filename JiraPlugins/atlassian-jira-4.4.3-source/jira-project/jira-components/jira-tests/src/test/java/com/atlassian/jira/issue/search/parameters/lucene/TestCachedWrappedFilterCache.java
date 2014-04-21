package com.atlassian.jira.issue.search.parameters.lucene;

import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import org.apache.lucene.search.Filter;
import org.apache.lucene.index.IndexReader;

import java.util.BitSet;
import java.io.IOException;

import com.opensymphony.user.User;
import com.atlassian.jira.MockProviderAccessor;

public class TestCachedWrappedFilterCache extends ListeningTestCase
{
    @Test
    public void testStoreFilter() throws Exception
    {
        CachedWrappedFilterCache cache = new CachedWrappedFilterCache();
        Filter filter1 = new MockFilter();
        final MockProviderAccessor mockProviderAccessor = new MockProviderAccessor();
        User fred = new User("fred", mockProviderAccessor, new MockCrowdService());
        User barney = new User("barney", mockProviderAccessor, new MockCrowdService());

        cache.storeFilter(filter1, fred);
        final Filter returnedFilter1 = cache.getFilter(fred);
        assertNotNull(returnedFilter1);
        assertNull(cache.getFilter(barney));

        cache.storeFilter(filter1, barney);
        final Filter returnedFilter2 = cache.getFilter(barney);
        assertNotNull(returnedFilter2);
        assertEquals(filter1, returnedFilter1);
        assertEquals(filter1, returnedFilter2);
    }

    private static final class MockFilter extends Filter
    {
        public BitSet bits(final IndexReader reader) throws IOException
        {
            return null;
        }
    }
}
